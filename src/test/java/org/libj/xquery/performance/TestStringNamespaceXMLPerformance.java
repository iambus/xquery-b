package org.libj.xquery.performance;

import org.junit.Test;
import org.libj.xquery.xml.XML;
import org.libj.xquery.xml.str.StringNamespaceXML;

import static org.junit.Assert.assertTrue;
import static org.libj.xquery.Asserts.*;

public class TestStringNamespaceXMLPerformance {
    @Test
    public void testTag() {
        int n = 1000*1000*1;
        int targetMillis = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            XML x = new StringNamespaceXML("<root:Event xmlns:root=\"http://test.libj.org/root\"><home:ID xmlns:home=\"http://test.libj.org/home\">1</home:ID></root:Event>");
            XML node = (XML) x.getElementsByTagNameNS("http://test.libj.org/home", "ID");
        }
        long end = System.currentTimeMillis();
        long executionMillis = end - start;
        String script = "$x/ID";
        logTime(script, executionMillis, n);
        logSpeed(n, executionMillis);
        assertTrue(String.format("Timeout: %d > %d (loop %s) for script: " + breakLine(script), executionMillis, targetMillis, number(n)), executionMillis <= targetMillis);
    }
}
