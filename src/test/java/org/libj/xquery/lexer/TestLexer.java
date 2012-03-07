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
}
