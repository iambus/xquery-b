package org.libj.xquery.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Cons<E> implements Iterable<E> {
    private E car;
    private Cons cdr;

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
    public void car(E x) {
        car = x;
    }
    public Cons<E> cdr(Cons<E> x) {
        cdr = x;
        return x;
    }
    public E first() {
        return car;
    }
    public Cons<E> next() {
        return cdr;
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
}
