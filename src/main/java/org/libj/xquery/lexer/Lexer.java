package org.libj.xquery.lexer;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.libj.xquery.lexer.Token.t;
import static org.libj.xquery.lexer.TokenType.*;
import static java.lang.Character.isWhitespace;

public class Lexer extends LL1Reader {

    public Lexer(Reader reader) throws IOException {
        super(reader);
        consume();
    }

    public Lexer(String text) throws IOException {
        this(new StringReader(text));
    }

    public Token nextToken() throws IOException {
        while (!eof()) {
            switch (c) {
                case ' ': case '\t': case '\n': case '\r':
                    skipWhitespaces();
                    continue;
                case '(':
                    consume();
                    if (c == ':') {
                        consume();
                        endComment();
                        continue;
                    }
                    else {
                        return t(LPAREN, "(");
                    }
                case ')':
                    consume();
                    return t(RPAREN, ")");
                case '{':
                    consume();
                    return t(LBRACK, "{");
                case '}':
                    consume();
                    return t(RBRACK, "}");
                case '[':
                    consume();
                    return t(LBRACKET, "[");
                case ']':
                    consume();
                    return t(RBRACKET, "]");
                case '+':
                    consume();
                    return t(PLUS, "+");
                case '-':
                    consume();
                    return t(MINUS, "-");
                case '*':
                    consume();
                    return t(MULTIPLY, "*");
                case '/':
                    consume();
                    return t(DIV, "/");
                case '=':
                    consume();
                    return t(EQ, "=");
                case ':':
                    consume();
                    match('=');
                    return t(ASSIGN, ":=");
                case ',':
                    consume();
                    return t(COMMA, ",");
                case ';':
                    consume();
                    return t(SEMI, ";");
                case '\'':
                    return readSingleString();
                case '"':
                    return readDoubleString();
                case '$':
                    return readXPath();
                case '<':
                    return readTag();
                default:
                    if (isIDStart()) {
                        return readID();
                    }
                    else if ('0' <= c && c <= '9') {
                        return readNumber();
                    }
                    else {
                        throw new LexerException(("invalid character: " + (char)c));
                    }
            }
        }
        return t(EOF, "<EOF>");
    }

    private Token readID() throws IOException {
        StringBuilder builder = new StringBuilder();
        do {
            builder.append((char)c);
            consume();
        } while (isIDPart());
        return t(ID, builder.toString());
    }

    private Token readNumber() throws IOException {
        StringBuilder builder = new StringBuilder();
        do {
            builder.append((char)c);
            consume();
        } while (('0' <= c && c <= '9') || c == '.');
        return t(NUMBER, builder.toString());
    }
    
    private Token readSingleString() throws IOException {
        consume();
        StringBuilder builder = new StringBuilder();
        while (c != EOF && c != '\'') {
            builder.append((char)c); // TODO: FIXME: escaping!
            consume();
        }
        match('\'');
        return t(STRING, builder.toString());
    }
    
    private Token readDoubleString() throws IOException {
        consume();
        StringBuilder builder = new StringBuilder();
        while (c != EOF && c != '\"') {
            builder.append((char)c); // TODO: FIXME: escaping!
            consume();
        }
        match('"');
        return t(STRING, builder.toString());
    }

    private Token readXPath() throws IOException {
        String xpath = new XPathLexer(this).getXPath();
        return t(XPATH, xpath);
    }

    private Token readTag() throws IOException {
        StringBuilder builder = new StringBuilder();
        while (c != EOF) {
            builder.append((char)c);
            if (c == '>') {
                consume();
                break;
            }
            consume();
        }
        return t(TAG, builder.toString());
    }

    private void skipWhitespaces() throws IOException {
        while (isWhitespace(c)) {
            consume();
        }
    }

    private void endComment() throws IOException {
        while (c != EOF) {
            if (c == ':') {
                do {
                    consume();
                } while (c == ':');
                if (c == ')') {
                    consume();
                    return;
                }
                else {
                    consume();
                }
            }
            else {
                consume();
            }
        }
    }

    private boolean isIDStart() {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
    }

    private boolean isIDPart() {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9') || c == ':' || c == '_' || c == '-';
    }


}
