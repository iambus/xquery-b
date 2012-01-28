package org.libj.xquery;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.libj.xquery.compiler.Compiler;
import org.libj.xquery.xml.dom.DomXMLFactory;

public class TestXMLFactory {
    @Test
    public void testDefaultFactory() {
        Compiler compiler = new Compiler();
        assertEquals("<a>2</a>", compiler.eval("let $x := <x><a>2</a></x> return $x/a").toString());
        assertEquals("", compiler.eval("let $x := <x><a>2</a></x> return $x/x/a").toString());
    }
    @Test
    public void testDomFactory() {
        Compiler compiler = new Compiler();
        compiler.setXMLFactory(DomXMLFactory.class);
        assertEquals("<a>2</a>", compiler.eval("<a>2</a>").toString());
        assertEquals("<a>2</a>", compiler.eval("let $x := <x><a>2</a></x> return $x/a").toString());
        assertEquals("", compiler.eval("let $x := <x><a>2</a></x> return $x/x/a").toString());
    }
}
