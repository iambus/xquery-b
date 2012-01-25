package org.libj.xquery.lexer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class TokenType {
    public static final int EOF = -1;

    public static final int WORD = 1;
    public static final int NUMBER = 2;
    public static final int STRING = 3;
    public static final int VARIABLE = 4;
    public static final int XPATH = 5;
    public static final int TAGOPEN = 6;
    public static final int TAGCLOSE = 7;
    public static final int TAGUNIT = 8;
    public static final int TEXT = 9; // text node

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

    public static final int DECLARE = 101;
    public static final int LET = 102;
    public static final int FOR = 103;
    public static final int IN = 104;
    public static final int AS = 105;
    public static final int IF = 107;
    public static final int THEN = 108;
    public static final int ELSE = 109;
    public static final int WHERE = 110;
    public static final int RETURN = 111;

    public static final int NAMESPACE = 121;

    public static final int AND = 131;
    public static final int OR = 132;
    public static final int TO = 133;
    public static final int NEGATIVE = 134;

    public static final int PROG = 201;
    public static final int DECLARES = 202;
    public static final int NODE = 203;
    public static final int CALL = 204;
    public static final int LIST = 205;
    public static final int INDEX = 206;

    private static Map<Integer, String> types;

    private static Map<Integer, String> staticTypes () {
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        try {
            Class c = TokenType.class;
            Field[] fields = c.getDeclaredFields();
            for (Field f : fields) {
                if (f.getType() == int.class) {
                    int t = f.getInt(null);
                    String name = f.getName();
                    if (map.containsKey(t)) {
                        throw new RuntimeException(String.format("Duplicate fields: %s (%s, %s)", t, map.get(t), name));
                    }
                    map.put(t, name);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public static String toTypeName(int v) {
        // WARN: this is slow! only for debugging purpose!
        if (types == null) {
            types = staticTypes();
        }
        return types.get(v);
    }
}
