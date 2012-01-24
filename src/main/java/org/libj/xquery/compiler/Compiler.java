package org.libj.xquery.compiler;

import org.libj.xquery.XQuery;
import org.libj.xquery.parser.AST;

import org.objectweb.asm.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Compiler implements Opcodes {
    public static byte[] compileToByteArray(AST ast, String className) {
        Assembler assembler = new Assembler(className, ast);
        return assembler.toByteArray();
    }

    public static void compileToFile(AST ast, String className, File path) throws IOException {
        byte[] bytes = compileToByteArray(ast, className);
        FileOutputStream output = new FileOutputStream(path);
        try {
            output.write(bytes);
        } finally {
            output.close();
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
