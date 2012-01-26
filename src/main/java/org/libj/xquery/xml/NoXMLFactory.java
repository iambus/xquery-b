package org.libj.xquery.xml;

public class NoXMLFactory implements XMLFactory {
    public XML toXML(String xml) {
        return new NoXML(xml);
    }
}
