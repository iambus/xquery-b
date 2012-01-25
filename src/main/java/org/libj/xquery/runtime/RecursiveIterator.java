package org.libj.xquery.runtime;

import java.util.ArrayList;
import java.util.Iterator;

public class RecursiveIterator implements Iterator<Object> {
    private ArrayList<Iterator> iterators = new ArrayList<Iterator>();
    private Object next;

    public RecursiveIterator(Iterator it) {
        iterators.add(it);
        next = nextAtom();
    }
    public boolean hasNext() {
        return next != null;
    }

    public Object next() {
        try {
            return next;
        } finally {
            next = nextAtom();
        }
    }

    private void popDead() {
        for (int i = iterators.size() - 1; i >= 0 && !iterators.get(i).hasNext(); i--) {
            iterators.remove(i);
        }
    }

    private Object nextAtom() {
        popDead();
        if (iterators.isEmpty()) {
            return null;
        }
        Iterator it = iterators.get(iterators.size()-1);
        Object v = it.next();
        while (v instanceof Iterable) {
            iterators.add(((Iterable) v).iterator());
            popDead();
            if (iterators.isEmpty()) {
                return null;
            }
            it = iterators.get(iterators.size()-1);
            v = it.next();
        }
        return v;
    }

    public void remove() {
        throw new UnsupportedOperationException("remove");
    }
}
