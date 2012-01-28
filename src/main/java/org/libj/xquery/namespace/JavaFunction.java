package org.libj.xquery.namespace;

public interface JavaFunction extends Function {
    public String getClassName();
    public String getFunctionName();
    public String getSignature();
    public Class<?>[] getParameterTypes();
    public Class<?> getReturnType();
    public boolean isMethod();
    public boolean isVarArgs();
}
