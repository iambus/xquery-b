package org.libj.xquery;

import org.junit.Test;

import static org.libj.xquery.Asserts.assertEvalString;

public class TestXPathSimple {
    @Test
    public void testAttr() {
        assertEvalString("let $x := <x a='1'></x> return $x/@a", "1");
        assertEvalString("let $x := <x aa='1'></x> return $x/@a", "");
        assertEvalString("let $x := <x b='1'></x> return $x/@a", "");
        assertEvalString("let $x := <x a=\"\" b=''></x> return $x/@b", "");
        assertEvalString("let $x := <x a=\"\" b='123'></x> return $x/@b", "123");
    }
}
