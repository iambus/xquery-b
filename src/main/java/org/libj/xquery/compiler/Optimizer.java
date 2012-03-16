package org.libj.xquery.compiler;

import org.libj.xquery.lexer.Token;
import org.libj.xquery.lexer.TokenType;
import org.libj.xquery.lisp.Cons;
import org.libj.xquery.parser.Element;
import org.libj.xquery.parser.TypedElement;
import org.libj.xquery.parser.VariableElement;
import org.libj.xquery.parser.AST;

import java.util.ArrayList;

import static org.libj.xquery.lisp.Cons.*;

public class Optimizer {
    //////////////////////////////////////////////////
    // optimize where clauses
    //////////////////////////////////////////////////
    public static Cons optimizeWhere(Cons flower) {
//        if (true) return flower; // turn off optimization
        Cons forlets = (Cons) flower.nth(1);
        Cons where = (Cons) flower.nth(3);
        if (isNil(where)) {
            return flower;
        }
        Cons conditions = breakAnd(where);
        where = null;
        for (Object x: conditions) {
            Cons condition = (Cons) x;
            int level = referenceLevel(forlets, condition, -1);
            level++;
            if (level == forlets.size()) {
                where = joinAnd(where, condition);
            }
            else {
                Cons forlet = (Cons) forlets.nth(level);
                if (forlet.size() == 3) {
                    forlet = append(forlet, condition);
                }
                else {
                    forlet = forlet.assoc(3, joinAnd((Cons) forlet.nth(3), condition));
                }
                forlets = forlets.assoc(level, forlet);
            }
        }
        return list(flower.first(), forlets, flower.third(), where);
    }


    private static Cons breakAnd(Cons condition) {
        if (isAnd(condition)) {
            return concat(breakAnd((Cons) condition.second()), breakAnd((Cons) condition.third()));
        }
        else {
            return list(condition);
        }
    }

    private static Cons joinAnd(Cons left, Cons right) {
        if (isNil(left)) {
            return right;
        }
        else if (isNil(right)) {
            return left;
        }
        else {
            return list(new TypedElement(TokenType.AND, boolean.class), left, right);
        }
    }
    private static boolean isAnd(Cons condition) {
        return AST.getNodeType(condition) == TokenType.AND;
    }

    private static int referenceLevel(Cons forlets, Cons condition, int currentLevel) {
        if (isNil(condition)) {
            return currentLevel;
        }
        Object x = condition.first();
        if (x instanceof VariableElement) {
            int variableLevel = referenceLevel(forlets, (VariableElement) x);
            return referenceLevel(forlets, condition.next(), variableLevel > currentLevel ? variableLevel : currentLevel);
        }
        else if (x instanceof Cons) {
            currentLevel = referenceLevel(forlets, (Cons) x, currentLevel);
            return referenceLevel(forlets, condition.next(), currentLevel);
        }
        else if (x instanceof Element) {
            return referenceLevel(forlets, condition.next(), currentLevel);
        }
        else if (x instanceof String || x instanceof Integer || x instanceof Double || x instanceof TokenType) {
            return currentLevel;
        }
        throw new RuntimeException("Not Implemented! "+x.getClass());
    }

    private static int referenceLevel(Cons forlets, VariableElement variable) {
        for (int i = forlets.size() - 1; i >= 0; i--) {
            Cons forlet = (Cons) forlets.nth(i);
            VariableElement v = (VariableElement) forlet.second();
            if (v.getRef() == variable.getRef()) {
                return i;
            }
        }
        return -1;
    }
    //////////////////////////////////////////////////
    // xpath cache
    //////////////////////////////////////////////////




    public static Cons cacheXPath(Cons expr) {
//        ArrayList<Cons> xpaths = new ArrayList<Cons>();
//        collectXPath(expr, xpaths);
//        System.out.println(xpaths);
        return expr;
    }

    private static void collectXPath(Cons expr, ArrayList<Cons> list) {
        if (isNil(expr)) {
            return;
        }
        Object x = expr.first();
        if (x instanceof Element && AST.getNodeType(expr) == TokenType.XPATH) {
            list.add(expr);
            return;
        }
        if (x instanceof Cons) {
            collectXPath((Cons) x, list);
        }
        collectXPath(expr.next(), list);
    }


}
