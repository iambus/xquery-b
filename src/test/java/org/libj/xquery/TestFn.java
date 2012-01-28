package org.libj.xquery;

import org.junit.Test;

import static org.libj.xquery.Asserts.*;

public class TestFn {
    @Test
    public void testString() {
        assertEval("string(1)", "1");
        assertEval("fn:string(1)", "1");
        assertEval("fn:string(<x>a</x>)", "a");
        assertEval("fn:concat(1,2,2,3)", "1223");
        assertEval("fn:substring('abc',1,2)", "ab");
    }
}
