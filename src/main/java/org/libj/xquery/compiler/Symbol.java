package org.libj.xquery.compiler;

public class Symbol {
    private String name;
    private int index;
    private Class type;

    public Symbol(String name, int index, Class type) {
        this.name = name;
        this.index = index;
        this.type = type;
    }

    public Symbol(String name, int index) {
        this(name, index, Object.class);
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public Class getType() {
        return type;
    }
}
