package com.sheffield.instrumenter.instrumentation.modifiers;

import com.sheffield.instrumenter.analysis.DependencyTree;
import com.sheffield.instrumenter.instrumentation.InstrumentingClassLoader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class SuperReplacementVisitor extends MethodVisitor {
	private String className;
	private String methodName;

	public SuperReplacementVisitor(MethodVisitor mv, String className, String methodName,
                                   String desc, int access) {
		super(Opcodes.ASM5, mv);
		this.className = className;
		this.methodName = methodName;
	}

	@Override
	public void visitMethodInsn(int i, String s, String s1, String s2, boolean b) {
		if (InstrumentingClassLoader.getInstance().shouldReplaceSuperClass
				(s)){
			s = InstrumentingClassLoader.getInstance()
					.superClassReplacement(s);
		}

		super.visitMethodInsn(i, s, s1, s2, b);
	}
}
