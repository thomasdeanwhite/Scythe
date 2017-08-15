package com.scythe.instrumenter.instrumentation.objectrepresentation;

import com.scythe.instrumenter.testcase.TestCaseWrapper;

import java.util.ArrayList;
import java.util.List;

public class Branch extends CoverableGoal {
  private int trueHits;
  private int falseHits;
  private List<TestCaseWrapper> trueBranchCoveringTests = new ArrayList<TestCaseWrapper>();
  private List<TestCaseWrapper> falseBranchCoveringTests = new ArrayList<TestCaseWrapper>();

  public Branch(String className, String methodName, int lineNumber) {
    super(className, methodName, lineNumber);
  }

  public void trueHit(int trueHits) {
    this.trueHits += trueHits;
  }

  public void falseHit(int falseHits) {
    this.falseHits += falseHits;
  }

  public int getTrueHits() {
    return trueHits;
  }

  public int getFalseHits() {
    return falseHits;
  }

  @Override
  public void reset() {
    trueHits = 0;
    falseHits = 0;
  }

  @Override
  public String toString() {
    return lineNumber + "-" + goalId + "#true, " + lineNumber + "#false";
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Branch)) {
      return false;
    }
    if (lineNumber == ((Branch) other).lineNumber && className.equals(((Branch) other).className)
        && goalId == ((Branch) other).goalId) {
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + lineNumber;
    result = prime * result + className.hashCode();
    result = prime * result + goalId;
    return result;
  }

  @Override
  public void setClassName(String name) {
    className = name;
  }

  @Override
  public Branch clone() {
    Branch clone = new Branch(className, methodName, lineNumber);
    clone.setGoalId(goalId);
    clone.trueHits = trueHits;
    clone.falseHits = falseHits;
    clone.coveredBy = new ArrayList<TestCaseWrapper>(coveredBy);
    clone.falseBranchCoveringTests = new ArrayList<TestCaseWrapper>(falseBranchCoveringTests);
    clone.trueBranchCoveringTests = new ArrayList<TestCaseWrapper>(trueBranchCoveringTests);
    return clone;
  }

  @Override
  public void addCoveringTest(TestCaseWrapper t) {
    super.addCoveringTest(t);
    if (trueHits > 0) {
      trueBranchCoveringTests.add(t);
    }
    if (falseHits > 0) {
      falseBranchCoveringTests.add(t);
    }
  }

  public List<TestCaseWrapper> getTrueBranchCoveringTests() {
    return trueBranchCoveringTests;
  }

  public List<TestCaseWrapper> getfalseBranchCoveringTests() {
    return falseBranchCoveringTests;
  }

}