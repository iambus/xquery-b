package org.libj.xquery.xml.str;

import org.libj.xquery.xml.XML;
import org.libj.xquery.xml.XMLFactory;

import java.util.HashMap;
import java.util.Map;

public class StringNamespaceXMLFactory implements XMLFactory {
    private Map<String, String> namespaces = new HashMap<String, String>();
    public XML toXML(String xml) {
        return new StringNamespaceXML(xml, namespaces);
    }

    public void registerNamespace(String prefix, String namespace) {
        namespaces.put(prefix, namespace);
    }
}
