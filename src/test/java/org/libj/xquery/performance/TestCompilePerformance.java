package org.libj.xquery.performance;

import org.junit.Test;

import static org.libj.xquery.Asserts.*;

public class TestCompilePerformance {
    @Test
    public void testLiteral() {
        assertRepeatedCompilePerSecond("1", 1000*1);
    }
    @Test
    public void testFlower() {
        assertRepeatedCompilePerSecond("for $i in (1 to 100) return $i + 2", 1000*1);
    }
}
