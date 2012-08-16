package org.libj.xquery.performance;

import org.junit.Test;
import org.libj.xquery.Compile;

import static org.libj.xquery.Asserts.*;

public class TestXMLPerformance {
    @Test
    public void testMiniXML() {
        // without xpath, we can get almost 1 billion per second!
        assertRepeatedEvalPerSecond("<x>{2}</x>", 1000*1000*1000);
    }
    @Test
    public void testRepeatedXML() {
        assertEvalMillis("for $i in (1 to 1000*1000*2) return <x>{$i}</x>", 1000);
    }
    @Test
    public void testText() {
        assertRepeatedEvalPerSecond("fn:string(<x>{2}</x>)", 1000 * 1000 * 10);
    }
    @Test
    public void testBiggerXML() {
        String xquery =
                "<message>" +
                        "<id>{'EX2006'}</id>" +
                        "<time>{'2006-XX-XX'}</time>" +
                        "<city>{'Nanjing'}</city>" +
                        "<content>{'nothing'}</content>" +
                        "</message>";
        System.out.println(xquery);
        System.out.println(Compile.eval(xquery));
        assertRepeatedEvalPerSecond(xquery, 1000 * 1000 * 1000);
    }
}
