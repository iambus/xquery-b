package org.libj.xquery.runtime;

import org.libj.xquery.annotation.Function;
import org.libj.xquery.annotation.Namespace;
import org.libj.xquery.xml.XML;

import java.util.ArrayList;

@Namespace(name="op")
public class Op {
    //////////////////////////////////////////////////
    /// ugly names for non-export APIs, for Java code
    //////////////////////////////////////////////////
    public static final Object NIL = null;

    public static boolean isList(Object x) {
        return x instanceof Iterable;
    }

    public static Iterable<Object> asList(Object x) {
        if (x instanceof Iterable) {
            return (Iterable<Object>) x;
        }
        else {
            return new SingletonList(x);
//            java.util.List<Object> list = new ArrayList<Object>(1);
//            list.add(x);
//            return list;
        }
    }

    public static boolean asBool(Object x) {
        if (x instanceof Boolean) {
            return (Boolean)x;
        }
        else if (x instanceof Integer) {
            return (Integer)x != 0;
        }
        else if (x instanceof String) {
            return ((String) x).isEmpty();
        }
        throw new RuntimeException("Not Implemented!");
    }

    public static int sizeOf(Object x) {
        if (x instanceof java.util.List) {
            return ((java.util.List) x).size();
        }
        else if (x instanceof List) {
            return ((List) x).size();
        }
        else {
            return 1;
        }
    }

    public static Object elementAt(Object x, int index) {
        if (x instanceof java.util.List) {
            java.util.List<Object> list = (java.util.List<Object>) x;
            if (1 <= index && index <= list.size()) {
                return list.get(index - 1);
            }
            else {
                return Op.NIL;
            }
        }
        else if (x instanceof List) {
            List list = (List) x;
            return list.nth(index - 1);
        }
        else {
            if (index == 1) {
                return x;
            }
            else {
                return Op.NIL;
            }
        }
    }
    //////////////////////////////////////////////////
    /// Below APIs are exported to namespace op:*
    //////////////////////////////////////////////////

    @Function
    public static Object list(Object x) {
        return asList(x);
    }

    @Function
    public static Object add(Object x, Object y) {
        if (x instanceof Integer && y instanceof Integer) {
            return (Integer) x + (Integer) y;
        }
        double dx = x instanceof Integer ? (Integer) x : (Double) x;
        double dy = y instanceof Integer ? (Integer) y : (Double) y;
        return dx + dy;
    }

    @Function
    public static Object subtract(Object x, Object y) {
        if (x instanceof Integer && y instanceof Integer) {
            return (Integer) x - (Integer) y;
        }
        double dx = x instanceof Integer ? (Integer) x : (Double) x;
        double dy = y instanceof Integer ? (Integer) y : (Double) y;
        return dx - dy;
    }

    @Function
    public static Object multiply(Object x, Object y) {
        if (x instanceof Integer && y instanceof Integer) {
            return (Integer) x * (Integer) y;
        }
        double dx = x instanceof Integer ? (Integer) x : (Double) x;
        double dy = y instanceof Integer ? (Integer) y : (Double) y;
        return dx * dy;
    }

    @Function
    public static Object div(Object x, Object y) {
        if (x instanceof Integer && y instanceof Integer) {
            return (Integer) x / (Integer) y;
        }
        double dx = x instanceof Integer ? (Integer) x : (Double) x;
        double dy = y instanceof Integer ? (Integer) y : (Double) y;
        return dx / dy;
    }

    @Function
    public static Object negative(Object x) {
        Number n = (Number) x;
        if (n instanceof Integer) {
            return -(Integer)n;
        }
        else if (n instanceof Double) {
            return -(Double)n;
        }
        else {
            throw new RuntimeException("Not Implemented!");
        }
    }

    @Function
    public static Object mod(Object x, Object y) {
        if (x instanceof Integer && y instanceof Integer) {
            return (Integer) x % (Integer) y;
        }
        double dx = x instanceof Integer ? (Integer) x : (Double) x;
        double dy = y instanceof Integer ? (Integer) y : (Double) y;
        return dx % dy;
    }

    @Function
    public static boolean eq(Object x, Object y) {
        if (x instanceof XML && y instanceof XML) {
            return ((XML) x).text().equals(((XML) y).text());
        }
        if (x instanceof XML) {
            // XXX: this might be incorrect when comparing xml with int
            // e.g. <x>1.0</x> = 1 should return true
            return ((XML) x).text().equals(y.toString());
        }
        if (y instanceof XML) {
            // XXX: this might be incorrect when comparing xml with int
            return x.toString().equals(((XML) y).text());
        }
        if (x instanceof List || y instanceof List) {
            throw new RuntimeException("Not Implemented!");
        }
        return x.equals(y);
    }

    @Function
    public static boolean ne(Object x, Object y) {
        return !eq(x, y);
    }

    @Function
    public static boolean lt(Object x, Object y) {
        throw new RuntimeException("Not Implemented!");
    }

    @Function
    public static boolean le(Object x, Object y) {
        return !lt(y, x);
    }

    @Function
    public static boolean gt(Object x, Object y) {
        return lt(y, x);
    }

    @Function
    public static boolean ge(Object x, Object y) {
        return !lt(x, y);
    }

    @Function
    public static boolean and(Object x, Object y) {
        return asBool(x) && asBool(y);
    }

    @Function
    public static boolean or(Object x, Object y) {
        return asBool(x) || asBool(y);
    }

    @Function
    public static Range to(int x, int y) {
        int start = x;
        int end = y;
        return new Range(start, end+1);
    }

    @Function
    public static Object at(Object x, int i) {
        int index = i;
        return elementAt(x, index);
    }

    @Function
    public static XML xpath(XML x, String y) {
        return x.eval(y);
    }
}
