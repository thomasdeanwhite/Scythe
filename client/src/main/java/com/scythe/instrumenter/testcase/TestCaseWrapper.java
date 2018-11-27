package com.scythe.instrumenter.testcase;

import java.lang.reflect.Method;

public class TestCaseWrapper {
  private Class<?> testClass;
  private Method testMethod;

  public TestCaseWrapper(Class<?> testClass, Method testMethod) {
    this.testClass = testClass;
    this.testMethod = testMethod;
  }

  public Class<?> getTestClass() {
    return testClass;
  }

  public Method getTestMethod() {
    return testMethod;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof TestCaseWrapper)) {
      return false;
    }
    TestCaseWrapper otherWrapper = (TestCaseWrapper) other;
    return (testMethod != null && testMethod.equals(otherWrapper.testMethod))
        && (testClass != null && testClass.equals(otherWrapper.testClass));
  }
}
