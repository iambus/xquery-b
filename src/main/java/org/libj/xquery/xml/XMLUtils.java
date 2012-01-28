package org.libj.xquery.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

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

    public static String xml(Node node) {
        TransformerFactory factory = TransformerFactory.newInstance();
        return xml(factory, node);
    }

    public static String xml(TransformerFactory transformerFactory, Node node) {
        Writer writer = new StringWriter();
        DOMSource source = new DOMSource(node);
        StreamResult result = new StreamResult(writer);
        try {
            transformerFactory.newTransformer().transform(source, result);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
        return writer.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
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
