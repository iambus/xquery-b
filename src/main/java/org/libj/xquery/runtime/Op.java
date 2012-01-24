package org.libj.xquery.runtime;

import java.util.ArrayList;
import java.util.List;

public class Op {
    public static List<Object> asList(Object x) {
        if (x instanceof List) {
            return (List<Object>) x;
        }
        else {
            List<Object> list = new ArrayList<Object>(1);
            list.add(1);
            return list;
        }
    }

    public static Object add(Object x, Object y) {
        if (x instanceof Integer && y instanceof Integer) {
            return (Integer) x + (Integer) y;
        }
        double dx = x instanceof Integer ? (Integer) x : (Double) x;
        double dy = y instanceof Integer ? (Integer) y : (Double) y;
        return dx + dy;
    }

    public static Object subtract(Object x, Object y) {
        if (x instanceof Integer && y instanceof Integer) {
            return (Integer) x - (Integer) y;
        }
        double dx = x instanceof Integer ? (Integer) x : (Double) x;
        double dy = y instanceof Integer ? (Integer) y : (Double) y;
        return dx - dy;
    }

    public static Object multiply(Object x, Object y) {
        if (x instanceof Integer && y instanceof Integer) {
            return (Integer) x * (Integer) y;
        }
        double dx = x instanceof Integer ? (Integer) x : (Double) x;
        double dy = y instanceof Integer ? (Integer) y : (Double) y;
        return dx * dy;
    }

    public static Object div(Object x, Object y) {
        if (x instanceof Integer && y instanceof Integer) {
            return (Integer) x / (Integer) y;
        }
        double dx = x instanceof Integer ? (Integer) x : (Double) x;
        double dy = y instanceof Integer ? (Integer) y : (Double) y;
        return dx / dy;
    }
    
    public static Object list(Object x, Object y) {
        int start = (Integer)x;
        int end = (Integer)y;
        List<Object> list = new ArrayList<Object>(end - start + 1);
        for (int i = start; i <= end; i++) {
            list.add(i);
        }
        return list;
    }
}
