package org.libj.xquery.compiler;

import org.libj.xquery.lexer.Token;
import org.libj.xquery.namespace.*;
import org.libj.xquery.parser.*;
import org.libj.xquery.xml.XML;

import static org.libj.xquery.lexer.TokenType.*;
import static org.libj.xquery.compiler.Constants.*;

import java.util.ArrayList;
import java.util.Map;

public class Walker {
    private AST ast;
    private ArrayList<Symbol> symbols = new ArrayList<Symbol>();
    private Scope scope = new Scope();
    private Scope freeScope = new Scope();
    private Namespace namespace;

    private int locals = LOCAL_VAR_START; // index 2 is used as temporary double variable

    public Walker(AST tree, Namespace namespace) {
        ast = tree;
        this.namespace = namespace;
    }

    public int getLocals() {
        return locals;
    }

    public Map<String, Symbol> getFreeVariables() {
        return freeScope.getSymbols();
    }

    public AST walk() {
        return walkExpr(ast);
    }

    private AST walkExpr(AST expr) {
        switch (expr.getNodeType()) {
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
                return walkNumber(expr.getToken());
            case STRING:
                return walkString(expr.getToken());
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

    private AST walkNumber(Token token) {
        String text = token.text;
        if (text.indexOf('.') == -1) {
            int n = Integer.parseInt(text);
            return new AST(new ConstantElement(token, n, int.class));
        }
        else {
            double d = Double.parseDouble(text);
            return new AST(new ConstantElement(token, d, double.class));
        }
    }

    private AST walkString(Token token) {
        return new AST(new ConstantElement(token, token.text, String.class));
    }

    private AST walkVariable(AST expr) {
        String variable = expr.getNodeText();
        if (!isFree(variable)) {
            return new AST(new VariableElement(expr.getElement(), resolveType(variable), resolve(variable)));
        }
        else {
            int index = resolveFree(variable);
            return new AST(new VariableElement(expr.getElement(), Object.class, index));
        }
    }

    private AST walkOp(AST expr) {
        int type = expr.getNodeType();
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

    private AST walkBinaryArithmetic(AST expr) {
        return unifyArithmetic(walkSub2(expr));
    }

    private AST walkNegative(AST expr) {
        AST v = walkExpr(expr.nth(1));
        assoc1(expr, v);
        assocType(expr, v.getEvalType());
        return expr;
    }

    private AST walkSub2(AST expr) {
        AST left = walkExpr(expr.nth(1));
        AST right = walkExpr(expr.nth(2));
        assoc1(expr, left);
        assoc2(expr, right);
        return expr;
    }

    private AST walkLogic(AST expr) {
        AST left = castTo(walkExpr(expr.nth(1)), boolean.class);
        AST right = castTo(walkExpr(expr.nth(2)), boolean.class);
        assoc1(expr, left);
        assoc2(expr, right);
        assocType(expr, boolean.class);
        return expr;
    }

    private AST walkComparison(AST expr) {
        return assocType(walkBinaryArithmetic(expr), boolean.class);
    }

    private AST walkXPath(AST expr) {
        assocType(expr, XML_INTERFACE_TYPE);
        assoc1(expr, castTo(walkVariable(expr.nth(1)), XML_INTERFACE_TYPE));
        return expr;
    }

    private AST walkIndex(AST expr) {
        AST list = expr.nth(1);
        AST at = expr.nth(2);
        if (list.getNodeType() == FLOWER) {
            AST flower = walkFlower(list);
            AST flowerAt = castTo(walkExpr(at), int.class);
            expr = new AST(FLOWERAT);
            expr.appendLast(flower);
            expr.appendLast(flowerAt);
            return expr;
        }
        else {
            AST left = castTo(walkExpr(list), Object.class);
            AST right = castTo(walkExpr(at), int.class);
            assoc1(expr, left);
            assoc2(expr, right);
            assocType(expr, Object.class);
            return expr;
        }
    }

    private AST walkTo(AST expr) {
        AST left = castTo(walkExpr(expr.nth(1)), int.class);
        AST right = castTo(walkExpr(expr.nth(2)), int.class);
        assoc1(expr, left);
        assoc2(expr, right);
        assocType(expr, LIST_CLASS_TYPE);
        return expr;
    }

    private AST walkList(AST expr) {
        AST ast = new AST(expr.getElement());
        assocType(ast, LIST_CLASS_TYPE);
        for (Unit e: expr.rest()) {
            ast.appendLast(castTo(walkExpr((AST) e), Object.class));
        }
        return ast;
    }

    private AST walkIf(AST expr) {
        AST condition = walkExpr(expr.nth(1));
        AST thenValue = walkExpr(expr.nth(2));
        AST elseValue = walkExpr(expr.nth(3));
        assoc1(expr, castTo(condition, boolean.class));
        if (elseValue.getEvalType() == thenValue.getEvalType()) {
            assoc2(expr, thenValue);
            assoc3(expr, elseValue);
            assocType(expr, elseValue.getEvalType());
            return expr;
        }
        else {
            assoc2(expr, castTo(thenValue, Object.class));
            assoc3(expr, castTo(elseValue, Object.class));
            assocType(expr, Object.class);
            return expr;
        }
    }

    private AST walkFlower(AST expr) {
        AST forlets = expr.nth(1).rest();
        AST body = (AST) expr.next().next().first();
        AST where =  (AST) expr.next().next().next().first();
        expr = new AST(new TypedElement(expr.getElement(), LIST_CLASS_TYPE));
        expr.cdr(walkForlet(forlets, body, where));
        return expr;
    }

    private AST walkForlet(AST forlets, AST body, AST where) {
        if (forlets == null || forlets.isNil()) {
            return walkFlowerWhereBody(body, where);
        }
        else {
            switch (((AST)forlets.first()).getNodeType()) {
                case LET:
                    return walkLet(forlets, body, where);
                case FOR:
                    return walkFor(forlets, body, where);
                default:
                    throw new RuntimeException("Wrong code!");
            }
        }
    }

    private AST walkFlowerWhereBody(AST body, AST where) {
        if (where != null && !where.isNil()) {
            where = walkExpr(where);
        }
        body = walkExpr(body);
        return tuple(null, body, where);
    }

    private AST walkLet(AST forlets, AST body, AST where) {
        pushScope();

        AST expr = (AST) forlets.first();
        AST variableExpr = expr.nth(1);
        String variableName = variableExpr.getNodeText();
        AST valueExpr = expr.nth(2);

        valueExpr = walkExpr(valueExpr);
        Class valueType = valueExpr.getEvalType();
        int index = define(variableName, valueType);
        variableExpr = new AST(new VariableElement(variableExpr.getElement(), valueType, index));

        assocType(expr, valueType);
        assoc1(expr, variableExpr);
        assoc2(expr, valueExpr);

        AST result = walkForlet(forlets.rest(), body, where);
        AST thisLet = new AST(expr);
        thisLet.cdr((Cons<Unit>) result.first());
        result.car(thisLet);

        popScope();
        return result;
    }

    private AST walkFor(AST forlets, AST body, AST where) {
        if (((AST)forlets.first()).nth(2).getNodeType() == TO) {
            return walkForRange(forlets, body, where);
        }
        else {
            return walkForGeneral(forlets, body, where);
        }
    }

    private AST walkForRange(AST forlets, AST body, AST where) {
        pushScope();

        AST expr = (AST) forlets.first();
        AST variableExpr = expr.nth(1);
        String variableName = variableExpr.getNodeText();
        AST rangeExpr = expr.nth(2);

        AST start = castTo(walkExpr(rangeExpr.nth(1)), int.class);
        AST end = castTo(walkExpr(rangeExpr.nth(2)), int.class);
        int element = define(variableName, int.class);

        variableExpr = new AST(new VariableElement(variableExpr.getElement(), int.class, element));

        assocType(expr, LIST_CLASS_TYPE);
        assoc1(expr, variableExpr);
        assoc2(expr, start);
        expr.appendLast(end);
        expr.getToken().type = FORRANGE;

        AST result = walkForlet(forlets.rest(), body, where);
        AST thisFor = new AST(expr);
        thisFor.cdr((Cons<Unit>) result.first());
        result.car(thisFor);

        popScope();
        return result;
    }

    private AST walkForGeneral(AST forlets, AST body, AST where) {
        pushScope();

        AST expr = (AST) forlets.first();
        AST variableExpr = expr.nth(1);
        String variableName = variableExpr.getNodeText();
        AST collectionExpr = expr.nth(2);

        collectionExpr = castTo(walkExpr(collectionExpr), Object.class);
        int element = define(variableName, Object.class);
        variableExpr = new AST(new VariableElement(variableExpr.getElement(), Object.class, element));

        assocType(expr, LIST_CLASS_TYPE);
        assoc1(expr, variableExpr);
        assoc2(expr, collectionExpr);

        AST result = walkForlet(forlets.rest(), body, where);
        AST thisFor = new AST(expr);
        thisFor.cdr((Cons<Unit>) result.first());
        result.car(thisFor);

        popScope();
        return result;
    }


    private AST walkNode(AST expr) {
        ArrayList<AST> list = new ArrayList<AST>();
        flattenNode(expr, list);
        list = mergeStringNode(list);
        expr = new AST(expr.getElement());
        for (AST ast: list) {
            if (isNodeLiteral(ast)) {
                expr.appendLast(ast);
            }
            else {
                expr.appendLast(walkExpr(ast));
            }
        }
        assocType(expr, XML.class);
        return expr;
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

    private boolean isNodeLiteral(AST node) {
        switch (node.getNodeType()) {
            case TEXT: case TAGOPEN: case TAGCLOSE: case TAGUNIT:
                return true;
            default:
                return false;
        }
    }

    private ArrayList<AST> mergeStringNode(ArrayList<AST> source) {
        ArrayList<AST> target = new ArrayList<AST>();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < source.size(); i++) {
            AST node = source.get(i);
            if (isNodeLiteral(node)) {
                if (buffer.length() == 0 && i + 1 < source.size() && !isNodeLiteral(source.get(i+1))) {
                    node.getToken().type = TEXT;
                    target.add(node);
                }
                else {
                    buffer.append(node.getNodeText());
                }
            }
            else {
                if (buffer.length() != 0) {
                    target.add(new AST(new Token(TEXT, buffer.toString())));
                    buffer.setLength(0);
                }
                target.add(node);
            }
        }
        if (buffer.length() != 0) {
            target.add(new AST(new Token(TEXT, buffer.toString())));
        }
        return target;
    }

    private AST walkCall(AST expr) {
        String functionName = expr.nth(1).getNodeText();
        Function fn = (Function) namespace.lookup(functionName);
//        return invokeFunction(functionName, ((AST) expr.next()).rest());
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

    private AST walkFunctionArguments(JavaFunction fn, AST expr, AST arguments) {
        expr.cdr(null);
        assoc0(expr, new FunctionElement(expr.getElement(), fn.getReturnType(), fn));

        if (fn.isMethod()) {
            expr.appendLast(castTo((AST) arguments.first(), Object.class)); // should I cast it to the Method class?
            arguments = arguments.rest();
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
                expr.appendLast(castTo(arguments.nth(i), parameterTypes[i]));
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
                expr.appendLast(castTo(arguments.nth(i), parameterTypes[i]));
            }
            Class elementType = parameterTypes[normalParamameterNumber];
            if (!elementType.isArray()) {
                throw new RuntimeException("Not Implemented!");
            }
            elementType = elementType.getComponentType();
            for (int i = 0; i < varParameterNumber; i++) {
                expr.appendLast(castTo(arguments.nth(normalParamameterNumber+i), elementType));
            }
            return expr;
        }
    }
    private AST walkFunction(JavaFunction fn, AST expr) {
        AST arguments = expr.rest().rest();
        AST newExpr = new AST(expr.getElement());
        for (Unit arg: arguments) {
            newExpr.appendLast(walkExpr((AST)arg));
        }
        return walkFunctionArguments(fn, expr, newExpr.rest());
    }


    private AST walkFunction(OverloadedFunction dispatcher, AST expr) {
        AST arguments = expr.rest().rest();
        int n = arguments.size();

        AST newExpr = new AST(expr.getElement());
        Class[] argumentTypes = new Class[n];
        for (int i = 0; i < n; i++) {
            AST arg = walkExpr(arguments.nth(i));
            newExpr.appendLast(arg);
            argumentTypes[i] = arg.getEvalType();
        }
        JavaFunction fn = dispatcher.resolveFunction(argumentTypes);
        return walkFunctionArguments(fn, newExpr, newExpr.rest());
    }

    //////////////////////////////////////////////////
    /// caster
    //////////////////////////////////////////////////

    private AST unifyArithmetic(AST expr) {
        AST leftTree = expr.nth(1);
        AST rightTree = expr.nth(2);
        Class leftType = leftTree.getEvalType();
        Class rightType = rightTree.getEvalType();
        if (leftType == rightType) {
            assocType(expr, leftType);
            return expr;
        }
        else if (leftType.isPrimitive() && rightType.isPrimitive()) {
            // convert primitive to primitive
            if (leftType == int.class && rightType == double.class) {
                assocType(expr, double.class);
                assoc1(expr, castTo(leftTree, double.class));
                return expr;
            }
            else if (leftType == double.class && rightType == int.class) {
                assocType(expr, double.class);
                assoc2(expr, castTo(rightTree, double.class));
                return expr;
            }
            else {
                throw new RuntimeException("Not Implemented! "+leftType+" + "+rightType);
            }
        }
        else if (leftType == Object.class && rightType.isPrimitive()) {
            assocType(expr, Object.class);
            assoc2(expr, castTo(rightTree, Object.class));
            return expr;
        }
        else if (leftType.isPrimitive() && rightType == Object.class) {
            assocType(expr, Object.class);
            assoc1(expr, castTo(leftTree, Object.class));
            return expr;
        }
        else if (leftType == XML_INTERFACE_TYPE || rightType == XML_INTERFACE_TYPE) {
            assocType(expr, String.class);
            assoc1(expr, castTo(leftTree, String.class));
            assoc2(expr, castTo(rightTree, String.class));
            return expr;
        }
        else if (!leftType.isPrimitive() && !rightType.isPrimitive()) {
            // object -> object
            assocType(expr, Object.class);
            assoc1(expr, castTo(leftTree, Object.class));
            assoc2(expr, castTo(rightTree, Object.class));
            return expr;
        }
        else {
            throw new RuntimeException("Not Implemented! "+leftType+" to "+rightType);
        }
    }

    private AST castTo(AST expr, Class target) {
        Class source = expr.getEvalType();
        if (source == target) {
            return expr;
        }
        else if (!source.isPrimitive() && !target.isPrimitive()) {
            // object to object
            if (target == Object.class) {
                return expr;
            }
            else {
                return new AST(new CastElement(expr, source, target));
//                throw new RuntimeException("Not Implemented! " + source + " to "+target);
            }
        }
        else {
            return new AST(new CastElement(expr, source, target));
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
    private AST assoc0(AST tree, Unit x) {
        tree.car(x);
        return tree;
    }
    private AST assoc1(AST tree, Unit x) {
        tree.next().car(x);
        return tree;
    }
    private AST assoc2(AST tree, Unit x) {
        tree.next().next().car(x);
        return tree;
    }
    private AST assoc3(AST tree, Unit x) {
        tree.next().next().next().car(x);
        return tree;
    }
    private AST assocType(AST tree, Class type) {
        Element e = tree.getElement();
        return assoc0(tree, new TypedElement(e, type));
    }
    private AST astree(Unit u1, Unit u2) {
        AST root = new AST(u1);
        root.cdr(new AST(u2));
        return root;
    }
    private AST astree(Unit u1, Unit u2, Unit u3) {
        AST root = new AST(u1);
        root.cdr(new AST(u2)).cdr(new AST(u3));
        return root;
    }
    private AST astree(Unit u1, Unit u2, Unit u3, Unit u4) {
        AST root = new AST(u1);
        root.cdr(new AST(u2)).cdr(new AST(u3)).cdr(new AST(u4));
        return root;
    }
    private AST pair(AST x, AST y) {
        x = new AST(x);
        x.cdr(new AST(y));
        return x;
    }
    private AST tuple(AST x, AST y, AST z) {
        x = new AST(x);
        x.cdr(new AST(y)).cdr(new AST(z));
        return x;
    }

}
