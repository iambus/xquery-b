package org.libj.xquery;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.libj.xquery.compiler.Compiler;
import org.libj.xquery.lexer.LexerException;
import org.libj.xquery.parser.ParserException;

public class Main {

    private static boolean isEOF(Exception e) {
        return e.getMessage().endsWith("<EOF:<EOF>>");
    }

    private static void e(Compiler compiler, BufferedReader in) throws IOException {
        String xquery = "";
        while (true) {
            String line = in.readLine();
            if (line == null) {
                System.exit(0);
            }
            if (line.matches("\\s*")) {
                continue;
            }
            xquery += line;
            try {
                System.out.println(compiler.eval(xquery));
                return;
            } catch (LexerException e) {
                if (isEOF(e)) {
                    continue;
                } else {
                    throw e;
                }
            } catch (ParserException e) {
                if (isEOF(e)) {
                    continue;
                } else {
                    throw e;
                }
            }
        }
    }

    public static void repl() throws IOException {
        Compiler compiler = new Compiler();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int n = 0;
        while (true) {
            System.out.print(String.format("(: %d :) ", ++n));
            System.out.flush();
            try {
                e(compiler, in);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            repl();
        }
        else if (args.length == 2) {
            String xqueryPath = args[0];
            String className = args[0];
            File source = new File(xqueryPath);
            File target = new File(className.replace('.', '/')+".class");
            Compile.compileToFile(Compile.compileToAST(source), className, target);
        }
        else {
            System.out.println("java org.libj.xquery.Main");
            System.out.println("java org.libj.xquery.Main xquery-path class-name");
        }
    }
}
