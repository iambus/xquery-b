package org.libj.xquery.compiler;

import org.libj.xquery.lexer.TokenType;
import org.libj.xquery.lisp.Cons;
import org.libj.xquery.lisp.Fn;
import org.libj.xquery.namespace.*;
import org.libj.xquery.parser.*;
import org.libj.xquery.xml.XML;

import static org.libj.xquery.lexer.TokenType.*;
import static org.libj.xquery.compiler.Constants.*;
import static org.libj.xquery.lisp.Cons.*;

import java.util.ArrayList;
import java.util.Map;

public class Analysis {
    private Cons ast;
    private Map<String, Class> externals;
    private Scope scope = new Scope();
    private Scope freeScope = new Scope();
    private Namespace namespace;

    private int locals = _LOCAL_VAR_START;

    public Analysis(Cons tree, String[] vars, Map<String, Class> externals, Namespace namespace, boolean hasCallback) {
        ast = tree;
        this.namespace = namespace;
        if (!hasCallback) {
            locals--;
        }
        for (String var: vars) {
            Class<Object> t = externals.get(var);
            if (t == null) {
                t = Object.class;
            }
            Symbol sym = new Symbol("$"+var, locals++, t);
            scope.define(sym);
        }
        this.externals = externals;
    }

    public int getLocals() {
        return locals;
    }

    public Map<String, Symbol> getFreeVariables() {
        return freeScope.getSymbols();
    }

    private Map<String, Class> collectExternalVarialbes() {
        throw new RuntimeException("Not Implemented!");
    }

    public Cons walk() {
        return Optimizer.cacheXPath(walkExpr(ast));
    }

    private Cons walkExpr(Cons expr) {
        switch (AST.getNodeType(expr)) {
            case FLOWER:
                return walkFlower(expr);
            case IF:
                return walkIf(expr);
            case ELEMENT:
                return walkElement(expr);
            case LIST:
                return walkList(expr);
            case VARIABLE:
                return walkVariable(expr);
            case NUMBER:
                return walkNumber(expr);
            case STRING:
                return walkString(expr);
            case PLUS: case MINUS: case MULTIPLY: case DIV: case NEGATIVE: case MOD:
            case EQ: case NE:
            case LT: case LE: case GT: case GE:
            case AND: case OR:
            case TO: case INDEX: case XPATH: case ATTR_AT:
                return walkOp(expr);
            case CALL:
                return walkCall(expr);
            default:
                throw new RuntimeException("Not Implemented! "+expr);
        }
    }

    private Cons walkNumber(Cons expr) {
        TokenType t = (TokenType) expr.first();
        String text = (String) expr.second();
        if (text.indexOf('.') == -1) {
            int n = Integer.parseInt(text);
            return list(new ConstantElement(t, n, int.class), n);
        }
        else {
            double d = Double.parseDouble(text);
            return list(new ConstantElement(t, d, double.class), d);
        }
    }

    private Cons walkString(Cons expr) {
        TokenType t = (TokenType) expr.first();
        String text = (String) expr.second();
        return list(new ConstantElement(t, text, String.class), text);
    }

    private Cons walkVariable(Cons expr) {
        String variable = (String) expr.second();
        if (!isFree(variable)) {
            return list(new VariableElement(resolveType(variable), resolve(variable)));
        }
        else {
            Symbol symbol = resolveFree(variable);
            return list(new VariableElement(symbol.getType(), symbol.getIndex()));
        }
    }

    private Cons walkOp(Cons expr) {
        TokenType type = AST.getNodeType(expr);
        switch (type) {
            case PLUS: case MINUS: case MULTIPLY: case DIV: case MOD:
                return walkBinaryArithmetic(expr);
            case NEGATIVE:
                return walkNegative(expr);
            case EQ: case NE:
            case LT: case LE: case GT: case GE:
                return walkComparison(expr);
            case AND: case OR:
                return walkLogic(expr);
            case TO:
                return walkTo(expr);
            case INDEX:
                return walkIndex(expr);
            case XPATH:
                return walkXPath(expr);
            case ATTR_AT:
                return walkAttrAt(expr);
            default:
                throw new RuntimeException("Not Implemented! "+toTypeName(type));
        }
    }

