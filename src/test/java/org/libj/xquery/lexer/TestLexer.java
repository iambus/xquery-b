package org.libj.xquery.lexer;


import org.junit.Test;
import java.io.IOException;
import static org.junit.Assert.assertEquals;

public class TestLexer {
    @Test
    public void testID() throws IOException {
        Lexer lexer = new Lexer("$my-name");
        assertEquals(new Token(TokenType.VARIABLE, "$my-name"), lexer.nextToken());
    }
    @Test
    public void testXPath() throws IOException {
        Lexer lexer = new Lexer("$x/v");
        assertEquals(new Token(TokenType.VARIABLE, "$x"), lexer.nextToken());
        assertEquals(new Token(TokenType.XPATH, "/"), lexer.nextToken());
    }
}
