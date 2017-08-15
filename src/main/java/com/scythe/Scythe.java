package com.scythe;

import com.google.gson.Gson;
import com.scythe.instrumenter.FileHandler;
import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.instrumenter.analysis.ClassAnalyzer;
import com.scythe.instrumenter.instrumentation.objectrepresentation.LineHit;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

/**
 * Created by thomas on 15/08/2017.
 */
public class Scythe {

    private static void addExitLogger() {
        System.setSecurityManager(new LogOnExitSecurityManager());
    }

    private static Gson gson = new Gson();

    private static class LogOnExitSecurityManager extends SecurityManager {

        @Override
        public void checkExit(int status) {
            Map<Integer, Map<Integer, LineHit>> lines = ClassAnalyzer
                    .getRawLines();

            if (!InstrumentationProperties.OUTPUT.contains("%s")){
                InstrumentationProperties.OUTPUT = InstrumentationProperties
                        .OUTPUT + ".%s.JSON";
            }

            File outputLines = new File(String.format(InstrumentationProperties
                    .OUTPUT, "lines"));

            if (!outputLines.exists()){
                if (!outputLines.getParentFile().exists()){
                    outputLines.getParentFile().mkdirs();
                }
            } else {
                int backupNumber = 0;

                File backup = new File(String.format(InstrumentationProperties
                        .OUTPUT, "lines.backup." + backupNumber));

                while(backup.exists()){
                    backup = new File(String.format(InstrumentationProperties
                            .OUTPUT, "lines.backup." + backupNumber++));
                }

                outputLines.renameTo(backup);

                outputLines = new File(String.format(InstrumentationProperties
                        .OUTPUT, "lines"));
            }

            try {
                outputLines.createNewFile();
                FileHandler.writeToFile(outputLines, gson.toJson(lines));
            } catch (IOException e) {
                e.printStackTrace(ClassAnalyzer.out);
            }

            //TODO: Implement storing branches
            File outputBranches = new File(String.format
                    (InstrumentationProperties
                    .OUTPUT, "branches"));
        }
    }

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

        addExitLogger();

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
