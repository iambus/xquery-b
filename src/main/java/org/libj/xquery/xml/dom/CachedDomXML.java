package org.libj.xquery.xml.dom;

import org.libj.xquery.runtime.Nil;
import org.libj.xquery.xml.XML;
import org.libj.xquery.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
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

    public Object eval(String path) {
        try {
            if (doc == null) {
//                        doc = XMLUtils.doc(xml);
                doc = documentBuilder.parse(new InputSource(new StringReader(xml)));
                node = doc.getDocumentElement();
            }
//                return xpath.evaluate('.'+path, node, XPathConstants.NODE);
            Node result = (Node) xpath.evaluate('.' + path, node, XPathConstants.NODE);
            if (result != null) {
                return new CachedDomXML(XMLUtils.xml(result), documentBuilder, xpath);
            }
            else {
                return Nil.NIL;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        return xml;
    }
}
