package org.libj.xquery;

import org.junit.Test;
import org.libj.xquery.xml.str.StringXML;

import static org.libj.xquery.Asserts.assertEvalString;

public class TestXPath {
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
}
