package org.libj.xquery;

public interface XQuery {
    public Object eval(Environment environment);
    public Object eval();
    public void eval(Callback callback);
}
