package org.libj.xquery.parser;

import org.libj.xquery.lexer.TokenType;

public class ConstantElement extends TypedElement {
    private Object value;

    public ConstantElement(TokenType t, Object value, Class type) {
        super(t, type);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
