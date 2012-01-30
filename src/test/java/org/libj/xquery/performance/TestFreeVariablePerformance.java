package org.libj.xquery.performance;


import org.junit.Test;
import org.libj.xquery.Environment;

import static org.libj.xquery.Asserts.*;

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
}
