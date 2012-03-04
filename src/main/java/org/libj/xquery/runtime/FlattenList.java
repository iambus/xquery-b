package org.libj.xquery.runtime;

import org.libj.xquery.Callback;

import java.util.ArrayList;
import java.util.Iterator;

public class FlattenList implements MutableList, Callback {
    private ArrayList list = new ArrayList();
    public void call(Object result) {
        list.add(result);
    }

    public void add(Object x) {
        if (x == null) {
            return;
        }
        else if (x instanceof Iterable) {
            for (Object v: (Iterable)x) {
                call(v);
            }
        }
        else {
            call(x);
        }
    }

    public Iterator iterator() {
        return list.iterator();
    }

    public Object nth(int i) {
        if (0 <= i && i < list.size()) {
            return list.get(i);
        }
        else {
            return Op.NIL;
        }
    }

    public int size() {
        return list.size();
    }

    public String toString() {
        return ListUtils.toString(this);
    }
}
