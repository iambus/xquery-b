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

    private ArrayList<Symbol> symbols = new ArrayList<Symbol>();
    private Scope scope = new Scope();
    private Scope freeScope = new Scope();
    private int locals = 5; // index 2 is used as temporary double variable
    private final int environment_index = 1;
    private final int result_index = 2;
    private final int temp_index = 3;

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
        Class returnType = visitAST();
        if (returnType.isPrimitive()) {
            Caster.castToObject(mv, returnType);
            mv.visitInsn(ARETURN);
        }
        else {
//            mv.visitVarInsn(ASTORE, result_index);
//            mv.visitVarInsn(ALOAD, result_index);
//            Label returnLabel = new Label();
//            mv.visitJumpInsn(IFNONNULL, returnLabel);
//            pushNil();
//            mv.visitInsn(ARETURN);
//            mv.visitLabel(returnLabel);
//            mv.visitVarInsn(ALOAD, result_index);
//            mv.visitInsn(ARETURN);

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
        return visitExpr(code);
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

    private Class visitExpr(AST expr) {
        switch (expr.getNodeType()) {
            case FLOWER:
                return visitFlower(expr);
            case LIST:
                return visitList(expr);
            case CALL:
                return visitCall(expr);
            case PLUS: case MINUS: case MULTIPLY: case DIV: case NEGATIVE: case MOD:
            case EQ: case NE: case AND: case OR:
            case TO: case INDEX: case XPATH:
                return visitOp(expr);
            case VARIABLE:
                return visitVariable(expr);
            case STRING:
                mv.visitLdcInsn(expr.getNodeText());
                return String.class;
            case NUMBER:
                return visitNumber(expr.getNodeText());
            case NODE:
                return visitNode(expr);
            default:
                throw new RuntimeException("Not Implemented: "+toTypeName(expr.getNodeType()));
        }
    }

    private Class visitVariable(AST expr) {
        String variable = expr.getNodeText();
        if (!isFree(variable)) {
            Class t = resolveType(variable);
            if (t.isPrimitive()) {
                if (t == int.class) {
                    mv.visitVarInsn(ILOAD, resolve(variable));
                }
                else if (t == double.class) {
                    mv.visitVarInsn(DLOAD, resolve(variable));
                }
                else {
                    throw new RuntimeException("Not Implemented!");
                }
            }
            else {
                mv.visitVarInsn(ALOAD, resolve(variable));
            }
            return t;
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
        return Object.class;
    }

    private Class visitCall(AST expr) {
        String functionName = expr.nth(1).getNodeText();
        return invokeFunction(functionName, ((AST) expr.next()).rest());
    }

    private Class visitNumber(String text) {
        if (text.indexOf('.') == -1) {
            return visitInt(text);
        }
        else {
            return visitDouble(text);
        }
    }

    private Class visitInt(String text) {
        int n = Integer.parseInt(text);
        pushConst(n);
//        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
//        return Integer.class;
        return int.class;
    }

    private Class visitDouble(String text) {
        double d = Double.parseDouble(text);
        // TODO: optimize:
        // mv.visitInsn(DCONST_0);
        // mv.visitInsn(DCONST_1);
        mv.visitLdcInsn(d);
//        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
//        return Double.class;
        return double.class;
    }

    private Class visitList(AST expr) {
        Class t = newList();
        for (Object element: expr.rest()) {
            mv.visitInsn(DUP);
            Class elementType = visitExpr((AST) element);
            Caster.castToObject(mv, elementType);
            pushToList();
        }
        return t;
    }

    private Class visitFlower(AST expr) {
        Class t = newList();
        int result = defineAnonymous();
        mv.visitVarInsn(ASTORE, result);

        AST forlets = expr.nth(1).rest();
        AST body = (AST) expr.next().next();

        visitForLets(forlets, body, result);

        mv.visitVarInsn(ALOAD, result);
        return t;
    }

    private void visitForLets(AST forlets, AST body, int result) {
        if (forlets == null) {
            visitFlowerBody(body, result);
        }
        else {
            switch (((AST)forlets.first()).getNodeType()) {
                case FOR:
                    visitFor(forlets, body, result);
                    break;
                case LET:
                    visitLet(forlets, body, result);
                    break;
                default:
                    throw new RuntimeException("Not Implemented!");
            }
        }
    }

    private void visitFlowerBody(AST expr, int result) {
        AST body = expr.nth(0);
        AST where = expr.nth(1);
        // loop body
        if (where != null && !where.isNil()) {
            Class t = visitExpr(where);
            if (t.isPrimitive()) {
                if (t == boolean.class) {
                    // already boolean, do nothing
                }
                else {
                    throw new RuntimeException("Not Implemented!");
                }
            }
            else {
                mv.visitMethodInsn(INVOKESTATIC, RUNTIME_OP, "asBool", "(Ljava/lang/Object;)Z");
            }
            Label endif = new Label();
            mv.visitJumpInsn(IFEQ, endif);
            // if body
            mv.visitVarInsn(ALOAD, result);
            Class elementType = visitExpr(body);
            Caster.castToObject(mv, elementType);
            pushToList();
            // end if
            mv.visitLabel(endif);
        } else {
            mv.visitVarInsn(ALOAD, result);
            Class elementType = visitExpr(body);
            Caster.castToObject(mv, elementType);
            pushToList();
        }
    }

    private void visitFor(AST forlets, AST body, int result) {
        pushScope();
        AST expr = (AST) forlets.first();
        String variable = expr.nth(1).getNodeText();
        AST varExpr = expr.nth(2);

        if (isForRange(varExpr)) {
            visitForRange(variable, varExpr, forlets, body, result);
        }
        else {
            visitForGeneral(variable, varExpr, forlets, body, result);
        }

        popScope();
    }

    private boolean isForRange(AST varExpr) {
        return varExpr.getNodeType() == TO;
    }

    private void visitForRange(String variable, AST range, AST forlets, AST body, int result) {
        int i = define(variable, int.class);
        // TODO: if max is literal, use pushConst instead of variable
        int max = defineAnonymous();
        Class left = visitExpr(range.nth(1));
        Caster.cast(mv, left, int.class);
        mv.visitVarInsn(ISTORE, i);
        Class right = visitExpr(range.nth(2));
        Caster.cast(mv, right, int.class);
        mv.visitVarInsn(ISTORE, max);

        Label condition = new Label();
        Label loop = new Label();
        mv.visitJumpInsn(GOTO, condition);

        // do
        mv.visitLabel(loop);
        visitForLets((AST) forlets.next(), body, result);

        // i++
        mv.visitIincInsn(i, 1);

        // if i < max?
        mv.visitLabel(condition);
        mv.visitVarInsn(ILOAD, i);
        mv.visitVarInsn(ILOAD, max);
        mv.visitJumpInsn(IF_ICMPLE, loop);
    }

    private void visitForGeneral(String variable, AST varExpr, AST forlets, AST body, int result) {
        int iterator = defineAnonymous();
        int element = define(variable, Object.class);
        Class collectionType = visitExpr(varExpr);
        Caster.castToObject(mv, collectionType);
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

        visitForLets((AST) forlets.next(), body, result);

        // loop condition
        mv.visitLabel(condition);
        mv.visitVarInsn(ALOAD, iterator);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
        mv.visitJumpInsn(IFNE, loop);
    }

    private void visitLet(AST forlets, AST body, int result) {
        pushScope();
        AST expr = (AST) forlets.first();
        String variable = expr.nth(1).getNodeText();
        AST varExpr = expr.nth(2);

        Class varType = visitExpr(varExpr);
        int index = define(variable, varType);
        if (varType.isPrimitive()) {
            if (varType == int.class) {
                mv.visitVarInsn(ISTORE, index);
            }
            else if (varType == double.class) {
                mv.visitVarInsn(DSTORE, index);
            }
            else {
                throw new RuntimeException("Not Implemented!");
            }
        }
        else {
            mv.visitVarInsn(ASTORE, index);
        }


        visitForLets((AST) forlets.next(), body, result);
//        if (where != null && !where.isNil()) {
//            visitExpr(where);
////            mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
////            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
//            mv.visitMethodInsn(INVOKESTATIC, RUNTIME_OP, "asBool", "(Ljava/lang/Object;)Z");
//            Label endif = new Label();
//            Label els = new Label();
//            mv.visitJumpInsn(IFEQ, els);
//            // if body
//            Class t = visitExpr(body);
//            Caster.castToObject(mv, t);
//            mv.visitJumpInsn(GOTO, endif);
//            // else body
//            mv.visitLabel(els);
//            pushNil();
//            // end if
//            mv.visitLabel(endif);
//        }
//        else
//        {
//            returnType = visitExpr(body);
//        }

        popScope();
    }

    private Class visitOp(AST expr) {
        int type = expr.getNodeType();
        String op;
        switch (type) {
            case PLUS:
            case MINUS:
            case MULTIPLY:
            case DIV:
            case MOD:
                return visitArithmeticOp(expr);
            case NEGATIVE:
                op = "negative";
                break;
            case EQ:
            case NE:
                return visitArithmeticCompare(expr);
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
        return invokeFunction("op:" + op, (AST) expr.next());
    }

    private Class visitArithmeticOp(AST expr) {
        if (expr.size() != 3) {
            throw new RuntimeException("Not Implemented!");
        }
        int opType = expr.getNodeType();
        Class type = pushNumbers((AST)(expr.next().first()), (AST)(expr.next().next().first()));
        switch (opType) {
            case PLUS:
                return invokeArithmeticOp(type, IADD, DADD, "add");
            case MINUS:
                return invokeArithmeticOp(type, ISUB, DSUB, "subtract");
            case MULTIPLY:
                return invokeArithmeticOp(type, IMUL, DMUL, "multiply");
            case DIV:
                return invokeArithmeticOp(type, IDIV, DDIV, "div");
            case MOD:
                return invokeArithmeticOp(type, IREM, DREM, "mod");
            default:
                throw new RuntimeException("Not Implemented!");
        }
    }

    private Class pushNumbers(AST left, AST right) {
        Class leftType = visitExpr(left);
        Class rightType = visitExpr(right);
        if (!leftType.isPrimitive()) {
            // object + ...
            Caster.castToObject(mv, rightType);
            return Object.class;
        }
        else if (!rightType.isPrimitive()) {
            // integer/double + object
            if (rightType == Integer.class) {
                return Caster.castBetweenPrimitives(mv, Caster.castToPrimitiveValue(mv, rightType), leftType);
            }
            else if (leftType == int.class) {
                mv.visitInsn(SWAP);
                Caster.castToObject(mv, leftType);
                mv.visitInsn(SWAP);
                return Object.class;
            }
            else {
                throw new RuntimeException("Not Implemented! "+leftType+" + "+rightType);
            }
        }
        else {
            return unifyNumberType(leftType, rightType);
        }
    }

    private Class unifyNumberType(Class leftType, Class rightType) {
        if (leftType == rightType) {
            return rightType;
        } else if (leftType == int.class && rightType == double.class) {
            mv.visitVarInsn(DSTORE, temp_index);
            Caster.castBetweenPrimitives(mv, int.class, double.class);
            mv.visitVarInsn(DLOAD, temp_index);
            return double.class;
        } else if (leftType == double.class && rightType == int.class) {
            return Caster.castBetweenPrimitives(mv, int.class, double.class);
        } else {
            throw new RuntimeException("Not Implemented!");
        }
    }

    private Class invokeArithmeticOp(Class valueType, int intInstruction, int doubleInstruction, String methodName) {
        if (valueType == int.class) {
            mv.visitInsn(intInstruction);
            return int.class;
        }
        else if (valueType == double.class) {
            mv.visitInsn(doubleInstruction);
            return double.class;
        }
        else if (!valueType.isPrimitive()) {
            return invokeBinaryOp(methodName);
        }
        else {
            throw new RuntimeException("Not Implemented!");
        }
    }

    private Class invokeBinaryOp(String methodName) {
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME_OP, methodName, "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        return Object.class;
    }

    private Class visitArithmeticCompare(AST expr) {
        if (expr.size() != 3) {
            throw new RuntimeException("Not Implemented!");
        }
        int opType = expr.getNodeType();
        AST leftTree = (AST)(expr.next().first());
        AST rightTree = (AST)(expr.next().next().first());
        Class leftType = visitExpr(leftTree);
        if (leftType != int.class) {
            Caster.cast(mv, leftType, Object.class);
            Caster.cast(mv, visitExpr(rightTree), Object.class);
            return invokeBinaryOp(opType == EQ ? "eq" : "ne");
        }
        else {
            Class rightType = visitExpr(rightTree);
            if (rightType == int.class) {
                mv.visitInsn(ISUB);
                if (opType == EQ) {
                    Label trueLabel = new Label();
                    Label falseLabel = new Label();
                    Label doneLabel = new Label();
                    mv.visitJumpInsn(IFEQ, trueLabel);
                    mv.visitLabel(falseLabel);
                    mv.visitInsn(ICONST_0);
                    mv.visitJumpInsn(GOTO, doneLabel);
                    mv.visitLabel(trueLabel);
                    mv.visitInsn(ICONST_1);
                    mv.visitLabel(doneLabel);
                }
                else {
                    Label trueLabel = new Label();
                    Label falseLabel = new Label();
                    Label doneLabel = new Label();
                    mv.visitJumpInsn(IFNE, trueLabel);
                    mv.visitLabel(falseLabel);
                    mv.visitInsn(ICONST_0);
                    mv.visitJumpInsn(GOTO, doneLabel);
                    mv.visitLabel(trueLabel);
                    mv.visitInsn(ICONST_1);
                    mv.visitLabel(doneLabel);
                }
                return boolean.class;
            }
            else {
                throw new RuntimeException("Not Implemented!");
            }
        }
    }

    private Class<XML> visitNode(AST expr) {
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
                        Class t = visitExpr(element);
                        if (t.isPrimitive()) {
                            if (t == int.class) {
                                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
                            }
                            else if (t == double.class) {
                                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(D)Ljava/lang/StringBuilder;");
                            }
                            else {
                                throw new RuntimeException("Not Implemented!");
                            }
                        }
                        else {
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                        }
                        break;
                }
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
        }
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(SWAP);
        mv.visitMethodInsn(INVOKESPECIAL, compiledClassName.replace('.', '/'), "toXML", "(Ljava/lang/String;)L" + XML_INTERFACE + ";");
        return XML.class;
    }

    private void flattenNode(AST expr, ArrayList<AST> list) {
        switch (expr.getNodeType()) {
            case NODE:
                for (Object node: expr.rest()) {
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

    private Class invokeFunction(String functionName, AST arguments) {
        Function fn = (Function) namespace.lookup(functionName);
        if (fn == null) {
            throw new RuntimeException("Function not found: "+functionName);
        }
        else if (fn instanceof StandardStaticFunction) {
            return invokeFunction((StandardStaticFunction) fn, arguments);
        }
        else if (fn instanceof StandardStaticVarlistFunction) {
            return invokeFunction((StandardStaticVarlistFunction) fn, arguments);
        }
        else if (fn instanceof StandardStaticOverloadedFunction) {
            return invokeFunction((StandardStaticOverloadedFunction) fn, arguments);
        }
        else if (fn instanceof NormalStaticFunction) {
            return invokeFunction((NormalStaticFunction) fn, arguments);
        }
        else if (fn instanceof NormalMethodFunction) {
            return invokeFunction((NormalMethodFunction) fn, arguments);
        }
        else if (fn instanceof NormalConstructorFunction) {
            return invokeFunction((NormalConstructorFunction) fn, arguments);
        }
        else if (fn instanceof OverloadedFunction) {
            return invokeFunction((OverloadedFunction) fn, arguments);
        }
        else {
            throw new RuntimeException("Not Implemented: " + fn);
        }
    }

    private Class invokeFunction(NormalStaticFunction fn, AST arguments) {
        int n = arguments.size();
        Class<?>[] params = fn.getParameterTypes();
        checkArgumentsNumber(n, fn.getParameterNumber());
        for (int i = 0; i < n; i++) {
            Class argumentType = visitExpr(arguments.nth(i));
            Caster.cast(mv, argumentType, params[i]);
        }
        mv.visitMethodInsn(INVOKESTATIC, fn.getClassName(), fn.getFunctionName(), fn.getSignature());
        return fn.getReturnType();
    }

    private Class invokeFunction(StandardStaticFunction fn, AST arguments) {
        int n = arguments.size();
        checkArgumentsNumber(n, fn.getParameterNumber());
        for (int i = 0; i < n; i++) {
            Class argumentType = visitExpr(arguments.nth(i));
            Caster.castToObject(mv, argumentType);
        }
        mv.visitMethodInsn(INVOKESTATIC, fn.getClassName(), fn.getFunctionName(), fn.getSignature());
        return Object.class;
    }

    private Class invokeFunction(StandardStaticOverloadedFunction fn, AST arguments) {
        return invokeFunction(fn.getFunction(arguments.size()), arguments);
    }

    private Class invokeFunction(StandardStaticVarlistFunction fn, AST arguments) {
        int n = arguments.size();
        pushConst(n);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        for (int i = 0; i < n; i++) {
            mv.visitInsn(DUP);
            pushConst(i);
            Class argumentType = visitExpr(arguments.nth(i));
            Caster.castToObject(mv, argumentType);
            mv.visitInsn(AASTORE);
        }
        mv.visitMethodInsn(INVOKESTATIC, fn.getClassName(), fn.getFunctionName(), fn.getSignature());
        return Object.class;
    }

    private Class invokeFunction(NormalMethodFunction fn, AST arguments) {
        Class instanceType = visitExpr((AST) arguments.first());
        arguments = arguments.rest();
        Caster.castToObject(mv, instanceType);
        int n = arguments.size();
        Class<?>[] params = fn.getParameterTypes();
        checkArgumentsNumber(n, params.length);
        for (int i = 0; i < n; i++) {
            Class argumentType = visitExpr(arguments.nth(i));
            Caster.cast(mv, argumentType, params[i]);
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, fn.getClassName(), fn.getFunctionName(), fn.getSignature());
        return fn.getReturnType();
    }

    private Class invokeFunction(NormalConstructorFunction fn, AST arguments) {
        mv.visitTypeInsn(NEW, fn.getClassName());
        mv.visitInsn(DUP);
        int n = arguments.size();
        Class<?>[] params = fn.getParameterTypes();
        checkArgumentsNumber(n, params.length);
        for (int i = 0; i < n; i++) {
            visitExpr(arguments.nth(i));
            Caster.castObjectTo(mv, params[i]);
        }
        mv.visitMethodInsn(INVOKESPECIAL, fn.getClassName(), fn.getFunctionName(), fn.getSignature());
        return fn.getReturnType();
    }
    private Class invokeFunction(OverloadedFunction dispatcher, AST arguments) {
        if (dispatcher.getFunctionName().equals("<init>")) {
            mv.visitTypeInsn(NEW, dispatcher.getClassName());
            mv.visitInsn(DUP);
        }
        int n = arguments.size();
        Class[] argumentTypes = new Class[n];
        for (int i = 0; i < n; i++) {
            argumentTypes[i] = visitExpr(arguments.nth(i));
        }
        JavaFunction fn = dispatcher.resolveFunction(argumentTypes);
        if (fn instanceof NormalConstructorFunction) {
            mv.visitMethodInsn(INVOKESPECIAL, fn.getClassName(), fn.getFunctionName(), fn.getSignature());
        }
        else if (fn instanceof NormalStaticFunction) {
            throw new RuntimeException("Not Implemented! "+fn);
        }
        else if (fn instanceof NormalMethodFunction) {
            throw new RuntimeException("Not Implemented! "+fn);
        }
        else {
            throw new RuntimeException("Not Implemented! "+fn);
        }
        return fn.getReturnType();
    }

    private void checkArgumentsNumber(int expected, int actual) {
        if (actual != expected) {
            throw new RuntimeException(
                    String.format("Too % arguments. Expected: %d, actual: %s",
                            actual < expected ? "few" : "many",
                            expected,
                            actual));
        }
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

    private int define(String name, Class type) {
        int index = locals++;
        if (type == double.class || type == long.class) {
            locals++;
        }
        Symbol sym = new Symbol(name, index, type);
        scope.define(sym);
        symbols.add(sym);
        return index;
    }

    private int defineFree(String name) {
        int index = locals;
        locals += 2;
        Symbol sym = new Symbol(name, index);
        freeScope.define(sym);
        symbols.add(sym);
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

    private Class resolveType(String name) {
        Symbol s = scope.resolve(name);
        if (s == null) {
            throw new CompilerException("Variable undefined: "+name);
        }
        return s.getType();
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
