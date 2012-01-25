package org.libj.xquery.namespace;

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

    public static boolean isPublic(Method method) {
        return Modifier.isPublic(method.getModifiers());
    }

    public static boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    public static String getMethodSignature(Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        for (Class c: method.getParameterTypes()) {
            builder.append(getTypeSignature(c));
        }
        builder.append(')');
        builder.append(getTypeSignature(method.getReturnType()));
        return builder.toString();
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

    public static void x(int i){}
    public static void x(int[] i){}

    public static void main(String[] args) throws NoSuchMethodException {
        for (Method m: getMethods(Reflector.class))
            System.out.println(m.getName() +':'+ getMethodSignature(m));
    }
}
