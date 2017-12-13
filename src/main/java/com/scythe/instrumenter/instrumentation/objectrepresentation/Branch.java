package com.scythe.instrumenter.instrumentation.objectrepresentation;

import com.scythe.instrumenter.testcase.TestCaseWrapper;

import java.util.ArrayList;
import java.util.List;

public class Branch extends CoverableGoal {
  private int hits;
  private List<TestCaseWrapper> trueBranchCoveringTests = new ArrayList<TestCaseWrapper>();
  private List<TestCaseWrapper> falseBranchCoveringTests = new ArrayList<TestCaseWrapper>();

  public Branch(String className, String methodName, int lineNumber) {
    super(className, methodName, lineNumber);
  }

  public void hit(int hits) {
    this.hits += hits;
  }

  public int getHits() {
    return hits;
  }

  @Override
  public void reset() {
    hits = 0;
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
    clone.hits = hits;
    clone.coveredBy = new ArrayList<TestCaseWrapper>(coveredBy);
    clone.coveredBy.addAll(super.getCoveringTests());
    return clone;
  }

}
