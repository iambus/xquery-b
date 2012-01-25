xquery-b
=============
A XQuery to Java Bytecode compiler.

xquery-b is a XQuery to Java Bytecode compiler. Small in size, fast in performance, easy in use.
It aims to generate the fastest bytecode on JVM.

Quickstart
----------
Use in Java:

	import org.libj.xquery.Compiler;

	XQuery q = Compiler.compile("string(<a>3+5 is {3+5}</a>)");
	System.out.println(q.eval());

REPL from command line:

	java org.libj.xquery.Compiler

Installation
------------
...

API
---
It's a very new project (started from 2012-01-23). So there is no doc yet. Keep follow me ^_^


Comparison
----------
It's unfair to compare my project with any other XQuery implementations, because it's new! Please revisit this section a few days later...

Feature Table
------------
Be patient...

Coming soon
-----------
1. xpath
2. Java interop
3. type annotation/inference
4. Free variables

