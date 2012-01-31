package org.libj.xquery;

import org.junit.Test;
import org.libj.xquery.xml.str.StringXML;

import static org.libj.xquery.Asserts.assertEval;
import static org.libj.xquery.Asserts.assertEvalString;
import static org.libj.xquery.Asserts.assertRepeatedEvalPerSecond;

public class TestXPath {
    @Test
    public void testEq() {
        assertEvalString("let $x := <x><a>2</a></x> where $x/a = 2 return 7", "7");
        assertEvalString("let $x := <x><a><b>2</b></a></x> where $x/a/b = 2 return 7", "7");
    }
    /*
<books>
	<book>
		<name>something about me</name>
		<author>me</author>
		<year>this year</year>
		<comments>my favorate</comments>
	</book>
	<book>
		<name>something about you</name>
		<author>me</author>
		<year>this year</year>
		<comments>my favorate</comments>
	</book>
</books>

     */
    private String books = "<books>\n" +
            "\t<book>\n" +
            "\t\t<name>something about me</name>\n" +
            "\t\t<author>me</author>\n" +
            "\t\t<year>this year</year>\n" +
            "\t\t<comments>my favorate</comments>\n" +
            "\t</book>\n" +
            "</books>"; // 149 bytes

    /*
<Event>
	<ID>EX2006</ID>
	<Time>2006-XX-XX</Time>
	<Peoples>
		<Name>You</Name>
		<Name>Me</Name>
	</Peoples>
	<Location>Nanjing</Location>
</Event>
     */
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
    public void testBook() {
        Environment env = new Environment();
        env.putVariable("books", new StringXML(books));
        String book = "<book>\n" +
                "\t\t<name>something about me</name>\n" +
                "\t\t<author>me</author>\n" +
                "\t\t<year>this year</year>\n" +
                "\t\t<comments>my favorate</comments>\n" +
                "\t</book>";
        assertEvalString("$books/book", book, env);
    }
    @Test
    public void testBookName() {
        Environment env = new Environment();
        env.putVariable("books", new StringXML(books));
        String name = "<name>something about me</name>";
        assertEvalString("$books/book/name", name, env);
    }
    @Test
    public void testBookComments() {
        Environment env = new Environment();
        env.putVariable("books", new StringXML(books));
        String comments = "<comments>my favorate</comments>";
        assertEvalString("$books/book/comments", comments, env);
    }
    @Test
    public void testBookNodes() {
        Environment env = new Environment();
        env.putVariable("books", new StringXML(books));
        assertEvalString("let $book := $books/book return $book/author", "<author>me</author>", env);
    }
    @Test
    public void testEvent() {
        Environment env = new Environment();
        env.putVariable("event", new StringXML(event));
        assertEvalString("$event/Time", "<Time>2006-XX-XX</Time>", env);
        assertEvalString("$event/Location", "<Location>Nanjing</Location>", env);
    }
    //////////////////////////////////////////////////
    // namespace
    //////////////////////////////////////////////////
    /*
<ns:Event xmlns:ns="http://xquery.libj.org/examples/Event">
  <ns:EventData>
    <ID>7</ID>
    <Value>3</Value>
  </ns:EventData>
</ns:Event>

declare namespace ns = "http://xquery.libj.org/examples/Event";

for $fv1 in ()
where $fv1/ns:EventData/ID != '0'
return
    <root>
        <value>{fn:string($fv1/ns:EventData/Value)}</value>
    </root>

declare namespace ns = "http://xquery.libj.org/examples/Event";

for $fv1 in (<ns:Event xmlns:ns="http://xquery.libj.org/examples/Event">
  <ns:EventData>
    <ID>7</ID>
    <Value>3</Value>
  </ns:EventData>
</ns:Event>)
where $fv1/ns:EventData/ID != '0'
return
    <root>
        <value>{fn:string($fv1/ns:EventData/Value)}</value>
    </root>


     */
    @Test
    public void testEventWithNamespace() {
        String xquery = "declare namespace ns = \"http://xquery.libj.org/examples/Event\";\n" +
                "\n" +
                "for $fv1 in (<ns:Event xmlns:ns=\"http://xquery.libj.org/examples/Event\">\n" +
                "  <ns:EventData>\n" +
                "    <ID>7</ID>\n" +
                "    <Value>3</ID>\n" +
                "  </ns:EventData>\n" +
                "</ns:Event>)\n" +
                "where $fv1/ns:EventData/ID != '0'\n" +
                "return\n" +
                "    <root>\n" +
                "        <value>{fn:string($fv1/ns:EventData/Value)}</value>\n" +
                "    </root>\n" +
                "";
        String result = "<root>\n" +
                "        <value>3</value>\n" +
                "    </root>";
        assertEvalString(xquery, result);
    }
    @Test
    public void testEventWithDifferentNamespacePrefix() {
        String xquery = "declare namespace ns = \"http://xquery.libj.org/examples/Event\";\n" +
                "\n" +
                "for $fv1 in (<e:Event xmlns:e=\"http://xquery.libj.org/examples/Event\">\n" +
                "  <e:EventData>\n" +
                "    <ID>7</ID>\n" +
                "    <Value>3</ID>\n" +
                "  </e:EventData>\n" +
                "</e:Event>)\n" +
                "where $fv1/ns:EventData/ID = '7'\n" +
                "return\n" +
                "    <root>\n" +
                "        <value>{fn:string($fv1/ns:EventData/Value)}</value>\n" +
                "    </root>\n" +
                "";
        String result = "<root>\n" +
                "        <value>3</value>\n" +
                "    </root>";
        assertEvalString(xquery, result);
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
            "</my:Event>"; // 305 bytes

    @Test
    public void testEventNS2() {
        String xquery =
                "declare namespace i = \"http://xquery.libj.org/event/Mine\";\n" +
                        "declare namespace u = \"http://xquery.libj.org/event/Yours\";\n" +
                        "let $event := " + nsevent + "return $event/u:story/content";
        assertEvalString(xquery, "<content>nothing</content>");
    }
    @Test
    public void testEventNS() {
        String xquery =
                "declare namespace i = \"http://xquery.libj.org/event/Mine\";\n" +
                        "declare namespace u = \"http://xquery.libj.org/event/Yours\";\n" +
                        "let $event := <x>" + nsevent + "</x> return string($event/i:Event/u:story/content)";
        assertEvalString(xquery, "nothing");
    }
    @Test
    public void testEvent3FieldsWrongPath() {
        String xquery =
                "declare namespace i = \"http://xquery.libj.org/event/Mine\";\n" +
                        "declare namespace u = \"http://xquery.libj.org/event/Yours\";\n" +
                        "let $event := " + nsevent + "\n" +
                        "return <x>" +
                        "<id>{string($event/story/ID)}</id>" +
                        "<city>{string($event/story/Location)}</city>" +
                        "<content>{string($event/u:story/content)}</content>" +
                        "</x>";
        assertEvalString(xquery, "<x><id></id><city></city><content>nothing</content></x>");
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
        assertEvalString(xquery, "<x><id>EX2006</id><city>Nanjing</city><content>nothing</content></x>");
    }
}
