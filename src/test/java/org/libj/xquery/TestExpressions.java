package org.libj.xquery;

import org.junit.Test;

import static org.libj.xquery.Asserts.*;

public class TestExpressions {
    @Test
    public void testNumberLiterals() {
        assertEval("1", 1);
        assertEval("1.1", 1.1);
        assertEval(" 1 ", 1);
        assertEval(" 1.1 ", 1.1);
    }

    @Test
    public void testStringLiterals() {
        assertEval("'1'", "1");
        assertEval("'a'", "a");
        assertEval("'a b'", "a b");
        assertEval("\"1\"", "1");
        assertEval("\"a\"", "a");
        assertEval("\"a b\"", "a b");
    }

    @Test
    public void testNodeLiterals() {
        assertEvalString(" <a/> ", "<a/>");
    }

    @Test
    public void testArithmeticExpressions() {
        assertEval(" 1 + 1 ", 2);
        assertEval(" 1 + 3 * (5 + 2) - 10 div 3", 19);
        assertEval(" 11 + (-7)", 4);
        assertEval(" 101 mod 7", 3);
    }

    @Test
    public void testComparison() {
        assertEval("1+1=2", true);
        assertEval("1=1", true);
        assertEval("1=2", false);
        assertEval("10 mod 2 = 0", true);
        assertEval("10 mod 3 = 0", false);
        assertEval("10 mod 3 = 1", true);
        assertEval("10 mod 2 != 0", false);
        assertEval("10 mod 3 != 0", true);
        assertEval("10 mod 3 != 1", false);
    }
    @Test
    public void testLogical() {
        assertEval("1 = 1 and 2 = 2", true);
        assertEval("1 = 2 and 1 = 1", false);
        assertEval("1 = 1 and 1 = 2", false);
        assertEval("1 = 2 and 1 = 2", false);
        assertEval("1 = 1 or 2 = 2", true);
        assertEval("1 = 2 or 1 = 1", true);
        assertEval("1 = 1 or 1 = 2", true);
        assertEval("1 = 2 or 1 = 2", false);
        assertEval("1 = 1 and 2 = 2 and 3 = 3", true);
        assertEval("1 = 1 and 2 = 2 and 3 = 4", false);
        assertEval("1 = 2 or 1 = 2 or 1 = 3", false);
        assertEval("1 = 2 or 1 = 2 or 1 = 1", true);
        assertEval("(1 = 2 or 1 = 1) and (1 = 2 or 3 = 2)", false);
        assertEval("(1 = 2 or 1 = 1) and (1 = 1 or 3 = 3)", true);
    }
    @Test
    public void testList() {
        assertEvalString(" 1 to -1 ", "");
        assertEvalString(" 1 to 2 ", "1 2");
        assertEvalString(" 3 to 3 ", "3");
        assertEvalString(" (11 to 17)[0] ", "");
        assertEvalString(" (11 to 17)[1] ", "11");
        assertEvalString(" (11 to 17)[7] ", "17");
        assertEvalString(" (11 to 17)[8] ", "");
        assertEvalString(" (1, 2, 3) ", "1 2 3");
        assertEval(" (1) ", 1);
        assertEvalString(" (1,'2') ", "1 2");
        assertEvalString("(4,5,6)[2][1]", "5");
        assertEvalString("(4,5,6)[-1]", "");
        assertEvalString("(1 to 1)[-2]", "");
        assertEvalString("(1,2,3)", "1 2 3");
        assertEvalString("(1, 2, 3)", "1 2 3");
        assertEvalString("(1,2, 3, (4,5,6), <x/>, <x>2</x>)", "1 2 3 4 5 6<x/><x>2</x>");
        assertEvalString("(10*100-2 to 10*100)", "998 999 1000");
    }
    @Test
    public void testFor() {
        assertEvalString("for $x in (3,4,5) return $x", "3 4 5");
        assertEvalString("for $x in (3,4,5) return $x - 2", "1 2 3");
        assertEvalString("for $x in (1 to 10) where $x mod 2 = 1 return $x", "1 3 5 7 9");
        assertEvalString("for $x in (1 to 10) where $x mod 2 = 1 return $x", "1 3 5 7 9");
        assertEvalString("for $i in 3 return $i", "3");
        assertEvalString("for $i in (1,2, 3, (4,5,6)) return $i", "1 2 3 4 5 6");
        assertEvalString("for $i in 1 to 10 where $i mod 2 = 0 return $i", "2 4 6 8 10");
        assertEvalString("for $x in 1 to 10 let $i := 1 where $x mod 2 = 0 return ($i, $x)", "1 2 1 4 1 6 1 8 1 10");
        assertEvalString("for $i in 1 to 10 where 2 = 0 return 2", "");
    }
    @Test
    public void testLet() {
        assertEvalString("let $i := 2 return $i", "2");
        assertEvalString("let $x := 7 return 3", "3");
        assertEvalString("let $x := 7 return 3+7", "10");
        assertEvalString("let $x := (1,2,4) return $x", "1 2 4");
        assertEvalString("let $x := 7 where $x = 7 return $x", "7");
        assertEvalString("let $x := 7 where $x = 3 return $x", "");
    }
    @Test
    public void testNode() {
        assertEvalString("<x/>", "<x/>");
        assertEvalString("<x>{2}</x>", "<x>2</x>");
        assertEvalString("<x>$x</x>", "<x>$x</x>");
        assertEvalString("<x a='{1+3}'>$x</x>", "<x a='4'>$x</x>");
        assertEvalString("let $x := 3 return <x><w/><a>{$x}</a></x>", "<x><w/><a>3</a></x>");
        assertEvalString("for $x in (3,4,5) return <a>{$x}</a>", "<a>3</a><a>4</a><a>5</a>");
        assertEvalString("let $x :='i' return <a attr='{$x}'>[{$x}]<x v='{$x}'/></a>", "<a attr='i'>[i]<x v='i'/></a>");
        assertEvalString("for $i in (1 to 10) return <a>{$i}</a>", "<a>1</a><a>2</a><a>3</a><a>4</a><a>5</a><a>6</a><a>7</a><a>8</a><a>9</a><a>10</a>");
    }
    @Test
    public void testXPath() {
        assertEvalString("let $x := <x><a>2</a></x> return $x/a", "<a>2</a>");
        assertEvalString("let $x := <x><a>2</a></x> return $x/x/a", "");
    }
    @Test
    public void testQexoExamples() {
        // www.gnu.org/software/qexo/index.html
        // http://www.gnu.org/software/qexo/XQuery-Intro.html
        assertEvalString("for $x in (3,4,5) return <a>{$x}</a>", "<a>3</a><a>4</a><a>5</a>");
//        assertEvalString("string(<a>3+5 is {3+5}</a>)", "3+5 is 8");
    }
}
