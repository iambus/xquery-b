package org.libj.xquery.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AnnotationReader {
    public static String namespace(Class clazz) {
        Namespace annotation = (Namespace) clazz.getAnnotation(Namespace.class);
        if (annotation != null && !annotation.name().isEmpty()) {
            return annotation.name();
        }
        return null;
    }
    public static List<Method> functions(Class clazz) {
        List<Method> functions = new ArrayList<Method>();
        for (Method m: clazz.getMethods()) {
            if (m.isAnnotationPresent(Function.class)) {
                functions.add(m);
            }
        }
        return functions;
    }
    public static String functionName(Method function) {
        Function annotation = function.getAnnotation(Function.class);
        if (annotation != null && !annotation.name().isEmpty()) {
            return annotation.name();
        }
        return function.getName();
    }
}
