package org.libj.xquery.compiler;

import org.libj.xquery.lexer.Token;
import org.libj.xquery.lisp.Cons;
import org.libj.xquery.namespace.*;
import org.libj.xquery.parser.*;
import org.libj.xquery.xml.XML;

import static org.libj.xquery.lexer.TokenType.*;
import static org.libj.xquery.compiler.Constants.*;

import java.util.ArrayList;
import java.util.Map;

public class Walker {
    private Cons ast;
    private ArrayList<Symbol> symbols = new ArrayList<Symbol>();
    private Scope scope = new Scope();
    private Scope freeScope = new Scope();
    private Namespace namespace;

    private int locals = LOCAL_VAR_START; // index 2 is used as temporary double variable

    public Walker(Cons tree, Namespace namespace) {
        ast = tree;
        this.namespace = namespace;
    }

    public int getLocals() {
        return locals;
    }

    public Map<String, Symbol> getFreeVariables() {
        return freeScope.getSymbols();
    }

    public Cons walk() {
        return walkExpr(ast);
    }

    private Cons walkExpr(Cons expr) {
        switch (AST.getNodeType(expr)) {
            case FLOWER:
                return walkFlower(expr);
            case IF:
                return walkIf(expr);
            case NODE:
                return walkNode(expr);
            case LIST:
                return walkList(expr);
            case VARIABLE:
                return walkVariable(expr);
            case NUMBER:
                return walkNumber(AST.getToken(expr));
            case STRING:
                return walkString(AST.getToken(expr));
            case PLUS: case MINUS: case MULTIPLY: case DIV: case NEGATIVE: case MOD:
            case EQ: case NE: case AND: case OR:
            case TO: case INDEX: case XPATH:
                return walkOp(expr);
            case CALL:
                return walkCall(expr);
            default:
                throw new RuntimeException("Not Implemented! "+expr);
        }
    }

    private Cons walkNumber(Token token) {
        String text = token.text;
        if (text.indexOf('.') == -1) {
            int n = Integer.parseInt(text);
            return AST.createAST(new ConstantElement(token, n, int.class));
        }
        else {
            double d = Double.parseDouble(text);
            return AST.createAST(new ConstantElement(token, d, double.class));
        }
    }

    private Cons walkString(Token token) {
        return AST.createAST(new ConstantElement(token, token.text, String.class));
    }

    private Cons walkVariable(Cons expr) {
        String variable = AST.getNodeText(expr);
        if (!isFree(variable)) {
            return AST.createAST(new VariableElement(AST.getElement(expr), resolveType(variable), resolve(variable)));
        }
        else {
            int index = resolveFree(variable);
            return AST.createAST(new VariableElement(AST.getElement(expr), Object.class, index));
        }
    }

    private Cons walkOp(Cons expr) {
        int type = AST.getNodeType(expr);
        switch (type) {
            case PLUS: case MINUS: case MULTIPLY: case DIV: case MOD:
                return walkBinaryArithmetic(expr);
            case NEGATIVE:
                return walkNegative(expr);
            case EQ: case NE:
                return walkComparison(expr);
            case AND: case OR:
                return walkLogic(expr);
            case TO:
                return walkTo(expr);
            case INDEX:
                return walkIndex(expr);
            case XPATH:
                return walkXPath(expr);
            default:
                throw new RuntimeException("Not Implemented! "+toTypeName(type));
        }
    }

    private Cons walkBinaryArithmetic(Cons expr) {
        return unifyArithmetic(walkSub2(expr));
    }

    private Cons walkNegative(Cons expr) {
        Cons v = walkExpr(AST.nthAST(expr, 1));
        expr = assoc1(expr, v);
        expr = assocType(expr, AST.getEvalType(v));
        return expr;
    }

    private Cons walkSub2(Cons expr) {
        Cons left = walkExpr(AST.nthAST(expr, 1));
        Cons right = walkExpr(AST.nthAST(expr, 2));
        expr = assoc1(expr, left);
        expr = assoc2(expr, right);
        return expr;
    }

