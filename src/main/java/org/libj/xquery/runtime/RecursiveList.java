package org.libj.xquery.runtime;

import java.util.ArrayList;
import java.util.Iterator;

public class RecursiveList implements List {
    private ArrayList list = new ArrayList();

    public Iterator iterator() {
        return new RecursiveIterator(list.iterator());
    }

    public Object nth(int i) {
        return list.get(i);
    }

    public int size() {
        throw new UnsupportedOperationException("size");
    }
    
    public void add(Object x) {
        if (x instanceof Nil) {
            return;
        }
        list.add(x);
    }
    
    public java.util.List toNonRecursiveList() {
        return list;
    }

    public String toString() {
//        return list.toString();
        return ListUtils.toString(this);
    }

}
