package com.sheffield.instrumenter.util;

import com.sheffield.instrumenter.InstrumentationProperties;
import com.sheffield.instrumenter.PropertySource;
import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Branch;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Line;
import com.sheffield.instrumenter.instrumentation.visitors.ClassTester;
import com.sheffield.util.ClassUtils;
import org.junit.Before;
import org.junit.Test;
import test.classes.ExampleClass;
import test.classes.ExampleEnum;
import test.classes.ExampleInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

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
