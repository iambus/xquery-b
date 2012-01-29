package org.libj.xquery;

import org.libj.xquery.xml.XML;
import org.libj.xquery.xml.XMLFactory;
import org.libj.xquery.xml.str.StringNamespaceXMLFactory;

public abstract class XQueryBase implements XQuery {
    private XMLFactory xmlFactory;

    protected void initNamespaces() {
    }

    private void initFactory() {
        if (xmlFactory == null) {
            xmlFactory = new StringNamespaceXMLFactory();
            initNamespaces();
        }
    }
    protected XML toXML(String s) {
        initFactory();
        return xmlFactory.toXML(s);
    }

    protected void registerNamespace(String prefix, String uri) {
        initFactory();
        xmlFactory.registerNamespace(prefix, uri);
    }
}
