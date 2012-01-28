package org.libj.xquery.namespace;

import org.libj.xquery.lib.Debug;
import org.libj.xquery.lib.Fn;
import org.libj.xquery.runtime.Op;

public class DefaultRootNamespace extends RootNamespace {
    private JavaNamespace classNamespace = new JavaNamespace();
    public DefaultRootNamespace() {
        register("class", classNamespace);
        register("fn", new LibNamespace(Fn.class));
        register("op", new LibNamespace(Op.class));
        register("debug", new LibNamespace(Debug.class));
        importDefault("fn");
    }

    @Override
    public Symbol lookup(String name) {
        if (name.indexOf('.') != -1 && name.indexOf('.') < name.indexOf(':')) {
            int colon = name.indexOf(':');
            if (colon == -1) {
                throw new RuntimeException("Not Implemented!");
            }
            String className = name.substring(0, colon);
            String methodName = name.substring(colon+1);
            return ((Namespace) classNamespace.lookup(className)).lookup(methodName);
        }
        return super.lookup(name);
    }
}
