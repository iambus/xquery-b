package org.libj.xquery.compiler;

import org.libj.xquery.Callback;
import org.libj.xquery.Environment;
import org.libj.xquery.XQuery;
import org.libj.xquery.lexer.Token;
import org.libj.xquery.namespace.*;
import org.libj.xquery.parser.AST;
import static org.libj.xquery.lexer.TokenType.*;

import org.libj.xquery.runtime.Nil;
import org.libj.xquery.runtime.Op;
import org.libj.xquery.runtime.RecursiveList;
import org.libj.xquery.xml.DomSimpleXPathXMLFactory;
import org.libj.xquery.xml.XML;
import org.libj.xquery.xml.XMLFactory;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.List;

public class Assembler implements Opcodes {
    private AST ast;
//    private ClassWriter cw = new ClassWriter(0);
    private ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    private String compiledClassName;

    private Scope scope = new Scope();
    private Scope freeScope = new Scope();
    private int locals = 2;
    private final int environment_index = 1;

    private RootNamespace namespace = new DefaultRootNamespace();

    MethodVisitor mv;

    public Assembler(String className, AST ast) {
        this.compiledClassName = className.replace('.', '/');
        this.ast = ast;
        visitClass();
    }

    private static final String QUERY_BASE = XQuery.class.getName().replace('.', '/');

    private static final String QUERY_CALLBACK = Callback.class.getName().replace('.', '/');
//    private static final String QUERY_LIST = CallbackList.class.getName().replace('.', '/');
//    private static final String QUERY_LIST = ArrayList.class.getName().replace('.', '/');
    private static final String QUERY_LIST = RecursiveList.class.getName().replace('.', '/');
    private static final String ENVIRONMENT_CLASS = Environment.class.getName().replace('.', '/');
    private static final String NIL = Nil.class.getName().replace('.', '/');

    private static final String RUNTIME_OP = Op.class.getName().replace('.', '/');
    private static final String XML_FACTORY_INTERFACE = XMLFactory.class.getName().replace('.', '/');
//    private static final String XML_FACTORY_IMPLEMENTATION = DomXMLFactory.class.getName().replace('.', '/');
    private static final String XML_FACTORY_IMPLEMENTATION = DomSimpleXPathXMLFactory.class.getName().replace('.', '/');
    private static final String XML_INTERFACE = XML.class.getName().replace('.', '/');

    private void visitClass() {
        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, compiledClassName, null,
                "java/lang/Object",
                new String[] {QUERY_BASE});
        visitInit();
        visitFactory();
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
        visitAST();
        Label returnLabel = new Label();
        mv.visitInsn(DUP);
        mv.visitJumpInsn(IFNONNULL, returnLabel);
        mv.visitInsn(POP);
        pushNilObject();
        mv.visitLabel(returnLabel);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void visitAST() {
        AST declares = ast.nth(1);
        AST code = ast.nth(2);
        visitDeclares(declares);
        visitExpr(code);
    }

    private void visitDeclares(AST declares) {
        for (Object declare: declares.getChildren()) {
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
            case CALL:
                visitCall(expr);
                break;
            case PLUS: case MINUS: case MULTIPLY: case DIV: case NEGATIVE: case MOD:
            case EQ: case NE: case AND: case OR:
            case TO: case INDEX: case XPATH:
                visitOp(expr);
                break;
            case VARIABLE:
                visitVariable(expr);
                break;
            case STRING:
                mv.visitLdcInsn(expr.getNodeText());
                break;
            case NUMBER:
                visitNumber(expr.getNodeText());
                break;
            case NODE:
                visitNode(expr);
                break;
            default:
                throw new RuntimeException("Not Implemented: "+toTypeName(expr.getNodeType()));
        }
    }

