package com.scythe.instrumenter.instrumentation.objectrepresentation;

import com.scythe.instrumenter.testcase.TestCaseWrapper;
import java.util.ArrayList;

public class Line extends CoverableGoal {
  private long hits;

  public Line(String className, String methodName, int lineNumber) {
    super(className, methodName, lineNumber);
  }

  public void hit(long newHits) {
    if (newHits >= 0 && newHits < Long.MAX_VALUE) {
      this.hits = newHits;
    } else {
      this.hits = Long.MAX_VALUE;
    }
  }

  public long getHits() {
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
