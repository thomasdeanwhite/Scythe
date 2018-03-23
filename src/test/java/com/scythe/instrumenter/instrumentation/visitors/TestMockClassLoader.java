package com.scythe.instrumenter.instrumentation.visitors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import test.classes.ExampleClass;

public class TestMockClassLoader {

  @Test
  public void testClassIsNotInstrumented() throws ClassNotFoundException {
    Class<?> cl = ClassTester.getNonInstrumentedTestClass();
    try{
      cl.getDeclaredMethod(ArrayClassVisitor.COUNTER_METHOD_NAME);
      fail("Should not contain "+ArrayClassVisitor.COUNTER_METHOD_NAME);
    }catch(NoSuchMethodException e){

    }
  }

  @Test
  public void testSameNumberOfMethods() throws ClassNotFoundException {
    Class<?> cl = ExampleClass.class;
    Class<?> nonInstrumented = ClassTester.getNonInstrumentedTestClass();
    assertEquals(cl.getMethods().length, nonInstrumented.getMethods().length);
  }
}
