package org.libj.xquery.lisp;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Cons<E> implements Iterable<E> {
    public static final Cons NIL = null;

    private E car;
    private Cons<E> cdr;

    public Cons(E car, Cons<E> cdr) {
        this.cdr = cdr;
        this.car = car;
    }
    public Cons(E car) {
        this(car, null);
    }
    public Cons() {
        this(null);
    }

    public E first() {
        return car;
    }
    public Cons<E> next() {
        return cdr;
    }

    public E second() {
        return next().first();
    }

    public E third() {
        return next().next().first();
    }

    public static <E> Cons<E> cons(E x, Cons<E> list) {
        return new Cons<E>(x, list);
    }

    public Cons<E> assoc(int n, E x) {
        if (n == 0) {
            return cons(x, next());
        }
        else {
            return cons(first(), next().assoc(--n, x));
        }
    }

    public Cons<E> conj(E x) {
        return new Cons<E>(x, this);
    }

    public static <E> Cons<E> list(E...elements) {
        Cons<E> result = null;
        for (int i = elements.length - 1; i >= 0; i--) {
            result = new Cons<E>(elements[i], result);
        }
        return result;
    }

    public static <E> Cons<E> append(Cons<E> list, E x) {
        if (list == null) {
            return new Cons<E>(x);
        }
        else {
            return new Cons<E>(list.car, append(list.cdr, x));
        }
    }

    public static <E> Cons<E> concat(Cons<E> x1, Cons<E> x2) {
        if (x1 == null) {
            return x2;
        }
        else {
            return cons(x1.first(), concat(x1.next(), x2));
        }
    }

    public static <E> Cons<E> reverse(Cons<E> x) {
        Cons<E> result = null;
        while (x != null) {
            result = cons(x.first(), result);
            x = x.next();
        }
        return result;
    }

    public Cons<E> reverse() {
        return reverse(this);
    }

    public E nth(int i) {
        if (i < 0) {
            throw new RuntimeException("Negative index");
        }
        else if (i == 0) {
            return first();
        }
        else {
            return next().nth(--i);
        }
    }

    public static int size(Cons list) {
        int n = 0;
        while (list != null) {
            n++;
            list = list.next();
        }
        return n;
    }

    public int size() {
        return size(this);
    }

    public static <E> Iterator<E> iterator(final Cons<E> i) {
        return new Iterator<E>() {
            private Cons<E> it = i;
            public boolean hasNext() {
                return it != null;
            }

            public E next() {
                E v = it.first();
                it = it.next();
                return v;
            }

            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }

    public Iterator<E> iterator() {
        final Cons<E> i = this;
        return iterator(i);
    }

    public static <E> List<E> toList(Cons<E> list) {
        List<E> result = new ArrayList<E>();
        while (list != null) {
            result.add(list.first());
            list = list.next();
        }
        return result;
    }

    public List<E> toList() {
        return toList(this);
    }

    public static Cons rest(Cons list) {
        Cons rest = list.next();
        if (rest != null) {
            return rest;
        }
        return nilList();
    }

    public Cons<E> rest() {
        return rest(this);
    }

    private static class NilCons extends Cons {
        @Override
        public Iterator iterator() {
            return new Iterator<Object>() {

                public boolean hasNext() {
                    return false;
                }

                public Object next() {
                    throw new UnsupportedOperationException("next");
                }

                public void remove() {
                    throw new UnsupportedOperationException("remove");
                }
            };
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Object first() {
            throw new RuntimeException("Nil access");
        }

        @Override
        public Cons next() {
            throw new RuntimeException("Nil access");
        }
    }

    public static Cons nilList() {
        return new NilCons();
    }

    public static boolean isNil(Cons list) {
        return list == null || list instanceof NilCons || list.first() == null;
    }

    public static Cons map(Fn fn, Cons list) {
        if (list == null) {
            return null;
        }
        else {
            return cons(fn.call(list.first()), map(fn, list.next()));
        }
    }

    public String toString(Cons cons) {
        if (cons == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(first());
        for (Object x: rest()) {
            builder.append(' ');
            builder.append(x);
        }
        builder.append(')');
        return builder.toString();
    }

    public String toString() {
        return toString(this);
    }

}
