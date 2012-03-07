package org.libj.xquery.lexer;

import org.junit.Test;
import java.io.IOException;
import static org.junit.Assert.assertEquals;

public class TestXPathLexer {
    private static void assertXPath(String xpath) throws IOException {
        XPathLexer lexer = new XPathLexer(new Lexer(xpath));
        assertEquals(xpath, lexer.getXPath());
    }
    @Test
    public void testXPath() throws IOException {
//        assertXPath( "$x/v[2]");
//        assertXPath("$x/v[1 < 2]");
//        assertXPath("$x/v[@n > 2]");
        assertXPath("$x/v[text() = '']");
    }
}
