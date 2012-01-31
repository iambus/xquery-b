package org.libj.xquery.parser;

import org.libj.xquery.lexer.Token;
import org.libj.xquery.lexer.TokenType;
import org.libj.xquery.lisp.Cons;

public class CastElement extends TypedElement {
    public Class source;
    public Class target;

    public CastElement(Class source, Class target) {
        super(new Token(TokenType.CAST, "cast"), target);
        this.source = source;
        this.target = target;
    }
}
