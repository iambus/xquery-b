package org.libj.xquery.namespace;

import java.lang.reflect.Constructor;

public class NormalConstructorFunction implements Function {
    private String className;
    private Constructor constructor;
    private String functionName;
    private String signature;

    public NormalConstructorFunction(String className, Constructor constructor) {
        this.className = className.replace('.', '/');
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


}
