package org.libj.xquery.runtime;

import java.util.Iterator;

public interface List extends Iterable {
    public Iterator iterator();
    public Object nth(int i);
    public int size();
}
