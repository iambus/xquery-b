package org.libj.xquery.performance;


import org.junit.Test;
import org.libj.xquery.Environment;

import static org.libj.xquery.Asserts.*;

public class TestFreeVariablePerformance {
    @Test
    public void testLiteral() {
        Environment env = new Environment();
        env.putVariable("i", 1);
        env.putVariable("j", 7);
        env.putVariable("s", "abc");
        assertRepeatedEvalPerSecond(" 1 ", 1000*1000*100, env);
        assertRepeatedEvalPerSecond(" $i ", 1000*1000*10, env);
        assertRepeatedEvalPerSecond(" $i + $j ", 1000*1000*10, env);
        assertRepeatedEvalPerSecond(" for $i in 1 to 10 return $i + $j ", 1000*1000*1, env);
    }
}
