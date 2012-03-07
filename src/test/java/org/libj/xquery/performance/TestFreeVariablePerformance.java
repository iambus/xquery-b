package org.libj.xquery.performance;


import org.junit.Test;
import org.libj.xquery.Environment;
import org.libj.xquery.XQuery;
import org.libj.xquery.compiler.*;

import static org.junit.Assert.assertTrue;
import static org.libj.xquery.Asserts.*;
import static org.libj.xquery.Compile.compile;

public class TestFreeVariablePerformance {
    private Environment env = new Environment();
    public TestFreeVariablePerformance() {
        env.putVariable("i", 1);
        env.putVariable("j", 7);
        env.putVariable("s", "abc");
    }

    @Test
    public void testLiteral() {
        assertRepeatedEvalPerSecond(" 1 ", 1000*1000*100, env);
    }
    @Test
    public void testSimpleExpressions() {
        assertRepeatedEvalPerSecond(" $i ", 1000*1000*10, env);
        assertRepeatedEvalPerSecond(" $i + $j ", 1000*1000*10, env);
        assertRepeatedEvalPerSecond(" for $i in 1 to 10 return $i + $j ", 1000*1000*1, env);
    }
    @Test
    public void testXML() {
        assertRepeatedEvalPerSecond(" <x>{$i}</x> ", 1000*1000*10, env);
    }

    @Test
    public void testDynamicVars() {
        assertRepeatedEvalMillis(" $i + $j ", 1000*1000*10, 1000, env);
    }
    @Test
    public void testStaticVars() {
        assertRepeatedEvalMillisVar2(" $i + $j ", 1000*1000*200, 1000, 1, 3);
    }

    public static void assertRepeatedEvalMillisVar2(String script, int n, int targetMillis, Object var1, Object var2) {
        XQuery q = compile(script, "i", "j");
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            q.eval(var1, var2);
        }
        long end = System.currentTimeMillis();
        long executionMillis = end - start;
        logTime(script, executionMillis, n);
        logSpeed(n, executionMillis);
        assertTrue(String.format("Timeout: %d > %d (loop %s) for script: "+breakLine(script), executionMillis, targetMillis, number(n)), executionMillis <= targetMillis);
    }

}
