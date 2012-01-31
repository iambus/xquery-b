package org.libj.xquery.compiler;

import org.libj.xquery.lisp.Cons;
import org.libj.xquery.parser.Element;
import org.libj.xquery.parser.VariableElement;

import static org.libj.xquery.lisp.Cons.*;

public class Optimizer {
    public static Cons optimizeWhere(Cons flower) {
//        if (true) return flower; // turn off optimization
        Cons forlets = (Cons) flower.nth(1);
        Cons where = (Cons) flower.nth(3);
        if (isNil(where)) {
            return flower;
        }
        int level = referenceLevel(forlets, where, -1);
        level++;
        if (level == forlets.size()) {
            return flower;
        }
        forlets = forlets.assoc(level, Cons.append((Cons)forlets.nth(level), where));
        return list(flower.first(), forlets, flower.third(), null);
    }
    public static int referenceLevel(Cons forlets, Cons condition, int currentLevel) {
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
        throw new RuntimeException("Not Implemented!");
    }
    public static int referenceLevel(Cons forlets, VariableElement variable) {
        for (int i = forlets.size() - 1; i >= 0; i--) {
            Cons forlet = (Cons) forlets.nth(i);
            VariableElement v = (VariableElement) forlet.second();
            if (v.getRef() == variable.getRef()) {
                return i;
            }
        }
        return -1;
    }
}
