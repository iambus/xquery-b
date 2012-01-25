xquery-b
=============
A XQuery to Java Bytecode compiler.

xquery-b is a XQuery to Java Bytecode compiler. Small in size, fast in performance, easy in use.
It aims to generate the fastest bytecode on JVM.

Quickstart
----------
	import org.libj.xquery.Compiler;

	XQuery q = Compiler.compile("string(<a>3+5 is {3+5}</a>)");
	System.out.println(q.eval());

Installation
------------
...

API
---
It's a very new project (started from 2012-01-23) at the time of this README is documented (2012-01-25). So there is no doc yet.


Comparison
----------
It's unfair to compare my project to any other XQuery implementations, because it's new! Please revisit this section a few days later...

Feature Table
------------
Be patient...

Coming soon
-----------
1. xpath
2. where clause
3. Java interop
4. Flatten list

