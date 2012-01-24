package org.libj.xquery;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.libj.xquery.compiler.Compiler;

public class Main {
    public static void repl() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int n = 0;
        while (true) {
            System.out.print(String.format("(: %d :) ", ++n));
            System.out.flush();
            String line = in.readLine();
            if (line == null) {
                break;
            }
            if (line.matches("\\s*")) {
                continue;
            }
            try {
                System.out.println(Compiler.eval(line));
            }
            catch (Exception e) {
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
            Compiler.compileToFile(Compiler.compileToAST(source), className, target);
        }
        else {
            System.out.println("java org.libj.xquery.Main");
            System.out.println("java org.libj.xquery.Main xquery-path class-name");
        }
    }
}
