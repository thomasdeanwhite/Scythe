package com.scythe;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scythe.instrumenter.FileHandler;
import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.instrumenter.analysis.ClassAnalyzer;
import com.scythe.instrumenter.instrumentation.objectrepresentation.LineHit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Type;
import java.security.Permission;
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
        public void checkPermission(Permission perm) {

        }

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

            int backupNumber = 0;

            if (!outputLines.exists()){
                if (outputLines.getParentFile() != null && !outputLines
                        .getParentFile()
                        .exists()){
                    outputLines.getParentFile().mkdirs();
                }
            } else {
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

            File classOutput = new File(String.format(InstrumentationProperties
                    .OUTPUT, "classes"));

            Map<String, Integer> classMappings = ClassAnalyzer
                    .getClassMapping();

            if (classOutput.exists()){
                File classBackup = new File(String.format
                        (InstrumentationProperties
                        .OUTPUT, "classes." + backupNumber));
                classOutput.renameTo(classBackup);
                classOutput = new File(String.format(InstrumentationProperties
                        .OUTPUT, "classes"));
            }

            try {
                classOutput.createNewFile();
                FileHandler.writeToFile(classOutput, gson.toJson(classMappings));
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
        if (arg != null && arg.length() > 0) {
            InstrumentationProperties.instance().setOptions(arg.split(" "));
        }
        addExitLogger();

    }

    public static void main(String... args){
        InstrumentationProperties.instance().setOptions(args);
        ClassAnalyzer.out.println(NAME);
        for (char c : NAME.substring(1).toCharArray()){
            ClassAnalyzer.out.println(c);
        }

        if (args.length > 0 && args[0].toLowerCase().contains("helpmd")){
            InstrumentationProperties.instance().printOptionsMd();
        } else if((args.length > 0 && args[0].toLowerCase().contains("helpmd")
                ) || args.length == 0) {
            InstrumentationProperties.instance().printOptions();
        }

        if (!InstrumentationProperties.OUTPUT.contains("%s")){
            InstrumentationProperties.OUTPUT = InstrumentationProperties
                    .OUTPUT + ".%s.JSON";
        }

        File outputLines = new File(String.format(InstrumentationProperties
                .OUTPUT, "lines"));

        File outputClasses = new File(String.format(InstrumentationProperties
                .OUTPUT, "classes"));

        if (!outputLines.exists()){
            ClassAnalyzer.out.println("File " + outputLines.getAbsolutePath()
                    + " does not exist!");
        }
        if (!outputClasses.exists()){
            ClassAnalyzer.out.println("File " + outputClasses.getAbsolutePath
                    () + " does not exist!");
        }

        ClassAnalyzer.out.println("Loaded lines covered and class files");

        try {
            Type typeLines = new TypeToken<Map<Integer, Map<Integer, LineHit>>>(){}
                    .getType();

            Map<Integer, Map<Integer, LineHit>> lines = gson.fromJson(FileHandler
                    .readFile(outputLines), typeLines);

            Type typeClasses = new TypeToken<Map<String, Integer>>(){}
                    .getType();

            Map<String, Integer> classes = gson.fromJson(FileHandler.readFile
                    (outputClasses), typeClasses);


            for (String s : classes.keySet()){
                File sourceFile = new File(InstrumentationProperties
                        .SOURCE_DIR + "/" + s.replace(".", "/"));

                String[] sourceLines = FileHandler.readFile(sourceFile).split
                        ("\n");

                Map<Integer, LineHit> classLines = lines.get(classes.get(s));

                for (int i = 0; i < sourceLines.length; i++){
                    int lineNumber = i+1;

                    String sourceLine = sourceLines[i];

                    if (classLines.containsKey(lineNumber)){
                        LineHit lh = classLines.get(lineNumber);

                        if (lh.getLine().getHits() < 0){
                            ClassAnalyzer.out.println("(hit) " + sourceLine);
                        } else {
                            ClassAnalyzer.out.println("(miss) " + sourceLine);
                        }
                    } else {
                        //assumed covered
                        ClassAnalyzer.out.println("(assumed) " + sourceLine);
                    }
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
