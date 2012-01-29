package org.libj.xquery.compiler;

import static org.libj.xquery.compiler.Constants.*;
import org.libj.xquery.namespace.*;
import org.libj.xquery.parser.AST;
import static org.libj.xquery.lexer.TokenType.*;

import org.libj.xquery.xml.str.StringNamespaceXMLFactory;
import org.objectweb.asm.*;

import java.util.ArrayList;

public class Assembler implements Opcodes {
    private AST ast;
    private ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    private String compiledClassName;

    private Namespace namespace;
    private final String XML_FACTORY_IMPLEMENTATION;

    private MethodVisitor mv;

    public Assembler(String className, AST ast, Namespace root, Class xmlFactory) {
        this.compiledClassName = className.replace('.', '/');
        this.ast = ast;
        this.namespace = root;
        XML_FACTORY_IMPLEMENTATION = xmlFactory.getName().replace('.', '/');
        visitClass();
    }

    public Assembler(String className, AST ast) {
        this(className, ast, new DefaultRootNamespace(), DEFAUL_XML_FACTORY_IMPLEMENTATION);
    }

    private void visitClass() {
        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, compiledClassName, null,
                SUPER_CLASS,
                new String[]{QUERY_BASE});
        visitInit();
        visitFactory();
//        visitNamespaces();
        visitEvalWithEnvironment();
        visitEval();
        visitEvalCallback();
        cw.visitEnd();
    }

    private void visitFactory() {
        FieldVisitor fv = cw.visitField(ACC_PRIVATE, "xmlFactory", "L"+XML_FACTORY_INTERFACE+";", null, null);
        fv.visitEnd();

        mv = cw.visitMethod(ACC_PRIVATE, "toXML", "(Ljava/lang/String;)L"+ XML_INTERFACE +";", null, null);
        mv.visitCode();
        // if xmlFactory == null
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, compiledClassName, "xmlFactory", "L"+XML_FACTORY_INTERFACE+";");
        Label endIf = new Label();
        mv.visitJumpInsn(IFNONNULL, endIf);
        // init xmlFactory
        mv.visitVarInsn(ALOAD, 0);
        newObject(XML_FACTORY_IMPLEMENTATION);
        mv.visitFieldInsn(PUTFIELD, compiledClassName, "xmlFactory", "L"+XML_FACTORY_INTERFACE+";");
        // register namespaces
        for (Object declare: ast.nth(1).rest()) {
            if (((AST)declare).nth(1).getNodeType() == NAMESPACE) {
                String alias = ((AST) declare).nth(2).getNodeText();
                String uri = ((AST) declare).nth(3).getNodeText();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, compiledClassName, "xmlFactory", "L"+XML_FACTORY_INTERFACE+";");
                pushConst(alias);
                pushConst(uri);
                mv.visitMethodInsn(INVOKEINTERFACE, XML_FACTORY_INTERFACE, "registerNamespace", "(Ljava/lang/String;Ljava/lang/String;)V");
            }
        }
        mv.visitLabel(endIf);
        // enf if
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, compiledClassName, "xmlFactory", "L" + XML_FACTORY_INTERFACE + ";");
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEINTERFACE, XML_FACTORY_INTERFACE, "toXML", "(Ljava/lang/String;)L" + XML_INTERFACE + ";");
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void visitNamespaces() {
        mv = cw.visitMethod(ACC_PROTECTED, "initNamespaces", "()V", null, null);
        mv.visitCode();

        for (Object declare: ast.nth(1).rest()) {
            if (((AST)declare).nth(1).getNodeType() == NAMESPACE) {
                String alias = ((AST) declare).nth(2).getNodeText();
                String uri = ((AST) declare).nth(3).getNodeText();
                mv.visitVarInsn(ALOAD, 0);
                pushConst(alias);
                pushConst(uri);
                mv.visitMethodInsn(INVOKEVIRTUAL, compiledClassName, "registerNamespace", "(Ljava/lang/String;Ljava/lang/String;)V");
            }
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void visitInit() {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, SUPER_CLASS, "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    //////////////////////////////////////////////////
    /// eval
    //////////////////////////////////////////////////

    private void visitEval() {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "eval", "()Ljava/lang/Object;", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ACONST_NULL);
        mv.visitMethodInsn(INVOKEVIRTUAL, compiledClassName, "eval", "(L" + ENVIRONMENT_CLASS + ";)Ljava/lang/Object;");
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    //////////////////////////////////////////////////
    /// eval(Environment)
    //////////////////////////////////////////////////

    private void visitEvalWithEnvironment() {
        mv = cw.visitMethod(ACC_PUBLIC, "eval", "(L"+ENVIRONMENT_CLASS+";)Ljava/lang/Object;", null, null);
        mv.visitCode();
        Class returnType = visitAST();
        if (returnType.isPrimitive()) {
            Caster.castToObject(mv, returnType);
            mv.visitInsn(ARETURN);
        }
        else {
            Label returnLabel = new Label();
            mv.visitInsn(DUP);
            mv.visitJumpInsn(IFNONNULL, returnLabel);
            mv.visitInsn(POP);
            pushNilObject();
            mv.visitLabel(returnLabel);
            mv.visitInsn(ARETURN);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private Class visitAST() {
        AST declares = ast.nth(1);
        AST code = ast.nth(2);
        visitDeclares(declares);
//        new Walker(code);
        return new OnePassEvalAssembler(mv, compiledClassName, namespace, XML_FACTORY_IMPLEMENTATION).visitExpr(code);
    }

    private void visitDeclares(AST declares) {
        for (Object declare: declares.rest()) {
            visitDeclare((AST) declare);
        }
    }

    private void visitDeclare(AST declare) {
        if (declare.nth(1).getNodeType() == NAMESPACE) {
            visitDeclareNamespace(declare);
        }
    }

    private void visitDeclareNamespace(AST declare) {
        String alias = declare.nth(2).getNodeText();
        String uri = declare.nth(3).getNodeText();
        if (uri.startsWith("class:")) {
            namespace.register(alias, namespace.lookup(uri));
        }
        else if (uri.startsWith("http:")) {
            namespace.register(alias, new URI(uri));
        }
        else {
            throw new RuntimeException("Not Implemented declare namespace: "+uri);
        }
    }


    //////////////////////////////////////////////////
    /// eval(Callback)
    //////////////////////////////////////////////////
    private void visitEvalCallback() {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "eval", "(L"+QUERY_CALLBACK+";)V", null, null);
        mv.visitCode();
        mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("Not implemented");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitMaxs(3, 2);
        mv.visitEnd();
    }
    //////////////////////////////////////////////////
    /// helper
    //////////////////////////////////////////////////

    private Class cast(Class source, Class target) {
        return Caster.cast(mv, source, target);
    }

    private void pushConst(int n) {
        switch (n) {
            case -1:
                mv.visitInsn(ICONST_M1);
                return;
            case 0:
                mv.visitInsn(ICONST_0);
                return;
            case 1:
                mv.visitInsn(ICONST_1);
                return;
            case 2:
                mv.visitInsn(ICONST_2);
                return;
            case 3:
                mv.visitInsn(ICONST_3);
                return;
            case 4:
                mv.visitInsn(ICONST_4);
                return;
            case 5:
                mv.visitInsn(ICONST_5);
                return;
        }
        if (-0x80 <= n && n <= 0x7f) {
            mv.visitIntInsn(BIPUSH, n);
        }
        else if (-0x8000 <= n && n <= 0x7fff) {
            mv.visitIntInsn(SIPUSH, n);
        }
        else {
            mv.visitLdcInsn(n);
        }
    }

    private void pushConst(Object o) {
        mv.visitLdcInsn(o);
    }

    private void newObject(String className) {
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "()V");
    }

    private void newArray(Class type, int length) {
        pushConst(length);
        if (type.isPrimitive()) {
            throw new RuntimeException("Not Implemented!");
        }
        else {
            mv.visitTypeInsn(ANEWARRAY, type.getName().replace('.', '/'));
        }
    }

    private void pushToArray(Class type) {
        if (type.isPrimitive()) {
            throw new RuntimeException("Not Implemented!");
        }
        else {
            mv.visitInsn(AASTORE);
        }
    }

    private void pushNil() {
//        newObject(NIL);
        mv.visitInsn(ACONST_NULL);
    }

    private void pushNilObject() {
//        newObject(NIL);
        mv.visitFieldInsn(GETSTATIC, NIL, "NIL", "L"+NIL+";");
    }

    private Class newList() {
        newObject(QUERY_LIST);
        return org.libj.xquery.runtime.List.class;
    }

    private void pushToList() {
//        mv.visitMethodInsn(INVOKEVIRTUAL, QUERY_LIST, "add", "(Ljava/lang/Object;)Z");
//        mv.visitInsn(POP);
        mv.visitMethodInsn(INVOKEVIRTUAL, QUERY_LIST, "add", "(Ljava/lang/Object;)V");
    }

    private void log(String message) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn(message);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
    }

    private void log() {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitInsn(SWAP);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
    }

    //////////////////////////////////////////////////
    /// public API
    //////////////////////////////////////////////////

    public byte[] toByteArray() {
        return cw.toByteArray();
    }
}
