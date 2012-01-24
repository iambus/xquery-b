package org.libj.xquery.parser;

import org.libj.xquery.lexer.Lexer;
import org.libj.xquery.lexer.LexerException;
import org.libj.xquery.lexer.Token;
import org.libj.xquery.lexer.TokenType;

import java.io.IOException;

public class LLkParser {
    private Lexer lexer;
    private int k;
    private Token[] lookahead;
    private int p = 0;

    public LLkParser(Lexer lexer, int k) throws IOException {
        this.lexer = lexer;
        this.k = k;
        this.lookahead = new Token[k];
        for (int i = 0; i < k; i++) {
            consume();
        }
    }
    public Token LT(int i) { // note: the index starts from 0
        return lookahead[(p+i-1)%k];
    }
    public int LA(int i) {
        return LT(i).type;
    }
    public void match(int x) throws IOException {
        if (LA(1) == x) {
            consume();
        }
        else {
            throw new LexerException("Expecting " + (TokenType.toTypeName(x)) + "; found " + LT(1));
        }
    }
    public Token consume() throws IOException {
        Token t = lookahead[p];
        lookahead[p] = lexer.nextToken();
//        System.out.println(lookahead[p]);
        p = (p+1) % k;
        return t;
    }
    public Token consume(int x) throws IOException {
        Token t = lookahead[p];
        match(x);
        return t;
    }
}
