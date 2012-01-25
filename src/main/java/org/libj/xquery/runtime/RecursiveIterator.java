package org.libj.xquery.runtime;

import java.util.ArrayList;
import java.util.Iterator;

public class RecursiveIterator implements Iterator<Object> {
    private ArrayList<Iterator> iterators = new ArrayList<Iterator>();

    public RecursiveIterator(Iterator it) {
        iterators.add(it);
    }
    public boolean hasNext() {
        popDead();
        return !iterators.isEmpty() && iterators.get(iterators.size()-1).hasNext();
    }

    public Object next() {
        popDead();
        Object v = iterators.get(iterators.size()-1).next();
        if (v instanceof Iterable) {
            iterators.add(((Iterable) v).iterator());
            return next();
        }
        else {
            return v;
        }
    }
    
    private void popDead() {
        for (int i = iterators.size() - 1; i >= 0 && !iterators.get(i).hasNext(); i--) {
            iterators.remove(i);
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("remove");
    }
}
