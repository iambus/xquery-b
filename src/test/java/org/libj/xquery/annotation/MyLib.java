package org.libj.xquery.annotation;

@Namespace
public class MyLib {
    @Function(name="long")
    public static long toLong(int i) {
        return i;
    }
}
