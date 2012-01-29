package org.libj.xquery.runtime;

import org.libj.xquery.annotation.Function;
import org.libj.xquery.annotation.Namespace;
import org.libj.xquery.namespace.StandardStaticNamespace;
import org.libj.xquery.xml.XML;

import java.util.ArrayList;

@Namespace(name="op")
public class Op extends StandardStaticNamespace {
    public Op() {
        super(Op.class.getName());
        registerStaticFunction("list", 1);
        registerStaticFunction("add", 2);
        registerStaticFunction("subtract", 2);
        registerStaticFunction("multiply", 2);
        registerStaticFunction("div", 2);
        registerStaticFunction("negative", 1);
        registerStaticFunction("mod", 2);
        registerStaticFunction("eq", 2);
        registerStaticFunction("ne", 2);
        registerStaticFunction("and", 2);
        registerStaticFunction("or", 2);
        registerStaticFunction("to", 2);
        registerStaticFunction("at", 2);
        registerStaticFunction("xpath", 2);
    }

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
            java.util.List<Object> list = new ArrayList<Object>(1);
            list.add(x);
            return list;
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
    public static Object eq(Object x, Object y) {
        if (x instanceof XML && y instanceof XML) {
            return ((XML) x).text().equals(((XML) y).text());
        }
        if (x instanceof XML) {
            return ((XML) x).text().equals(y.toString());
        }
        if (y instanceof XML) {
            return x.toString().equals(((XML) y).text());
        }
        if (x instanceof List || y instanceof List) {
            throw new RuntimeException("Not Implemented!");
        }
        return x.equals(y);
    }

    @Function
    public static Object ne(Object x, Object y) {
        return !(Boolean)eq(x, y);
    }

    @Function
    public static Object and(Object x, Object y) {
        return asBool(x) && asBool(y);
    }

    @Function
    public static Object or(Object x, Object y) {
        return asBool(x) || asBool(y);
    }

    @Function
    public static Object to(Object x, Object y) {
        int start = (Integer)x;
        int end = (Integer)y;
        return new Range(start, end+1);
    }

    @Function
    public static Object at(Object x, Object i) {
        int index = (Integer) i;
        return elementAt(x, index);
    }

    @Function
    public static Object xpath(Object x, Object y) {
        return ((XML)x).eval((String) y);
    }
}
