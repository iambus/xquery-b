package org.libj.xquery.xml;

public interface XML {
    XML eval(String path);
    Object getElementsByTagNameNS(String namespaceURI, String localName);
    String text();
}
