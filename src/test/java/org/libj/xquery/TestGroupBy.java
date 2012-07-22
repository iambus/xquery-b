package org.libj.xquery;

import org.junit.Test;

import static org.libj.xquery.Asserts.assertEval;
import static org.libj.xquery.Asserts.assertEvalString;

public class TestGroupBy {
    @Test
    public void testGroupBy() {
        assertEvalString("for $x in (1, 2, 3) group by $x return $x", "1 2 3");
        assertEvalString("for $x in (1, 2, 3) group by $x, $x return $x", "1 2 3");
    }
}
