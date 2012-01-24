package org.libj.xquery.compiler;

public class Symbol {
    private String name;
    private int index;

    public Symbol(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

}
