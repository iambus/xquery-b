package org.libj.xquery.namespace;

import org.libj.xquery.annotation.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class LibNamespace implements Namespace {
    private Class clazz = null;
    private String namespace = null;
    private Map<String, Function> table;

    public LibNamespace(Class clazz) {
        this.clazz = clazz;
    }

    private void init() {
        namespace = AnnotationReader.namespace(clazz);
        table = ClassNamespace.createMethodTable(AnnotationReader.functions(clazz), true);
    }

    public void register(String name, Symbol s) {
        throw new UnsupportedOperationException("register");
    }

    public Symbol lookup(String functionName) {
        if (table == null) {
            init();
        }
        if (table.containsKey(functionName)) {
            Function method = table.get(functionName);
            return method;
        }
        else {
            throw new RuntimeException("Unresolved symbol "+functionName);
        }
    }

    public String getNamespace() {
        return namespace;
    }
}
