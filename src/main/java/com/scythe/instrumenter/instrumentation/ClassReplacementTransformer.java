package com.scythe.instrumenter.instrumentation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;

import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.instrumenter.InstrumentationProperties.InstrumentationApproach;
import com.scythe.instrumenter.analysis.ClassAnalyzer;
import com.scythe.instrumenter.analysis.InstrumentingTask;
import com.scythe.instrumenter.analysis.task.Task;
import com.scythe.instrumenter.analysis.task.TaskTimer;
import com.scythe.util.ClassNameUtils;

public class ClassReplacementTransformer {

    private static ArrayList<String> seenClasses = new ArrayList<String>();
    private boolean shouldWriteClass = false;
    public ArrayList<ShouldInstrumentChecker> shouldInstrumentCheckers;

    public interface ShouldInstrumentChecker {
        boolean shouldInstrument(String className);
    }

    public ClassReplacementTransformer() {
        shouldInstrumentCheckers = new ArrayList<ShouldInstrumentChecker>();
        shouldInstrumentCheckers.add(new ShouldInstrumentChecker() {
            @Override
            public boolean shouldInstrument(String className) {
                return InstrumentationProperties.INSTRUMENTATION_APPROACH != InstrumentationApproach.NONE;
            }
        });

        shouldInstrumentCheckers.add(new ShouldInstrumentChecker() {
            @Override
            public boolean shouldInstrument(String className) {
                if (className == null) {
                    return false;
                }
                if (isForbiddenPackage(className)) {
                    return false;
                }
                return true;
            }
        });
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

        // if (InstrumentationProperties.EXILED_CLASSES != null) {
        // for (String s : Properties.EXILED_CLASSES) {
        // if (cName.equals(s)) {
        // // App.out.println("Not loaded class " + cName);
        // throw new IllegalClassFormatException();
        // }
        // }
        // }

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
                if (InstrumentationProperties.LOG) {
                    TaskTimer.taskStart(instrumentingTask);
                }
                // Handle JSR instructions
                cv = new ClassVisitor(Opcodes.ASM5, cv) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                            String[] exceptions) {
                        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                        return new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions);
                    }
                };
                try {
                    cr.accept(cv, ClassReader.EXPAND_FRAMES);
                } catch (Throwable t) {
                    t.printStackTrace(ClassAnalyzer.out);
                }
                newClass = cw.toByteArray();

                if (InstrumentationProperties.LOG) {
                    TaskTimer.taskEnd(instrumentingTask);
                }
            } catch (IOException e) {
                e.printStackTrace(ClassAnalyzer.out);
            }
            if (shouldWriteClass) {
                File file = new File("classes/" + cName.replace(".", "/") + ".class");
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
                "com/scythe/instrumenter", "org/eclipse" };

        // String[] defaultHiddenPackages = new
        // String[]{"com/scythe/leapmotion", "com/google/gson",
        // "com/sun", "java/", "sun/", "com/leapmotion", "jdk/", "javax/",
        // "org/json", "org/apache/commons/cli",
        // "com/scythe/instrumenter", "com/dpaterson", "org/junit"};

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
        className = ClassNameUtils.standardise(className);

        for (ShouldInstrumentChecker sic : shouldInstrumentCheckers) {
            if (!sic.shouldInstrument(className)) {
                return false;
            }
        }

        return true;
    }

    public void addShouldInstrumentChecker(ShouldInstrumentChecker s) {
        shouldInstrumentCheckers.add(s);
    }

    public void removeShouldInstrumentChecker(ShouldInstrumentChecker s) {
        shouldInstrumentCheckers.remove(s);
    }

    public static boolean isForbiddenPackage(String clazz) {
        for (String s : forbiddenPackages) {
            if (clazz.startsWith(ClassNameUtils.standardise(s))) {
                return true;
            }
        }
        return false;
    }
}
