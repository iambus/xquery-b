package org.libj.xquery.namespace;

import static org.libj.xquery.namespace.StandardStaticFunction.generateSignature;

public class StandardStaticOverloadedFunction implements Function {
    private String className;
    private String functionName;
    private int[] numbers;

    public StandardStaticOverloadedFunction(String className, String functionName, int...numbers) {
        this.className = className;
        this.functionName = functionName;
        this.numbers = numbers;
    }

    public String getClassName() {
        return className;
    }

    public String getSignature(int n) {
        for (int i = 0; i < numbers.length; i++) {
            if (numbers[i] == n) {
                return generateSignature(n);
            }
        }
        throw new RuntimeException("Overloaded function  not found: " + generateSignature(n));
    }

    public StandardStaticFunction getFunction(int n) {
        return new StandardStaticFunction(className, functionName, n);
    }
}
