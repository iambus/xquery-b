package org.libj.xquery.namespace;

import org.libj.xquery.annotation.AnnotationReader;

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
    private Map<String, Function> methodTable;
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
        methodTable = createMethodTable(methods, false);
    }

    public static Map<String, Function> createMethodTable(List<Method> methods, boolean useAnnotationName) {
        Map<String, Function> functionTable = new HashMap<String, Function>();
        HashMap<String, List<Method>> methodTable = new HashMap<String, List<Method>>();
        if (methods.isEmpty()) {
            return functionTable;
        }
        String className = methods.get(0).getDeclaringClass().getName();
        for (Method method: methods) {
            String name = useAnnotationName ? AnnotationReader.functionName(method) : method.getName();
            if (isPublic(method)) {
                if (methodTable.containsKey(name)) {
                    methodTable.get(name).add(method);
                }
                else {
                    List<Method> overloaded = new ArrayList<Method>();
                    overloaded.add(method);
                    methodTable.put(name, overloaded);
                }
            }
        }
        for (Map.Entry<String, List<Method>> pair: methodTable.entrySet()) {
            String functionName = pair.getKey();
            List<Method> overloadedMethods = pair.getValue();
            if (overloadedMethods.isEmpty()) {
                throw new RuntimeException("Not Implemented!");
            }
            else if (overloadedMethods.size() == 1) {
                Method method = overloadedMethods.get(0);
                    functionTable.put(functionName, toFunction(className, method));
            }
            else {
                List <JavaFunction> overloadedFunctions = new ArrayList<JavaFunction>();
                for (Method method: overloadedMethods) {
                    overloadedFunctions.add(toFunction(className, method));
                }
                functionTable.put(functionName, new OverloadedFunction(className, functionName, overloadedFunctions));
            }
        }
        return functionTable;
    }

    private static JavaFunction toFunction(String className, Method method) {
        if (isStatic(method)) {
            return new NormalStaticFunction(className, method);
        } else {
            return new NormalMethodFunction(className, method);
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
