package org.libj.xquery;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private Map<String, Object> variables;
    public Environment() {
    }
    public Object getVariable(String name) {
        if (variables != null) {
            Object v = variables.get(name);
            if (v != null) {
                return v;
            }
        }
        throw new RuntimeException("Unbound variable "+name);
    }
    public void putVariable(String name, Object v) {
        if (variables == null) {
            variables = new HashMap<String, Object>();
        }
        variables.put("$"+name, v);
    }
}
