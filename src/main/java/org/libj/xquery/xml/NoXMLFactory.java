package org.libj.xquery.xml;

public class NoXMLFactory implements XMLFactory {
    public XML toXML(String xml) {
        return new NoXML(xml);
    }

    public void registerNamespace(String prefix, String namespace) {
        throw new UnsupportedOperationException("registerNamespace");
    }
}
