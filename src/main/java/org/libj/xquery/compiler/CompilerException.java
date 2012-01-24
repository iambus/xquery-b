package org.libj.xquery.compiler;

public class CompilerException extends RuntimeException {
    public CompilerException(String message) {
        super(message);
    }
    public CompilerException(Exception e) {
        super(e);
    }
}
