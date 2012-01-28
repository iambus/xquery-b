package org.libj.xquery.xml;

import org.libj.xquery.runtime.Nil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import java.io.IOException;
import java.io.StringReader;

public class DomSimpleXPathXMLFactory implements XMLFactory {
    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;
    private TransformerFactory transformerFactory;

    public DomSimpleXPathXMLFactory() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        transformerFactory = TransformerFactory.newInstance();
    }

    public XML toXML(final String xml) {
        return new DomSimpleXPathXML(xml);
    }

    private class DomSimpleXPathXML implements XML {
        public Node node;
        private String xml;

        public DomSimpleXPathXML(String xml) {
            this.xml = xml;
        }
        public DomSimpleXPathXML(Node node) {
            this.node = node;
        }

        public Object eval(String path) {
            initNode();
            Node node = XPathUtils.evalSimpleXPathOnDom(path, this.node);
            if (node != null) {
                return new DomSimpleXPathXML(node);
            }
            else {
                return Nil.NIL;
            }
        }

        private void initNode() {
            if (node == null) {
//                        doc = XMLUtils.doc(xml);
                try {
                    Document doc = documentBuilder.parse(new InputSource(new StringReader(xml)));
                    node = doc.getDocumentElement();
                } catch (SAXException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public String text() {
            initNode();
            return node.getTextContent();
        }

        public String toString() {
            if (xml == null ){
                xml = XMLUtils.xml(transformerFactory, node);
            }
            return xml;
        }
    }
}
