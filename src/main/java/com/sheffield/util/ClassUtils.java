package com.sheffield.util;

/**
 * Created by davidpaterson on 22/11/2016.
 */
public class ClassUtils {

    /**
     * Returns whether or not a class will have been instrumented by this instrumentation package.
     * Classes that are interfaces, annotations, enums will not be instrumented.
     * TODO: Fix implementation of ASM Opcode checking so that subclasses of enums that are classes can be instrumented
     * @param cl - the class to test
     * @return true if the class will have been instrumented by this package
     */
    public static boolean isInstrumented(Class<?> cl){
        return !cl.isInterface() && !cl.isAnnotation() && !cl.isEnum();// && !cl.getSuperclass().isEnum();
    }
}
