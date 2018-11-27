package com.scythe.instrumenter.instrumentation.visitors;

import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.instrumenter.analysis.ClassAnalyzer;
import com.scythe.instrumenter.instrumentation.ClassReplacementTransformer;
import com.scythe.instrumenter.instrumentation.InstrumentingClassLoader;
import com.scythe.instrumenter.instrumentation.MockClassLoader;
import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import org.junit.Before;
import test.classes.ExampleClass;
import test.classes.SubExampleClass;

/**
 * Created by thomas on 23/11/2016.
 */
public class ClassTester {

  private static File tmpFile;
  public static InstrumentingClassLoader ICL = null;
  private static final boolean WRITE_CLASS = true;

  static {
    ICL = InstrumentingClassLoader.getInstance();
    try {
      tmpFile = Files.createTempDirectory("bytecode").toFile();
      tmpFile.deleteOnExit();
    } catch (IOException e) {
      e.printStackTrace();
    }
    ICL.setBuildDependencyTree(true);
  }

  @Before
  public void setup() {
    ClassAnalyzer.softReset();
    if (WRITE_CLASS) {
      InstrumentationProperties.WRITE_CLASS = true;
      InstrumentationProperties.BYTECODE_DIR = tmpFile.getAbsolutePath();
    }

  }


  private static Class instrumentedClass = null;
  private static Class instrumentedClass2 = null;

  private static ClassReplacementTransformer crt = new ClassReplacementTransformer();
  private static Class<?> nonInstrumentedClass = null;

  public static Class getInstrumentedTestClass()
      throws ClassNotFoundException {
    if (instrumentedClass != null) {
      return instrumentedClass;
    }
    instrumentedClass = ICL.loadClass(ExampleClass.class.getCanonicalName(),
        false);
    return instrumentedClass;
  }

  public static Class<?> getNonInstrumentedTestClass() throws ClassNotFoundException {
    if (nonInstrumentedClass == null) {
      MockClassLoader loader = new MockClassLoader(
          ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs(), crt);
      nonInstrumentedClass = loader.loadClass(ExampleClass.class.getCanonicalName());
    }
    return nonInstrumentedClass;
  }

  private static Class subInstrumentedClass = null;

  public static Class getSubInstrumentedTestClass()
      throws ClassNotFoundException {
    if (subInstrumentedClass != null) {
      return subInstrumentedClass;
    }
    subInstrumentedClass = ICL.loadClass(SubExampleClass.class
            .getCanonicalName(),
        false);
    return subInstrumentedClass;
  }


}
