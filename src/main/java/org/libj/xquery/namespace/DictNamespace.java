package org.libj.xquery.namespace;

import java.util.HashMap;
import java.util.Map;

public class DictNamespace implements Namespace {
    private Map<String, Symbol> table = new HashMap<String, Symbol>();
    public void register(String name, Symbol s) {
        if (table.containsKey(name)) {
            throw new RuntimeException("Not Implemented!");
        }
        table.put(name, s);
    }
    public Symbol lookup(String name) {
        return table.get(name);
    }
    public void importNamespace(DictNamespace other) {
        table.putAll(other.table);
    }
}
