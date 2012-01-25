package org.libj.xquery.runtime;

import java.util.Iterator;

public class ListUtils {
    public static String toString(Iterable collection) {
        StringBuffer buffer = new StringBuffer();
        for (Object x: collection) {
            buffer.append(x);
        }
        return buffer.toString();
    }
}
