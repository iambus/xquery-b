package org.libj.xquery.xml;

public interface XMLFactory {
    public XML toXML(String xml);
    public void registerNamespace(String prefix, String namespace);
}
