package org.libj.xquery.namespace;

import java.util.LinkedList;
import java.util.List;

public class RootNamespace extends DictNamespace {

    public void register(String name, Symbol s) {
        if (name.indexOf(':') != -1) {
            throw new RuntimeException("Not Implemented!");
        }
        else {
            super.register(name, s);
        }
    }

    public Symbol lookup(String name) {
        String[] ns = name.split(":");
        Symbol s = super.lookup(ns[0]);
        if (s == null) {
            throw new NameException("Unresolved symbol "+ns[0]);
        }
        for (int i = 1; i < ns.length; i++) {
            if (s instanceof Namespace) {
                s = ((Namespace) s).lookup(ns[i]);
            }
            else if (s != null) {
                throw new NameException(name + " is not a namespace: "+s);
            }
            else {
                throw new NameException("Could not find namespace "+ns[i]+" of " +name);
            }
        }
        if (s == null) {
            throw new NameException("Unresolved symbol "+name);
        }
        return s;
    }

    public void importDefault(String ns) {
        importNamespace((Namespace) lookup(ns));
    }

}
