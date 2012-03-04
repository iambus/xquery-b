package org.libj.xquery.compiler;

import org.libj.xquery.Callback;
import org.libj.xquery.Environment;
import org.libj.xquery.XQuery;
import org.libj.xquery.lib.Fn;
import org.libj.xquery.runtime.*;
import org.libj.xquery.xml.XML;
import org.libj.xquery.xml.XMLFactory;
import org.libj.xquery.xml.str.StringNamespaceXMLFactory;

public class Constants {
    public static String classToSignature(Class c) {
        return c.getName().replace('.', '/');
    }

    public static final String QUERY_BASE_CLASS = classToSignature(XQuery.class);
//    public static final String QUERY_BASE_CLASS = classToSignature(XQueryBase.class);
    public static final String SUPER_CLASS = classToSignature(Object.class);
//    public static final String SUPER_CLASS = classToSignature(XQueryBase.class);

    public static final Class LIST_INTERFACE_CLASS = List.class;
    public static final String LIST_INTERFACE = classToSignature(LIST_INTERFACE_CLASS);
    public static final String MUTABLE_LIST = classToSignature(MutableList.class);

    public static final String CALLBACK = classToSignature(Callback.class);
    public static final String CALLBACK_LIST = classToSignature(CallbackList.class);
//    public static final String LIST_IMPLEMENTATION = classToSignature(ArrayList.class);
//    public static final String LIST_IMPLEMENTATION = classToSignature(RecursiveList.class);
    public static final String LIST_IMPLEMENTATION = classToSignature(FlattenList.class);
    public static final String ENVIRONMENT = classToSignature(Environment.class);
    public static final String NIL = classToSignature(Nil.class);

    public static final String OP = classToSignature(Op.class);
    public static final String FN = classToSignature(Fn.class);
    public static final String XML_FACTORY = classToSignature(XMLFactory.class);
    public static final Class XML_INTERFACE_Class = XML.class;
    public static final String XML_INTERFACE = classToSignature(XML.class);
//    public static final String DEFAUL_XML_FACTORY_IMPLEMENTATION = classToSignature(DomXMLFactory.class);
//    public static final String DEFAUL_XML_FACTORY_IMPLEMENTATION = classToSignature(DomSimpleXPathXMLFactory.class);
//    public static final Class DEFAUL_XML_FACTORY_IMPLEMENTATION_CLASS = StringXMLFactory.class;
    public static final Class DEFAUL_XML_FACTORY_IMPLEMENTATION_CLASS = StringNamespaceXMLFactory.class;

    public static final int LOCAL_ENV_INDEX = 1;
    public static final int LOCAL_CALLBACK_INDEX = 2;
    public static final int LOCAL_TEMP_INDEX = 3;
    public static final int LOCAL_VAR_START = 5;
}
