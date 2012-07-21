package org.libj.xquery.lisp;

public class Walker {
    private static Object walk(Fn inner, Fn outer, Object form) {
        if (form instanceof Cons) {
            return outer.call(Cons.map(inner, (Cons) form));
        } else {
            return outer.call(form);
        }
    }

    public static Object postwalk(final Fn f, Object form) {
        return walk(new Fn() {
            public Object call(Object x) {
                return postwalk(f, x);
            }
        }, f, form);
    }

    public static Object prewalk(final Fn f, Object form) {
        return walk(new Fn() {
            public Object call(Object x) {
                return prewalk(f, x);
            }
        }, Core.identity, f.call(form));
    }
}
