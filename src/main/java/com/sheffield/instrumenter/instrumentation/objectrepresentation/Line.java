package com.sheffield.instrumenter.instrumentation.objectrepresentation;

import java.util.ArrayList;

import com.sheffield.instrumenter.testcase.TestCaseWrapper;

public class Line extends CoverableGoal {
  private int hits;

  public Line(String className, int lineNumber) {
    super(className, lineNumber);
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
    Line clone = new Line(className, lineNumber);
    clone.hits = hits;
    clone.goalId = goalId;
    clone.coveredBy = new ArrayList<TestCaseWrapper>(coveredBy);
    return clone;
  }
}
