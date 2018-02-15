package com.scythe.instrumenter.analysis;

import com.scythe.instrumenter.analysis.task.AbstractTask;

public class InstrumentingTask extends AbstractTask {
  private String className;

  public InstrumentingTask(String className) {
    this.className = className;
  }

  @Override
  public String asString() {
    return "Instrumenting " + className;
  }
}