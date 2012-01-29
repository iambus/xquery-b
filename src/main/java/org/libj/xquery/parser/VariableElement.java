package org.libj.xquery.parser;

import org.libj.xquery.lexer.Token;

public class VariableElement extends TypedElement {
    private int ref;

    public VariableElement(Token t, Class type, int ref) {
        super(t, type);
        this.ref = ref;
    }

    public VariableElement(Element e, Class type, int ref) {
        super(e, type);
        this.ref = ref;
    }

    public int getRef() {
        return ref;
    }
}
