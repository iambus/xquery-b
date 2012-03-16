package org.libj.xquery.parser;

import org.libj.xquery.lexer.TokenType;

public class VariableElement extends TypedElement {
    private int ref;

    public VariableElement(Class type, int ref) {
        super(TokenType.VARIABLE, type);
        this.ref = ref;
    }

    public int getRef() {
        return ref;
    }
}
