package org.libj.xquery;

public interface XQuery {
    public Object eval();
    public Object eval(Object var1);
    public Object eval(Object var1, Object var2);
    public Object eval(Object var1, Object var2, Object var3);
    public Object eval(Object var1, Object var2, Object var3, Object var4);
    public Object eval(Object var1, Object var2, Object var3, Object var4, Object var5);
    public Object eval(Object var1, Object var2, Object var3, Object var4, Object var5, Object var6);
    public Object eval(Object var1, Object var2, Object var3, Object var4, Object var5, Object var6, Object var7);
    public Object eval(Object var1, Object var2, Object var3, Object var4, Object var5, Object var6, Object var7, Object var8);
    public Object eval(Object var1, Object var2, Object var3, Object var4, Object var5, Object var6, Object var7, Object var8, Object var9);
    public Object eval(Object var1, Object var2, Object var3, Object var4, Object var5, Object var6, Object var7, Object var8, Object var9, Object var10);
    public Object eval(Object... vars);
    public Object eval(Environment environment);

    public void eval(Callback callback);
    public void eval(Callback callback, Object var1);
    public void eval(Callback callback, Object var1, Object var2);
    public void eval(Callback callback, Object var1, Object var2, Object var3);
    public void eval(Callback callback, Object var1, Object var2, Object var3, Object var4);
    public void eval(Callback callback, Object var1, Object var2, Object var3, Object var4, Object var5);
    public void eval(Callback callback, Object var1, Object var2, Object var3, Object var4, Object var5, Object var6);
    public void eval(Callback callback, Object var1, Object var2, Object var3, Object var4, Object var5, Object var6, Object var7);
    public void eval(Callback callback, Object var1, Object var2, Object var3, Object var4, Object var5, Object var6, Object var7, Object var8);
    public void eval(Callback callback, Object var1, Object var2, Object var3, Object var4, Object var5, Object var6, Object var7, Object var8, Object var9);
    public void eval(Callback callback, Object var1, Object var2, Object var3, Object var4, Object var5, Object var6, Object var7, Object var8, Object var9, Object var10);
    public void eval(Callback callback, Object... vars);
    public void eval(Callback callback, Environment environment);

}
