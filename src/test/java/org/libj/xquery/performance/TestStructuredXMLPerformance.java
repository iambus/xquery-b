package org.libj.xquery.performance;

import org.junit.Test;
import org.libj.xquery.Compile;
import org.libj.xquery.xml.XML;
import org.libj.xquery.xml.str.StringXML;

import static org.junit.Assert.assertTrue;
import static org.libj.xquery.Asserts.*;

public class TestStructuredXMLPerformance {
    @Test
    public void testTag() {
        XML x = (XML) Compile.eval("<x><ID>1</ID></x>");
        int n = 1000*1000*10;
        int targetMillis = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            XML node = (XML) x.getElementsByTagNameNS(null, "ID");
            System.out.println("node = " + node);
        }
        long end = System.currentTimeMillis();
        long executionMillis = end - start;
        String script = "$x/ID";
        logTime(script, executionMillis, n);
        logSpeed(n, executionMillis);
        assertTrue(String.format("Timeout: %d > %d (loop %s) for script: " + breakLine(script), executionMillis, targetMillis, number(n)), executionMillis <= targetMillis);
    }
    @Test
    public void testText() {
        XML x = (XML) Compile.eval("<x>1</x>");
        int n = 1000*1000*10;
        int targetMillis = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            String s = x.text();
        }
        long end = System.currentTimeMillis();
        long executionMillis = end - start;
        String script = "string($x)";
        logTime(script, executionMillis, n);
        logSpeed(n, executionMillis);
        assertTrue(String.format("Timeout: %d > %d (loop %s) for script: " + breakLine(script), executionMillis, targetMillis, number(n)), executionMillis <= targetMillis);
    }
    @Test
    public void testText2() {
        XML x = (XML) Compile.eval("<x><ID>1</ID></x>");
        int n = 1000*1000*10;
        int targetMillis = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            XML node = (XML) x.getElementsByTagNameNS(null, "ID");
            String s = node.text();
        }
        long end = System.currentTimeMillis();
        long executionMillis = end - start;
        String script = "string($x/ID)";
        logTime(script, executionMillis, n);
        logSpeed(n, executionMillis);
        assertTrue(String.format("Timeout: %d > %d (loop %s) for script: " + breakLine(script), executionMillis, targetMillis, number(n)), executionMillis <= targetMillis);
    }
}
