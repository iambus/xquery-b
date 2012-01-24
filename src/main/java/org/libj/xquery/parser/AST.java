package org.libj.xquery.parser;

import org.libj.xquery.lexer.Token;
import static org.libj.xquery.lexer.TokenType.*;

import java.util.ArrayList;
import java.util.List;

public class AST {
    private Token token;
    private List<AST> children;
    
    public AST() {
        // do nothing
    }
    
    public AST(Token token) {
        this.token = token;
    }
    
    public AST(int tokenType) {
        this(new Token(tokenType, null));
    }
    
    public int getNodeType() {
        return token.type;
    }
    
    public void addChild(AST t) {
        if (children == null) {
            children = new ArrayList<AST>();
        }
        children.add(t);
    }

    public void addChild(Token t) {
        addChild(new AST(t));
    }

    public AST nth(int i) { // i starts from 1
        return children.get(i-1);
    }

    public boolean isNil() {
        return token == null;
    }

    public String toString() {
        if (isNil()) {
            return "nil";
        }
        if (token.type == NUMBER || token.type == VARIABLE || token.type == WORD) {
            return token.text;
        }
        if (token.type == STRING) {
            return '"'+token.text+'"';
        }
        if (token.type == STRING) {
            return '"'+token.text.replace('"', '\'')+'"';
        }
        if (token.type == XPATH) {
            return "(xpath \""+token.text.replace("\"", "\\\"")+"\")";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        if (token.type == CALL) {
            builder.append("funcall");
        }
        else if (token.type == FOR || token.type == LET ||
                 token.type == AND || token.type == OR ||
                 token.type == PLUS || token.type == MINUS || token.type == MULTIPLY || token.type == DIV ||
                 token.type == EQ || token.type == ASSIGN) {
            builder.append(token.text);
        }
        else if (token.text == null) {
            builder.append(toTypeName(token.type));
        }
        else {
            builder.append(token);
        }
        if (children != null) {
            for (AST node: children) {
                builder.append(' ');
                builder.append(node);
            }
        }
        builder.append(")");
        return builder.toString();
    }
}
