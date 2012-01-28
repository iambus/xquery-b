package org.libj.xquery.lib;

import org.libj.xquery.annotation.Function;
import org.libj.xquery.annotation.Namespace;

@Namespace(name="debug")
public class Debug {
    @Function
    public static void print(Object v) {
        System.out.println(v);
    }
}
