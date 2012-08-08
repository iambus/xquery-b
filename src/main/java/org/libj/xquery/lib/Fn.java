package org.libj.xquery.lib;

import org.libj.xquery.annotation.Function;
import org.libj.xquery.annotation.Namespace;
import org.libj.xquery.runtime.Op;
import org.libj.xquery.xml.XML;

@Namespace(name="fn")
public class Fn {
    //////////////////////////////////////////////////
    /// string
    //////////////////////////////////////////////////

    @Function
    public static String string(Object v) {
        if (v instanceof XML) {
            return ((XML) v).text();
        }
        else if (v == null) {
            return "";
        }
        else {
            return v.toString();
        }
    }

    @Function
    public static String string(double v) {
        return Double.toString(v);
    }

    @Function
    public static String string(int v) {
        return Integer.toString(v);
    }

    @Function
    public static String string(long v) {
        return Long.toString(v);
    }

    @Function
    public static String string(String v) {
        return v;
    }

    @Function
    public static String string(XML v) {
        return v == null ? "" : v.text();
    }

    @Function
    public static String substring(String s, int start) {
        return s.substring(start - 1);
    }

    @Function
    public static String substring(String s, int start, int length) {
        return s.substring(start - 1, start - 1 + length);
    }

    @Function
    public static String concat(Object...args) {
        StringBuilder buffer = new StringBuilder();
        for (Object x: args) {
            buffer.append(x);
        }
        return buffer.toString();
    }

    @Function(name="string-join")
    public static String string_join(Object x, String y) {
        if (!Op.isList(x)) {
            return x.toString();
        }
        String separator = y;
        StringBuilder buffer = new StringBuilder();
        boolean first = true;
        for (Object v: Op.asList(x))
        {
            if (first) {
                first = false;
            }
            else {
                buffer.append(separator);
            }
            buffer.append(v);
        }
        return buffer.toString();
    }

    @Function(name="upper-cast")
    public static String upper_case(String x) {
        return x.toUpperCase();
    }

    @Function(name="upper-cast")
    public static String lower_case(String x) {
        return x.toLowerCase();
    }

    @Function
    public static boolean contains(String s, String sub) {
        return s.contains(sub);
    }
    @Function(name="starts-with")
    public static boolean starts_with(String s, String sub) {
        return s.startsWith(sub);
    }
    @Function(name="ends-with")
    public static boolean ends_with(String s, String sub) {
        return s.endsWith(sub);
    }

    // fn:matches($input as xs:string?, $pattern as xs:string) as xs:boolean
    // fn:matches($input as xs:string?, $pattern as xs:string, $flags as xs:string) as xs:boolean
    // fn:replace($input as xs:string?, $pattern as xs:string, $replacement as xs:string) as xs:string
    // fn:replace($input as xs:string?, $pattern as xs:string, $replacement as xs:string, $flags as xs:string) as xs:string
    // fn:tokenize($input as xs:string?, $pattern as xs:string) as xs:string*
    // fn:tokenize($input as xs:string?, $pattern as xs:string, $flags as xs:string) as xs:string*


    //////////////////////////////////////////////////
    /// number
    //////////////////////////////////////////////////

    @Function
    public static double number(Object v) {
        if (v instanceof XML) {
            try {
                return Double.parseDouble(((XML) v).text());
            } catch (NumberFormatException e) {
                return Double.NaN;
            }
        }
        else if (v instanceof String) {
            try {
                return Double.parseDouble((String) v);
            } catch (NumberFormatException e) {
                return Double.NaN;
            }
        }
        else if (v instanceof Double) {
            return (Double)v;
        }
        else if (v instanceof Integer) {
            return (Integer)v;
        }
        else {
            throw new RuntimeException("Can't cast "+v+" to number");
        }
    }

    //////////////////////////////////////////////////
    /// ...
    //////////////////////////////////////////////////
}
