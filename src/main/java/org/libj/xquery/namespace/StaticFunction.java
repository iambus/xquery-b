package org.libj.xquery.namespace;

public class StaticFunction implements Function {
    private String className;
    private String functionName;
    private String signature;

    public StaticFunction(String className, String functionName, String signature) {
        this.className = className;
        this.functionName = functionName;
        this.signature = signature;
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

}
