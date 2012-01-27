package org.libj.xquery.parser;

import com.sun.corba.se.impl.ior.OldJIDLObjectKeyTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Cons implements Iterable {
    private Object car;
    private Cons cdr;

    public Cons(Object car, Cons cdr) {
        this.cdr = cdr;
        this.car = car;
    }
    public Cons(Object car) {
        this(car, null);
    }
    public Cons() {
        this(null);
    }
    public void car(Object x) {
        car = x;
    }
    public void cdr(Cons x) {
        cdr = x;
    }
    public Object first() {
        return car;
    }
    public Cons next() {
        return cdr;
    }

    public static Iterator<Object> iterator(final Cons i) {
        return new Iterator<Object>() {
            private Cons it = i;
            public boolean hasNext() {
                return it != null;
            }

            public Object next() {
                Object v = it.first();
                it = it.next();
                return v;
            }

            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }

    public Iterator<Object> iterator() {
        final Cons i = this;
        return iterator(i);
    }

    public static List<Object> toList(Cons list) {
        List<Object> result = new ArrayList<Object>();
        while (list != null) {
            result.add(list.first());
            list = list.next();
        }
        return result;
    }
    public List<Object> toList() {
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
