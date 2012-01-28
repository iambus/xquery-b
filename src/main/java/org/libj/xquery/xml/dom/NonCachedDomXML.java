package org.libj.xquery.xml.dom;

import org.libj.xquery.xml.XML;
import org.libj.xquery.xml.XMLUtils;
import org.w3c.dom.Document;

public class NonCachedDomXML implements XML {
    private String xml;
    private Document doc;

    public NonCachedDomXML(String xml) {
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

    public String text() {
        return XMLUtils.text(xml);
    }
}
