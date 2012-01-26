package org.libj.xquery;

import org.junit.Test;

import static org.libj.xquery.Asserts.*;

public class TestNamespace {
    @Test
    public void testUnused() {
        assertEval("declare namespace Integer = \"http://xxx\"; 1 + 2 + 3", 6);
    }
}
