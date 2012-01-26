package org.libj.xquery.xml;

import org.w3c.dom.Document;

public class DomXML implements XML {
    private String xml;
    private Document doc;

    public DomXML(String xml) {
        this.xml = xml;
    }
    public String toString() {
        return xml;
    }
    public Object eval(String path) {
        if (doc == null) {
            doc = XMLUtils.doc(xml);
        }
        return XMLUtils.evalXPath(path, doc);
    }
}