    private void visitVariable(AST expr) {
        String variable = expr.getNodeText();
        if (!isFree(variable)) {
            mv.visitVarInsn(ALOAD, resolve(variable));
            return;
        }
        int index = resolveFree(variable);
        // load initializer flag
        // XXX: initializer flag doesn't work, as there is JVM VerifyError.
        // XXX: so we always look up the environment table for every reference.
        // Exception in thread "main" java.lang.VerifyError: ... Accessing value from uninitialized register 2
////        mv.visitIntInsn(ILOAD, index);
//        mv.visitInsn(ICONST_0);
        Label pushResult = new Label();
//        mv.visitJumpInsn(IFNE, pushResult);
        // if environment == null
        mv.visitVarInsn(ALOAD, environment_index);
        Label initVariable = new Label();
        mv.visitJumpInsn(IFNONNULL, initVariable);
        // throw exception
        mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("Unbound variable "+variable);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        // init free variable
        mv.visitLabel(initVariable);
        mv.visitVarInsn(ALOAD, environment_index);
        mv.visitLdcInsn(variable);
        mv.visitMethodInsn(INVOKEVIRTUAL, ENVIRONMENT_CLASS, "getVariable", "(Ljava/lang/String;)Ljava/lang/Object;");
        mv.visitVarInsn(ASTORE, index+1);
        // if variable value is null
        mv.visitVarInsn(ALOAD, index+1);
        mv.visitJumpInsn(IFNONNULL, pushResult);
        // throw exception
        mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("Unbound variable "+variable);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        // push result
        mv.visitLabel(pushResult);
        mv.visitVarInsn(ALOAD, index+1);
    }

    private void visitCall(AST expr) {
        String functionName = expr.nth(1).getNodeText();
        invokeFunction(functionName, expr.getChildren(), 1);
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
        pushConst(n);
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
        for (Object element: expr.getChildren()) {
            mv.visitInsn(DUP);
            visitExpr((AST) element);
            pushToList();
        }
    }

    private void visitFor(AST expr) {
        pushScope();
        String variable = expr.nth(1).getNodeText();
        AST varExpr = expr.nth(2);
        AST body = expr.nth(3);
        AST where = expr.nth(4);

        newList();
        int result = defineAnonymous();
        mv.visitVarInsn(ASTORE, result);

        int iterator = defineAnonymous();
        int element = define(variable);
        visitExpr(varExpr);
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME_OP, "asList", "(Ljava/lang/Object;)Ljava/lang/Iterable;");
        mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/Iterable", "iterator", "()Ljava/util/Iterator;");
        mv.visitVarInsn(ASTORE, iterator);

        Label condition = new Label();
        Label loop = new Label();
        mv.visitJumpInsn(GOTO, condition);

        // loop iteration
        mv.visitLabel(loop);
        mv.visitVarInsn(ALOAD, iterator);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
        mv.visitVarInsn(ASTORE, element);

        // loop body
        if (where != null && !where.isNil()) {
            visitExpr(where);
//            mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
//            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
            mv.visitMethodInsn(INVOKESTATIC, RUNTIME_OP, "asBool", "(Ljava/lang/Object;)Z");
            Label endif = new Label();
            mv.visitJumpInsn(IFEQ, endif);
            // if body
            mv.visitVarInsn(ALOAD, result);
            visitExpr(body);
            pushToList();
            // end if
            mv.visitLabel(endif);
        } else {
            mv.visitVarInsn(ALOAD, result);
            visitExpr(body);
            pushToList();
        }

        // loop condition
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
        AST where = expr.nth(4);

        int index = define(variable);
        visitExpr(varExpr);
        mv.visitVarInsn(ASTORE, index);

