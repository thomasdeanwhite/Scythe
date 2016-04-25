package com.sheffield.instrumenter.instrumentation.objectrepresentation;

public abstract class CoverableGoal {
  protected String className;
  protected int lineNumber;
  protected int goalId;

  protected CoverableGoal(String className, int lineNumber) {
    this.className = className;
    this.lineNumber = lineNumber;
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

  public abstract void reset();
}
