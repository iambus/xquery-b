package org.libj.xquery;

import org.junit.Test;

import org.libj.xquery.compiler.Compiler;

import static org.junit.Assert.assertEquals;
import static org.libj.xquery.Asserts.*;

public class TestFreeVariables {
    private Environment env = new Environment();
    public TestFreeVariables() {
        env.putVariable("i", 1);
        env.putVariable("j", 7);
        env.putVariable("s", "abc");
    }
    @Test
    public void testDynamicFreeVariables() {
        assertEval("$i", 1, env);
        assertEval("$j", 7, env);
        assertEval("$i + 3", 4, env);
        assertEval("$i + 3 + $j", 11, env);
        assertEval("$s", "abc", env);
        assertEvalString("<x>{$s}</x>", "<x>abc</x>", env);
        assertEvalString("<x>{$i}{$j}</x>", "<x>17</x>", env);
    }
    @Test
    public void testStaticVariables() {
        XQuery q = Compile.compile("$x + $y", "x", "y");
        assertEquals(q.eval(1, 3), 4);
        Environment env = new Environment();
        env.putVariable("x", 1);
        env.putVariable("y", 3);
        assertEquals(q.eval(env), 4);
    }
    @Test
    public void testExternalVariables() {
        XQuery q = Compile.compile("declare variable $x as xs:integer external; declare variable $y as xs:integer external; $x + $y", "x", "y");
        assertEquals(q.eval(1, 3), 4);
        Environment env = new Environment();
        env.putVariable("x", 1);
        env.putVariable("y", 3);
        assertEquals(q.eval(env), 4);
    }
    @Test
    public void testOverride() {
        assertEvalString("for $i in 1 to 10 return $i + $j", "8 9 10 11 12 13 14 15 16 17", env);
    }

    @Test
    public void enableFreeVaraibles() {
        Compiler compiler = new Compiler();
        compiler.compile("$x");
    }

    @Test
    public void disableFreeVaraibles() {
        Compiler compiler = new Compiler();
        compiler.enableFreeVariables(false);
        compiler.compile("$x", "x");
    }

    @Test(expected=RuntimeException.class)
    public void disableFreeVaraiblesAndReport() {
        Compiler compiler = new Compiler();
        compiler.enableFreeVariables(false);
        compiler.compile("$x", "x");
        compiler.compile("$x");
    }
}
