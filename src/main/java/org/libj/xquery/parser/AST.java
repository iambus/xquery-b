package org.libj.xquery.parser;

import org.libj.xquery.lexer.Token;
import org.libj.xquery.lexer.TokenType;
import org.libj.xquery.lisp.Cons;

import static org.libj.xquery.lexer.TokenType.*;

public class AST {

    public static Cons createAST(Token t) {
        return createAST(new Element(t));
    }

    public static Cons createAST(TokenType tokenType) {
        return createAST(new Token(tokenType, null));
    }

    public static Cons createAST(Unit x) {
        return new Cons(x);
    }

    public static Token getToken(Cons units) {
        return ((Element) units.first()).getToken();
    }

    public static TokenType getNodeType(Cons units) {
        return getToken(units).type;
    }

    public static String getNodeText(Cons units) {
        return getToken(units).text;
    }

    public static Element getElement(Cons units) {
        return (Element) units.first();
    }

    public static Class getEvalType(Cons units) {
        return ((TypedUnit) units.first()).getType();
    }

    public static Cons nthAST(Cons cons, int i) {
        return (Cons) cons.nth(i);
    }
}
