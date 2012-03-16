package org.libj.xquery.parser;

import org.libj.xquery.lexer.Token;
import org.libj.xquery.lexer.TokenType;
import org.libj.xquery.lisp.Cons;

import static org.libj.xquery.lexer.TokenType.*;

public class AST {

    public static TokenType getNodeType(Cons units) {
        return getTokenType(units);
    }

    public static TokenType getTokenType(Cons units) {
        Object x = units.first();
        if (x instanceof Element) {
            return ((Element) x).getTokenType();
        }
        return (TokenType) x;
    }

    public static Class getEvalType(Cons units) {
        return ((TypedUnit) units.first()).getType();
    }

    public static Cons nthAST(Cons cons, int i) {
        return (Cons) cons.nth(i);
    }

    public static Cons asAst(Token t) {
        return Cons.list(t.type, t.text);
    }
}
