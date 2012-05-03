package org.libj.xquery.runtime;

import org.libj.xquery.Callback;

import java.util.Iterator;

public class CallbackList implements MutableList {
    private Callback callback;

    public CallbackList(Callback callback) {
        this.callback = callback;
    }
    public Iterator iterator() {
        throw new UnsupportedOperationException("iterator");
    }

    public void add(Object x) {
        if (x == null) {
            return;
        }
        else if (x instanceof Iterable) {
            for (Object v: (Iterable)x) {
                callback.call(v);
            }
        }
        else {
            callback.call(x);
        }
    }

    public Object nth(int i) {
        throw new UnsupportedOperationException("nth");
    }

    public int size() {
        throw new UnsupportedOperationException("size");
    }
}
