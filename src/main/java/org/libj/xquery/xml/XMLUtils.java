package org.libj.xquery.xml;

public class XMLUtils {
    public static String escapeXML(String xml) {
        // TODO: this is incorrect and inefficient
        return xml.replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;");
    }
}
