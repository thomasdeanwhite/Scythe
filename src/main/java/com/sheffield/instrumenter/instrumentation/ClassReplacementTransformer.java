package com.sheffield.instrumenter.instrumentation;

import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.Properties.InstrumentationApproach;
import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.analysis.InstrumentingTask;
import com.sheffield.instrumenter.analysis.task.Task;
import com.sheffield.instrumenter.analysis.task.TaskTimer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;

public class ClassReplacementTransformer {

    private static ArrayList<String> seenClasses = new ArrayList<String>();
    private boolean shouldWriteClass = false;
    public ArrayList<ShouldInstrumentChecker> shouldInstrumentCheckers;

    public interface ShouldInstrumentChecker {
        boolean shouldInstrument(String className);
    }

    public ClassReplacementTransformer() {
        shouldInstrumentCheckers = new ArrayList<ShouldInstrumentChecker>();
    }

    public void setWriteClasses(boolean b) {
        shouldWriteClass = b;
    }

    public byte[] transform(String cName, byte[] cBytes, ClassVisitor cv, ClassWriter cw)
            throws IllegalClassFormatException {

        if (seenClasses.contains(cName)) {
            throw new IllegalClassFormatException("Class already loaded!");
        }
        seenClasses.add(cName);

        if (Properties.EXILED_CLASSES != null) {
            for (String s : Properties.EXILED_CLASSES) {
                if (cName.equals(s)) {
                    // App.out.println("Not loaded class " + cName);
                    throw new IllegalClassFormatException();
                }
            }
        }

        // App.out.println("Loaded class " + cName);
        try {
            if (!shouldInstrumentClass(cName)) {
                return cBytes;
            }

            // if (iClass == null) {
            // iClass = TestingClassLoader.getClassLoader().loadClass(cName,
            // cBytes);
            // }

            // iClass.

            InputStream ins = new ByteArrayInputStream(cBytes);
            byte[] newClass = cBytes;
            try {
                ClassReader cr = new ClassReader(ins);
                Task instrumentingTask = new InstrumentingTask(cName);
                if (Properties.LOG) {
                    TaskTimer.taskStart(instrumentingTask);
                }
                try {
                    cr.accept(cv, ClassReader.EXPAND_FRAMES);
                } catch (Throwable t) {
                    t.printStackTrace(ClassAnalyzer.out);
                }
                newClass = cw.toByteArray();
                if (Properties.LOG) {
                    TaskTimer.taskEnd(instrumentingTask);
                }
            } catch (IOException e) {
                e.printStackTrace(ClassAnalyzer.out);
            }
            if (shouldWriteClass) {
                File file = new File("classes/" + cName + ".class");
                file.getParentFile().mkdirs();
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
                fos.write(newClass);
                fos.close();
            }
            return newClass;
        } catch (Exception e) {
            e.printStackTrace(ClassAnalyzer.out);
            return cw.toByteArray();

        }

    }

    private static final ArrayList<String> forbiddenPackages = new ArrayList<String>();

    static {
        String[] defaultHiddenPackages = new String[] { "com/sun", "java/", "sun/", "jdk/",
                "com/sheffield/instrumenter", "org/eclipse" };

        // String[] defaultHiddenPackages = new
        // String[]{"com/sheffield/leapmotion", "com/google/gson",
        // "com/sun", "java/", "sun/", "com/leapmotion", "jdk/", "javax/",
        // "org/json", "org/apache/commons/cli",
        // "com/sheffield/instrumenter", "com/dpaterson", "org/junit"};

        for (String s : defaultHiddenPackages) {
            forbiddenPackages.add(s);
        }
    }

    /**
     * Add a package that should not be instrumented.
     *
     * @param forbiddenPackage
     *            the package name not to be instrumented, using / for subpackages (e.g. org/junit)
     */
    public static void addForbiddenPackage(String forbiddenPackage) {
        forbiddenPackages.add(forbiddenPackage);
    }

    public boolean shouldInstrumentClass(String className) {
        if (Properties.INSTRUMENTATION_APPROACH == InstrumentationApproach.NONE) {
            return false;
        }
        if (className == null) {
            return false;
        }
        if (className.contains(".")) {
            className = className.replace(".", "/");
        }
        if (isForbiddenPackage(className)) {
            return false;
        }
        if (className.contains("/")) {
            className = className.replace("/", ".");
        }

        for (ShouldInstrumentChecker sic : shouldInstrumentCheckers) {
            if (!sic.shouldInstrument(className)) {
                return false;
            }
        }

        if (Properties.INSTRUMENTED_PACKAGES == null) {
            return true;
        }
        for (String s : Properties.INSTRUMENTED_PACKAGES) {
            if (className.startsWith(s)) {
                return true;
            }
        }

        return false;
    }

    public void addShouldInstrumentChecker(ShouldInstrumentChecker s) {
        shouldInstrumentCheckers.add(s);
    }

    public void removeShouldInstrumentChecker(ShouldInstrumentChecker s) {
        shouldInstrumentCheckers.remove(s);
    }

    public static boolean isForbiddenPackage(String clazz) {
        for (String s : forbiddenPackages) {
            if (clazz.startsWith(s)) {
                return true;
            }
        }
        return false;
    }
}
