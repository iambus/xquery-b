package org.libj.xquery.namespace;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DictNamespace implements Namespace {
    private Map<String, Symbol> table = new HashMap<String, Symbol>();
    private List<Namespace> imported = new LinkedList<Namespace>();
    public void register(String name, Symbol s) {
        if (table.containsKey(name)) {
            throw new RuntimeException("Not Implemented!");
        }
        table.put(name, s);
    }
    public Symbol lookup(String name) {
        Symbol result = table.get(name);
        if (result != null) {
            return result;
        }
        for (Namespace n: imported) {
            result = n.lookup(name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
    public void importNamespace(Namespace other) {
        imported.add(other);
    }
}
