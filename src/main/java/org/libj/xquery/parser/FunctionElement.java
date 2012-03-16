package org.libj.xquery.parser;

import org.libj.xquery.lexer.TokenType;
import org.libj.xquery.namespace.Function;

public class FunctionElement extends TypedElement {
    private Function function;
    public FunctionElement(Class type, Function function) {
        super(TokenType.CALL, type);
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }
}
