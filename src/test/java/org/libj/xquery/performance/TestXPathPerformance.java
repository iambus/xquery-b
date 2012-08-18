package org.libj.xquery.performance;

import org.junit.Test;
import org.libj.xquery.Compile;
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
        assertRepeatedEvalPerSecond("let $x := <x><a>2</a></x> return $x/a", 1000*1000*1000);
    }

    @Test
    public void testMiniStringNamespaceXPath() {
        org.libj.xquery.compiler.Compiler compiler = new Compiler();
        compiler.enableGenerateInnerClasses(false);
        assertRepeatedEvalPerSecond(compiler, "let $x := <x><a>2</a></x> return $x/a", 1000*1000*5);
    }

    @Test
    public void testMiniFilterXPath() {
        assertRepeatedEvalPerSecond("let $x := <x><a>2</a></x> where $x/a = 2 return $x/a", 1000*1000*1000);
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




    private String deepevent = "<my:Event xmlns:my=\"http://xquery.libj.org/event/Mine\">\n" +
            "\t<events>\n" +
            "\t\t<event type='old'>\n" +
            "\t\t\t<ID>EX2006</ID>\n" +
            "\t\t\t<Time>2006-XX-XX</Time>\n" +
            "\t\t\t<Peoples>\n" +
            "\t\t\t\t<Name>You</Name>\n" +
            "\t\t\t\t<Name>Me</Name>\n" +
            "\t\t\t</Peoples>\n" +
            "\t\t\t<Location>Nanjing</Location>\n" +
            "\t\t\t<your:story xmlns:your=\"http://xquery.libj.org/event/Yours\">\n" +
            "\t\t\t\t<content>nothing</content>\n" +
            "\t\t\t\t<reference>memory</reference>\n" +
            "\t\t\t</your:story>\n" +
            "\t\t</event>\n" +
            "\t</events>\n" +
            "</my:Event>\n" +
            ""; // 413 bytes

    @Test
    public void testDeepXML() {
        String xquery =
                "declare namespace i = \"http://xquery.libj.org/event/Mine\";\n" +
                        "declare namespace u = \"http://xquery.libj.org/event/Yours\";\n" +
                        "let $event := " + deepevent + "\n" +
                        "where $event/events/event/ID != '2'\n"+
                        "return <message>" +
                        "<id>{$event/events/event/ID}</id>" +
                        "<time>{$event/events/event/Time}</time>" +
                        "<city>{$event/events/event/Location}</city>" +
                        "<content>{$event/events/event/u:story/content}</content>" +
                        "</message>";
        System.out.println(xquery);
        assertRepeatedEvalPerSecond(xquery, 1000 * 100);
    }
    @Test
    public void testDeepXMLCachedXPath() {
        String xquery =
                "declare namespace i = \"http://xquery.libj.org/event/Mine\";\n" +
                        "declare namespace u = \"http://xquery.libj.org/event/Yours\";\n" +
                        "let $event := " + deepevent + "\n" +
//                        "return $event/events/event/ID\n";
                        "let $e := $event/events/event\n" +
                        "where $e/ID != '2'\n"+
                        "return <message>" +
                        "<id>{$e/ID}</id>" +
                        "<time>{$e/Time}</time>" +
                        "<city>{$e/Location}</city>" +
                        "<content>{$e/u:story/content}</content>" +
                        "</message>";
        System.out.println(xquery);
        System.out.println(Compile.eval(xquery));
        assertRepeatedEvalPerSecond(xquery, 1000 * 200);
    }
}
