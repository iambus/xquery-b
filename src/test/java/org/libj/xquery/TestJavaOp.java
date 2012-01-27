package org.libj.xquery;

import org.junit.Test;

import static org.libj.xquery.Asserts.assertEval;
import static org.libj.xquery.Asserts.assertEvalRegex;
import static org.libj.xquery.Asserts.assertEvalString;

public class TestJavaOp {
    @Test
    public void testStaticFunctions() {
        assertEval("declare namespace Integer = \"class:java.lang.Integer\"; Integer:toHexString(255)", "ff");
        assertEval("class:java.lang.Integer:toHexString(255)", "ff");
        assertEval("java.lang.Integer:toHexString(255)", "ff");
    }

    @Test
    public void testNew() {
        assertEvalRegex("java.lang.Object:new()", "java\\.lang\\.Object@[\\da-f]+");
    }

    @Test
    public void testOverloadedNew() {
        assertEval("declare namespace Integer = \"class:java.lang.Integer\"; Integer:new(\"1234\")", 1234);
    }

    @Test
    public void testMethod() {
        assertEval("java.lang.Object:toString(3+4)", "7");
        assertEval("class:java.lang.Object:toString(3+4)", "7");
        assertEval("declare namespace o = \"class:java.lang.Object\"; o:toString(3+4)", "7");
        assertEval("declare namespace obj = \"class:java.lang.Object\";\n" +
                "obj:toString(3+4)", "7");
        assertEval("java.lang.Integer:equals(3, 4)", false);
    }
}
