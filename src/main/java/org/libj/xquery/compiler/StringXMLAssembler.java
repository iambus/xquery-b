package org.libj.xquery.compiler;

import org.libj.xquery.lexer.TokenType;
import org.libj.xquery.lisp.Cons;
import org.libj.xquery.parser.AST;
import org.libj.xquery.xml.XML;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;

import static org.libj.xquery.compiler.Constants.XML_INTERFACE;
import static org.libj.xquery.lexer.TokenType.TEXT;

public class StringXMLAssembler implements Opcodes {
    private EvalAssembler outer;
    private MethodVisitor mv;
    private String compiledClassName;

    StringXMLAssembler(EvalAssembler outer, MethodVisitor mv, String compiledClassName) {
        this.outer = outer;
        this.mv = mv;
        this.compiledClassName = compiledClassName;
    }

    Class visitElement(Cons expr) {
        ArrayList list = new ArrayList();
        flatten(expr, list);
        list = mergeStringNode(list);
        return visitNode(list);
    }

    private void flatten(Cons expr, ArrayList list) {
        switch (AST.getNodeType(expr)) {
            case ELEMENT:
                flattenElement(expr, list);
                break;
            default:
                list.add(expr);
        }
    }

    private void flattenElement(Cons expr, ArrayList list) {
        Object tag = expr.nth(1);
        Cons attrs = (Cons) expr.nth(2);
        Cons contents = (Cons) expr.nth(3);
        list.add("<"+ tag);
        if (attrs != null) {
            for (Object a: attrs) {
                Cons attr = (Cons) a;
                String name = (String) attr.first();
                Cons values = (Cons) attr.second();
                list.add(" " + name + "=\"");
                flattenValues(values, list);
                list.add("\"");
            }
        }
        if (contents == null) {
            list.add("/>");
        }
        else {
            list.add(">");
            flattenValues(contents, list);
            list.add("</" + tag + ">");
        }
    }

    private void flattenValues(Cons values, ArrayList list) {
        for (Object x: values) {
            list.add(x);
        }
    }

    private ArrayList mergeStringNode(ArrayList source) {
        ArrayList target = new ArrayList();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < source.size(); i++) {
            Object x = source.get(i);
            if (x instanceof String) {
                if (buffer.length() == 0 && i + 1 < source.size() && !(source.get(i + 1) instanceof String)) {
                    target.add(x);
                } else {
                    buffer.append((String) x);
                }
            } else {
                if (buffer.length() != 0) {
                    target.add(buffer.toString());
                    buffer.setLength(0);
                }
                target.add(x);
            }
        }
        if (buffer.length() != 0) {
            target.add(buffer.toString());
        }
        return target;
    }

    private Class visitNode(ArrayList subs) {
        mv.visitVarInsn(ALOAD, 0);
        if (subs.size() == 1) {
            String singleton = (String) subs.get(0);
            pushConst(singleton);
        }
        else {
            newObject("java/lang/StringBuilder");
            for (Object x: subs) {
                if (x instanceof String) {
                    pushConst(x);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                }
                else {
                    Cons element = (Cons) x;
                    Class t = outer.visitExpr(element);
                    if (t.isPrimitive()) {
                        if (t == int.class) {
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
                        }
                        else if (t == double.class) {
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(D)Ljava/lang/StringBuilder;");
                        }
                        else if (t == long.class) {
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;");
                        }
                        else {
                            throw new RuntimeException("Not Implemented!");
                        }
                    }
                    else {
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                    }
                }
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
        }
        mv.visitMethodInsn(INVOKESPECIAL, compiledClassName.replace('.', '/'), "toXML", "(Ljava/lang/String;)L" + XML_INTERFACE + ";");
        return XML.class;
    }

    private void pushConst(Object o) {
        mv.visitLdcInsn(o);
    }

    private void newObject(String className) {
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "()V");
    }

}
