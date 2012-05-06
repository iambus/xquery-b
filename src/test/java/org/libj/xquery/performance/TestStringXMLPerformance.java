package org.libj.xquery.performance;

import org.junit.Test;
import org.libj.xquery.xml.XML;
import org.libj.xquery.xml.str.StringXML;

import static org.junit.Assert.assertTrue;
import static org.libj.xquery.Asserts.*;
import static org.libj.xquery.Asserts.number;

public class TestStringXMLPerformance {
    @Test
    public void testConstruct() {
        int n = 1000*1000*10;
        int targetMillis = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            XML x = new StringXML("<ID>1</ID>");
        }
        long end = System.currentTimeMillis();
        long executionMillis = end - start;
        String script = "<ID>1</ID>";
        logTime(script, executionMillis, n);
        logSpeed(n, executionMillis);
        assertTrue(String.format("Timeout: %d > %d (loop %s) for script: "+breakLine(script), executionMillis, targetMillis, number(n)), executionMillis <= targetMillis);
    }
    @Test
    public void testTag() {
        int n = 1000*1000*5;
        int targetMillis = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            XML x = new StringXML("<x><ID>1</ID></x>");
            XML node = (XML) x.getElementsByTagNameNS(null, "ID");
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
        int n = 1000*1000*4;
        int targetMillis = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            XML x = new StringXML("<x><ID>1</ID></x>");
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