    private Cons walkLogic(Cons expr) {
        Cons left = castTo(walkExpr(AST.nthAST(expr, 1)), boolean.class);
        Cons right = castTo(walkExpr(AST.nthAST(expr, 2)), boolean.class);
        expr = assoc1(expr, left);
        expr = assoc2(expr, right);
        expr = assocType(expr, boolean.class);
        return expr;
    }

    private Cons walkComparison(Cons expr) {
        return assocType(walkBinaryArithmetic(expr), boolean.class);
    }

    private Cons walkXPath(Cons expr) {
        expr = assocType(expr, XML_INTERFACE_TYPE);
        expr = assoc1(expr, castTo(walkVariable(AST.nthAST(expr, 1)), XML_INTERFACE_TYPE));
        return expr;
    }

    private Cons walkIndex(Cons expr) {
        Cons list = AST.nthAST(expr, 1);
        Cons at = AST.nthAST(expr, 2);
        if (AST.getNodeType(list) == FLOWER) {
            Cons flower = walkFlower(list);
            Cons flowerAt = castTo(walkExpr(at), int.class);
            expr = AST.createAST(FLOWERAT);
            expr = Cons.append(expr, flower);
            expr = Cons.append(expr, flowerAt);
            return expr;
        }
        else {
            Cons left = castTo(walkExpr(list), Object.class);
            Cons right = castTo(walkExpr(at), int.class);
            expr = assoc1(expr, left);
            expr = assoc2(expr, right);
            expr = assocType(expr, Object.class);
            return expr;
        }
    }

    private Cons walkTo(Cons expr) {
        Cons left = castTo(walkExpr(AST.nthAST(expr, 1)), int.class);
        Cons right = castTo(walkExpr(AST.nthAST(expr, 2)), int.class);
        expr = assoc1(expr, left);
        expr = assoc2(expr, right);
        expr = assocType(expr, LIST_CLASS_TYPE);
        return expr;
    }

    private Cons walkList(Cons expr) {
        Cons ast = AST.createAST(AST.getElement(expr));
        ast = assocType(ast, LIST_CLASS_TYPE);
        for (Object e: Cons.rest(expr)) {
            ast = Cons.append(ast, castTo(walkExpr((Cons) e), Object.class));
        }
        return ast;
    }

    private Cons walkIf(Cons expr) {
        Cons condition = walkExpr(AST.nthAST(expr, 1));
        Cons thenValue = walkExpr(AST.nthAST(expr, 2));
        Cons elseValue = walkExpr(AST.nthAST(expr, 3));
        expr = assoc1(expr, castTo(condition, boolean.class));
        if (AST.getEvalType(elseValue) == AST.getEvalType(thenValue)) {
            expr = assoc2(expr, thenValue);
            expr = assoc3(expr, elseValue);
            expr = assocType(expr, AST.getEvalType(elseValue));
            return expr;
        }
        else {
            expr = assoc2(expr, castTo(thenValue, Object.class));
            expr = assoc3(expr, castTo(elseValue, Object.class));
            expr = assocType(expr, Object.class);
            return expr;
        }
    }

    private Cons walkFlower(Cons expr) {
        Cons forlets = Cons.rest(AST.nthAST(expr, 1));
        Cons body = (Cons) expr.next().next().first();
        Cons where =  (Cons) expr.next().next().next().first();
        expr = AST.createAST(new TypedElement(AST.getElement(expr), LIST_CLASS_TYPE));
        return Cons.cons(expr.first(), walkForlet(forlets, body, where));
    }

    private Cons walkForlet(Cons forlets, Cons body, Cons where) {
        if (forlets == null || Cons.isNil(forlets)) {
            return walkFlowerWhereBody(body, where);
        }
        else {
            switch (AST.getNodeType(((Cons) forlets.first()))) {
                case LET:
                    return walkLet(forlets, body, where);
                case FOR:
                    return walkFor(forlets, body, where);
                default:
                    throw new RuntimeException("Wrong code!");
            }
        }
    }

    private Cons walkFlowerWhereBody(Cons body, Cons where) {
        if (where != null && !Cons.isNil(where)) {
            where = walkExpr(where);
        }
        body = walkExpr(body);
        return tuple(null, body, where);
    }

