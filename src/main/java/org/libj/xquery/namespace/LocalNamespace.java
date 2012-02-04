package org.libj.xquery.namespace;

public class LocalNamespace extends RootNamespace {

    private RootNamespace root;

    public LocalNamespace(RootNamespace root) {
        this.root = root;
    }

    @Override
    public Symbol lookup(String name) {
        try {
            return super.lookup(name);
        } catch (NameException e) {
            return root.lookup(name);
        }
    }
}
