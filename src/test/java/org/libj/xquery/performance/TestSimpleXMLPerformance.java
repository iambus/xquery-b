package org.libj.xquery.performance;

import org.junit.Test;

import static org.libj.xquery.Asserts.*;

public class TestSimpleXMLPerformance {
    @Test
    public void testMiniXML() {
        // without xpath, we can get almost 10 millions per second!
        assertRepeatedEvalPerSecond("<x>{2}</x>", 1000*1000*8);
    }
}
