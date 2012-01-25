package org.libj.xquery.namespace;

public class ClassNamespace implements Namespace {
    private String className;

    public ClassNamespace(String className) {
        this.className = className;
    }

    public Symbol lookup(String name) {
        throw new RuntimeException("Not Implemented!");
    }

    public void register(String name, Symbol s) {
        throw new UnsupportedOperationException("register");
    }
}
