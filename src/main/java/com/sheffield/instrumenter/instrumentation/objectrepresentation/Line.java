package com.sheffield.instrumenter.instrumentation.objectrepresentation;

import com.sheffield.instrumenter.testcase.TestCaseWrapper;

import java.util.ArrayList;

public class Line extends CoverableGoal {
  private int hits;

  public Line(String className, String methodName, int lineNumber) {
    super(className, methodName, lineNumber);
  }

  public void hit(int newHits) {
    this.hits += newHits;
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
    return String.valueOf(lineNumber);
  }

  @Override
  public Line clone() {
    Line clone = new Line(className, methodName, lineNumber);
    clone.hits = hits;
    clone.goalId = goalId;
    clone.coveredBy = new ArrayList<TestCaseWrapper>(coveredBy);
    return clone;
  }
}