        if (where != null && !where.isNil()) {
            visitExpr(where);
//            mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
//            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
            mv.visitMethodInsn(INVOKESTATIC, RUNTIME_OP, "asBool", "(Ljava/lang/Object;)Z");
            Label endif = new Label();
            Label els = new Label();
            mv.visitJumpInsn(IFEQ, els);
            // if body
            visitExpr(body);
            mv.visitJumpInsn(GOTO, endif);
            // else body
            mv.visitLabel(els);
            pushNil();
            // end if
            mv.visitLabel(endif);
        }
        else
        {
            visitExpr(body);
        }

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
            case NEGATIVE:
                op = "negative";
                break;
            case MOD:
                op = "mod";
                break;
            case EQ:
                op = "eq";
                break;
            case NE:
                op = "ne";
                break;
            case AND:
                op = "and";
                break;
            case OR:
                op = "or";
                break;
            case TO:
                op = "to";
                break;
            case INDEX:
                op = "at";
                break;
            case XPATH:
                op = "xpath";
                break;
            default:
                throw new RuntimeException("Not Implemented: "+toTypeName(type));
        }
        invokeFunction("op:" + op, expr.getChildren(), 0);
    }

    private void visitNode(AST expr) {
        ArrayList<AST> list = new ArrayList<AST>();
        flattenNode(expr, list);
        list = mergeStringNode(list);
        if (list.size() == 1) {
            AST singleton = list.get(0);
            switch (singleton.getNodeType()) {
                case TEXT: case TAGOPEN: case TAGCLOSE: case TAGUNIT:
                    pushConst(singleton.getNodeText());
                    break;
                default:
                    throw new RuntimeException("Not supposed to happen...");
            }
        }
        else {
            newObject("java/lang/StringBuilder");
            for (AST element: list) {
                switch (element.getNodeType()) {
                    case TEXT: case TAGOPEN: case TAGCLOSE: case TAGUNIT:
                        pushConst(element.getNodeText());
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                        break;
                    default:
                        visitExpr(element);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                        break;
                }
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
        }
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(SWAP);
        mv.visitMethodInsn(INVOKESPECIAL, compiledClassName.replace('.', '/'), "toXML", "(Ljava/lang/String;)L" + XML_INTERFACE + ";");
    }

    private void flattenNode(AST expr, ArrayList<AST> list) {
        switch (expr.getNodeType()) {
            case NODE:
                for (Object node: expr.getChildren()) {
                    flattenNode((AST) node, list);
                }
                break;
            default:
                list.add(expr);
        }
    }

    private ArrayList<AST> mergeStringNode(ArrayList<AST> source) {
        ArrayList<AST> target = new ArrayList<AST>();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < source.size(); i++) {
            AST node = source.get(i);
            if (node.getNodeType() == STRING) {
                if (buffer.length() == 0 && i + 1 < source.size() && source.get(i+1).getNodeType()!=STRING) {
                    target.add(node);
                }
                else {
                    buffer.append(node.getNodeText());
                }
            }
            else {
                if (buffer.length() != 0) {
                    target.add(new AST(new Token(STRING, buffer.toString())));
                }
                target.add(node);
            }
        }
        if (buffer.length() != 0) {
            target.add(new AST(new Token(STRING, buffer.toString())));
        }
        return target;
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


    private void pushNil() {
//        newObject(NIL);
        mv.visitInsn(ACONST_NULL);
    }

    private void pushNilObject() {
        newObject(NIL);
    }

    private void newList() {
        newObject(QUERY_LIST);
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

    private void invokeFunction(String functionName, AST arguments, int argumentIndex) {
        Function fn = (Function) namespace.lookup(functionName);
        if (fn == null) {
            throw new RuntimeException("Function not found: "+functionName);
        }
        else if (fn instanceof StandardStaticFunction) {
            invokeFunction((StandardStaticFunction) fn, arguments, argumentIndex);
        }
        else if (fn instanceof StandardStaticVarlistFunction) {
            invokeFunction((StandardStaticVarlistFunction) fn, arguments, argumentIndex);
        }
        else if (fn instanceof StandardStaticOverloadedFunction) {
            invokeFunction((StandardStaticOverloadedFunction) fn, arguments, argumentIndex);
        }
        else if (fn instanceof NormalStaticFunction) {
            invokeFunction((NormalStaticFunction) fn, arguments, argumentIndex);
        }
        else if (fn instanceof NormalMethodFunction) {
            invokeFunction((NormalMethodFunction) fn, arguments, argumentIndex);
        }
        else if (fn instanceof NormalConstructorFunction) {
            invokeFunction((NormalConstructorFunction) fn, arguments, argumentIndex);
        }
        else {
            throw new RuntimeException("Not Implemented: " + fn);
        }
    }

    private void invokeFunction(NormalStaticFunction fn, AST arguments, int argumentIndex) {
        int n = arguments.size() - argumentIndex;
        Class<?>[] params = fn.getParameterTypes();
        if (n != params.length) {
            throw new RuntimeException(
                    String.format("Too % arguments. Expected: %d, actual: %s",
                            n < params.length ? "few" : "many",
                            params.length,
                            n));
        }
        for (int i = 0; i < n; i++) {
            visitExpr(arguments.nth(i + argumentIndex));
            Caster.castTo(mv, params[i]);
        }
        Caster.castFrom(mv, fn.getReturnType());
        mv.visitMethodInsn(INVOKESTATIC, fn.getClassName(), fn.getFunctionName(), fn.getSignature());
    }

    private void invokeFunction(StandardStaticFunction fn, AST arguments, int argumentIndex) {
        int n = arguments.size() - argumentIndex;
        if (n != fn.getParameterNumber()) {
            throw new RuntimeException(
                    String.format("Too % arguments. Expected: %d, actual: %s",
                            n < fn.getParameterNumber() ? "few" : "many",
                            fn.getParameterNumber(),
                            n));
        }
        for (int i = 0; i < n; i++) {
            visitExpr(arguments.nth(i + argumentIndex));
        }
        mv.visitMethodInsn(INVOKESTATIC, fn.getClassName(), fn.getFunctionName(), fn.getSignature());
    }

    private void invokeFunction(StandardStaticOverloadedFunction fn, AST arguments, int argumentIndex) {
        invokeFunction(fn.getFunction(arguments.size() - argumentIndex), arguments, argumentIndex);
    }

    private void invokeFunction(StandardStaticVarlistFunction fn, AST arguments, int argumentIndex) {
        int n = arguments.size() - argumentIndex;
        pushConst(n);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        for (int i = 0; i < n; i++) {
            mv.visitInsn(DUP);
            pushConst(i);
            visitExpr(arguments.nth(i + argumentIndex));
            mv.visitInsn(AASTORE);
        }
        mv.visitMethodInsn(INVOKESTATIC, fn.getClassName(), fn.getFunctionName(), fn.getSignature());
    }

    private void invokeFunction(NormalMethodFunction fn, AST arguments, int argumentIndex) {
        visitExpr(arguments.nth(argumentIndex++));
        int n = arguments.size() - argumentIndex;
        Class<?>[] params = fn.getParameterTypes();
        if (n != params.length) {
            throw new RuntimeException(
                    String.format("Too % arguments. Expected: %d, actual: %s",
                            n < params.length ? "few" : "many",
                            params.length,
                            n));
        }
        for (int i = 0; i < n; i++) {
            visitExpr(arguments.nth(i + argumentIndex));
            Caster.castTo(mv, params[i]);
        }
        Caster.castFrom(mv, fn.getReturnType());
        mv.visitMethodInsn(INVOKEVIRTUAL, fn.getClassName(), fn.getFunctionName(), fn.getSignature());
    }

    private void invokeFunction(NormalConstructorFunction fn, AST arguments, int argumentIndex) {
        mv.visitTypeInsn(NEW, fn.getClassName());
        mv.visitInsn(DUP);
        int n = arguments.size() - argumentIndex;
        Class<?>[] params = fn.getParameterTypes();
        if (n != params.length) {
            throw new RuntimeException(
                    String.format("Too % arguments. Expected: %d, actual: %s",
                            n < params.length ? "few" : "many",
                            params.length,
                            n));
        }
        for (int i = 0; i < n; i++) {
            visitExpr(arguments.nth(i + argumentIndex));
            Caster.castTo(mv, params[i]);
        }
        mv.visitMethodInsn(INVOKESPECIAL, fn.getClassName(), fn.getFunctionName(), fn.getSignature());
    }

    private void invokeStatic(AST expr, String className, String op) {
        StringBuilder signature = new StringBuilder();
        signature.append('(');
        expr = expr.getChildren();
        while (expr != null) {
            visitExpr((AST) expr.first());
            signature.append("Ljava/lang/Object;");
        }
        signature.append(")Ljava/lang/Object;");
        mv.visitMethodInsn(INVOKESTATIC, className, op, signature.toString());
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

    private int defineFree(String name) {
        int index = locals;
        locals += 2;
        freeScope.define(new Symbol(name, index));
        return index;
    }

    private boolean isFree(String name) {
        return scope.resolve(name) == null;
    }

    private int resolve(String name) {
        Symbol s = scope.resolve(name);
        if (s == null) {
            throw new CompilerException("Variable undefined: "+name);
        }
        return s.getIndex();
    }

    private int resolveFree(String name) {
        Symbol s = freeScope.resolve(name);
        if (s == null) {
            return defineFree(name);
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
