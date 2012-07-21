package org.libj.xquery.lisp;

public class Core {
    private static final class Identity implements Fn {
        public Object call(Object x) {
            return x;
        }
    }
    public static final Fn identity = new Identity();
}
