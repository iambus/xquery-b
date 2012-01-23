package org.libj.xquery.lexer;

import java.io.IOException;
import java.io.Reader;

public class LL1Reader {
    protected Reader source;
    protected int c;

    public LL1Reader(Reader reader) {
        this.source = reader;
    }

    public void consume() throws IOException {
        c = source.read();
    }

    public boolean eof() {
        return c == -1;
    }

    public void match(char x) throws IOException {
        if (c == x) {
            consume();
        }
        else {
            throw new LexerException("Expecting " + x + "; found " + c);
        }
    }
}