    private Cons walkLet(Cons forlets, Cons body, Cons where) {
        pushScope();

        Cons expr = (Cons) forlets.first();
        Cons variableExpr = AST.nthAST(expr, 1);
        String variableName = AST.getNodeText(variableExpr);
        Cons valueExpr = AST.nthAST(expr, 2);

        valueExpr = walkExpr(valueExpr);
        Class valueType = AST.getEvalType(valueExpr);
        int index = define(variableName, valueType);
        variableExpr = AST.createAST(new VariableElement(AST.getElement(variableExpr), valueType, index));

        expr = assocType(expr, valueType);
        expr = assoc1(expr, variableExpr);
        expr = assoc2(expr, valueExpr);

        Cons result = walkForlet(Cons.rest(forlets), body, where);
        Cons thisLet = AST.createAnyAST(expr);
        thisLet = Cons.cons(thisLet.first(), (Cons) result.first());
        result = Cons.cons(thisLet, result.next());

        popScope();
        return result;
    }

    private Cons walkFor(Cons forlets, Cons body, Cons where) {
        if (AST.getNodeType(AST.nthAST(((Cons) forlets.first()), 2)) == TO) {
            return walkForRange(forlets, body, where);
        }
        else {
            return walkForGeneral(forlets, body, where);
        }
    }

    private Cons walkForRange(Cons forlets, Cons body, Cons where) {
        pushScope();

        Cons expr = (Cons) forlets.first();
        Cons variableExpr = AST.nthAST(expr, 1);
        String variableName = AST.getNodeText(variableExpr);
        Cons rangeExpr = AST.nthAST(expr, 2);

        Cons start = castTo(walkExpr(AST.nthAST(rangeExpr, 1)), int.class);
        Cons end = castTo(walkExpr(AST.nthAST(rangeExpr, 2)), int.class);
        int element = define(variableName, int.class);

        variableExpr = AST.createAST(new VariableElement(AST.getElement(variableExpr), int.class, element));

        expr = assocType(expr, LIST_CLASS_TYPE);
        expr = assoc1(expr, variableExpr);
        expr = assoc2(expr, start);
        expr = Cons.append(expr, end);
        AST.getToken(expr).type = FORRANGE;

        Cons result = walkForlet(Cons.rest(forlets), body, where);
        Cons thisFor = AST.createAnyAST(expr);
        thisFor = Cons.cons(thisFor.first(), (Cons)result.first());
        result = Cons.cons(thisFor, result.next());

        popScope();
        return result;
    }

    private Cons walkForGeneral(Cons forlets, Cons body, Cons where) {
        pushScope();

        Cons expr = (Cons) forlets.first();
        Cons variableExpr = AST.nthAST(expr, 1);
        String variableName = AST.getNodeText(variableExpr);
        Cons collectionExpr = AST.nthAST(expr, 2);

        collectionExpr = castTo(walkExpr(collectionExpr), Object.class);
        int element = define(variableName, Object.class);
        variableExpr = AST.createAST(new VariableElement(AST.getElement(variableExpr), Object.class, element));

        expr = assocType(expr, LIST_CLASS_TYPE);
        expr = assoc1(expr, variableExpr);
        expr = assoc2(expr, collectionExpr);

        Cons result = walkForlet(Cons.rest(forlets), body, where);
        Cons thisFor = AST.createAnyAST(expr);
        thisFor = Cons.cons(thisFor.first(), (Cons)result.first());
        result = Cons.cons(thisFor, result.next());

        popScope();
        return result;
    }


    private Cons walkNode(Cons expr) {
        ArrayList<Cons> list = new ArrayList<Cons>();
        flattenNode(expr, list);
        list = mergeStringNode(list);
        expr = AST.createAST(AST.getElement(expr));
        for (Cons<Unit> ast: list) {
            if (isNodeLiteral(ast)) {
                expr = Cons.append(expr, ast);
            }
            else {
                expr = Cons.append(expr, walkExpr(ast));
            }
        }
        expr = assocType(expr, XML.class);
        return expr;
    }

