package org.libj.xquery.parser;

import org.libj.xquery.lexer.Token;

public class TypedElement extends Element implements TypedUnit {
    private Class type;
    public TypedElement(Token t, Class type) {
        super(t);
        this.type = type;
    }
    public TypedElement(Element e, Class type) {
        this(e.getToken(), type);
    }

    public Class getType() {
        return type;
    }
}
