package org.libj.xquery;

import org.libj.xquery.XQuery;
import org.libj.xquery.compiler.Assembler;
import org.libj.xquery.compiler.CompilerException;
import org.libj.xquery.lexer.Lexer;
import org.libj.xquery.parser.AST;

import org.libj.xquery.parser.Parser;
import org.objectweb.asm.*;

import java.io.*;

public class Compile {
    public static AST compileToAST(Reader reader) {
        try {
            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);
            return parser.xquery();
        } catch (IOException e) {
            throw new CompilerException(e);
        }
    }
    
    public static AST compileToAST(String xquery) {
        return compileToAST(new StringReader(xquery));
    }

    public static AST compileToAST(FileInputStream input) {
        Reader reader = new InputStreamReader(input);
        return compileToAST(reader);
    }

    public static AST compileToAST(File path) {
        try {
            FileInputStream input = new FileInputStream(path);
            try {
                return compileToAST(input);
            } finally {
                input.close();
            }
        } catch (IOException e) {
            throw new CompilerException(e);
        }
    }
    
    public static byte[] compileToByteArray(AST ast, String className) {
        Assembler assembler = new Assembler(className, ast);
        return assembler.toByteArray();
    }

    public static void compileToFile(AST ast, String className, File path) {
        try {
            byte[] bytes = compileToByteArray(ast, className);
            FileOutputStream output = new FileOutputStream(path);
            try {
                output.write(bytes);
            } finally {
                output.close();
            }
        } catch (IOException e) {
            throw new CompilerException(e);
        }
    }

    public static Class compileToClass(AST ast, String className) {
        return new DefaultClassLoader().define(className, compileToByteArray(ast, className));
    }
    
    public static XQuery compileToXQuery(AST ast, String className) {
        Class c = compileToClass(ast, className);
        try {
            return (XQuery)c.newInstance();
        } catch (InstantiationException e) {
            throw new CompilerException(e);
        } catch (IllegalAccessException e) {
            throw new CompilerException(e);
        }
    }

    public static XQuery compileToXQuery(AST ast) {
        return compileToXQuery(ast, randomClassName());
    }
    
    public static XQuery compile(String xquery) {
        return compileToXQuery(compileToAST(xquery));
    }

    public static Object eval(String xquery) {
        return compile(xquery).eval();
    }
    
    public static class DefaultClassLoader extends ClassLoader {
        public Class<?> define(String className, byte[] bytecode) {
            return super.defineClass(className, bytecode, 0, bytecode.length);
        }
    }
    
    public static String randomClassName() {
        return "org.libj.xquery.dynamic.Q$"+randomID();
    }

    public static String randomClassName(String packageName) {
        return packageName + ".Q$"+randomID();
    }
    
    public static String randomID() {
        return java.util.UUID.randomUUID().toString().replace('-', '_');
    }

}
