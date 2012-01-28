package org.libj.xquery.parser;

import org.libj.xquery.lexer.Lexer;
import org.libj.xquery.lexer.Token;

import java.io.IOException;

import static org.libj.xquery.lexer.TokenType.*;

public class Parser extends LLkParser {
    public Parser(Lexer lexer) throws IOException {
        super(lexer, 2);
    }

    public AST xquery() throws IOException {
        AST ast = new AST(PROG);
        ast.appendLast(declares());
        ast.appendLast(expr());
        match(EOF);
        return ast;
    }

    private AST primary() throws IOException {
        switch (LA(1)) {
            case NUMBER:
                return number();
            case STRING:
                return string();
            case VARIABLE:
                return variable();
            case XPATH:
                return xpath();
            case LPAREN:
                return list();
            case LBRACK:
                match(LBRACK);
                AST ast = expr();
                match(RBRACK);
                return ast;
            case WORD:
                if (LA(2) == LPAREN) {
                    return call();
                }
            case TAGOPEN:
            case TAGUNIT:
                return node();
            default:
                throw new ParserException("Unexpected primary expr token: " + LT(1));
        }
    }

    private AST expr() throws IOException {
        switch (LA(1)) {
            case FOR:
            case LET:
                return flower();
            case IF:
                return ifExpr();
            default:
                return or();
        }
    }

    private AST or() throws IOException {
        AST ast = and();
        while (LA(1) == OR) {
            ast = binary(ast, consume(), and());
        }
        return ast;
    }

    private AST and() throws IOException {
        AST ast = compare();
        while (LA(1) == AND) {
            ast = binary(ast, consume(), compare());
        }
        return ast;
    }

    private AST compare() throws IOException {
        AST ast = range();
        if (LA(1) == EQ || LA(1) == NE) {
            ast = binary(ast, consume(), range());
        }
        return ast;
    }

    private AST range() throws IOException {
        AST ast = add();
        if (LA(1) == TO) {
            ast = binary(ast, consume(), add());
        }
        return ast;
    }

    private AST add() throws IOException {
        AST ast = multiply();
        while (LA(1) == PLUS || LA(1) == MINUS) {
            ast = binary(ast, consume(), multiply());
        }
        return ast;
    }

    private AST multiply() throws IOException {
        AST ast = unary();
        while (LA(1) == MULTIPLY || LA(1) == DIV || LA(1) == MOD) {
            ast = binary(ast, consume(), unary());
        }
        return ast;
    }

    private AST unary() throws IOException {
        if (LA(1) == MINUS) {
            AST ast = new AST(NEGATIVE);
            consume();
            ast.appendLast(value());
            return ast;
        }
        else {
            return value();
        }
    }

    private AST value() throws IOException {
        AST ast = primary();
        while (LA(1) == LBRACKET) {
            match(LBRACKET);
            AST index = add();
            match(RBRACKET);
            ast = binary(ast, Token.t(INDEX), index);
        }
        return ast;
    }

    private AST binary(AST left, Token op, AST right) throws IOException {
        AST root = new AST(op);
        root.appendLast(left);
        root.appendLast(right);
        return root;
    }

    private AST ifExpr() throws IOException {
        AST ast = new AST(consume(IF));
        match(LPAREN);
        ast.appendLast(expr());
        match(RPAREN);
        match(THEN);
        ast.appendLast(expr());
        match(ELSE);
        ast.appendLast(expr());
        return ast;
    }

    private AST body() throws IOException {
        switch (LA(1)) {
            case FOR:
                return forIn();
            case LET:
                return let();
            default:
                match(RETURN);
                return expr();
        }
    }

    private AST call() throws IOException {
        AST ast = new AST(CALL);
        ast.appendLast(consume(WORD));
        match(LPAREN);
        if (LA(1) == RPAREN) {
            match(RPAREN);
            return ast;
        }
        ast.appendLast(expr());
        while (LA(1) == COMMA) {
            match(COMMA);
            ast.appendLast(primary());
        }
        match(RPAREN);
        return ast;
    }

