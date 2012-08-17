package org.libj.xquery;

import org.junit.Test;

import static org.libj.xquery.Asserts.assertEvalString;

public class TestRegression {
    @Test
    public void popupPrimitiveAbsent() {
        assertEvalString("let $x := java.lang.Integer:longValue(1) where 1 = 2 return $x", "");
    }
}