    private Cons walkBinaryArithmetic(Cons expr) {
        return unifyArithmetic(walkSub2(expr));
    }

    private Cons walkNegative(Cons expr) {
        Cons v = walkExpr((Cons) expr.second());
        expr = assoc1(expr, v);
        expr = assocType(expr, AST.getEvalType(v));
        return expr;
    }

    private Cons walkSub2(Cons expr) {
        Cons left = walkExpr((Cons) expr.second());
        Cons right = walkExpr((Cons) expr.third());
        expr = assoc1(expr, left);
        expr = assoc2(expr, right);
        return expr;
    }

    private Cons walkLogic(Cons expr) {
        Cons left = castTo(walkExpr((Cons) expr.second()), boolean.class);
        Cons right = castTo(walkExpr((Cons) expr.third()), boolean.class);
        expr = assoc1(expr, left);
        expr = assoc2(expr, right);
        expr = assocType(expr, boolean.class);
        return expr;
    }

    private Cons walkComparison(Cons expr) {
        return assocType(walkBinaryArithmetic(expr), boolean.class);
    }

    private Cons walkXPath(Cons expr) {
        Cons object = (Cons) expr.second();
        Cons xml = castTo(walkExpr(object), XML_INTERFACE_Class);
        String path = (String) expr.third();
        String ns = null;
        int i = path.indexOf(':');
        if (i != -1) {
            String prefix = path.substring(0, i);
            ns = ((URI)namespace.lookup(prefix)).getUri();
            path = path.substring(i+1);
        }
        return list(new TypedElement(TokenType.XPATH, XML_INTERFACE_Class), xml, path, ns);
    }

    private Cons walkAttrAt(Cons expr) {
        Cons object = (Cons) expr.second();
        Cons xml = castTo(walkExpr(object), XML_INTERFACE_Class);
        String attr = (String) expr.third();
        return list(new TypedElement(TokenType.ATTR_AT, String.class), xml, attr);
    }

    private boolean isPureLets(Cons flower) {
        for (Object x: ((Cons)flower.second()).rest()) {
            Cons forlet = (Cons) x;
            if (AST.getNodeType(forlet) != LET) {
                return false;
            }
        }
        return true;
    }

