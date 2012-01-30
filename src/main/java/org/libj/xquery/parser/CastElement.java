package org.libj.xquery.parser;

import org.libj.xquery.lexer.Token;
import org.libj.xquery.lexer.TokenType;
import org.libj.xquery.lisp.Cons;

public class CastElement extends TypedElement {
    public Cons expr;
    public Class source;
    public Class target;

    public CastElement(Cons expr, Class source, Class target) {
        super(new Token(TokenType.CAST, "cast"), target);
        this.source = source;
        this.expr = expr;
        this.target = target;
    }
}
