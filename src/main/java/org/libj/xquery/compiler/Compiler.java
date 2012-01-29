package org.libj.xquery.compiler;

import org.libj.xquery.XQuery;
import org.libj.xquery.lexer.Lexer;
import org.libj.xquery.namespace.DefaultRootNamespace;
import org.libj.xquery.namespace.LibNamespace;
import org.libj.xquery.namespace.Namespace;
import org.libj.xquery.parser.AST;
import org.libj.xquery.parser.Parser;

import java.io.*;

public class Compiler {

    private Namespace namespace = new DefaultRootNamespace();
    private DefaultClassLoader loader = new DefaultClassLoader();

    private String defaultPackage = "org.libj.xquery.dynamic";
    private Class xmlFactory = Constants.DEFAUL_XML_FACTORY_IMPLEMENTATION;

    public void registerLib(String prefix, Class c) {
        namespace.register(prefix, new LibNamespace(c));
    }

    public void setDefaultPackage(String defaultPackage) {
        this.defaultPackage = defaultPackage;
    }

    public void setXMLFactory(Class factory) {
        this.xmlFactory = factory;
    }
    // generate AST

    public AST compileToAST(Reader reader) {
        try {
            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);
            return parser.xquery();
        } catch (IOException e) {
            throw new CompilerException(e);
        }
    }

    public AST compileToAST(String xquery) {
        return compileToAST(new StringReader(xquery));
    }

    public AST compileToAST(FileInputStream input) {
        Reader reader = new InputStreamReader(input);
        return compileToAST(reader);
    }

    public AST compileToAST(File path) {
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

    // generate bytecode

    public byte[] compileToByteArray(AST ast, String className) {
        Assembler assembler = new Assembler(className, ast, namespace, xmlFactory);
        return assembler.toByteArray();
    }

    public void compileToFile(AST ast, String className, File path) {
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

    public Class compileToClass(AST ast, String className) {
        return loader.define(className, compileToByteArray(ast, className));
    }

    public XQuery compileToXQuery(AST ast, String className) {
        Class c = compileToClass(ast, className);
        try {
            return (XQuery)c.newInstance();
        } catch (InstantiationException e) {
            throw new CompilerException(e);
        } catch (IllegalAccessException e) {
            throw new CompilerException(e);
        }
    }

    public XQuery compileToXQuery(AST ast) {
        return compileToXQuery(ast, randomClassName());
    }

    // one step

    public XQuery compile(String xquery) {
        return compileToXQuery(compileToAST(xquery));
    }

    public Object eval(String xquery) {
        return compile(xquery).eval();
    }

    // class loader

    public class DefaultClassLoader extends ClassLoader {
        public Class<?> define(String className, byte[] bytecode) {
            return super.defineClass(className, bytecode, 0, bytecode.length);
        }
    }

    // class name

    public String randomClassName() {
        return randomClassName(defaultPackage);
    }

    public String randomClassNameWithPrefix(String prefix) {
        return randomClassName(defaultPackage, prefix);
    }

    public static String randomClassName(String packageName) {
        return randomClassName(packageName, "Q$");
    }

    public static String randomClassName(String packageName, String prefix) {
        return packageName + '.' + prefix + randomID();
    }

    public static String randomID() {
        return java.util.UUID.randomUUID().toString().replace('-', '_');
    }
}
