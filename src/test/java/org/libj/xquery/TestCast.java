package org.libj.xquery;

import org.junit.Test;

import static org.libj.xquery.Asserts.*;
import static org.libj.xquery.Compile.eval;

public class TestCast {
    @Test
    public void testNumberCast() {
        assertEval("1 + 1.1", 2.1);
        assertEval("1 + 1", 2);
        assertEval("1.1 + 2", 3.1);
        assertEval("1 = 1.0", true);
        assertEval("1 = 2.1", false);
        assertEval("1.0 = 1", true);
        assertEval("2.1 = 1", false);
    }
    @Test
    public void testFunctionArgumentCast() {
    }
    @Test(expected=ClassCastException.class)
    public void testCastFailed() {
        eval("let $x := '1' return $x/a");
    }
}
