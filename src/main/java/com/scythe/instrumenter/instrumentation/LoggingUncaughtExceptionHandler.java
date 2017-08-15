package com.scythe.instrumenter.instrumentation;

import com.scythe.instrumenter.analysis.ClassAnalyzer;

/**
 * Created by thomas on 18/12/2015.
 */
public class LoggingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace(ClassAnalyzer.out);
        ClassAnalyzer.throwableThrown(e);
    }
}
