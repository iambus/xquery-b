package org.libj.xquery.xml;

import com.sun.org.apache.xpath.internal.NodeSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XPathUtils {
    public static String[] parseSimpleEnoughXPath(String xpath) {
        String[] tags = xpath.substring(1).split("/");
        for (String t: tags) {

        }
        return tags;
    }
    public static Node evalSimpleXPathOnDom(String xpath, Document doc) {
        if (xpath.charAt(0) != '/') {
            throw new RuntimeException("Not Implemented!");
        }
        String[] tags = xpath.substring(1).split("/");
        Node node = doc.getDocumentElement();
        if (!((Element) node).getTagName().equals(tags[0])) {
            return null;
        }
        for (int i = 1; i < tags.length; i++) {
            String tag = tags[i];
            node = selectNode(node, tag);
            if (node == null) {
                return null;
            }
        }
        return node;
    }
    public static Node selectNode(Node node, String tag) {
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n instanceof Element && ((Element) n).getTagName().equals(tag)) {
                return n;
            }
        }
        return null;
    }
}
