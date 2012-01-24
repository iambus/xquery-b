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
        ast.addChild(declares());
        ast.addChild(expr());
        return ast;
    }

    private AST primaryExpr() throws IOException {
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
            case WORD:
                if (LA(2) == LPAREN) {
                    return call();
                }
            default:
                throw new ParserException("Unexpected primary expr token: " + LT(1));
        }
    }

    private AST expr() throws IOException {
        switch (LA(1)) {
            case FOR:
                return forIn();
            case LET:
                return let();
            case IF:
                return ifExpr();
            case TAGOPEN:
                return node();
            default:
                return or();
        }
    }

    private AST or() throws IOException {
        AST ast = and();
        while (LA(1) == OR) {
            AST andNode = new AST(OR);
            andNode.addChild(ast);
            match(OR);
            andNode.addChild(and());
            ast = andNode;
        }
        return ast;
    }

    private AST and() throws IOException {
        AST ast = primaryExpr();
        while (LA(1) == AND) {
            AST andNode = new AST(AND);
            andNode.addChild(ast);
            match(AND);
            andNode.addChild(primaryExpr());
            ast = andNode;
        }
        return ast;
    }

    private AST ifExpr() {
        throw new RuntimeException("Not Implemented!");
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
        ast.addChild(consume(WORD));
        match(LPAREN);
        if (LA(1) == RPAREN) {
            match(RPAREN);
            return ast;
        }
        ast.addChild(expr());
        while (LA(1) == COMMA) {
            match(COMMA);
            ast.addChild(primaryExpr());
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
        ast.addChild(expr());
        while (LA(1) == COMMA) {
            match(COMMA);
            ast.addChild(primaryExpr());
        }
        match(RPAREN);
        return ast;
    }

    private AST node() throws IOException {
        AST ast = new AST(NODE);
        Token tag = consume(TAGOPEN);
        ast.addChild(tag);
        while (LA(1) != TAGCLOSE) {
            ast.addChild(nodeExpr());
        }
        Token endTag = consume(TAGCLOSE);
        // TODO: check if start and end tag matches
        return ast;
    }
    
    private AST nodeExpr() throws IOException {
        switch (LA(1)) {
            case TAGOPEN:
                return node();
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
        return new AST(consume(XPATH));
    }

    private AST variable() throws IOException {
        return new AST(consume(VARIABLE));
    }

    private AST let() throws IOException {
        AST ast = new AST(consume(LET));
        ast.addChild(consume(VARIABLE));
        consume(ASSIGN);
        ast.addChild(expr());
        AST where;
        if (LA(1) == WHERE) {
            match(WHERE);
            where = expr();
        }
        else {
            where = new AST();
        }
        ast.addChild(body());
        ast.addChild(where);
        return ast;
    }

    private AST forIn() throws IOException {
        AST ast = new AST(consume(FOR));
        ast.addChild(consume(VARIABLE));
        match(IN);
        ast.addChild(expr());
        AST where;
        if (LA(1) == WHERE) {
            match(WHERE);
            where = expr();
        }
        else {
            where = new AST();
        }
        ast.addChild(body());
        ast.addChild(where);
        return ast;
    }

    public AST declares() throws IOException {
        AST ast = new AST(DECLARES);
        while (LA(1) == DECLARE) {
            ast.addChild(declare());
        }
        return ast;
    }
    
    public AST declare() throws IOException {
        AST ast = new AST(DECLARES);
        match(DECLARE);
        ast.addChild(consume(WORD));
        while (LA(1) != SEMI && LA(1) != EOF) {
            ast.addChild(LT(1));
            consume();
        }
        match(SEMI);
        return ast;
    }

}
