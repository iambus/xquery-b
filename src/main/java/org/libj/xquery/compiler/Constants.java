package org.libj.xquery.compiler;

import org.libj.xquery.Callback;
import org.libj.xquery.Environment;
import org.libj.xquery.XQuery;
import org.libj.xquery.lib.Fn;
import org.libj.xquery.runtime.Nil;
import org.libj.xquery.runtime.Op;
import org.libj.xquery.runtime.RecursiveList;
import org.libj.xquery.xml.XML;
import org.libj.xquery.xml.XMLFactory;
import org.libj.xquery.xml.str.StringNamespaceXMLFactory;

public class Constants {
    public static final String QUERY_BASE = XQuery.class.getName().replace('.', '/');
//    public static final String QUERY_BASE = XQueryBase.class.getName().replace('.', '/');
    public static final String SUPER_CLASS = Object.class.getName().replace('.', '/');
//    public static final String SUPER_CLASS = XQueryBase.class.getName().replace('.', '/');

    public static final String QUERY_CALLBACK = Callback.class.getName().replace('.', '/');
//    public static final String QUERY_LIST = CallbackList.class.getName().replace('.', '/');
//    public static final String QUERY_LIST = ArrayList.class.getName().replace('.', '/');
    public static final String QUERY_LIST = RecursiveList.class.getName().replace('.', '/');
    public static final String ENVIRONMENT_CLASS = Environment.class.getName().replace('.', '/');
    public static final String NIL = Nil.class.getName().replace('.', '/');

    public static final String RUNTIME_OP = Op.class.getName().replace('.', '/');
    public static final String XML_FACTORY_INTERFACE = XMLFactory.class.getName().replace('.', '/');
//    public static final String XML_FACTORY_IMPLEMENTATION = DomXMLFactory.class.getName().replace('.', '/');
//    public static final String XML_FACTORY_IMPLEMENTATION = DomSimpleXPathXMLFactory.class.getName().replace('.', '/');
    public static final Class XML_INTERFACE_TYPE = XML.class;
    public static final String XML_INTERFACE = XML.class.getName().replace('.', '/');
    public static final String LIB_FN = Fn.class.getName().replace('.', '/');


//    public static final Class DEFAUL_XML_FACTORY_IMPLEMENTATION = StringXMLFactory.class;
    public static final Class DEFAUL_XML_FACTORY_IMPLEMENTATION = StringNamespaceXMLFactory.class;

    public static final Class LIST_CLASS_TYPE = org.libj.xquery.runtime.List.class;

    public static final int LOCAL_ENV_INDEX = 1;
    public static final int LOCAL_TEMP_INDEX = 2;
    public static final int LOCAL_VAR_START = 4;
}
