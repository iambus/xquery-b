package org.libj.xquery;

import org.junit.Test;

import org.libj.xquery.compiler.Compiler;

public class TestCompiler {
    @Test
    public void testRecompile() {
        Compiler compiler = new Compiler();
        compiler.compile("declare namespace a = \"http://a\"; 2");
        compiler.compile("declare namespace a = \"http://a\"; 2");
    }
}
