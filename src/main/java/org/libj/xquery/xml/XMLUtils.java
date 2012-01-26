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

public class XMLUtils {
    public static String escapeXML(String xml) {
        // TODO: this is incorrect and inefficient
        return xml.replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;");
    }

    public static Document doc(String xml) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document doc = null;
        try {
            doc = documentBuilder.parse(new InputSource(new StringReader(xml)));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return doc;
    }
    public static XPath newXPath() {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        return xpath;
    }
    public static String evalXPath(String path, Document doc) {
        XPath xpath = newXPath();
        try {
            return xpath.evaluate(path, doc);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }
}
