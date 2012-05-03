package org.libj.xquery;

import org.junit.Test;

import static org.libj.xquery.Asserts.assertEval;

public class TestDeclare {
    @Test
    public void testDefaultFactory() {
        assertEval("declare option x 2; 1", 1);
    }
}
