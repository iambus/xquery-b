package org.libj.xquery.xml;

public class NoXML implements XML {
    private String xml;

    public NoXML(String xml) {
        this.xml = xml;
    }

    public Object eval(String path) {
        throw new UnsupportedOperationException("eval");
    }

    public String toString() {
        return xml;
    }
}
