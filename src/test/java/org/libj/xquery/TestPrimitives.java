package org.libj.xquery;

import org.junit.Test;

import static org.libj.xquery.Asserts.assertEval;
import static org.libj.xquery.Asserts.assertEvalString;

public class TestPrimitives {
    @Test
    public void testCast() {
        assertEval("java.lang.Integer:new(1) + java.lang.Integer:new(1)", 2);
//        assertEval("java.lang.Integer:new(1) + 1", 2);
    }
    @Test
    public void testLong() {
        assertEval("java.lang.Integer:longValue(1)", 1L);
        assertEvalString("let $x := java.lang.Integer:longValue(1) return $x", "1");
        assertEvalString("let $x := java.lang.Integer:longValue(1) return $x + 2", "3");
        assertEvalString("<x>{java.lang.Integer:longValue(1)}</x>", "<x>1</x>");
        assertEvalString("java.lang.Integer:longValue(1) > 0", "true");
    }
    @Test
    public void testNaN() {
        assertEvalString("fn:number('x')", "NaN");
        assertEvalString("fn:number('')", "NaN");
        assertEvalString("fn:string(fn:number('x'))", "NaN");
        assertEvalString("fn:string(fn:number(''))", "NaN");
    }
}
