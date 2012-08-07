package org.libj.xquery;

import org.junit.Test;

import static org.libj.xquery.Asserts.assertEvalString;

public class TestStaticXML {
    @Test
    public void test1() {
        assertEvalString("<x/>", "<x/>");
        assertEvalString("<x>{1+1}</x>", "<x>2</x>");
        assertEvalString("<x>1</x>", "<x>1</x>");
        assertEvalString("<x><y> <c/> </y> {1+1}</x>", "<x><y> <c/> </y> 2</x>");
    }
}
