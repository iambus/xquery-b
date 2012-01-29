package org.libj.xquery.namespace;

import java.lang.reflect.Method;

public class NormalMethodFunction implements JavaFunction {
    private String className;
    private Method method;
    private String functionName;
    private String signature;

    public NormalMethodFunction(String className, Method method) {
        this.className = className.replace('.', '/');
        this.method = method;
        this.functionName = method.getName();
        this.signature = Reflector.getMethodSignature(method);
    }

    public String getClassName() {
        return className;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getSignature() {
        return signature;
    }

    public Class<?>[] getParameterTypes() {
        return method.getParameterTypes();
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    public boolean isMethod() {
        return true;
    }

    public boolean isVarArgs() {
        return method.isVarArgs();
    }


}
