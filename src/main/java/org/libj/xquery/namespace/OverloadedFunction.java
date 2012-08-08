package org.libj.xquery.namespace;

import java.util.ArrayList;
import java.util.List;

public class OverloadedFunction implements Function {
    private String className;
    private String functionName;
    private List<JavaFunction> functions;

    public OverloadedFunction(String className, String functionName, List<JavaFunction> functions) {
        this.className = className.replace('.', '/');
        this.functionName = functionName;
        this.functions = functions;
    }

    public JavaFunction resolveFunction(Class... argumentTypes) {
        // check number of parameters
        List<JavaFunction> numberMatched = new ArrayList<JavaFunction>();
        for (JavaFunction f: functions) {
            if (f.getParameterTypes().length == argumentTypes.length - (f.isMethod() ? 1 : 0)) {
                numberMatched.add(f);
            }
        }
        if (numberMatched.isEmpty()) {
            throw new RuntimeException("No matching method found: "+functions.get(0).getClassName()+'/'+functions.get(0).getFunctionName());
        }
        if (numberMatched.size() == 1) {
            return numberMatched.get(0);
        }
        // find perfect match of parameter types
        List<JavaFunction> exactlyMatched = new ArrayList<JavaFunction>();
        for (JavaFunction f: functions) {
            if(isExactlyMatched(f, argumentTypes)) {
                exactlyMatched.add(f);
            }
        }
        if (exactlyMatched.size() > 1) {
            // should not happen
            throw new RuntimeException("Can't resolve function: "+functions.get(0).getClassName()+'/'+functions.get(0).getFunctionName());
        }
        if (exactlyMatched.size() == 1) {
            return exactlyMatched.get(0);
        }
        // find upcasting of parameter types
        List<JavaFunction> looselyMatched = new ArrayList<JavaFunction>();
        for (JavaFunction f: functions) {
            if(isLooselyMatched(f, argumentTypes)) {
                looselyMatched.add(f);
            }
        }
        if (looselyMatched.size() > 1) {
            // should not happen
            throw new RuntimeException("Can't resolve function: "+functions.get(0).getClassName()+'/'+functions.get(0).getFunctionName());
        }
        if (looselyMatched.size() == 1) {
            return looselyMatched.get(0);
        }
        if (looselyMatched.isEmpty()) {
            throw new RuntimeException("No matching method found: "+functions.get(0).getClassName()+'/'+functions.get(0).getFunctionName());
        }
        throw new RuntimeException("Not Implemented!");
    }

    private static boolean isExactlyMatched(JavaFunction f, Class[] argumentTypes) {
        Class[] parameterTypes = f.getParameterTypes();
        if (parameterTypes.length != argumentTypes.length - (f.isMethod() ? 1 : 0)) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i] != argumentTypes[i + (f.isMethod() ? 1 : 0)]) {
                return false;
            }
        }
        return true;
    }

    private static boolean isLooselyMatched(JavaFunction f, Class[] argumentTypes) {
        Class[] parameterTypes = f.getParameterTypes();
        if (parameterTypes.length != argumentTypes.length - (f.isMethod() ? 1 : 0)) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (! isAssignableFrom(parameterTypes[i], (argumentTypes[i + (f.isMethod() ? 1 : 0)]))) {
                return false;
            }
        }
        return true;
    }

    private static Class primitiveTypeToClass(Class x) {
        if (x == int.class) {
            return Integer.class;
        }
        else if (x == double.class) {
            return Double.class;
        }
        else if (x == long.class) {
            return Long.class;
        }
        else if (x == boolean.class) {
            return Boolean.class;
        }
        else if (x == float.class) {
            return Float.class;
        }
        else if (x == short.class) {
            return Short.class;
        }
        else if (x == byte.class) {
            return Byte.class;
        }
        else if (x == char.class) {
            return Character.class;
        }
        else {
            throw new RuntimeException("Not Implemented: "+x);
        }
    }
    private static boolean isAssignableFrom(Class x, Class y) {
        if (x.isAssignableFrom(y)) {
            return true;
        }
        else if (x.isPrimitive() && !y.isPrimitive()) {
            return primitiveTypeToClass(x).isAssignableFrom(y);
        }
        else if (!x.isPrimitive() && y.isPrimitive()) {
            return x.isAssignableFrom(primitiveTypeToClass(y));
        }
        return false;
    }

    public String getClassName() {
        return className;
    }

    public String getFunctionName() {
        return functionName;
    }
}
