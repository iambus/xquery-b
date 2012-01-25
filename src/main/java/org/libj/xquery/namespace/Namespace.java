package org.libj.xquery.namespace;

public interface Namespace extends Symbol {
    void register(String name, Symbol s);
    Symbol lookup(String name);
}
