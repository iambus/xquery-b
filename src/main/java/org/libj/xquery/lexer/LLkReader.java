package org.libj.xquery.lexer;

import java.io.IOException;
import java.io.Reader;

public class LLkReader {
    private Reader source;
    private int k;
    private int[] lookahead;
    private int p = 0;

    public LLkReader(Reader reader, int k) throws IOException {
        this.source = reader;
        this.k = k;
        this.lookahead = new int[k];
        for (int i = 0; i < k; i++) {
            consume();
        }
    }
    public void consume() throws IOException {
        lookahead[p] = source.read();
        p = (p+1) % k;
    }
    public int LL(int i) { // note: the index starts from 0
        return lookahead[(p+i-1)%k];
    }
    public void match(char x) throws IOException {
        if (LL(1) == x) {
            consume();
        }
        else {
            throw new LexerException("Expecting " + x + "; found " + LL(1));
        }
    }
}
