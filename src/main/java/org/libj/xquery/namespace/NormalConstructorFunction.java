package org.libj.xquery.namespace;

import java.lang.reflect.Constructor;

public class NormalConstructorFunction implements JavaFunction {
    private Class clazz;
    private String className;
    private Constructor constructor;
    private String functionName;
    private String signature;

    public NormalConstructorFunction(Class clazz, Constructor constructor) {
        this.clazz = clazz;
        this.className = clazz.getName().replace('.', '/');
        this.constructor = constructor;
        this.functionName = "<init>";
        this.signature = Reflector.getConstructorSignature(constructor);
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
        return constructor.getParameterTypes();
    }

    public Class<?> getReturnType() {
        return clazz;
    }

    public boolean isMethod() {
        return false;
    }


}
