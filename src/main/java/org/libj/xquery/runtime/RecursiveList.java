package org.libj.xquery.runtime;

import java.util.ArrayList;
import java.util.Iterator;

public class RecursiveList implements List {
    private ArrayList list = new ArrayList();

    public Iterator iterator() {
        return new RecursiveIterator(list.iterator());
    }

    public Object nth(int i) {
        Iterator it = iterator();
        Object v = null;
        while (it.hasNext() && i-- >= 0) {
            v = it.next();
        }
        if (i < 0 && v != null) {
            return v;
        }
        else {
            return new Nil();
        }
    }

    public int size() {
        return ListUtils.size(list);
    }
    
    public void add(Object x) {
        if (x instanceof Nil) {
            return;
        }
        list.add(x);
    }
    
    public java.util.List toNonRecursiveNonFlattenList() {
        // non-flatten version
        return list;
    }

    public String toString() {
//        return list.toString();
        return ListUtils.toString(this);
    }

}
