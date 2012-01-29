package org.libj.xquery.compiler;

import org.libj.xquery.namespace.Namespace;
import org.libj.xquery.parser.AST;
import org.objectweb.asm.MethodVisitor;

public class TwoPassEvalAssembler {
    private String compiledClassName;
    private Namespace namespace;
    private MethodVisitor mv;

    public TwoPassEvalAssembler(MethodVisitor mv, String compiledClassName, Namespace namespace) {
        this.compiledClassName = compiledClassName;
        this.namespace = namespace;
        this.mv = mv;
    }

    public Class visit(AST ast) {
        new Walker(ast);
        throw new RuntimeException("You can ignore this...");
    }
}
