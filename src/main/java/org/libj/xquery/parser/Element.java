package org.libj.xquery.parser;

import org.libj.xquery.lexer.Token;
import org.libj.xquery.lexer.TokenType;

public class Element implements Unit {
    private Token token;

    public Element(Token t) {
        this.token = t;
    }

    public Token getToken() {
        return token;
    }

    public String toString() {
        return TokenType.toString(token);
//        return "[["+token.toString()+"]]";
    }
}
