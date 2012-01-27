package org.libj.xquery.namespace;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.libj.xquery.namespace.Reflector.*;

public class ClassNamespace implements Namespace {
    private String className;
    private Class clazz;
    private List<Method> methods;
    private HashMap<String, Function> methodTable;
    private Function constructor;

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
            Function method = methodTable.get(functionName);
            return method;
        }
        else if ("new".equals(functionName)) {
            if (constructor == null) {
                throw new RuntimeException("No constructor found: "+className);
            }
            return constructor;
        }
        else {
            throw new RuntimeException("No matching method found: "+className+"/"+functionName);
        }
    }

    public void register(String name, Symbol s) {
        throw new UnsupportedOperationException("register");
    }

    private void init() {
        initMethods();
        initConstructors();
    }

    private void initMethods() {
        methods = getMethods(clazz);
        methodTable = new HashMap<String, Function>();
        HashMap<String, List<Method>> table = new HashMap<String, List<Method>>();
        for (Method method: methods) {
            String name = method.getName();
            if (isPublic(method)) {
                if (table.containsKey(name)) {
                    table.get(name).add(method);
                }
                else {
                    List<Method> overloaded = new ArrayList<Method>();
                    overloaded.add(method);
                    table.put(name, overloaded);
                }
            }
        }
        for (Map.Entry<String, List<Method>> pair: table.entrySet()) {
            String functionName = pair.getKey();
            List<Method> methods = pair.getValue();
            if (methods.isEmpty()) {
                throw new RuntimeException("Not Implemented!");
            }
            else if (methods.size() == 1) {
                Method method = methods.get(0);
                if (isStatic(method)) {
                    methodTable.put(functionName, new NormalStaticFunction(className, method));
                }
                else {
                    methodTable.put(functionName, new NormalMethodFunction(className, method));
                }
            }
            else {
                List <JavaFunction> overloaded = new ArrayList<JavaFunction>();
                for (Method method: methods) {
                    if (isStatic(method)) {
                        overloaded.add(new NormalStaticFunction(className, method));
                    }
                    else {
                        overloaded.add(new NormalMethodFunction(className, method));
                    }
                }
                methodTable.put(functionName, new OverloadedFunction(className, functionName, overloaded));
            }
        }
    }

    private void initConstructors() {
        ArrayList<Constructor> constructors = new ArrayList<Constructor>();
        for (Constructor constructor: getConstructors(clazz)) {
            if (isPublic(constructor)) {
                constructors.add(constructor);
            }
        }
        if (constructors.isEmpty()) {
            this.constructor = null;
        }
        else if (constructors.size() == 1) {
            this.constructor = new NormalConstructorFunction(clazz, constructors.get(0));
        }
        else {
            List <JavaFunction> overloaded = new ArrayList<JavaFunction>();
            for (Constructor ctor: constructors) {
                overloaded.add(new NormalConstructorFunction(clazz, ctor));
            }
            this.constructor = new OverloadedFunction(className, "<init>", overloaded);
        }
    }
}
