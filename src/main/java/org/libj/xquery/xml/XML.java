package org.libj.xquery.xml;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class XML {
    private String xml;
    private Document doc;

    public XML(String xml) {
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