    private void flattenNode(Cons expr, ArrayList<Cons> list) {
        switch (AST.getNodeType(expr)) {
            case NODE:
                for (Object node: Cons.rest(expr)) {
                    flattenNode((Cons) node, list);
                }
                break;
            default:
                list.add(expr);
        }
    }

    private boolean isNodeLiteral(Cons node) {
        switch (AST.getNodeType(node)) {
            case TEXT: case TAGOPEN: case TAGCLOSE: case TAGUNIT:
                return true;
            default:
                return false;
        }
    }

    private ArrayList<Cons> mergeStringNode(ArrayList<Cons> source) {
        ArrayList<Cons> target = new ArrayList<Cons>();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < source.size(); i++) {
            Cons node = source.get(i);
            if (isNodeLiteral(node)) {
                if (buffer.length() == 0 && i + 1 < source.size() && !isNodeLiteral(source.get(i+1))) {
                    AST.getToken(node).type = TEXT;
                    target.add(node);
                }
                else {
                    buffer.append(AST.getNodeText(node));
                }
            }
            else {
                if (buffer.length() != 0) {
                    target.add(AST.createAST(new Token(TEXT, buffer.toString())));
                    buffer.setLength(0);
                }
                target.add(node);
            }
        }
        if (buffer.length() != 0) {
            target.add(AST.createAST(new Token(TEXT, buffer.toString())));
        }
        return target;
    }

    private Cons walkCall(Cons expr) {
        String functionName = AST.getNodeText(AST.nthAST(expr, 1));
        Function fn = (Function) namespace.lookup(functionName);
//        return invokeFunction(functionName, ((Cons) expr.next()).rest());
        if (fn instanceof JavaFunction) {
            return walkFunction((JavaFunction) fn, expr);
        }
        else if (fn instanceof OverloadedFunction) {
            return walkFunction((OverloadedFunction) fn, expr);
        }
        else {
            throw new RuntimeException("Not Implemented: " + fn);
        }
    }

    private Cons walkFunctionArguments(JavaFunction fn, Cons expr, Cons arguments) {
        expr = new Cons(new FunctionElement(AST.getElement(expr), fn.getReturnType(), fn));

        if (fn.isMethod()) {
            expr = Cons.append(expr, castTo((Cons) arguments.first(), Object.class)); // should I cast it to the Method class?
            arguments = Cons.rest(arguments);
        }

        Class<?>[] parameterTypes = fn.getParameterTypes();
        int parameterSize = parameterTypes.length;
        int argumentSize = arguments.size();

        if (!fn.isVarArgs()) {
            if (parameterSize != argumentSize) {
                throw new RuntimeException(
                        String.format("Too %s arguments. Expected: %d, actual: %s",
                                argumentSize < parameterSize ? "few" : "many",
                                parameterSize,
                                argumentSize));
            }
            for (int i = 0; i < argumentSize; i++) {
                expr = Cons.append(expr, castTo(AST.nthAST(arguments, i), parameterTypes[i]));
            }
            return expr;
        }
        else {
            int normalParamameterNumber = parameterSize - 1;
            int varParameterNumber = argumentSize - normalParamameterNumber;
            if (varParameterNumber < 0) {
                throw new RuntimeException("Not Implemented!");
            }
            for (int i = 0; i < normalParamameterNumber; i++) {
                expr = Cons.append(expr, castTo(AST.nthAST(arguments, i), parameterTypes[i]));
            }
            Class elementType = parameterTypes[normalParamameterNumber];
            if (!elementType.isArray()) {
                throw new RuntimeException("Not Implemented!");
            }
            elementType = elementType.getComponentType();
            for (int i = 0; i < varParameterNumber; i++) {
                expr = Cons.append(expr, castTo(AST.nthAST(arguments, normalParamameterNumber + i), elementType));
            }
            return expr;
        }
    }
    private Cons walkFunction(JavaFunction fn, Cons expr) {
        Cons arguments = Cons.rest(Cons.rest(expr));
        Cons newExpr = AST.createAST(AST.getElement(expr));
        for (Object arg: arguments) {
            newExpr = Cons.append(newExpr, walkExpr((Cons) arg));
        }
        return walkFunctionArguments(fn, expr, Cons.rest(newExpr));
    }


    private Cons walkFunction(OverloadedFunction dispatcher, Cons expr) {
        Cons arguments = Cons.rest(Cons.rest(expr));
        int n = arguments.size();

        Cons newExpr = AST.createAST(AST.getElement(expr));
        Class[] argumentTypes = new Class[n];
        for (int i = 0; i < n; i++) {
            Cons arg = walkExpr(AST.nthAST(arguments, i));
            newExpr = Cons.append(newExpr, arg);
            argumentTypes[i] = AST.getEvalType(arg);
        }
        JavaFunction fn = dispatcher.resolveFunction(argumentTypes);
        return walkFunctionArguments(fn, newExpr, Cons.rest(newExpr));
    }

    //////////////////////////////////////////////////
    /// caster
    //////////////////////////////////////////////////

    private Cons unifyArithmetic(Cons expr) {
        Cons leftTree = AST.nthAST(expr, 1);
        Cons rightTree = AST.nthAST(expr, 2);
        Class leftType = AST.getEvalType(leftTree);
        Class rightType = AST.getEvalType(rightTree);
        if (leftType == rightType) {
            expr = assocType(expr, leftType);
            return expr;
        }
        else if (leftType.isPrimitive() && rightType.isPrimitive()) {
            // convert primitive to primitive
            if (leftType == int.class && rightType == double.class) {
                expr = assocType(expr, double.class);
                expr = assoc1(expr, castTo(leftTree, double.class));
                return expr;
            }
            else if (leftType == double.class && rightType == int.class) {
                expr = assocType(expr, double.class);
                expr = assoc2(expr, castTo(rightTree, double.class));
                return expr;
            }
            else {
                throw new RuntimeException("Not Implemented! "+leftType+" + "+rightType);
            }
        }
        else if (leftType == Object.class && rightType.isPrimitive()) {
            expr = assocType(expr, Object.class);
            expr = assoc2(expr, castTo(rightTree, Object.class));
            return expr;
        }
        else if (leftType.isPrimitive() && rightType == Object.class) {
            expr = assocType(expr, Object.class);
            expr = assoc1(expr, castTo(leftTree, Object.class));
            return expr;
        }
        else if (leftType == XML_INTERFACE_TYPE || rightType == XML_INTERFACE_TYPE) {
            expr = assocType(expr, String.class);
            expr = assoc1(expr, castTo(leftTree, String.class));
            expr = assoc2(expr, castTo(rightTree, String.class));
            return expr;
        }
        else if (!leftType.isPrimitive() && !rightType.isPrimitive()) {
            // object -> object
            expr = assocType(expr, Object.class);
            expr = assoc1(expr, castTo(leftTree, Object.class));
            expr = assoc2(expr, castTo(rightTree, Object.class));
            return expr;
        }
        else {
            throw new RuntimeException("Not Implemented! "+leftType+" to "+rightType);
        }
    }

    private Cons castTo(Cons expr, Class target) {
        Class source = AST.getEvalType(expr);
        if (source == target) {
            return expr;
        }
        else if (!source.isPrimitive() && !target.isPrimitive()) {
            // object to object
            if (target == Object.class) {
                return expr;
            }
            else {
                return AST.createAST(new CastElement(expr, source, target));
//                throw new RuntimeException("Not Implemented! " + source + " to "+target);
            }
        }
        else {
            return AST.createAST(new CastElement(expr, source, target));
        }
    }

    //////////////////////////////////////////////////
    /// scope
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
    /// utils
    //////////////////////////////////////////////////
    private Cons assoc0(Cons tree, Object x) {
        return tree.assoc(0, x);
    }
    private Cons assoc1(Cons tree, Object x) {
        return tree.assoc(1, x);
    }
    private Cons assoc2(Cons tree, Object x) {
        return tree.assoc(2, x);
    }
    private Cons assoc3(Cons tree, Object x) {
        return tree.assoc(3, x);
    }
    private Cons assocType(Cons tree, Class type) {
        Element e = AST.getElement(tree);
        return assoc0(tree, new TypedElement(e, type));
    }
    private Cons tuple(Cons x, Cons y, Cons z) {
        return Cons.list(x, y, z);
    }

}
