package org.libj.xquery.performance;

import org.junit.Test;
import org.libj.xquery.Callback;
import org.libj.xquery.XQuery;

import static org.junit.Assert.assertTrue;
import static org.libj.xquery.Asserts.*;
import static org.libj.xquery.Asserts.number;
import static org.libj.xquery.Compile.compile;

public class TestCallbackPerformance {
    @Test
    public void testTrivial() {
        assertRepeatedEvalMillisCallback(" 1 ", 1000*1000*10, 1000);
        assertRepeatedEvalMillisCallback(" $w ", 1000*1000*10, 1000);
        assertRepeatedEvalMillisCallback(" let $x := $w return $x ", 1000*1000*10, 1000);
        assertRepeatedEvalMillisCallback(" for $x in $w return $x ", 1000*1000*10, 1000);
    }

    public static void assertRepeatedEvalMillisCallback(String script, int n, int targetMillis) {
        Callback callback = new Callback() {
            public void call(Object result) {
                // do nothing
            }
        };
        XQuery q = compile(script, "w");
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            q.eval(callback, 1);
        }
        long end = System.currentTimeMillis();
        long executionMillis = end - start;
        logTime(script, executionMillis, n);
        logSpeed(n, executionMillis);
        assertTrue(String.format("Timeout: %d > %d (loop %s) for script: " + breakLine(script), executionMillis, targetMillis, number(n)), executionMillis <= targetMillis);
    }
}
