package org.libj.xquery.namespace;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class Reflector {

    public static List<Method> getMethods(Class<?> clazz) {
        ArrayList<Method> methods = new ArrayList<Method>();
        for (Method method: clazz.getDeclaredMethods()) {
                methods.add(method);
        }
        return methods;
    }
    public static List<Method> getMethods(String className) {
        try {
            return getMethods(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Constructor> getConstructors(Class<?> clazz) {
        ArrayList<Constructor> constructors = new ArrayList<Constructor>();
        for (Constructor constructor: clazz.getConstructors()) {
            constructors.add(constructor);
        }
        return constructors;
    }

    public static boolean isPublic(Method method) {
        return Modifier.isPublic(method.getModifiers());
    }

    public static boolean isPublic(Constructor constructor) {
        return Modifier.isPublic(constructor.getModifiers());
    }

    public static boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    public static String getMethodSignature(Class<?>[] parameterTypes, Class<?> returnType) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        for (Class c: parameterTypes) {
            builder.append(getTypeSignature(c));
        }
        builder.append(')');
        builder.append(getTypeSignature(returnType));
        return builder.toString();
    }

    public static String getMethodSignature(Method method) {
        return getMethodSignature(method.getParameterTypes(), method.getReturnType());
    }

    public static String getConstructorSignature(Constructor constructor) {
        return getMethodSignature(constructor.getParameterTypes(), Void.TYPE);
    }

    public static String getTypeSignature(Class c) {
        if (c.isPrimitive()) {
            String typeName = c.getName();
            if (typeName.equals("byte")) {
                return "B";
            }
            if (typeName.equals("char")) {
                return "C";
            }
            if (typeName.equals("double")) {
                return "D";
            }
            if (typeName.equals("float")) {
                return "F";
            }
            if (typeName.equals("int")) {
                return "I";
            }
            if (typeName.equals("long")) {
                return "J";
            }
            if (typeName.equals("short")) {
                return "S";
            }
            if (typeName.equals("boolean")) {
                return "Z";
            }
            if (typeName.equals("void")) {
                return "V";
            }
            throw new RuntimeException("Not Implemented: "+typeName);
        } else if (c.isArray()) {
            return c.getName().replace('.', '/');
        } else {
            return 'L' + c.getName().replace('.', '/') + ';';
        }
    }

}
