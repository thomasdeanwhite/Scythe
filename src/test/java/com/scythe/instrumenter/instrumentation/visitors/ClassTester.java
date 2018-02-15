package com.scythe.instrumenter.instrumentation.visitors;

import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.instrumenter.instrumentation.InstrumentingClassLoader;
import test.classes.ExampleClass;
import test.classes.SubExampleClass;

/**
 * Created by thomas on 23/11/2016.
 */
public class ClassTester {

    public static InstrumentingClassLoader ICL = null;

    static {
        ICL = InstrumentingClassLoader
                .getInstance();

        InstrumentationProperties.WRITE_CLASS = true;
        InstrumentationProperties.BYTECODE_DIR = ExampleClass.class.getResource
                ("").getFile();

        ICL.setBuildDependencyTree(true);
    }

    private static Class instrumentedClass = null;
    private static Class instrumentedClass2 = null;

    public static Class getInstrumentedTestClass()
            throws ClassNotFoundException {
        if (instrumentedClass != null){
            return instrumentedClass;
        }
        instrumentedClass = ICL.loadClass(ExampleClass.class.getCanonicalName(),
                false);
        return instrumentedClass;
    }

    private static Class subInstrumentedClass = null;

    public static Class getSubInstrumentedTestClass()
            throws ClassNotFoundException {
        if (subInstrumentedClass != null){
            return subInstrumentedClass;
        }
        subInstrumentedClass = ICL.loadClass(SubExampleClass.class
                        .getCanonicalName(),
                false);
        return subInstrumentedClass;
    }



}
