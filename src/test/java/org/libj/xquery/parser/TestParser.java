package org.libj.xquery.parser;

import org.junit.Test;
import org.libj.xquery.lexer.Lexer;
import org.libj.xquery.lisp.Cons;

import java.io.IOException;

public class TestParser {
    public static Cons parseString(String xquery) throws IOException {
        Parser parser = new Parser(new Lexer(xquery));
        Cons ast = parser.xquery();
//        System.out.println(ast.third());
        return ast;
    }
    @Test
    public void testUnit() throws IOException {
        parseString("<x/>");
        parseString("<x/> + 1");
    }
    @Test
    public void testAttrUnit() throws IOException {
        parseString("<x a=''/>");
        parseString("<x a='x'/>");
    }
    @Test
    public void testAttrCodeUnit() throws IOException {
        parseString("<x a='{1 + 1}'/>");
        parseString("<x a='[{1 + 1}]'/>");
        parseString("<x a='[{1}{2},{3}]'/>");
    }
    @Test
    public void testXML() throws IOException {
        parseString("<x a='{1+1}' a:b='2'> {2} </x>");
        parseString("<x a='{1+1}' a:b='2'><a>{2}</a><b>b</b> <c/> </x>");
    }
}
