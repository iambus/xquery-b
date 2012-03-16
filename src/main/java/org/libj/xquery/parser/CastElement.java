package org.libj.xquery.parser;

import org.libj.xquery.lexer.TokenType;

public class CastElement extends TypedElement {
    public Class source;
    public Class target;

    public CastElement(Class source, Class target) {
        super(TokenType.CAST, target);
        this.source = source;
        this.target = target;
    }
}
