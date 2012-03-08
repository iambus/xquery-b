package org.libj.xquery.xml.str;

import org.libj.xquery.xml.NilXML;
import org.libj.xquery.xml.XML;

import java.util.HashMap;
import java.util.Map;

public class StringNamespaceXML extends StringXML {
    private SharedNamespace xmlNamespaces;
    private Map<String, String> xpathNamespaces;
    public StringNamespaceXML(String xml, int start, SharedNamespace xmlNamespaces, Map<String, String> xpathNamespaces) {
        super(xml, start);
        this.xmlNamespaces = xmlNamespaces;
        this.xpathNamespaces = xpathNamespaces;
    }
    public StringNamespaceXML(String xml, SharedNamespace xmlNamespaces, Map<String, String> xpathNamespaces) {
        super(xml);
        this.xmlNamespaces = xmlNamespaces;
        this.xpathNamespaces = xpathNamespaces;
    }
    public StringNamespaceXML(String xml, Map<String, String> xpathNamespaces) {
        this(xml, null, xpathNamespaces);
    }
    public StringNamespaceXML(String xml) {
        this(xml, null);
    }
    public XML eval(String path) {
        if (xmlNamespaces == null) {
            int i = start + 1;
            while (isTagChar(xml.charAt(i)) || xml.charAt(i) == ':') {
                i++;
            }
            xmlNamespaces = parseNamespace(i);
        }
        StringNamespaceXML result = this;
//        String [] tags = path.substring(1).split("/");
//        for (String tag: tags) {
//            String prefix = null;
//            int c = tag.indexOf(':');
//            if (c != -1) {
//                prefix = tag.substring(0, c);
//                tag = tag.substring(c+1);
//            }
//            result = result.selectNode(prefix, tag);
//            if (result == null) {
//                return NilXML.NIL;
//            }
//        }
        StringBuilder builder = new StringBuilder();
        int i = 1;
        while (i < path.length()) {
            char c = path.charAt(i);
            while (c != ':' && c != '/') {
                builder.append(c);
                if (++i >= path.length()) {
                    break;
                }
                c = path.charAt(i);
            }
            i++;
            if (c == ':') {
                String prefix = builder.toString();
                builder.setLength(0);
                while (i < path.length() && path.charAt(i) != '/') {
                    builder.append(path.charAt(i++));
                }
                result = result.selectNode(prefix, builder.toString());
                if (result == null) {
                    return NilXML.NIL;
                }
                builder.setLength(0);
                i++;
            }
            else {
                result = result.selectNode(null, builder.toString());
                if (result == null) {
                    return NilXML.NIL;
                }
                builder.setLength(0);
            }
        }

        if (result == null) {
            return NilXML.NIL;
        }
        return result;
    }

    protected StringNamespaceXML selectNode(String prefix, String tag) {
        String namespace = null;
        if (prefix != null) {
            namespace = xpathNamespaces.get(prefix);
        }

        return getElementByTagNameNS(namespace, tag);
    }

    public Object getElementsByTagNameNS(String namespaceURI, String localName) {
        if (xmlNamespaces == null) {
            int i = start + 1;
            while (isTagChar(xml.charAt(i)) || xml.charAt(i) == ':') {
                i++;
            }
            xmlNamespaces = parseNamespace(i);
        }
        XML result = getElementByTagNameNS(namespaceURI, localName);
        if (result == null) {
            result = NilXML.NIL;
        }
        return result;
    }

    private StringNamespaceXML getElementByTagNameNS(String namespace, String tag) {
        int i = start;
        int max = xml.length();
        int tagmax = tag.length();
        // jump to the end of open tag
        while (xml.charAt(i++) != '>') {
        }
        if (xml.charAt(i-2) == '/' || i >= max) {
            // oh, no! there is no sub node!
            return null;
        }
        while (i < max) {
            // skip text node
            while (xml.charAt(i) != '<') {
                i++;
            }
            // now we are in <
            int result = i++;
            if (xml.charAt(i) == '/') {
                // we missed all sub nodes, no lucky.
                return null;
            }
            int t = 0; // trace current position of tag name
            while (isTagChar(xml.charAt(i+(++t)))) {
            }
            // we are now at : or end of tag name
            if (xml.charAt(i+t) == ':') {
                // this tag has prefix
                int prefixStart = i;
                int prefixEnd = i+t;
                i += t + 1;
                t = 0;
                while (t < tagmax && xml.charAt(i+t) == tag.charAt(t)) {
                    ++t;
                }
                i += t;
                if (t == tagmax && (Character.isWhitespace(xml.charAt(i)) || xml.charAt(i) == '>' || xml.charAt(i) == '/')) {
                    // tag name matched
                    // todo: parse namespaces
                    SharedNamespace newNamespace = parseNamespace(i);
                    String thisNamespace = newNamespace.get(xml.substring(prefixStart, prefixEnd));
                    if ((thisNamespace == null && namespace==null) || (thisNamespace != null && thisNamespace.equals(namespace))) {
                        return new StringNamespaceXML(xml, result, newNamespace, xpathNamespaces);
                    }
                    else {
                        // namespace wrong, continue...
                        i = skipNode(i);
                    }
                }
            }
            else {
                // tag without namespace
                if (t != tagmax) {
                    // tag name length is different, safely skip this node.
                    i = skipNode(i+t);
                }
                else if (!subEquals(tag, i, t)) {
                    // tag name is wrong, skip node
                    i = skipNode(i);
                }
                else {
                    i += t;
                    SharedNamespace newNamespace = parseNamespace(i);
                    if ((namespace == null && newNamespace.getDefaultNamespace() == null) || (namespace != null && namespace.equals(newNamespace.getDefaultNamespace()))) {
                        return new StringNamespaceXML(xml, result, newNamespace, xpathNamespaces);
                    }
                    else {
                        i = skipString(i);
                    }
                }
            }
        }
        return null;
    }

