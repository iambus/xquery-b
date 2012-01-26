package org.libj.xquery.runtime;

import java.util.Iterator;

public class Range implements List {
    private int start;
    private int end;

    public Range(int start, int end) {
        // [start, end)
        this.start = start;
        this.end = end;
    }

    public Iterator iterator() {
        return new RangeIterator(start, end);
    }
    
    public int size() {
        if (start > end) {
            return 0;
        }
        else {
            return end - start;
        }
    }
    
    public Object nth(int i) {
        // Note: the index start from 0
        int v = start + i;
        if (v < start || end <= v) {
//            throw new IndexOutOfBoundsException(String.format("Index: %d, Range: [%d, %d)", v, start, end));
//            throw new IndexOutOfBoundsException(String.format("Index: %d, Size: %d", i, size()));
            return Op.NIL;
        }
        return v;
    }
    
    public String toString() {
        return ListUtils.toString(this);
    }
    
    public static class RangeIterator implements Iterator {
        private int start;
        private int end;
        private int current;

        public RangeIterator(int start, int end) {
            this.start = start;
            this.end = end;
            this.current = start;
        }
        
        public boolean hasNext() {
            return current < end;
        }

        public Object next() {
            return current++;
        }

        public void remove() {
            throw new RuntimeException("Not Implemented!");
        }
    }
}
