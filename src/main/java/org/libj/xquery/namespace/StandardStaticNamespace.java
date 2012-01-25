package org.libj.xquery.namespace;

public class StandardStaticNamespace extends DictNamespace {
    private String className;

    public StandardStaticNamespace(String className) {
        this.className = className.replace('.', '/');
    }
    
    public void registerStaticFunction(String alias, String methodName, int n) {
        register(alias, new StandardStaticFunction(className, methodName, n));
    }
    
    public void registerStaticFunction(String methodName, int n) {
        registerStaticFunction(methodName, methodName, n);
    }

    public void registerStaticVarlistFunction(String alias, String methodName) {
        register(alias, new StandardStaticVarlistFunction(className, methodName));
    }

    public void registerStaticVarlistFunction(String methodName) {
        registerStaticVarlistFunction(methodName, methodName);
    }

    public void registerStaticOverloadedFunction(String alias, String methodName, int...n) {
        register(alias, new StandardStaticOverloadedFunction(className, methodName, n));
    }

    public void registerStaticOverloadedFunction(String methodName, int...n) {
        registerStaticOverloadedFunction(methodName, methodName, n);
    }
}
