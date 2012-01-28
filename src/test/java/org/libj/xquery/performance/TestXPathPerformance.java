package org.libj.xquery.performance;

import org.junit.Test;
import org.libj.xquery.Environment;
import org.libj.xquery.compiler.*;
import org.libj.xquery.compiler.Compiler;
import org.libj.xquery.xml.DomSimpleXPathXMLFactory;
import org.libj.xquery.xml.dom.DomXMLFactory;
import org.libj.xquery.xml.str.StringXML;
import org.libj.xquery.xml.str.StringXMLFactory;

import static org.libj.xquery.Asserts.assertEvalString;
import static org.libj.xquery.Asserts.assertRepeatedEvalPerSecond;

public class TestXPathPerformance {
    //
    // different XML factories on minimal xmls
    //

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
        assertRepeatedEvalPerSecond(compiler, "let $x := <x><a>2</a></x> return $x/a", 1000);
    }
    @Test
    public void testMiniStringXPath() {
        org.libj.xquery.compiler.Compiler compiler = new Compiler();
        compiler.setXMLFactory(StringXMLFactory.class);
        assertRepeatedEvalPerSecond(compiler, "let $x := <x><a>2</a></x> return $x/a", 1000*1000*2);
    }

    //
    // test on StringXMLFactory
    //

    private String xml = "<books>\n" +
            "\t<book>\n" +
            "\t\t<name>something about me</name>\n" +
            "\t\t<author>me</author>\n" +
            "\t\t<year>this year</year>\n" +
            "\t\t<comments>my favorate</comments>\n" +
            "\t</book>\n" +
            "\t<book>\n" +
            "\t\t<name>something about you</name>\n" +
            "\t\t<author>me</author>\n" +
            "\t\t<year>this year</year>\n" +
            "\t\t<comments>my favorate</comments>\n" +
            "\t</book>\n" +
            "</books>"; // 283 bytes
    @Test
    public void testBook() {
        org.libj.xquery.compiler.Compiler compiler = new Compiler();
        compiler.setXMLFactory(StringXMLFactory.class);
        System.out.println(xml.length());
        assertRepeatedEvalPerSecond(compiler, "let $x := "+xml+" return $x/book", 1000*100);
    }

    @Test
    public void testBookName() {
        org.libj.xquery.compiler.Compiler compiler = new Compiler();
        compiler.setXMLFactory(StringXMLFactory.class);
        System.out.println(xml.length());
        assertRepeatedEvalPerSecond(compiler, "let $x := " + xml + " return $x/book/name", 1000 * 100);
    }


    private String event = "<Event>\n" +
            "\t<ID>EX2006</ID>\n" +
            "\t<Time>2006-XX-XX</Time>\n" +
            "\t<Peoples>\n" +
            "\t\t<Name>You</Name>\n" +
            "\t\t<Name>Me</Name>\n" +
            "\t</Peoples>\n" +
            "\t<Location>Nanjing</Location>\n" +
            "</Event>"; // 148 bytes
    @Test
    public void testEvent() {
        assertRepeatedEvalPerSecond("let $event := "+event+" return $event/Location", 1000 * 500);
        assertRepeatedEvalPerSecond("let $event := "+event+" return $event/Location", 1000 * 500);
    }

    @Test
    public void testEventVariable() {
        Environment env = new Environment();
        env.putVariable("event", new StringXML(event));
        assertRepeatedEvalPerSecond("$event/Location", 1000 * 1000, env);
        assertRepeatedEvalPerSecond("$event/Location", 1000 * 1000, env);
    }
}
