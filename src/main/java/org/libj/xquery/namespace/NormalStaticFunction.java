package org.libj.xquery.namespace;

import java.lang.reflect.Method;

public class NormalStaticFunction extends StaticFunction {
    private Method method;

    public NormalStaticFunction(String className, Method method) {
        super(className, method.getName(), Reflector.getMethodSignature(method));
        this.method = method;
    }

    public Class<?>[] getParameterTypes() {
        return method.getParameterTypes();
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    @Override
    public boolean isVarArgs() {
        return method.isVarArgs();
    }
}
