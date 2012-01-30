package org.libj.xquery.xml;

public class NoXML implements XML {
    private String xml;

    public NoXML(String xml) {
        this.xml = xml;
    }

    public XML eval(String path) {
        throw new UnsupportedOperationException("eval");
    }

    public String text() {
        return XMLUtils.text(xml);
    }

    public String toString() {
        return xml;
    }
}
