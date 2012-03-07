package org.libj.xquery;

import org.libj.xquery.compiler.Compiler;
import org.libj.xquery.lisp.Cons;

import java.io.*;

public class Compile {
    public static Cons compileToAST(Reader reader) {
        return new Compiler().compileToAST(reader);
    }

    public static Cons compileToAST(String xquery) {
        return new Compiler().compileToAST(xquery);
    }

    public static Cons compileToAST(FileInputStream input) {
        return new Compiler().compileToAST(input);
    }

    public static Cons compileToAST(File path) {
        return new Compiler().compileToAST(path);
    }

    public static byte[] compileToByteArray(Cons ast, String className, String...vars) {
        return new Compiler().compileToByteArray(ast, className, vars);
    }

    public static void compileToFile(Cons ast, String className, File path, String...vars) {
        new Compiler().compileToFile(ast, className, path, vars);
    }

    public static Class compileToClass(Cons ast, String className, String...vars) {
        return new Compiler().compileToClass(ast, className, vars);
    }

    public static XQuery compileToXQuery(Cons ast, String className, String...vars) {
        return new Compiler().compileToXQuery(ast, className, vars);
    }

    public static XQuery compileToXQuery(Cons ast) {
        return new Compiler().compileToXQuery(ast);
    }

    public static XQuery compile(String xquery, String...vars) {
        return new Compiler().compile(xquery, vars);
    }

    public static Object eval(String xquery) {
        return new Compiler().eval(xquery);
    }

}
