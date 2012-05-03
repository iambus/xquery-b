package org.libj.xquery.runtime;

import java.util.Iterator;

public class SingletonList implements List {
    private Object value;

    public SingletonList(Object value) {
        this.value = value;
    }

    public Iterator iterator() {
        return new Iterator<Object>() {
            private boolean read = false;
            public boolean hasNext() {
                return !read;
            }

            public Object next() {
                if (read) {
                    throw new RuntimeException("Not Implemented!");
                }
                read = true;
                return value;
            }

            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }

    public Object nth(int i) {
        return i == 1 ? value : Op.NIL;
    }

    public int size() {
        throw new UnsupportedOperationException("size");
    }
}
