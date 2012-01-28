package org.libj.xquery;

import org.libj.xquery.XQuery;
import org.libj.xquery.compiler.Compiler;
import org.libj.xquery.compiler.Assembler;
import org.libj.xquery.compiler.CompilerException;
import org.libj.xquery.lexer.Lexer;
import org.libj.xquery.parser.AST;

import org.libj.xquery.parser.Parser;
import org.objectweb.asm.*;

import java.io.*;

public class Compile {
    public static AST compileToAST(Reader reader) {
        return new Compiler().compileToAST(reader);
    }

    public static AST compileToAST(String xquery) {
        return new Compiler().compileToAST(xquery);
    }

    public static AST compileToAST(FileInputStream input) {
        return new Compiler().compileToAST(input);
    }

    public static AST compileToAST(File path) {
        return new Compiler().compileToAST(path);
    }

    public static byte[] compileToByteArray(AST ast, String className) {
        return new Compiler().compileToByteArray(ast, className);
    }

    public static void compileToFile(AST ast, String className, File path) {
        new Compiler().compileToFile(ast, className, path);
    }

    public static Class compileToClass(AST ast, String className) {
        return new Compiler().compileToClass(ast, className);
    }

    public static XQuery compileToXQuery(AST ast, String className) {
        return new Compiler().compileToXQuery(ast, className);
    }

    public static XQuery compileToXQuery(AST ast) {
        return new Compiler().compileToXQuery(ast);
    }

    public static XQuery compile(String xquery) {
        return new Compiler().compile(xquery);
    }

    public static Object eval(String xquery) {
        return new Compiler().eval(xquery);
    }

}
