package org.libj.xquery.namespace;

public class StandardStaticFunction extends StaticFunction {
    private int n;

    public StandardStaticFunction(String className, String functionName, int n) {
        super(className, functionName, generateSignature(n));
        this.n = n;
    }

    public int getParameterNumber() {
        return n;
    }

    public static String generateSignature(int n) {
        StringBuilder signature = new StringBuilder();
        signature.append('(');
        for (int i = 0; i < n; i++) {
            signature.append("Ljava/lang/Object;");
        }
        signature.append(")Ljava/lang/Object;");
        return signature.toString();
    }

}
