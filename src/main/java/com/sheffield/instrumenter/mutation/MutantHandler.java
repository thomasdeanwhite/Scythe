package com.sheffield.instrumenter.mutation;

import org.objectweb.asm.Type;

import com.sheffield.instrumenter.instrumentation.visitors.MutationClassVisitor;

public class MutantHandler {
    public static String type = Type.getInternalName(MutantHandler.class);
    public static String enabledMethodName = "enabled";
    public static String enabledMethodDescriptor = "(I)Z";

    public static boolean enabled(int mutantId) {
        return MutationClassVisitor.activeMutantIds.contains(mutantId);
    }
}
