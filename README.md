xquery-b
=============
A XQuery to Java Bytecode compiler.

Quickstart
----------
	import org.libj.xquery.Compiler;

	XQuery q = Compiler.compile("string(<a>3+5 is {3+5}</a>)");
	System.out.println(q.eval());

