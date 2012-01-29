package org.libj.xquery.compiler;

import org.libj.xquery.lexer.Token;
import org.libj.xquery.parser.*;
import org.libj.xquery.xml.XML;

import static org.libj.xquery.lexer.TokenType.*;
import static org.libj.xquery.compiler.Constants.*;

import java.util.ArrayList;

public class Walker {
    private AST ast;
    private ArrayList<Symbol> symbols = new ArrayList<Symbol>();
    private Scope scope = new Scope();
    private Scope freeScope = new Scope();

    private int locals = LOCAL_VAR_START; // index 2 is used as temporary double variable

    public Walker(AST tree) {
        ast = tree;
        AST result = walkExpr(tree);
        System.out.println(result);
    }

    public AST walkExpr(AST expr) {
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
            Class t = resolveType(variable);
            return assocType(expr, t);
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
                throw new RuntimeException("Not Implemented!");
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
        assoc1(expr, walkVariable(expr.nth(1)));
        return expr;
    }

    private AST walkIndex(AST expr) {
        AST left = walkExpr(expr.nth(1));
        AST right = castTo(walkExpr(expr.nth(2)), int.class);
        assoc1(expr, left);
        assoc2(expr, right);
        assocType(expr, Object.class);
        return expr;
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
        throw new RuntimeException("Not Implemented!");
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
        int element = define(variableName, Object.class);
        int iterator = defineAnonymous();
        if (iterator != element + 1) {
            throw new RuntimeException("There must be an anonymous variable after iterator!");
        }
        variableExpr = new AST(new VariableElement(variableExpr.getElement(), int.class, element));

        assocType(expr, LIST_CLASS_TYPE);
        assoc1(expr, variableExpr);
        assoc2(expr, start);
        expr.appendLast(end);

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
        int iterator = defineAnonymous();
        if (iterator != element + 1) {
            throw new RuntimeException("There must be an anonymous variable after iterator!");
        }
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
            throw new RuntimeException("Not Implemented!");
        }
        else {
            throw new RuntimeException("Not Implemented!");
        }
    }

    private AST castTo(AST expr, Class target) {
        Class source = expr.getEvalType();
        if (source == target) {
            return expr;
        }
        else if (!source.isPrimitive() && !target.isPrimitive()) {
            if (target == Object.class) {
                return expr;
            }
            else {
                throw new RuntimeException("Not Implemented! " + source + " to "+target);
            }
        }
        else {
            return new AST(new CastUnit(expr, source, target));
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
    private AST assocType(AST tree, Class type) {
        Element e = (Element) tree.first();
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
