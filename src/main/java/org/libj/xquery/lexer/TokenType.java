package org.libj.xquery.lexer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public enum TokenType {
    EOF,

    WORD,
    NUMBER,
    STRING,
    VARIABLE,
    XPATH,
    TAGOPEN,
    TAGCLOSE,
    TAGUNIT,
    TEXT, // text node

    LPAREN,
    RPAREN,
    LBRACKET,
    RBRACKET,
    LBRACK,
    RBRACK,

    SEMI,
    COMMA,
    EQ,
    NE,
    ASSIGN,
    LT,
    GT,
    LE,
    GE,

    PLUS,
    MINUS,
    MULTIPLY,
    DIV,

    DECLARE,
    LET,
    FOR,
    IN,
    AS,
    IF,
    THEN,
    ELSE,
    WHERE,
    GROUP,
    ORDER,
    BY,
    RETURN,

    NAMESPACE,

    AND,
    OR,
    TO,
    NEGATIVE,
    MOD,

    PROG,
    DECLARES,
    FLOWER,
    FORLETS,
    NODE,
    CALL,
    LIST,
    INDEX,
    FORRANGE,
    FLOWERAT,

    CAST;

    public static String toTypeName(TokenType v) {
        return v.toString();
    }

    public static String toString(Token t) {
        if (t == null) {
            return "<null token!>";
        }
        switch (t.type) {
            case TEXT:
                return '"'+t.text+'"';
            case VARIABLE:
            case NUMBER:
            case FOR:
            case LET:
            case TO:
            case PLUS:
            case MINUS:
            case EQ:
            case AND:
            case OR:
                return t.text;
            case PROG:
            case DECLARES:
            case FLOWER:
            case NODE:
            case TAGOPEN:
            case TAGCLOSE:
            case FORLETS:
                return toTypeName(t.type);
            case XPATH:
                return "xpath";
            default:
                return t.toString();
        }
    }

    public static void main(String[] args) {
        System.out.println(TEXT);
        System.out.println(toTypeName(TEXT));
    }
};
