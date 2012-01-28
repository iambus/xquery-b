package org.libj.xquery.namespace;

public class StandardStaticVarlistFunction extends StaticFunction {
    public StandardStaticVarlistFunction(String className, String functionName) {
        super(className, functionName, "([Ljava/lang/Object;)Ljava/lang/Object;");
    }

    @Override
    public boolean isVarArgs() {
        return true;
    }
}
