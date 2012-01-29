package org.libj.xquery.parser;

import org.libj.xquery.lexer.Token;
import org.libj.xquery.namespace.Function;

public class FunctionElement extends TypedElement {
    private Function function;
    public FunctionElement(Token t, Class type, Function function) {
        super(t, type);
        this.function = function;
    }
    public FunctionElement(Element e, Class type, Function function) {
        super(e, type);
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }
}
