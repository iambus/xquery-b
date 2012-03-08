package org.libj.xquery.xml;

public class NilXML implements XML {
    public static final NilXML NIL = new NilXML();
    public XML eval(String path) {
        return this;
    }

    public Object getElementsByTagNameNS(String namespaceURI, String localName) {
        return this;
    }

    public String text() {
        return "";
    }

    public String toString() {
        return "";
    }
}
