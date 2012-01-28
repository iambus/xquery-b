package org.libj.xquery;

import java.text.NumberFormat;
import java.util.Locale;

import org.libj.xquery.compiler.Compiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.libj.xquery.Compile.eval;
import static org.libj.xquery.Compile.compile;

public class Asserts {
    public static void assertEval(String script, Object expected) {
        assertEquals(script, expected, eval(script));
    }
    public static void assertEval(String script, Object expected, Environment env) {
        XQuery q = compile(script);
        assertEquals(script, expected, q.eval(env));
    }

    public static void assertEvalString(String script, String expected) {
        assertEquals(script, expected, eval(script).toString());
    }

    public static void assertEvalString(String script, String expected, Environment env) {
        XQuery q = compile(script);
        assertEquals(script, expected, q.eval(env).toString());
    }

    public static void assertEvalRegex(String script, String expected) {
        String result = eval(script).toString();
        assertTrue(String.format("%s doesn't match pattern %s", result, expected), result.matches(expected));
    }

    public static void assertEvalRegex(String script, String expected, Environment env) {
        XQuery q = compile(script);
        String result = q.eval(env).toString();
        assertTrue(String.format("%s doesn't match pattern %s", result, expected), result.matches(expected));
    }

    //
    // for performance testing...
    //

    // one eval
    public static void assertEvalMillis(String script, int targetMillis) {
        XQuery q = compile(script);
        long start = System.currentTimeMillis();
        q.eval();
        long end = System.currentTimeMillis();
        long executionMillis = end - start;
        logTime(script, executionMillis);
        assertTrue(String.format("Timeout: %d > %d", executionMillis, targetMillis), executionMillis <= targetMillis);
    }

    // repeated eval
    public static void assertRepeatedEvalMillis(Compiler compiler, String script, int n, int targetMillis, Environment env) {
        XQuery q = compiler == null ? compile(script) : compiler.compile(script);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            q.eval(env);
        }
        long end = System.currentTimeMillis();
        long executionMillis = end - start;
        logTime(script, executionMillis, n);
        logSpeed(n, executionMillis);
        assertTrue(String.format("Timeout: %d > %d (loop %s) for script: "+breakLine(script), executionMillis, targetMillis, number(n)), executionMillis <= targetMillis);
    }

    public static void assertRepeatedEvalMillis(String script, int n, int targetMillis, Environment env) {
        assertRepeatedEvalMillis(null, script, n, targetMillis, env);
    }

    public static void assertRepeatedEvalMillis(Compiler compiler, String script, int n, int targetMillis) {
        assertRepeatedEvalMillis(compiler, script, n, targetMillis, null);
    }

    public static void assertRepeatedEvalMillis(String script, int n, int targetMillis) {
        assertRepeatedEvalMillis(null, script, n, targetMillis, null);
    }

    // repeated eval for 1 second
    public static void assertRepeatedEvalPerSecond(Compiler compiler, String script, int n, Environment env) {
        assertRepeatedEvalMillis(compiler, script, n, 1000, env);
    }

    public static void assertRepeatedEvalPerSecond(String script, int n, Environment env) {
        assertRepeatedEvalMillis(script, n, 1000, env);
    }

    public static void assertRepeatedEvalPerSecond(Compiler compiler, String script, int n) {
        assertRepeatedEvalPerSecond(compiler, script, n, null);
    }

    public static void assertRepeatedEvalPerSecond(String script, int n) {
        assertRepeatedEvalPerSecond(script, n, null);
    }

    // repeated compile
    public static void assertRepeatedCompileMillis(Compiler compiler, String script, int n, int targetMillis) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            if (compiler == null) {
                XQuery q = compile(script);
            }
            else {
                XQuery q = compiler.compile(script);
            }
        }
        long end = System.currentTimeMillis();
        long executionMillis = end - start;
        logTime(script, executionMillis, n);
        logSpeed(n, executionMillis);
        assertTrue(String.format("Timeout: %d > %d (loop %s) for script: "+breakLine(script), executionMillis, targetMillis, number(n)), executionMillis < targetMillis);
    }
    public static void assertRepeatedCompileMillis(String script, int n, int targetMillis) {
        assertRepeatedCompileMillis(null, n, targetMillis);
    }

    // repeated compile for 1 second
    public static void assertRepeatedCompilePerSecond(Compiler compiler, String script, int n) {
        assertRepeatedCompileMillis(compiler, script, n, 1000);
    }
    public static void assertRepeatedCompilePerSecond(String script, int n) {
        assertRepeatedCompilePerSecond(null, script, n);
    }

    private static void logTime(String script, long ms) {
        System.out.println("----------------------------------------");
        System.out.println("It takes "+ms+" ms to execute this script: " + breakLine(script));
    }
    private static void logTime(String script, long ms, long loop) {
        System.out.println("----------------------------------------");
        System.out.println(String.format("It takes %s ms to execute this script (loop %s): %s", ms, number(loop), breakLine(script)));
    }
    private static void logSpeed(long n, long ms) {
        long perCall = ms/n;
        long perSecond = 1000*n/ms;
        System.out.println("Average execution time: "+perCall+ " ms");
        System.out.println("Speed: "+number(perSecond)+ " /s");
    }
    private static String number(long n) {
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.US);
        return format.format(n);
    }
    private static String breakLine(String script) {
        boolean multiline = script.indexOf('\n') == -1;
        return (multiline ? "" : "\n") + script;
    }
}
