package org.libj.xquery.xml.str;

import org.libj.xquery.xml.NilXML;
import org.libj.xquery.xml.XML;
import org.libj.xquery.xml.XMLUtils;

public class StringXML implements XML {
    final protected String xml;
    final protected int start;
    private int end = -1;
    private String nodeXML;
    private String text;

    public StringXML(String xml, int start) {
        this.xml = xml;
        this.start = start;
    }

    public StringXML(String xml) {
        this(xml, 0);
        end = xml.length();
    }
    public XML eval(String path) {
        String [] tags = path.substring(1).split("/");
        int i = start;
        for (String tag: tags) {
            i = selectNode(i, tag);
            if (i < 0) {
                return NilXML.NIL;
            }
        }
        if (i < 0) {
            return NilXML.NIL;
        }
        return new StringXML(xml, i);
    }

    public Object getElementsByTagNameNS(String namespaceURI, String localName) {
        return eval("/"+localName);
    }

    public String text() {
        if (text == null) {
//            text = XMLUtils.text(toString());
            text = nextTextNode();
        }
        return text;
    }

    protected int selectNode(int i, String subNode) {
        int max = xml.length();
        int tagmax = subNode.length();
        // jump to the end of open tag
        while (xml.charAt(i++) != '>') {
        }
        if (xml.charAt(i-2) == '/' || i >= max) {
            // oh, no! there is no sub node!
            return -1;
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
                return -1;
            }
            int t = 0; // trace current position of tag name
            while (t < tagmax && xml.charAt(i+t) == subNode.charAt(t)) {
                ++t;
            }
            i += t;
            if (t == tagmax && (Character.isWhitespace(xml.charAt(i)) || xml.charAt(i) == '>' || xml.charAt(i) == '/')) {
                return result;
            }
            i = skipNode(i);
        }
        return -1;
    }

    protected int skipNode(int i) {
        // jump to the end of open tag
        while (xml.charAt(i++) != '>') {
        }
        // if there is no sub node, we are done!
        if (xml.charAt(i-2) == '/') {
            return i;
        }
        while (true) {
            // skip text node
            while (xml.charAt(i) != '<') {
                i++;
            }
            // now we are in <
            if (xml.charAt(++i) == '/') {
                // we find a closing tag, navigate to the end of this tag
                while (xml.charAt(i++) != '>') {
                }
                // and return the position of next node
                return i;
            }
            // it's not a closing tag, so it must be a nested node, the recursion!
            i = skipNode(i);
        }
    }

    public String nextTextNode() {
//        int x = xml.indexOf('>') + 1;
        // jump to the end of open tag
        int i = start;
        while (xml.charAt(++i) != '>') {
        }
        StringBuilder builder = new StringBuilder();
        while (xml.charAt(++i) != '<') {
            char c = xml.charAt(i);
            if (c == '&') {
                switch (xml.charAt(++i)) {
                    case 'a':
                        switch (xml.charAt(++i)) {
                            case 'm':
                                if (xml.charAt(++i) != 'p' || xml.charAt(++i) != ';') {
                                    throw new RuntimeException("Incorrect XML escaping!");
                                }
                                builder.append('&');
                                break;
                            case 'p':
                                if (xml.charAt(++i) != 'o' || xml.charAt(++i) != 's' || xml.charAt(++i) != ';') {
                                    throw new RuntimeException("Incorrect XML escaping!");
                                }
                                builder.append('\'');
                                break;
                            default:
                                throw new RuntimeException("Incorrect XML escaping!");
                        }
                        break;
                    case 'l':
                        if (xml.charAt(++i) != 't' || xml.charAt(++i) != ';') {
                            throw new RuntimeException("Incorrect XML escaping!");
                        }
                        builder.append('<');
                        break;
                    case 'g':
                        if (xml.charAt(++i) != 't' || xml.charAt(++i) != ';') {
                            throw new RuntimeException("Incorrect XML escaping!");
                        }
                        builder.append('>');
                        break;
                    case 'q':
                        if (xml.charAt(++i) != 'u' || xml.charAt(++i) != 'o' || xml.charAt(++i) != 't' || xml.charAt(++i) != ';') {
                            throw new RuntimeException("Incorrect XML escaping!");
                        }
                        builder.append('"');
                        break;
                    default:
                        throw new RuntimeException("Incorrect XML escaping!");
                }
            }
            else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    public String toString() {
        if (nodeXML == null) {
            if (end < 0) {
                end = skipNode(start);
            }
            nodeXML = xml.substring(start, end);
        }
        return nodeXML;
    }
    public static void main(String[] args) {
        XML x = new StringXML("<x><a>2</a></x>");
//        System.out.println(x.eval("/a"));
//        System.out.println(x.eval("/x/a"));
//        System.out.println(x.eval("/a/b"));
//        XML x = new StringXML("<x><w/><a>2</a></x>");
//        System.out.println(x.eval("/a"));
        System.out.println(((XML)x.getElementsByTagNameNS(null, "a")).text());
    }
}
