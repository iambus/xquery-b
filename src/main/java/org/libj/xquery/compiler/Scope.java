package org.libj.xquery.compiler;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    private Map<String, Symbol> symbols = new HashMap<String, Symbol>();
    private Scope parent;

    public Scope(Scope parent) {
        this.parent = parent;
    }

    public Scope() {
        this(null);
    }

    public Scope getEnclosingScope() {
        return parent;
    }

    public void define(Symbol sym) {
        String name = sym.getName();
        if (symbols.containsKey(name)) {
            throw new CompilerException("Variable redefine: "+name);
        }
        symbols.put(name, sym);
    }
    
    public Symbol resolve(String name) {
        Symbol s = symbols.get(name);
        if (s == null && parent != null) {
            s = parent.resolve(name);
        }
        return s;
    }

    public Map<String, Symbol> getSymbols() {
        return symbols;
    }

    public String toString() {
        return symbols.toString();
    }
}
