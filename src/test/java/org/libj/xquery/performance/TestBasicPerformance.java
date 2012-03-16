package org.libj.xquery.performance;

import org.junit.Test;

import static org.libj.xquery.Asserts.*;

public class TestBasicPerformance {
    @Test
    public void testLiteral() {
        assertRepeatedEvalPerSecond(" 1 ", 1000*1000*1000);
        assertRepeatedEvalPerSecond(" 's' ", 1000*1000*1000);
    }
    @Test
    public void testLiteralXML() {
        assertRepeatedEvalPerSecond(" <x/> ", 1000*1000*1000);
    }
    @Test
    public void testArithmeticExpression() {
        assertRepeatedEvalPerSecond(" 1 + 3 div 10 - 100 mod 4 ", 1000*1000*1000);
    }
}
