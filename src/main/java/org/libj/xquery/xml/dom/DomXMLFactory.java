package org.libj.xquery.xml.dom;

import org.libj.xquery.xml.XML;
import org.libj.xquery.xml.XMLFactory;
import org.libj.xquery.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
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
        return new CachedDomXML(xml, documentBuilder, xpath);
    }

}
