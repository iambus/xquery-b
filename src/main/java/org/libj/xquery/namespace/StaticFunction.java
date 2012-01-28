package org.libj.xquery.namespace;

public class StaticFunction implements JavaFunction {
    private String className;
    private String functionName;
    private String signature;

    public StaticFunction(String className, String functionName, String signature) {
        this.className = className.replace('.', '/');
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

    public Class<?>[] getParameterTypes() {
        throw new UnsupportedOperationException("getParameterTypes");
    }

    public Class<?> getReturnType() {
        throw new UnsupportedOperationException("getReturnType");
    }

    public boolean isMethod() {
        return false;
    }

    public boolean isVarArgs() {
        return false;
    }

    public int getParameterNumber() {
        return parseSignatureNumber(signature);
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

}
