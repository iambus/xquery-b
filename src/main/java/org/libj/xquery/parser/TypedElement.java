package org.libj.xquery.parser;

import org.libj.xquery.lexer.TokenType;

public class TypedElement extends Element implements TypedUnit {
    private Class type;
    public TypedElement(TokenType t, Class type) {
        super(t);
        this.type = type;
    }

    public Class getType() {
        return type;
    }
}
