package com.scythe.instrumenter.instrumentation.objectrepresentation;

import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.instrumenter.testcase.TestCaseWrapper;

import java.util.ArrayList;
import java.util.List;

public abstract class CoverableGoal {
  protected String className;
  protected String methodName;
  protected int lineNumber;
  protected int goalId;
  protected List<TestCaseWrapper> coveredBy = new ArrayList<TestCaseWrapper>();

  protected CoverableGoal(String className, String methodName, int lineNumber) {
    this.className = className;
    this.lineNumber = lineNumber;
    this.methodName = methodName;
  }

  public int getGoalId() {
    return goalId;
  }

  public void setGoalId(int goalId) {
    this.goalId = goalId;
  }

  public void setClassName(String name) {
    className = name;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public String getClassName() {
    return className;
  }

  public String getMethodName() {
    return methodName;
  }

  public void addCoveringTest(TestCaseWrapper t) {
    coveredBy.add(t);
  }

  /**
   * This method is reliant on a few things - firstly, {@link InstrumentationProperties#TRACK_ACTIVE_TESTCASE} <i>must</i> be true. Setting this value to true will automatically populate this list,
   * while if it is not true then this data must be collected manually. Secondly, in subclassing this class to make {@link Branch}, there are test cases that cover both the true & false branches of a
   * branch. This information is collected separately, but still reliant on {@link InstrumentationProperties#TRACK_ACTIVE_TESTCASE}
   * 
   * @return A list of test cases that cover this goal. In the case of branches, it is a test case that covers true, false or both. For individual coverage goals then use
   *         {@link Branch#getTrueBranchCoveringTests()} and {@link Branch#getfalseBranchCoveringTests()}
   */
  public List<TestCaseWrapper> getCoveringTests() {
    return coveredBy;
  }

  public abstract void reset();
}
