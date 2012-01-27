package org.libj.xquery;

import org.junit.Test;

import static org.libj.xquery.Asserts.*;

public class TestFreeVariables {
    private Environment env = new Environment();
    public TestFreeVariables() {
        env.putVariable("i", 1);
        env.putVariable("j", 7);
        env.putVariable("s", "abc");
    }
    @Test
    public void testSimple() {
        assertEval("$i", 1, env);
        assertEval("$j", 7, env);
        assertEval("$i + 3", 4, env);
        assertEval("$i + 3 + $j", 11, env);
        assertEval("$s", "abc", env);
        assertEvalString("<x>{$s}</x>", "<x>abc</x>", env);
        assertEvalString("<x>{$i}{$j}</x>", "<x>17</x>", env);
    }
    @Test
    public void testOverride() {
        assertEvalString("for $i in 1 to 10 return $i + $j", "8 9 10 11 12 13 14 15 16 17", env);
    }
}