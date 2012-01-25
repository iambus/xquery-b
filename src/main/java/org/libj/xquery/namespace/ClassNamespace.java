package org.libj.xquery.namespace;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;

import static org.libj.xquery.namespace.Reflector.*;

public class ClassNamespace implements Namespace {
    private String className;
    private List<Method> methods;
    private HashMap<String, Method> signatures;

    public ClassNamespace(String className) {
        this.className = className;
    }

    public Symbol lookup(String functionName) {
        if (signatures == null) {
            init();
        }
        if (signatures.containsKey(functionName)) {
            Method method = signatures.get(functionName);
            if (method == null) {
                throw new RuntimeException("Can't resolve method: "+className+"/"+functionName);
            }
            else {
                if (isStatic(method)) {
                    return new NormalStaticFunction(className, method);
                }
                else {
                    return new NormalMethodFunction(className, method);
                }
            }
        }
        else {
            throw new RuntimeException("No matching method found: "+className+"/"+functionName);
        }
    }

    public void register(String name, Symbol s) {
        throw new UnsupportedOperationException("register");
    }

    private void init() {
        methods = getMethods(className);
        signatures = new HashMap<String, Method>();
        for (Method method: methods) {
            String name = method.getName();
            if (isPublic(method)) {
                if (signatures.containsKey(name)) {
                    signatures.put(name, null); // can't resolve
                }
                else {
                    signatures.put(name, method); // can't resolve
                }
            }
        }
    }
}
