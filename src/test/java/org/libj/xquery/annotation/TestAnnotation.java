package org.libj.xquery.annotation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.libj.xquery.Asserts.assertEval;
import org.libj.xquery.compiler.Compiler;

public class TestAnnotation {
    @Test
    public void testString() {
        Compiler compiler = new Compiler();
        compiler.registerLib("my", MyLib.class);
        assertEquals(1L, compiler.eval("my:long(1)"));
    }
}
