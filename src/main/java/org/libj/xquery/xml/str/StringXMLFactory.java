package org.libj.xquery.xml.str;

import org.libj.xquery.xml.XML;
import org.libj.xquery.xml.XMLFactory;

public class StringXMLFactory implements XMLFactory {
    public XML toXML(String xml) {
        return new StringXML(xml);
    }
}
