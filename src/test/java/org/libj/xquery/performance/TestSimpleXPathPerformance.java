package org.libj.xquery.performance;

import org.junit.Test;
import org.libj.xquery.compiler.*;
import org.libj.xquery.compiler.Compiler;
import org.libj.xquery.xml.DomSimpleXPathXMLFactory;
import org.libj.xquery.xml.dom.DomXMLFactory;
import org.libj.xquery.xml.str.StringXMLFactory;

import static org.libj.xquery.Asserts.assertEvalString;
import static org.libj.xquery.Asserts.assertRepeatedEvalPerSecond;

public class TestSimpleXPathPerformance {
    @Test
    public void testMiniDefaultXPath() {
        assertRepeatedEvalPerSecond("let $x := <x><a>2</a></x> return $x/a", 500);
    }
    @Test
    public void testMiniDomXPath() {
        org.libj.xquery.compiler.Compiler compiler = new Compiler();
        compiler.setXMLFactory(DomXMLFactory.class);
        assertRepeatedEvalPerSecond(compiler, "let $x := <x><a>2</a></x> return $x/a", 500);
    }
    @Test
    public void testMiniDomSimpleXPath() {
        org.libj.xquery.compiler.Compiler compiler = new Compiler();
        compiler.setXMLFactory(DomSimpleXPathXMLFactory.class);
        assertRepeatedEvalPerSecond(compiler, "let $x := <x><a>2</a></x> return $x/a", 500);
    }
    @Test
    public void testMiniStringXPath() {
        org.libj.xquery.compiler.Compiler compiler = new Compiler();
        compiler.setXMLFactory(StringXMLFactory.class);
        assertRepeatedEvalPerSecond(compiler, "let $x := <x><a>2</a></x> return $x/a", 1000*1000*1);
    }
}
