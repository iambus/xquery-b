package org.libj.xquery.parser;

import org.libj.xquery.lexer.Token;
import org.libj.xquery.lisp.Cons;

import static org.libj.xquery.lexer.TokenType.*;

import java.util.Iterator;
import java.util.List;

public class AST extends Cons<Unit> implements Unit {
    
    public AST() {
        // do nothing
    }
    
    public AST(Unit x) {
        super(x);
    }
    
    public AST(Token  t) {
        this(new Element(t));
    }
    
    public AST(int tokenType) {
        this(new Token(tokenType, null));
    }

    public Token getToken() {
        return ((Element) first()).getToken();
    }

    public int getNodeType() {
        return getToken().type;
    }

    public String getNodeText() {
        return getToken().text;
    }

    public Element getElement() {
        return (Element) first();
    }

    public Class getEvalType() {
        return ((TypedUnit) first()).getType();
    }

    public AST rest() {
        AST rest = (AST) next();
        if (rest != null) {
            return rest;
        }
        return new AST() {
            @Override
            public Cons next() {
                throw new NullPointerException("Nil access");
            }

            @Override
            public void setCar(Unit x) {
                throw new NullPointerException("Nil access");
            }

            @Override
            public Cons setCdr(Cons x) {
                throw new NullPointerException("Nil access");
            }

            @Override
            public Unit first() {
                throw new NullPointerException("Nil access");
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public List<Unit> toList() {
                return toList(null);
            }

            @Override
            public Iterator<Unit> iterator() {
                return iterator(null);
            }

            @Override
            public AST nth(int i) {
                throw new NullPointerException("Nil access");
            }

            @Override
            public String toString() {
                return "nil";
            }

            @Override
            public boolean isNil() {
                return true;
            }
        };
    }
    public void appendLast(AST t) {
        Cons list = this;
        while (list.next() != null) {
            list = list.next();
        }
        list.setCdr(new AST(t));
    }

    public void appendLast(Token t) {
        appendLast(new AST(t));
    }

    public AST nth(int i) {
        Cons list = this;
        while (i-- > 0) {
            list = list.next();
            if (list == null) {
                throw new IndexOutOfBoundsException(""+i);
            }
        }
        return (AST) list.first();
    }

    public boolean isNil() {
        return first() == null;
    }

    public String toString() {
        Unit head = first();
        if (head == null) {
            return "nil";
        }
        if (!(head instanceof Element)) {
            return toVectorString(this);
        }
        Token token = ((Element) head).getToken();
        if (token.type == DECLARES || token.type == FORLETS) {
            AST subs = rest();
            return toVectorString(subs);
        }
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
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        if (token.type == CALL) {
            builder.append("funcall");
        }
        else if (token.type == LIST) {
            builder.append("list");
        }
        else if (token.type == INDEX) {
            builder.append("at");
        }
        else if (token.type == INDEX) {
            builder.append("xpath");
        }
        else if (token.type == FOR || token.type == LET || token.type == TO ||
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
        AST node = (AST) next();
        while (node != null) {
            builder.append(' ');
            builder.append(node.first());
            node = (AST) node.next();
        }
        builder.append(")");
        return builder.toString();
    }

    private String toVectorString(AST ast) {
        StringBuilder builder = new StringBuilder();
        builder.append("["); // XXX: TODO: FIXME: the output doesn't make sense...
        if (ast.size() > 0) {
            builder.append(ast.first());
            for (Unit x : ast.rest()) {
                builder.append(' ');
                builder.append(x);
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
