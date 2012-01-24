package org.libj.xquery.compiler;

import org.libj.xquery.Callback;
import org.libj.xquery.XQuery;
import org.libj.xquery.parser.AST;
import static org.libj.xquery.lexer.TokenType.*;

import org.libj.xquery.runtime.Op;
import org.objectweb.asm.*;

import java.util.ArrayList;

public class Assembler implements Opcodes {
    private AST ast;
//    private ClassWriter cw = new ClassWriter(0);
    private ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    private String className;
    
    private Scope scope;
    private int locals = 1;

    MethodVisitor mv;

    public Assembler(String className, AST ast) {
        this.className = className;
        this.ast = ast;
        visitClass();
    }
    
    private static final String QUERY_BASE = XQuery.class.getName().replace('.', '/');

    private static final String QUERY_CALLBACK = Callback.class.getName().replace('.', '/');
//    private static final String QUERY_LIST = CallbackList.class.getName().replace('.', '/');
    private static final String QUERY_LIST = ArrayList.class.getName().replace('.', '/');

    private static final String RUNTIME_OP = Op.class.getName().replace('.', '/');

    private void visitClass() {
        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, className.replace('.', '/'), null,
                "java/lang/Object",
                new String[] {QUERY_BASE});
        visitInit();
        visitEval();
        visitEvalCallback();
        cw.visitEnd();
    }

    private void visitInit() {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    //////////////////////////////////////////////////
    /// eval()
    //////////////////////////////////////////////////

    private void visitEval() {
        mv = cw.visitMethod(ACC_PUBLIC, "eval", "()Ljava/lang/Object;", null, null);
        mv.visitCode();
        visitAST();
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void visitAST() {
//        AST declares = ast.nth(1);
        AST code = ast.nth(2);
        visitExpr(code);
    }

    private void visitExpr(AST expr) {
        switch (expr.getNodeType()) {
            case LET:
                visitLet(expr);
                break;
            case FOR:
                visitFor(expr);
                break;
            case LIST:
                visitList(expr);
                break;
            case PLUS: case MINUS: case MULTIPLY: case DIV: case TO: case INDEX:
                visitOp(expr);
                break;
            case VARIABLE:
                mv.visitVarInsn(ALOAD, resolve(expr.getNodeText()));
                break;
            case STRING:
                mv.visitLdcInsn(expr.getNodeText());
                break;
            case NUMBER:
                visitNumber(expr.getNodeText());
                break;
            default:
                throw new RuntimeException("Not Implemented: "+toTypeName(expr.getNodeType()));
        }
    }

    private void visitNumber(String text) {
        if (text.indexOf('.') == -1) {
            visitInt(text);
        }
        else {
            visitDouble(text);
        }
    }

    private void visitInt(String text) {
        int n = Integer.parseInt(text);
        if (-0x80 <= n && n <= 0x7f) {
            mv.visitIntInsn(BIPUSH, n);
        }
        else if (-0x8000 <= n && n <= 0x7fff) {
            mv.visitIntInsn(SIPUSH, n);
        }
        else {
            mv.visitLdcInsn(n);
        }
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
    }

    private void visitDouble(String text) {
        double d = Double.parseDouble(text);
        // TODO: optimize:
        // mv.visitInsn(DCONST_0);
        // mv.visitInsn(DCONST_1);
        mv.visitLdcInsn(d);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
    }

    private void visitList(AST expr) {
        newList();
        for (AST element: expr.getChildren()) {
            mv.visitInsn(DUP);
            visitExpr(element);
            pushToList();
        }
    }

    private void visitFor(AST expr) {
        pushScope();
        String variable = expr.nth(1).getNodeText();
        AST varExpr = expr.nth(2);
        AST body = expr.nth(3);
        if (expr.nth(4) != null && !expr.nth(4).isNil()) {
            throw new RuntimeException("Not Implemented: "+expr.nth(4));
        }

        newList();
        int result = defineAnonymous();
        mv.visitVarInsn(ASTORE, result);

        int iterator = defineAnonymous();
        int element = define(variable);
        visitExpr(varExpr);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/Iterable", "iterator", "()Ljava/util/Iterator;");
        mv.visitVarInsn(ASTORE, iterator);

        Label condition = new Label();
        Label loop = new Label();
        mv.visitJumpInsn(GOTO, condition);

        mv.visitLabel(loop);
        mv.visitVarInsn(ALOAD, iterator);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
        mv.visitVarInsn(ASTORE, element);

        visitExpr(body);
        mv.visitVarInsn(ALOAD, result);
        mv.visitInsn(SWAP);
        pushToList();

        mv.visitLabel(condition);
        mv.visitVarInsn(ALOAD, iterator);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
        mv.visitJumpInsn(IFNE, loop);

        mv.visitVarInsn(ALOAD, result);
        popScope();
    }

    private void visitLet(AST expr) {
        pushScope();
        String variable = expr.nth(1).getNodeText();
        AST varExpr = expr.nth(2);
        AST body = expr.nth(3);
        if (expr.nth(4) != null && !expr.nth(4).isNil()) {
            throw new RuntimeException("Not Implemented where: "+expr.nth(4));
        }

        int index = define(variable);
        visitExpr(varExpr);
        mv.visitVarInsn(ASTORE, index);
        visitExpr(body);

        popScope();
    }

    private void visitOp(AST expr) {
        int type = expr.getNodeType();
        String op;
        switch (type) {
            case PLUS:
                op = "add";
                break;
            case MINUS:
                op = "subtract";
                break;
            case MULTIPLY:
                op = "multiply";
                break;
            case DIV:
                op = "div";
                break;
            case TO:
                op = "list";
                break;
            case INDEX:
                op = "at";
                break;
            default:
                throw new RuntimeException("Not Implemented: "+toTypeName(type));
        }
        for (AST operand: expr.getChildren()) {
            visitExpr(operand);
        }
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME_OP, op, "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
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

    private void newObject(String className) {
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "()V");
    }
    
    private void newList() {
        newObject(QUERY_LIST);
    }

    private void pushToList() {
        mv.visitMethodInsn(INVOKEVIRTUAL, QUERY_LIST, "add", "(Ljava/lang/Object;)Z");
        mv.visitInsn(POP);
    }
    
    private void log(String message) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn(message);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
    }
    //////////////////////////////////////////////////
    /// analyze
    //////////////////////////////////////////////////
    private void pushScope() {
        scope = new Scope(scope);
    }

    private void popScope() {
        if (scope == null) {
            throw new CompilerException("Internal error: no more scope to pop");
        }
        scope = scope.getEnclosingScope();
    }
    
    private int defineAnonymous() {
        return locals++;
    }

    private int define(String name) {
        int index = locals++;
        scope.define(new Symbol(name, index));
        return index;
    }
    
    private int resolve(String name) {
        Symbol s = scope.resolve(name);
        if (s == null) {
            throw new CompilerException("Variable undefined: "+name);
        }
        return s.getIndex();
    }
    
    //////////////////////////////////////////////////
    /// public API
    //////////////////////////////////////////////////

    public byte[] toByteArray() {
        return cw.toByteArray();
    }
}