    private Cons walkIndex(Cons expr) {
        Cons list = (Cons) expr.second();
        Cons at = (Cons) expr.third();
        if (AST.getNodeType(list) == FLOWER && !isPureLets(list)) {
            Cons flower = walkFlower(list);
            Cons flowerAt = castTo(walkExpr(at), int.class);
            expr = list(FLOWERAT);
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
        Cons left = castTo(walkExpr((Cons) expr.nth(1)), int.class);
        Cons right = castTo(walkExpr((Cons) expr.nth(2)), int.class);
        expr = assoc1(expr, left);
        expr = assoc2(expr, right);
        expr = assocType(expr, LIST_INTERFACE_CLASS);
        return expr;
    }

    private Cons walkList(Cons expr) {
        Cons ast = list(new TypedElement(AST.getTokenType(expr), LIST_INTERFACE_CLASS));
        for (Object e: Cons.rest(expr)) {
            ast = Cons.append(ast, castTo(walkExpr((Cons) e), Object.class));
        }
        return ast;
    }

    private Cons walkIf(Cons expr) {
        Cons condition = walkExpr((Cons) expr.nth(1));
        Cons thenValue = walkExpr((Cons) expr.nth(2));
        Cons elseValue = walkExpr((Cons) expr.nth(3));
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
        Cons forlets = ((Cons) expr.nth(1)).rest();
        Cons body = (Cons) expr.nth(2);
        Cons where =  (Cons) expr.nth(3);
        expr = list(new TypedElement(AST.getTokenType(expr), LIST_INTERFACE_CLASS));
        expr = Cons.cons(expr.first(), walkForlet(forlets, body, where));
        expr = Optimizer.optimizeWhere(expr);
        return expr;
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
        Cons variableExpr = (Cons) expr.nth(1);
        String variableName = (String) variableExpr.second();
        Cons valueExpr = (Cons) expr.nth(2);

        valueExpr = walkExpr(valueExpr);
        Class valueType = AST.getEvalType(valueExpr);
        int index = define(variableName, valueType);
        VariableElement variable = new VariableElement(valueType, index);

        expr = assocType(expr, valueType);
        expr = assoc1(expr, variable);
        expr = assoc2(expr, valueExpr);

        Cons result = walkForlet(Cons.rest(forlets), body, where);
        result = cons(cons(expr, (Cons) result.first()), result.next());

        popScope();
        return result;
    }

    private Cons walkFor(Cons forlets, Cons body, Cons where) {
        if (AST.getNodeType((Cons) ((Cons) forlets.first()).nth(2)) == TO) {
            return walkForRange(forlets, body, where);
        }
        else {
            return walkForGeneral(forlets, body, where);
        }
    }

    private Cons walkForRange(Cons forlets, Cons body, Cons where) {
        pushScope();

        Cons expr = (Cons) forlets.first();
        Cons variableExpr = (Cons) expr.nth(1);
        String variableName = (String) variableExpr.second();
        Cons rangeExpr = (Cons) expr.nth(2);

        Cons start = castTo(walkExpr(AST.nthAST(rangeExpr, 1)), int.class);
        Cons end = castTo(walkExpr(AST.nthAST(rangeExpr, 2)), int.class);
        int element = define(variableName, int.class);

        VariableElement variable = new VariableElement(int.class, element);

        expr = list(new TypedElement(FORRANGE, LIST_INTERFACE_CLASS), variable, list(start, end));

        Cons result = walkForlet(Cons.rest(forlets), body, where);
        result = cons(cons(expr, (Cons)result.first()), result.next());

        popScope();
        return result;
    }

    private Cons walkForGeneral(Cons forlets, Cons body, Cons where) {
        pushScope();

        Cons expr = (Cons) forlets.first();
        Cons variableExpr = AST.nthAST(expr, 1);
        String variableName = (String) variableExpr.second();
        Cons collectionExpr = AST.nthAST(expr, 2);

        collectionExpr = castTo(walkExpr(collectionExpr), Object.class);
        int element = define(variableName, Object.class);
        VariableElement variable = new VariableElement(Object.class, element);

        expr = assocType(expr, LIST_INTERFACE_CLASS);
        expr = assoc1(expr, variable);
        expr = assoc2(expr, collectionExpr);

        Cons result = walkForlet(Cons.rest(forlets), body, where);
        result = cons(cons(expr, (Cons) result.first()), result.next());

        popScope();
        return result;
    }

    private Cons walkElement(Cons expr) {
        TypedElement t = new TypedElement(AST.getTokenType(expr), XML.class);
        Cons attrs = map(new Fn() {
            public Object call(Object x) {
                return walkAttr((Cons) x);
            }
        }, (Cons) expr.nth(2));
        Cons content = walkTexts((Cons) expr.nth(3));
        return list(t, expr.nth(1), attrs, content);
    }

    private Cons walkTexts(Cons value) {
        value = map(new Fn() {
            public Object call(Object x) {
                if (x instanceof String) {
                    return x;
                }
                else {
                    return walkExpr((Cons) x);
                }
            }
        }, value);
        return value;
    }

    private Cons walkAttr(Cons attr) {
        Cons value = (Cons) attr.second();
        value = walkTexts(value);
        return attr.assoc(1, value);
    }

    private Cons walkCall(Cons expr) {
        String functionName = (String) expr.second();
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
        expr = new Cons(new FunctionElement(fn.getReturnType(), fn));

        if (fn.isMethod()) {
            if (Cons.size(expr) < 1) {
                throw new RuntimeException("Not Implemented!");
            }
            expr = Cons.append(expr, castTo((Cons) arguments.first(), Object.class)); // should I cast it to the Method class?
            arguments = Cons.rest(arguments);
        }

        Class<?>[] parameterTypes = fn.getParameterTypes();
        int parameterSize = parameterTypes.length;
        int argumentSize = arguments.size();

        if (!fn.isVarArgs()) {
            if (parameterSize != argumentSize) {
                throw new RuntimeException(
                        String.format("Too %s arguments (%s:%s). Expected: %d, actual: %s",
                                argumentSize < parameterSize ? "few" : "many",
                                fn.getClassName().replace('/', '.'),
                                fn.getFunctionName(),
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
        Cons newExpr = null;
        for (Object arg: arguments) {
            newExpr = Cons.append(newExpr, walkExpr((Cons) arg));
        }
        if (newExpr == null) {
            newExpr = Cons.nilList();
        }
        return walkFunctionArguments(fn, expr, newExpr);
    }


    private Cons walkFunction(OverloadedFunction dispatcher, Cons expr) {
        Cons arguments = Cons.rest(Cons.rest(expr));
        int n = arguments.size();

        Cons newExpr = null;
        Class[] argumentTypes = new Class[n];
        for (int i = 0; i < n; i++) {
            Cons arg = walkExpr(AST.nthAST(arguments, i));
            newExpr = Cons.append(newExpr, arg);
            argumentTypes[i] = AST.getEvalType(arg);
        }
        if (newExpr == null) {
            newExpr = Cons.nilList();
        }
        JavaFunction fn = dispatcher.resolveFunction(argumentTypes);
        return walkFunctionArguments(fn, newExpr, newExpr);
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
            if (leftType == Integer.class) {
                expr = assocType(expr, int.class);
                expr = assoc1(expr, castTo(leftTree, int.class));
                expr = assoc2(expr, castTo(rightTree, int.class));
                return expr;
            }
            // TODO: add more: double + double, long + long, etc.
            else {
                expr = assocType(expr, leftType);
                return expr;
            }
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
            else if (leftType == int.class && rightType == long.class) {
                expr = assocType(expr, long.class);
                expr = assoc1(expr, castTo(leftTree, long.class));
                return expr;
            }
            else if (leftType == long.class && rightType == int.class) {
                expr = assocType(expr, long.class);
                expr = assoc2(expr, castTo(rightTree, long.class));
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
        else if (leftType == XML_INTERFACE_Class || rightType == XML_INTERFACE_Class) {
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
                return Cons.list(new CastElement(source, target), expr);
//                throw new RuntimeException("Not Implemented! " + source + " to "+target);
            }
        }
        else {
            return Cons.list(new CastElement(source, target), expr);
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
        return index;
    }

    private Symbol defineFree(String name) {
        int index = locals;
        locals += 2;
        Class t = externals.get(name.substring(1));
        if (t == null) {
            t = Object.class;
        }
        Symbol sym = new Symbol(name, index, t);
        freeScope.define(sym);
        return sym;
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

    private Symbol resolveFree(String name) {
        Symbol s = freeScope.resolve(name);
        if (s != null) {
            return s;
        }
        else {
            return defineFree(name);
        }
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
        Object x = tree.first();
        TokenType t;
        if (x instanceof Element) {
            t = ((Element) x).getTokenType();
        }
        else {
            t = (TokenType) x;
        }
        return assoc0(tree, new TypedElement(t, type));
    }
    private Cons tuple(Cons x, Cons y, Cons z) {
        return Cons.list(x, y, z);
    }

}
