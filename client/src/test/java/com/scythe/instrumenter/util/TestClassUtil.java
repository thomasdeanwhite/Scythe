package com.scythe.instrumenter.util;

import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.instrumenter.instrumentation.visitors.ClassTester;
import com.scythe.util.ClassUtils;
import org.junit.Test;
import test.classes.ExampleEnum;
import test.classes.ExampleInterface;

import static org.junit.Assert.*;

/**
 * Created by thomas on 23/11/2016.
 */
public class TestClassUtil {

    Class ic = null;
    Class original = null;

    @Test
    public void canInstrument() {
        try {
            assertTrue(ClassUtils
                    .isInstrumented(ClassTester.getInstrumentedTestClass()));
        } catch (ClassNotFoundException e) {
            fail("Class cannot be found.");
        }
    }


    @Test
    public void cantInstrumentInterface() {
        assertFalse("Interface is being instrumented!",
                ClassUtils.isInstrumented(ExampleInterface.class));
    }


    @Test
    public void cantInstrumentEnum() {
        assertFalse("Enum is being instrumented!",
                ClassUtils.isInstrumented(ExampleEnum.class));
    }


    @Test
    public void cantInstrumentAnnotation() {
        assertFalse("Annotation is being instrumented!",
                ClassUtils.isInstrumented(InstrumentationProperties.Parameter.class));
    }


}
