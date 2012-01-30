package org.libj.xquery.xml.dom;

import org.libj.xquery.runtime.Nil;
import org.libj.xquery.xml.NilXML;
import org.libj.xquery.xml.XML;
import org.libj.xquery.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.StringReader;

public class CachedDomXML implements XML {

//    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;
//    private XPathFactory xpathFactory;
    private XPath xpath;

    private Document doc;
    private Element node;
    private final String xml;

    public CachedDomXML(String xml, DocumentBuilder documentBuilder, XPath xpath) {
        this.xml = xml;
        this.xpath = xpath;
        this.documentBuilder = documentBuilder;
    }

    public XML eval(String path) {
        initNode();
        Node result = null;
        try {
            result = (Node) xpath.evaluate('.' + path, node, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        if (result != null) {
            return new CachedDomXML(XMLUtils.xml(result), documentBuilder, xpath);
        } else {
            return NilXML.NIL;
        }
    }

    private void initNode() {
        if (doc == null) {
            try {
                doc = documentBuilder.parse(new InputSource(new StringReader(xml)));
            } catch (SAXException e1) {
                throw new RuntimeException(e1);
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            node = doc.getDocumentElement();
        }
    }

    public String text() {
        initNode();
        return node.getTextContent();
    }

    public String toString() {
        return xml;
    }
}
