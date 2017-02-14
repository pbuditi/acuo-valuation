package com.acuo.valuation.learning;

import org.junit.Test;

public class RandomValuationTest {

    @Test
    public void testRandomVar()
    {
        double startVar = 1;
        for(int i = 0; i < 100; i++)
        {
            int index = (i + 1)/2 ;
            double rate = 0.2;
            if(i % 2 == 0)
                System.out.println(startVar + rate * index);
            else
                System.out.println(startVar - rate * index);
        }
    }
}
