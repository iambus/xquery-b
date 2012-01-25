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
