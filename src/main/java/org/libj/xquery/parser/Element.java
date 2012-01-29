package org.libj.xquery.parser;

import org.libj.xquery.lexer.Token;

public class Element implements Unit {
    private Token token;

    public Element(Token t) {
        this.token = t;
    }

    public Token getToken() {
        return token;
    }
}
