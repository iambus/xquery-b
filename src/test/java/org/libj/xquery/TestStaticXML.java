package org.libj.xquery;

import org.junit.Test;

import static org.libj.xquery.Asserts.assertEvalString;

public class TestStaticXML {
    @Test
    public void test1() {
        assertEvalString("<x/>", "<x/>");
        assertEvalString("<x>{1+1}</x>", "<x>2</x>");
        assertEvalString("<x>1</x>", "<x>1</x>");
        assertEvalString("<x><y> <c/> </y> {1+1}</x>", "<x><y><c/></y>2</x>");
        assertEvalString("<x>{1}{2}{3}{4}</x>", "<x>1234</x>");
        assertEvalString("<x a='{1}' b='{2}'/>", "<x a=\"1\" b=\"2\"/>");
        testInnerXML();
    }

    @Test
    public void testInnerXML() {
        String xml = "<Event><ID>{1}</ID><name a='1'>{'Me'}</name><status>on</status></Event>";
        assertEvalString(xml + " / name", "<name a=\"1\">Me</name>");
        assertEvalString("fn:string(" + xml + " / name)", "Me");
        assertEvalString("fn:string(" + xml + " /name/@a)", "1");
    }
    @Test
    public void testInnerXMLAttribute() {
        String xml = "<Event><ID>{1}</ID><name a='{3}'/><status>on</status></Event>";
        assertEvalString("fn:string(" + xml + " /name/@a)", "3");
    }
    @Test
    public void testInnerXML2() {
        String xml = "<Event a='{1}'><X><name>{'Me'}</name></X></Event>";
        assertEvalString(xml + " / X / name", "<name>Me</name>");
    }
}
