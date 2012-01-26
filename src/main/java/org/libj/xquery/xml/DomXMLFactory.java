package org.libj.xquery.xml;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;

public class DomXMLFactory implements XMLFactory {

    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;
    private XPathFactory xpathFactory;
    private XPath xpath;

    public DomXMLFactory() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        xpathFactory = XPathFactory.newInstance();
        xpath = xpathFactory.newXPath();
    }

    public XML toXML(final String xml) {
        return new XML() {
            private Document doc;

            public Object eval(String path) {
                try {
                    if (doc == null) {
//                        doc = XMLUtils.doc(xml);
                        doc = documentBuilder.parse(new InputSource(new StringReader(xml)));
                    }
                    return xpath.evaluate(path, doc);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
