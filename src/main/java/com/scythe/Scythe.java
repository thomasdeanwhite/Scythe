package com.scythe;

import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.instrumenter.analysis.ClassAnalyzer;

import java.lang.instrument.Instrumentation;

/**
 * Created by thomas on 15/08/2017.
 */
public class Scythe {

    private static final String NAME = InstrumentationProperties.NAME;

    /**
     * Premain that will be triggered when application runs with this
     * attached as a Java agent.
     *
     * @param arg   runtime properties to change
     * @param instr Instrumentation instance to attach a ClassFileTransformer
     */
    public static void premain(String arg, Instrumentation instr) {
        InstrumentationProperties.instance().setOptions(arg.split(" "));

    }

    public static void main(String... args){
        ClassAnalyzer.out.println(NAME + " Instrumentation\n" +
                "Options:");

        if (args.length > 0 && args[0].toLowerCase().contains("helpmd")){
            InstrumentationProperties.instance().printOptionsMd();
        } else {
            InstrumentationProperties.instance().printOptions();
        }

        ClassAnalyzer.out.println(NAME + " attaches to processed as a Java " +
                "Agent. Please use javaagent:" + NAME + ".jar to run as a " +
                "java Agent");
    }

}
