package org.libj.xquery.performance;

import org.junit.Test;

import static org.libj.xquery.Asserts.*;

public class TestDoubleForWherePerformance {
    @Test
    public void outerCondition() {
        /*
    for $i in (1 to 100*1000*1000)
    let $lv1 := 1000 * 100 * 100 * 1000 + 2234 + 7521 - 1.0 div 1.0 + 1024 div 3.1415926535
    where $i = 1
    return
        $i
         */
        String xquery = "for $i in (1 to 1000*1000) " +
                "for $j in (1 to 1000*1000) " +
                "where $i = <x>1</x>" +
                "return $i+$j";
        assertEvalMillis(xquery, 1000);
    }
    @Test
    public void andCondition() {
        /*
    for $i in (1 to 100*1000*1000)
    let $lv1 := 1000 * 100 * 100 * 1000 + 2234 + 7521 - 1.0 div 1.0 + 1024 div 3.1415926535
    where $i = 1
    return
        $i
         */
        String xquery = "for $i in (1 to 1000*1000) " +
                "for $j in (1 to 1000*1000) " +
                "where $i = <x>1</x> and $j = <x>2</x>" +
                "return $i+$j";
        assertEvalMillis(xquery, 1000);
    }
}