    private AST list() throws IOException {
        AST ast = new AST(LIST);
        match(LPAREN);
        if (LA(1) == RPAREN) {
            match(RPAREN);
            return ast;
        }
        ast.appendLast(expr());
        while (LA(1) == COMMA) {
            match(COMMA);
            ast.appendLast(primary());
        }
        match(RPAREN);
        if (ast.size() == 2) {
            return ast.nth(1);
        }
        else {
            return ast;
        }
    }

    private AST node() throws IOException {
        AST ast = new AST(new Token(NODE, null));
        if (LA(1) == TAGUNIT) {
            ast.appendLast(consume(TAGUNIT));
            return ast;
        }
        ast.appendLast(consume(TAGOPEN));
        while (LA(1) != TAGCLOSE) {
            ast.appendLast(nodeExpr());
        }
        // TODO: check if start and end tag matches
        ast.appendLast(consume(TAGCLOSE));
        return ast;
    }

    private AST nodeExpr() throws IOException {
        switch (LA(1)) {
            case TAGOPEN: case TAGUNIT:
                return node();
            case TEXT:
                return new AST(consume());
            case LBRACK:
                match(LBRACK);
                AST ast = expr();
                match(RBRACK);
                return ast;
            default:
                throw new ParserException("Unexpected node expr token: " + LT(1));
        }
    }

    private AST string() throws IOException {
        return new AST(consume(STRING));
    }

    private AST number() throws IOException {
        return new AST(consume(NUMBER));
    }

    private AST xpath() throws IOException {
        String xpath = consume(XPATH).text;
        int separator = xpath.indexOf('/');
        if (separator <= 0) {
            throw new RuntimeException("Invalid xpath: "+xpath);
        }
        String variable = xpath.substring(0, separator);
        xpath = xpath.substring(separator);
        AST ast = new AST(XPATH);
        ast.appendLast(new Token(VARIABLE, variable));
        ast.appendLast(new Token(STRING, xpath));
        return ast;
    }

    private AST variable() throws IOException {
        return new AST(consume(VARIABLE));
    }

    private AST flower() throws IOException {
        AST ast = new AST(FLOWER);
        AST forlets = new AST(FORLETS);
        while (LA(1) == FOR || LA(1) == LET) {
            if (LA(1) == FOR) {
                forlets.appendLast(forIn());
            }
            else {
                forlets.appendLast(let());
            }
        }
        AST where = where();
        AST body = body();

        ast.appendLast(forlets);
        ast.appendLast(body);
        ast.appendLast(where);

        return ast;
    }

    private AST let() throws IOException {
        AST ast = new AST(consume(LET));
        ast.appendLast(consume(VARIABLE));
        consume(ASSIGN);
        ast.appendLast(expr());
        return ast;
    }

    private AST forIn() throws IOException {
        AST ast = new AST(consume(FOR));
        ast.appendLast(consume(VARIABLE));
        match(IN);
        ast.appendLast(expr());
        return ast;
    }

    private AST where() throws IOException {
        if (LA(1) == WHERE) {
            match(WHERE);
            return expr();
        }
        else {
            return new AST();
        }
    }

    public AST declares() throws IOException {
        AST ast = new AST(DECLARES);
        while (LA(1) == DECLARE) {
            ast.appendLast(declare());
        }
        return ast;
    }

    public AST declare() throws IOException {
        if (LA(2) == NAMESPACE) {
            return declareNamespace();
        }
        else {
            return declareAnyOther();
        }
    }

    public AST declareNamespace() throws IOException {
        AST ast = new AST(consume(DECLARE));
        ast.appendLast(consume(NAMESPACE));
        ast.appendLast(consume(WORD));
        match(EQ);
        ast.appendLast(consume(STRING));
        ast.appendLast(consume(SEMI));
        return ast;
    }

    public AST declareAnyOther() throws IOException {
        AST ast = new AST(consume(DECLARE));
        ast.appendLast(consume(WORD));
        while (LA(1) != SEMI && LA(1) != EOF) {
            ast.appendLast(LT(1));
            consume();
        }
        match(SEMI);
        return ast;
    }

}
