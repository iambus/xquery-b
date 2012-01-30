package org.libj.xquery.performance;

import org.junit.Test;
import org.libj.xquery.Environment;
import org.libj.xquery.compiler.*;
import org.libj.xquery.compiler.Compiler;
import org.libj.xquery.xml.DomSimpleXPathXMLFactory;
import org.libj.xquery.xml.dom.DomXMLFactory;
import org.libj.xquery.xml.str.StringNamespaceXMLFactory;
import org.libj.xquery.xml.str.StringXML;
import org.libj.xquery.xml.str.StringXMLFactory;

import static org.libj.xquery.Asserts.assertEvalString;
import static org.libj.xquery.Asserts.assertRepeatedCompileMillis;
import static org.libj.xquery.Asserts.assertRepeatedEvalPerSecond;

public class TestXPathPerformance {
    //
    // different XML factories on minimal xmls
    //

    @Test
    public void testMiniDefaultXPath() {
        assertRepeatedEvalPerSecond("let $x := <x><a>2</a></x> return $x/a", 1000*1000*3);
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
        assertRepeatedEvalPerSecond(compiler, "let $x := <x><a>2</a></x> return $x/a", 1000*1000*3);
    }

    @Test
    public void testMiniStringNamespaceXPath() {
        org.libj.xquery.compiler.Compiler compiler = new Compiler();
        compiler.setXMLFactory(StringNamespaceXMLFactory.class);
        assertRepeatedEvalPerSecond(compiler, "let $x := <x><a>2</a></x> return $x/a", 1000*1000*3);
    }

    //
    // test on StringNamespaceXMLFactory
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
        compiler.setXMLFactory(StringNamespaceXMLFactory.class);
        System.out.println(xml.length());
        assertRepeatedEvalPerSecond(compiler, "let $x := "+xml+" return $x/book", 1000*1000);
    }

    @Test
    public void testBookName() {
        org.libj.xquery.compiler.Compiler compiler = new Compiler();
        compiler.setXMLFactory(StringNamespaceXMLFactory.class);
        System.out.println(xml.length());
        assertRepeatedEvalPerSecond(compiler, "let $x := " + xml + " return $x/book/name", 1000 * 1000);
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
        assertRepeatedEvalPerSecond("let $event := " + event + " return $event/Location", 1000 * 1000);
        assertRepeatedEvalPerSecond("let $event := "+event+" return $event/Location", 1000 * 1000);
    }

    @Test
    public void testEventVariable() {
        Environment env = new Environment();
        env.putVariable("event", new StringXML(event));
        assertRepeatedEvalPerSecond("$event/Location", 1000 * 1000, env);
        assertRepeatedEvalPerSecond("$event/Location", 1000 * 1000, env);
    }

    @Test
    public void testEventText() {
//        assertRepeatedEvalPerSecond("debug:print(fn:string($event))", 1, env);
//        assertRepeatedEvalPerSecond("debug:print(fn:string("+event+"))", 1);
        assertRepeatedEvalPerSecond("fn:string("+event+")", 1000 * 500);
    }

    /*
<my:Event xmlns:my="http://xquery.libj.org/event/Mine">
	<ID>EX2006</ID>
	<Time>2006-XX-XX</Time>
	<Peoples>
		<Name>You</Name>
		<Name>Me</Name>
	</Peoples>
	<Location>Nanjing</Location>
	<your:story xmlns:your="http://xquery.libj.org/event/Yours">
		<content>nothing</content>
	</your:story>
</my:Event>

     */
    private String nsevent = "<my:Event xmlns:my=\"http://xquery.libj.org/event/Mine\">\n" +
            "\t<ID>EX2006</ID>\n" +
            "\t<Time>2006-XX-XX</Time>\n" +
            "\t<Peoples>\n" +
            "\t\t<Name>You</Name>\n" +
            "\t\t<Name>Me</Name>\n" +
            "\t</Peoples>\n" +
            "\t<Location>Nanjing</Location>\n" +
            "\t<your:story xmlns:your=\"http://xquery.libj.org/event/Yours\">\n" +
            "\t\t<content>nothing</content>\n" +
            "\t</your:story>\n" +
            "</my:Event>"; // 148 bytes

    @Test
    public void testEventNS() {
        String xquery =
                "declare namespace i = \"http://xquery.libj.org/event/Mine\";\n" +
                "declare namespace u = \"http://xquery.libj.org/event/Yours\";\n" +
                        "let $event := " + nsevent + "return $event/u:story/content";
        assertRepeatedEvalPerSecond(xquery, 1000 * 300);
    }
    @Test
    public void testEventNS2() {
        String xquery =
                "declare namespace i = \"http://xquery.libj.org/event/Mine\";\n" +
                        "declare namespace u = \"http://xquery.libj.org/event/Yours\";\n" +
                        "let $event := <x>" + nsevent + "</x> return string($event/i:Event/u:story/content)";
        assertRepeatedEvalPerSecond(xquery, 1000 * 100);
    }
    @Test
    public void testEvent3Fields() {
        String xquery =
                "declare namespace i = \"http://xquery.libj.org/event/Mine\";\n" +
                        "declare namespace u = \"http://xquery.libj.org/event/Yours\";\n" +
                        "let $event := " + nsevent + "\n" +
                        "return <x>" +
                        "<id>{string($event/ID)}</id>" +
                        "<city>{string($event/Location)}</city>" +
                        "<content>{string($event/u:story/content)}</content>" +
                        "</x>";
        assertRepeatedEvalPerSecond(xquery, 1000 * 100);
    }
    @Test
    public void testEventFilter() {
        String xquery =
                "declare namespace i = \"http://xquery.libj.org/event/Mine\";\n" +
                        "declare namespace u = \"http://xquery.libj.org/event/Yours\";\n" +
                        "let $event := " + nsevent + "\n" +
                        "where $event/ID != 2"+
                        "return $event";
        assertRepeatedEvalPerSecond(xquery, 1000 * 500);
    }
    @Test
    public void testEventForFilter() {
        String xquery =
                "declare namespace i = \"http://xquery.libj.org/event/Mine\";\n" +
                        "declare namespace u = \"http://xquery.libj.org/event/Yours\";\n" +
                        "for $event in " + nsevent + "\n" +
                        "where $event/ID != 2"+
                        "return $event";
        assertRepeatedEvalPerSecond(xquery, 1000 * 500);
    }
}
