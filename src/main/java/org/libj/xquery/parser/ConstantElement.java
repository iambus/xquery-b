package org.libj.xquery.parser;

import org.libj.xquery.lexer.Token;

public class ConstantElement extends TypedElement {
    private Object value;

    public ConstantElement(Token t, Object value, Class type) {
        super(t, type);
        this.value = value;
    }

    public ConstantElement(Element e, Object value, Class type) {
        this(e.getToken(), value, type);
    }

    public ConstantElement(Token t, Object value) {
        this(t, value, value.getClass());
    }

    public ConstantElement(Element e, Object value) {
        this(e, value, value.getClass());
    }

    public Object getValue() {
        return value;
    }
}
