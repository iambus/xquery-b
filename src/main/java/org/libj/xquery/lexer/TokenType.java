package org.libj.xquery.lexer;

public class TokenType {
    public static final int EOF = -1;

    public static final int ID = 1;
    public static final int NUMBER = 2;
    public static final int STRING = 3;
    public static final int XPATH = 4;
    public static final int TAG = 5;
    public static final int TEXT = 6; // text node

    public static final int LPAREN = 11;
    public static final int RPAREN = 12;
    public static final int LBRACKET = 13;
    public static final int RBRACKET = 14;
    public static final int LBRACK = 15;
    public static final int RBRACK = 16;

    public static final int SEMI = 21;
    public static final int COMMA = 22;
    public static final int EQ = 23;
    public static final int ASSIGN = 24;

    public static final int PLUS = 31;
    public static final int MINUS = 32;
    public static final int MULTIPLY = 33;
    public static final int DIV = 34;
}
