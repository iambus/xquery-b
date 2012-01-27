package org.libj.xquery.runtime;

import java.util.Iterator;

public class Nil implements List {
    public static final Nil NIL = new Nil();
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

    public Object nth(int i) {
        return this;
    }

    public int size() {
        return 0;
    }
    
    public String toString() {
        return "";
    }
}
