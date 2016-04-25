package com.sheffield.instrumenter.instrumentation;

import com.sheffield.instrumenter.analysis.ClassAnalyzer;

/**
 * Created by thomas on 18/12/2015.
 */
public class LoggingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        ClassAnalyzer.throwableThrown(e);
    }
}
