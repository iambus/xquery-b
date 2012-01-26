package org.libj.xquery.runtime;

import org.libj.xquery.xml.XML;
import org.libj.xquery.xml.XMLUtils;

public class ListUtils {
    public static void toString(Object v, StringBuilder buffer) {
        if (v instanceof Iterable) {
            toString((Iterable)v, buffer);
        }
        else if (v instanceof XML) {
            buffer.append(v);
        }
        else {
            if (buffer.length() != 0 && buffer.charAt(buffer.length()-1) != '>') {
                buffer.append(' ');
            }
            if (v == null) {
                buffer.append("");
            }
            else {
                buffer.append(XMLUtils.escapeXML(v.toString()));
            }
        }
    }
    public static void toString(Iterable collection, StringBuilder buffer) {
        for (Object x: collection) {
            toString(x, buffer);
        }
        buffer.toString();
    }
    public static String toString(Object x) {
        StringBuilder buffer = new StringBuilder();
        toString(x, buffer);
        return buffer.toString();
    }


    public static int size(Object x) {
        if (x instanceof Iterable) {
            if (x instanceof List) {
                return ((List) x).size();
            }
            else {
                int total = 0;
                for (Object y: (Iterable)x) {
                    total += size(y);
                }
                return total;
            }
        }
        else {
            return 1;
        }
    }
}
