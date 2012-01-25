package org.libj.xquery.lib;

import org.libj.xquery.namespace.StandardStaticNamespace;
import org.libj.xquery.runtime.Op;

public class Fn extends StandardStaticNamespace {
    public Fn() {
        super(Fn.class.getName());
        registerStaticFunction("string", 1);
        registerStaticVarlistFunction("concat");
        registerStaticOverloadedFunction("substring", 2, 3);
        registerStaticFunction("string-join", "string_join", 2);
        registerStaticFunction("upper-case", "upper_case", 1);
        registerStaticFunction("lower-case", "lower_case", 1);
        registerStaticFunction("contains", "contains", 2);
        registerStaticFunction("starts-with", "starts_with", 2);
    }

    //////////////////////////////////////////////////
    /// string
    //////////////////////////////////////////////////

    public static Object string(Object v) {
        return v.toString();
    }

    public static Object substring(Object s, Object start) {
        return ((String)s).substring((Integer)start-1);
    }


    public static Object substring(Object s, Object start, Object length) {
        return ((String)s).substring((Integer)start-1, (Integer)start-1+(Integer)length);
    }

    public static Object concat(Object...args) {
        StringBuilder buffer = new StringBuilder();
        for (Object x: args) {
            buffer.append(x);
        }
        return buffer.toString();
    }
    
    public static Object string_join(Object x, Object y) {
        if (!Op.isList(x)) {
            return x.toString();
        }
        String separator = (String) y;
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

    public static Object upper_case(Object x) {
        return ((String)x).toUpperCase();
    }

    public static Object lower_case(Object x) {
        return ((String)x).toLowerCase();
    }

    public static Object contains(Object s, Object sub) {
        return ((String)s).contains((String) sub);
    }
    public static Object starts_with(Object s, Object sub) {
        return ((String)s).startsWith((String) sub);
    }
    public static Object ends_with(Object s, Object sub) {
        return ((String)s).endsWith((String) sub);
    }

    // fn:matches($input as xs:string?, $pattern as xs:string) as xs:boolean
    // fn:matches($input as xs:string?, $pattern as xs:string, $flags as xs:string) as xs:boolean
    // fn:replace($input as xs:string?, $pattern as xs:string, $replacement as xs:string) as xs:string
    // fn:replace($input as xs:string?, $pattern as xs:string, $replacement as xs:string, $flags as xs:string) as xs:string
    // fn:tokenize($input as xs:string?, $pattern as xs:string) as xs:string*
    // fn:tokenize($input as xs:string?, $pattern as xs:string, $flags as xs:string) as xs:string*


    //////////////////////////////////////////////////
    /// ...
    //////////////////////////////////////////////////
}
