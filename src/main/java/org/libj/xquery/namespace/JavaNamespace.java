package org.libj.xquery.namespace;

public class JavaNamespace implements Namespace {

    public Symbol lookup(String className) {
        return new ClassNamespace(className);
    }

    public void register(String name, Symbol s) {
        throw new UnsupportedOperationException("register");
    }
}