    public static boolean isTagChar(char c) {
        switch (c) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
            case ':':
            case '<':
            case '/':
            case '>':
            case '=':
                return false;
            default:
                return true;
        }
    }

    private SharedNamespace parseNamespace(int i) {
        SharedNamespace ns = null;
        while (true) {
            while (Character.isWhitespace(xml.charAt(i))) {
                i++;
            }
            if (xml.charAt(i) == '/' || xml.charAt(i) == '>') {
                if (ns == null) {
                    ns = xmlNamespaces;
                }
                if (ns == null) {
                    ns = SharedNamespace.NilNamespace;
                }
                return ns;
            }
            int t = 0;
            while (t < 5 && xml.charAt(i+t) == "xmlns".charAt(t)) {
                t++;
            }
            i += t;
            if (t == 5) {
                if (xml.charAt(i)=='=' || Character.isWhitespace(i)) {
                    // it's a default namespace
                    while (xml.charAt(i++) != '=') {
                    }
                    while (Character.isWhitespace(xml.charAt(i))) {
                        i++;
                    }
                    String defaultNamespace = readString(i);
                    i += defaultNamespace.length()+2;
                    if (ns == null) {
                        ns = new SharedNamespace(xmlNamespaces);
                    }
                    ns.setDefaultNamespace(defaultNamespace);
                    continue;
                }
                else if (xml.charAt(i)==':') {
                    // ns prefix define
                    int prefixStart = ++i;
                    while (isTagChar(xml.charAt(++i))) {
                    }
                    String prefix = xml.substring(prefixStart, i);
                    while (xml.charAt(i++) != '=') {
                    }
                    while (Character.isWhitespace(xml.charAt(i))) {
                        i++;
                    }
                    String namespace = readString(i);
                    i += namespace.length()+2;
                    if (ns == null) {
                        ns = new SharedNamespace(xmlNamespaces);
                    }
                    ns.put(prefix, namespace);
                    continue;
                }
                else {

                }
            }
            // attribute name doesn't starts with xmlns
            while (xml.charAt(i++) != '=') {
            }
            while (Character.isWhitespace(xml.charAt(i))) {
                i++;
            }
            i = skipString(i);
        }
        
    }

    private int skipString(int i) {
        char x = xml.charAt(i++);
        while (xml.charAt(i++) != x) {
        }
        return i;
    }

    private String readString(int i) {
        char x = xml.charAt(i++);
        String s = xml.substring(i, xml.indexOf(x, i));
        return s;
//        StringBuilder builder = new StringBuilder();
//        while (xml.charAt(i) != x) {
//            builder.append(xml.charAt(i++));
//        }
//        return builder.toString();
    }


    private boolean subEquals(String tag, int start, int len) {
        for (int i = 0; i < len; i++) {
            if (xml.charAt(start+i) != tag.charAt(i)) {
                return false;
            }
        }
        return true;
//        return xml.substring(start, start+len).equals(tag);
    }


    public static void main(String[] args) {
        String xml = "<ns:Event xmlns:ns=\"http://xquery.libj.org/examples/Event\">\n" +
                "  <ns:EventData>\n" +
                "    <ID>7</ID>\n" +
                "    <Value>3</Value>\n" +
                "  </ns:EventData>\n" +
                "</ns:Event>";
        Map<String, String> xpathNamespaces = new HashMap<String, String>();
        xpathNamespaces.put("ns", "http://xquery.libj.org/examples/Event");
        StringNamespaceXML x = new StringNamespaceXML(xml, xpathNamespaces);
        System.out.println(x.eval("/ns:EventData/ID"));
//        System.out.println(((StringNamespaceXML)x.eval("/ns:EventData/ID")).text());
//        System.out.println(((StringNamespaceXML)x.eval("/ns:EventData")).eval("ID"));
//        System.out.println(((StringNamespaceXML)x.eval("/ns:EventData/ID")).text());
    }
}
