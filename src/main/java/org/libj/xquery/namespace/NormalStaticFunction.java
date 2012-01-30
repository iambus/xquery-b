package org.libj.xquery.namespace;

import java.lang.reflect.Method;

public class NormalStaticFunction implements JavaFunction {
    private Method method;
    private String className;
    private String signature;

    public NormalStaticFunction(String className, Method method) {
        this.method = method;
        this.className = className.replace('.', '/');
        this.signature = Reflector.getMethodSignature(method);
    }

    public static int parseSignatureNumber(String signature) {
        String params = signature.substring(signature.indexOf('(')+1, signature.indexOf(')'));
        int n = 0;
        int i = 0;
        while (i < params.length()) {
            char c = params.charAt(i);
            switch (c) {
                case 'B': case 'C': case 'D': case 'F': case 'I': case 'J': case 'S': case 'Z':
                    i++;
                    n++;
                    break;
                case 'L':
                    int j = params.indexOf(';', i) + 1;
                    if (j <= i) {
                        throw new RuntimeException("Parsing method signature failed: "+signature);
                    }
                    i = j;
                    n++;
                    break;
                case '[':
                    i++;
                    break;
                default:
                    throw new RuntimeException("Not Implemented: "+c);
            }
        }
        return n;
    }

    public Class<?>[] getParameterTypes() {
        return method.getParameterTypes();
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    public boolean isVarArgs() {
        return method.isVarArgs();
    }

    public String getClassName() {
        return className;
    }

    public String getFunctionName() {
        return method.getName();
    }

    public String getSignature() {
        return signature;
    }

    public boolean isMethod() {
        return false;
    }

}
