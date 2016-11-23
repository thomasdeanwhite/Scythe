package com.sheffield.instrumenter.instrumentation.visitors;

import com.sheffield.instrumenter.InstrumentationProperties;
import com.sheffield.instrumenter.instrumentation.InstrumentingClassLoader;
import test.classes.TestClass;

/**
 * Created by thomas on 23/11/2016.
 */
public class ClassTester {

    public static InstrumentingClassLoader ICL = null;

    static {
        ICL = InstrumentingClassLoader
                .getInstance();
        ICL.setShouldInstrument(true);

        InstrumentationProperties.WRITE_CLASS = true;
        InstrumentationProperties.BYTECODE_DIR = TestClass.class.getResource
                ("").getFile();
    }

    private static Class instrumentedClass = null;

    public static Class getInstrumentedTestClass()
            throws ClassNotFoundException {
        if (instrumentedClass != null){
            return instrumentedClass;
        }
        instrumentedClass = ICL.loadClass(TestClass.class.getCanonicalName(),
                false);
        return instrumentedClass;
    }



}