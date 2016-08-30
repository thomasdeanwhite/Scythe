package com.sheffield.instrumenter.util;

import com.sheffield.util.ClassNameUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomas on 30/08/2016.
 */
public class ClassNameUtilsTest {

    @Test
    public void testSlashes(){
        String className = "some/random/Class";

        assertEquals("some.random.Class", ClassNameUtils.replaceSlashes(className));
    }

    @Test
    public void testDots(){
        String className = "some.random.Class";

        assertEquals("some/random/Class", ClassNameUtils.replaceDots(className));
    }

    @Test
    public void testStandardise(){
        String className = " some/random.Class ";

        assertEquals("some/random/Class", ClassNameUtils.standardise(className));
    }
}
