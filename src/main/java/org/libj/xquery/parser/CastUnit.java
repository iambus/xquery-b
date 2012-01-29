package org.libj.xquery.parser;

public class CastUnit implements Unit {
    public AST expr;
    public Class source;
    public Class target;

    public CastUnit(AST expr, Class target, Class source) {
        this.expr = expr;
        this.target = target;
        this.source = source;
    }
}
