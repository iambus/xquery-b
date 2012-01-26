package org.libj.xquery.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

public class DomSimpleXPathXMLFactory implements XMLFactory {
    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;

    public DomSimpleXPathXMLFactory() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public XML toXML(final String xml) {
        return new XML() {
            private Document doc;
            public Object eval(String path) {
                if (doc == null) {
//                        doc = XMLUtils.doc(xml);
                    try {
                        doc = documentBuilder.parse(new InputSource(new StringReader(xml)));
                    } catch (SAXException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                Node node = XPathUtils.evalSimpleXPathOnDom(path, doc);
                if (node != null) {
                    return node.getTextContent();
                }
                else {
                    return "";
                }
            }
            public String toString() {
                return xml;
            }
        };
    }
}
