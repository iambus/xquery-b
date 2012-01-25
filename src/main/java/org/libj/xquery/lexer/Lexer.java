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
                    if (isWordStart()) {
                        return readWord();
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

    private Token readWord() throws IOException {
        StringBuilder builder = new StringBuilder();
        do {
            builder.append((char)c);
            consume();
        } while (isWordPart());
        String text = builder.toString();
        int type = WORD;
        if (text.equals("declare")) {
            type = DECLARE;
        }
        else if (text.equals("let")) {
            type = LET;
        }
        else if (text.equals("for")) {
            type = FOR;
        }
        else if (text.equals("in")) {
            type = IN;
        }
        else if (text.equals("as")) {
            type = AS;
        }
        else if (text.equals("if")) {
            type = IF;
        }
        else if (text.equals("then")) {
            type = THEN;
        }
        else if (text.equals("else")) {
            type = ELSE;
        }
        else if (text.equals("where")) {
            type = WHERE;
        }
        else if (text.equals("return")) {
            type = RETURN;
        }
        else if (text.equals("and")) {
            type = AND;
        }
        else if (text.equals("or")) {
            type = OR;
        }
        else if (text.equals("div")) {
            type = DIV;
        }
        else if (text.equals("to")) {
            type = TO;
        }
        else if (text.equals("namespace")) {
            type = NAMESPACE;
        }
        return t(type, text);
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
        if (xpath.indexOf('/') == -1) {
            return t(VARIABLE, xpath);
        }
        else {
            return t(XPATH, xpath);
        }
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
        String text = builder.toString();
        if (text.indexOf('/') == 1) {
            // TODO: FIXME: support < /x>, <x/>
            return t(TAGCLOSE, builder.toString());
        }
        else {
            return t(TAGOPEN, builder.toString());
        }
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

    private boolean isWordStart() {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
    }

    private boolean isWordPart() {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9') || c == ':' || c == '_' || c == '-';
    }


}
