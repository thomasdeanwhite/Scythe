package com.sheffield.instrumenter.instrumentation.objectrepresentation;

public class Branch extends CoverableGoal {
  private int trueHits;
  private int falseHits;

  public Branch(String className, int lineNumber) {
    super(className, lineNumber);
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
    return lineNumber + "#true, " + lineNumber + "#false";
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
  public void setClassName(String name) {
    className = name;
  }

  @Override
  public Branch clone() {
    Branch clone = new Branch(className, lineNumber);
    clone.setGoalId(goalId);
    clone.trueHits = trueHits;
    clone.falseHits = falseHits;
    return clone;
  }

}
