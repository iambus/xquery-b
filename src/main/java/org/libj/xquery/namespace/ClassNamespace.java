package org.libj.xquery.namespace;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import static org.libj.xquery.namespace.Reflector.*;

public class ClassNamespace implements Namespace {
    private String className;
    private Class clazz;
    private List<Method> methods;
    private HashMap<String, Method> methodTable;
    private List<Constructor> constructors;
    private HashMap<String, Constructor> constructorTable;

    public ClassNamespace(String className) {
        this.className = className;
        try {
            this.clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Symbol lookup(String functionName) {
        if (methodTable == null) {
            init();
        }
        if (methodTable.containsKey(functionName)) {
            Method method = methodTable.get(functionName);
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
        else if ("new".equals(functionName)) {
            if (!constructorTable.containsKey(functionName)) {
                throw new RuntimeException("No constructor found: "+className);
            }
            Constructor constructor = constructorTable.get(functionName);
            if (constructor == null) {
                throw new RuntimeException("Can't resolve constructor: "+className);
            }
            return new NormalConstructorFunction(className, constructor);
        }
        else {
            throw new RuntimeException("No matching method found: "+className+"/"+functionName);
        }
    }

    public void register(String name, Symbol s) {
        throw new UnsupportedOperationException("register");
    }

    private void init() {
        methods = getMethods(clazz);
        methodTable = new HashMap<String, Method>();
        for (Method method: methods) {
            String name = method.getName();
            if (isPublic(method)) {
                if (methodTable.containsKey(name)) {
                    methodTable.put(name, null); // can't resolve
                }
                else {
                    methodTable.put(name, method); // can't resolve
                }
            }
        }
        constructors = getConstructors(clazz);
        constructorTable = new HashMap<String, Constructor>();
        for (Constructor constructor: constructors) {
            String name = "new";
            if (isPublic(constructor)) {
                if (constructorTable.containsKey(name)) {
                    constructorTable.put(name, null); // can't resolve
                }
                else {
                    constructorTable.put(name, constructor); // can't resolve
                }
            }
        }
    }
}
