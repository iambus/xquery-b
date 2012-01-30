package org.libj.xquery;

import org.junit.Test;

import static org.libj.xquery.Asserts.*;

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
}
