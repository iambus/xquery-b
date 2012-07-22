package org.libj.xquery.performance;

import org.junit.Test;
import org.libj.xquery.XQuery;
import org.libj.xquery.xml.XML;
import org.libj.xquery.xml.str.StringNamespaceXML;
import org.libj.xquery.xml.str.StringXML;

import static org.junit.Assert.assertTrue;
import static org.libj.xquery.Asserts.*;
import static org.libj.xquery.Asserts.number;
import static org.libj.xquery.Compile.compile;

public class TestXPathFilterPerformance {
    @Test
    public void testStringXML() {
        String xquery = "let $fv1 := $arg\n" +
                        "let $x := $fv1/ID\n" +
                        "where $x = '2'\n" +
                        "return $fv1";
        XML x = new StringXML("<x><ID>1</ID></x>");
        assertRepeatedEvalMillisVar(xquery, 1000 * 1000 * 3, 1000, "arg", x);
    }

    @Test
    public void testStringNamespaceXML() {
        String xquery = "let $fv1 := $arg\n" +
                        "let $x := $fv1/ID\n" +
                        "where $x = '2'\n" +
                        "return $fv1";
        XML x = new StringNamespaceXML("<x><ID>1</ID></x>");
        assertRepeatedEvalMillisVar(xquery, 1000 * 1000 * 3, 1000, "arg", x);
    }

    public static void assertRepeatedEvalMillisVar(String script, int n, int targetMillis, String varName, Object var) {
        XQuery q = compile(script, varName);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            q.eval(var);
        }
        long end = System.currentTimeMillis();
        long executionMillis = end - start;
        logTime(script, executionMillis, n);
        logSpeed(n, executionMillis);
        assertTrue(String.format("Timeout: %d > %d (loop %s) for script: " + breakLine(script), executionMillis, targetMillis, number(n)), executionMillis <= targetMillis);
    }
}
