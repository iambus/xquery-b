package org.libj.xquery.parser;

import org.libj.xquery.lexer.Token;
import org.libj.xquery.lisp.Cons;

import static org.libj.xquery.lexer.TokenType.*;

public class AST {

    public static Cons createAST(Token t) {
        return createAST(new Element(t));
    }

    public static Cons createAST(int tokenType) {
        return createAST(new Token(tokenType, null));
    }

    public static Cons createAST(Unit x) {
        return new Cons(x);
    }

    public static Cons createAnyAST(Object x) {
        return new Cons(x);
    }

    public static Cons createAST() {
        return new Cons();
    }

    public static Token getToken(Cons units) {
        return ((Element) units.first()).getToken();
    }

    public static int getNodeType(Cons units) {
        return getToken(units).type;
    }

    public static String getNodeText(Cons units) {
        return getToken(units).text;
    }

    public static Element getElement(Cons units) {
        return (Element) units.first();
    }

    public static Class getEvalType(Cons units) {
        return ((TypedUnit) units.first()).getType();
    }

    public static String toString(Cons ast) {
        Unit head = (Unit) ast.first();
        if (head == null) {
            return "nil";
        }
        if (!(head instanceof Element)) {
            return toVectorString(ast);
        }
        Token token = ((Element) head).getToken();
        if (token.type == DECLARES || token.type == FORLETS) {
            Cons subs = Cons.rest(ast);
            return toVectorString(subs);
        }
        if (Cons.isNil(ast)) {
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
        Cons node = ast.next();
        while (node != null) {
            builder.append(' ');
            builder.append(node.first());
            node = node.next();
        }
        builder.append(")");
        return builder.toString();
    }

    private static String toVectorString(Cons ast) {
        StringBuilder builder = new StringBuilder();
        builder.append("["); // XXX: TODO: FIXME: the output doesn't make sense...
        if (ast.size() > 0) {
            builder.append(ast.first());
            for (Object x : Cons.rest(ast)) {
                builder.append(' ');
                builder.append(x);
            }
        }
        builder.append("]");
        return builder.toString();
    }

    public static Cons nthAST(Cons cons, int i) {
        return (Cons) cons.nth(i);
    }
}
