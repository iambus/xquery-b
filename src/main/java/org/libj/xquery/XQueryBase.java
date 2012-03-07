package org.libj.xquery;

import org.libj.xquery.runtime.CallbackList;
import org.libj.xquery.runtime.FlattenList;
import org.libj.xquery.runtime.List;
import org.libj.xquery.xml.XML;
import org.libj.xquery.xml.XMLFactory;
import org.libj.xquery.xml.str.StringNamespaceXMLFactory;

public abstract class XQueryBase implements XQuery {
    private XMLFactory xmlFactory;

    protected void initNamespaces() {
    }

    private void initFactory() {
        if (xmlFactory == null) {
            xmlFactory = new StringNamespaceXMLFactory();
            initNamespaces();
        }
    }
    protected XML toXML(String s) {
        initFactory();
        return xmlFactory.toXML(s);
    }

    protected void registerNamespace(String prefix, String uri) {
        initFactory();
        xmlFactory.registerNamespace(prefix, uri);
    }

    protected static List callbackToList(Callback callback)
    {
        if(callback == null) {
            return new FlattenList();
        }
        if(callback instanceof List) {
            return (List) callback;
        }
        return new CallbackList(callback);
    }

    private Object throwArity(int n){
        throw new IllegalArgumentException("Wrong number of vars (" + n + ") passed to eval");
    }

    public Object eval() {
        return throwArity(0);
    }

    public Object eval(Object x) {
        return throwArity(1);
    }

    public Object eval(Object x1, Object x2) {
        return throwArity(2);
    }

    public Object eval(Object x1, Object x2, Object x3) {
        return throwArity(3);
    }

    public Object eval(Object x1, Object x2, Object x3, Object x4) {
        return throwArity(4);
    }

    public Object eval(Object x1, Object x2, Object x3, Object x4, Object x5) {
        return throwArity(5);
    }

    public Object eval(Object x1, Object x2, Object x3, Object x4, Object x5, Object x6) {
        return throwArity(6);
    }

    public Object eval(Object x1, Object x2, Object x3, Object x4, Object x5, Object x6, Object x7) {
        return throwArity(7);
    }

    public Object eval(Object x1, Object x2, Object x3, Object x4, Object x5, Object x6, Object x7, Object x8) {
        return throwArity(8);
    }

    public Object eval(Object x1, Object x2, Object x3, Object x4, Object x5, Object x6, Object x7, Object x8, Object x9) {
        return throwArity(9);
    }

    public Object eval(Object x1, Object x2, Object x3, Object x4, Object x5, Object x6, Object x7, Object x8, Object x9, Object x10) {
        return throwArity(10);
    }

    public Object eval(Object... vars) {
        throw new RuntimeException("Not Implemented!");
    }

    public void eval(Callback callback) {
        throwArity(0);
    }

    public void eval(Callback callback, Object x) {
        throwArity(1);
    }

    public void eval(Callback callback, Object x1, Object x2) {
        throwArity(2);
    }

    public void eval(Callback callback, Object x1, Object x2, Object x3) {
        throwArity(3);
    }

    public void eval(Callback callback, Object x1, Object x2, Object x3, Object x4) {
        throwArity(4);
    }

    public void eval(Callback callback, Object x1, Object x2, Object x3, Object x4, Object x5) {
        throwArity(5);
    }

    public void eval(Callback callback, Object x1, Object x2, Object x3, Object x4, Object x5, Object x6) {
        throwArity(6);
    }

    public void eval(Callback callback, Object x1, Object x2, Object x3, Object x4, Object x5, Object x6, Object x7) {
        throwArity(7);
    }

    public void eval(Callback callback, Object x1, Object x2, Object x3, Object x4, Object x5, Object x6, Object x7, Object x8) {
        throwArity(8);
    }

    public void eval(Callback callback, Object x1, Object x2, Object x3, Object x4, Object x5, Object x6, Object x7, Object x8, Object x9) {
        throwArity(9);
    }

    public void eval(Callback callback, Object x1, Object x2, Object x3, Object x4, Object x5, Object x6, Object x7, Object x8, Object x9, Object x10) {
        throwArity(10);
    }

    public void eval(Callback callback, Object... vars) {
        throw new RuntimeException("Not Implemented!");
    }

}
