package org.libj.xquery.lexer;


public class Token {
    public int type;
    public String text;
    public Token(int type, String text) {
        this.type = type;
        this.text = text;
    }
    public String toString() {
        return String.format("<%s:%s>", TokenType.toTypeName(type), text);
    }
    
    public static Token t(int type, String text) {
        return new Token(type, text);
    }

    public static Token t(int type) {
        return t(type, null);
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof Token)) {
            return false;
        }
        Token t = (Token) other;
        return type == t.type && text.equals(t.text);
    }
}
