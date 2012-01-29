package org.libj.xquery.xml.str;

import java.util.HashMap;
import java.util.Map;

public class SharedNamespace {
    public static final SharedNamespace NilNamespace = new SharedNamespace();

    private SharedNamespace parent;
    private String defaultNamespace;
    private Map<String, String> namespaces;

    public SharedNamespace(){
    }

    public SharedNamespace(SharedNamespace parent, Map<String, String> namespaces, String defaultNamespace) {
        this.parent = parent;
        this.namespaces = namespaces;
        this.defaultNamespace = defaultNamespace;
    }

    public SharedNamespace(SharedNamespace parent) {
        this.parent = parent;
    }

    public void put(String prefix, String ns) {
        if (namespaces == null) {
            namespaces = new HashMap<String, String>();
        }
        namespaces.put(prefix, ns);
    }

    public String get(String prefix) {
        String ns = null;
        if (namespaces != null) {
            ns = namespaces.get(prefix);
        }
        if (ns != null) {
            return ns;
        }
        if (parent != null) {
            return parent.get(prefix);
        }
        return null;
    }

    public String getDefaultNamespace() {
        if (defaultNamespace != null) {
            return defaultNamespace;
        }
        if (parent != null) {
            return parent.getDefaultNamespace();
        }
        return null;
    }

    public void setDefaultNamespace(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

}
