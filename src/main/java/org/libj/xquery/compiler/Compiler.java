package org.libj.xquery.compiler;

import org.libj.xquery.XQuery;
import org.libj.xquery.lexer.Lexer;
import org.libj.xquery.lisp.Cons;
import org.libj.xquery.namespace.*;
import org.libj.xquery.parser.Parser;

import java.io.*;
import java.util.ArrayList;

public class Compiler {

    private RootNamespace namespace = new DefaultRootNamespace();
    private DefaultClassLoader loader = new DefaultClassLoader();

    private String defaultPackage = "org.libj.xquery.dynamic";
    private Class xmlFactory = Constants.DEFAUL_XML_FACTORY_IMPLEMENTATION_CLASS;

    private boolean disableFreeVariables = false;
    private boolean generateInnerClasses = true;

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

    public Cons compileToAST(Reader reader) {
        try {
            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);
            return parser.xquery();
        } catch (IOException e) {
            throw new CompilerException(e);
        }
    }

    public Cons compileToAST(String xquery) {
        return compileToAST(new StringReader(xquery));
    }

    public Cons compileToAST(FileInputStream input) {
        Reader reader = new InputStreamReader(input);
        return compileToAST(reader);
    }

    public Cons compileToAST(File path) {
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

    public Target compileToTarget(Cons ast, String className, String...vars) {
        if (className == null || className.isEmpty()) {
            className = randomClassName();
        }
        Assembler assembler = new Assembler(className, ast, vars, new LocalNamespace(namespace), xmlFactory, generateInnerClasses);
        if (disableFreeVariables && !assembler.getFreeVariables().isEmpty()) {
            throw new RuntimeException("Unresolved free variables: "+assembler.getFreeVariables());
        }
        return assembler.compile();
    }

    public byte[] compileToByteArray(Cons ast, String className, String...vars) {
        Target t = compileToTarget(ast, className, vars);
        if (!t.getInnerClasses().isEmpty()) {
            throw new RuntimeException("There are inner classes!");
        }
        return t.getMainClass().getBytes();
    }

    public void compileToDir(Cons ast, String className, File dir, String... vars) {
        Target t = compileToTarget(ast, className, vars);
        ArrayList<ClassInfo> classes = new ArrayList<ClassInfo>(t.getInnerClasses());
        classes.add(t.getMainClass());
        try {
            for (ClassInfo c : classes) {
                File path = new File(dir, c.getClassName() + ".class");
                FileOutputStream output = new FileOutputStream(path);
                try {
                    output.write(c.getBytes());
                } finally {
                    output.close();
                }
            }
        } catch (IOException e) {
            throw new CompilerException(e);
        }
    }

    public void compileToFile(Cons ast, String className, File path, String...vars) {
        try {
            byte[] bytes = compileToByteArray(ast, className, vars);
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

    public Class compileToClass(Cons ast, String className, String...vars) {
        if (className == null || className.isEmpty()) {
            className = randomClassName();
        }
        Target target = compileToTarget(ast, className, vars);
        for (ClassInfo c: target.getInnerClasses()) {
            loader.define(c.getClassName().replace('/', '.'), c.getBytes());
        }
        return loader.define(className, target.getMainClass().getBytes());
    }

    public XQuery compileToXQuery(Cons ast, String className, String...vars) {
        Class c = compileToClass(ast, className, vars);
        try {
            return (XQuery)c.newInstance();
        } catch (InstantiationException e) {
            throw new CompilerException(e);
        } catch (IllegalAccessException e) {
            throw new CompilerException(e);
        }
    }

    public XQuery compileToXQuery(Cons ast) {
        return compileToXQuery(ast, randomClassName());
    }

    // one step

    public XQuery compile(String xquery, String...vars) {
        return compileToXQuery(compileToAST(xquery), null, vars);
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

    // options
    public void enableFreeVariables(boolean enable) {
        disableFreeVariables = !enable;
    }

    public void enableGenerateInnerClasses(boolean enable) {
        this.generateInnerClasses = enable;
    }

}
