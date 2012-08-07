package org.libj.xquery.performance;

import org.junit.Test;
import org.libj.xquery.xml.XML;

import static org.junit.Assert.assertTrue;
import static org.libj.xquery.Asserts.*;
import static org.libj.xquery.Asserts.number;

public class TestManualStructuredXMLPerformance {
    @Test
    public void testConstruct() {
        int n = 1000*1000*10;
        int targetMillis = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            XML x = new XML1(1);
        }
        long end = System.currentTimeMillis();
        long executionMillis = end - start;
        String script = "<ID>1</ID>";
        logTime(script, executionMillis, n);
        logSpeed(n, executionMillis);
        assertTrue(String.format("Timeout: %d > %d (loop %s) for script: "+breakLine(script), executionMillis, targetMillis, number(n)), executionMillis <= targetMillis);
    }
    @Test
    public void testText() {
        int n = 1000*1000*10;
        int targetMillis = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            XML x = new XML1(1);
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
        int n = 1000*1000*10;
        int targetMillis = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            XML x = new XML2("3", "1");
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

    private class XML1 implements XML {
        private Object value;

        private XML1(Object value) {
            this.value = value;
        }

        public XML eval(String path) {
            throw new UnsupportedOperationException("eval");
        }

        public Object getElementsByTagNameNS(String namespaceURI, String localName) {
            return null;
        }

        public String getAttribute(String name) {
            return null;
        }

        public String text() {
            if (value != null) {
                return value.toString();
            }
            else {
                return "";
            }
        }
    }

    private class XML2 implements XML {
        private Object attr;
        private Object value;

        private XML2(Object attr, Object value) {
            this.attr = attr;
            this.value = value;
        }

        public XML eval(String path) {
            throw new UnsupportedOperationException("eval");
        }

        public Object getElementsByTagNameNS(String namespaceURI, String localName) {
            if ("ID".equals(localName)) {
                return new XML1(value);
            }
            else {
                return null;
            }
        }

        public String getAttribute(String name) {
            if ("a".endsWith(name)) {
                return attr.toString();
            }
            else {
                return null;
            }
        }

        public String text() {
            if (value != null) {
                return value.toString();
            }
            else {
                return "";
            }
        }
    }
}
