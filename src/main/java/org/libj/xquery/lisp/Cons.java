package org.libj.xquery.lisp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Cons<E> implements Iterable<E> {
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
    public void setCar(E x) {
        car = x;
    }
    public Cons<E> setCdr(Cons<E> x) {
        cdr = x;
        return x;
    }
    public E first() {
        return car;
    }
    public Cons<E> next() {
        return cdr;
    }

    public Cons<E> conj(E x) {
        return new Cons<E>(x, this);
    }

    public static <E> Cons<E> cons(E x, Cons<E> list) {
        return new Cons<E>(x, list);
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
