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
    public static Node evalSimpleXPathOnDom(String xpath, Node node) {
        if (xpath.charAt(0) != '/') {
            throw new RuntimeException("Not Implemented!");
        }
        String[] tags = xpath.substring(1).split("/");
        for (int i = 0; i < tags.length; i++) {
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
