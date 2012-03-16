package org.libj.xquery.parser;

import org.libj.xquery.lexer.TokenType;

public class Element implements Unit {
    private TokenType tokenType;

    public Element(TokenType t) {
        this.tokenType = t;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String toString() {
        return tokenType.toString();
    }
}
