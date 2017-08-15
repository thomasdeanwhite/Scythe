package com.scythe.instrumenter.instrumentation.visitors;

import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.instrumenter.analysis.ClassAnalyzer;
import com.scythe.instrumenter.instrumentation.modifiers.StaticBranchVisitor;
import com.scythe.instrumenter.instrumentation.modifiers.StaticLineVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class StaticClassVisitor extends ClassVisitor {
	public static final String ANALYZER_CLASS = Type.getInternalName(ClassAnalyzer.class);
	private String className;
	private int classId;
	private boolean isInterface;
	private boolean isEnum;
	private boolean shouldInstrument;

	public StaticClassVisitor(ClassVisitor mv, String className) {
		super(Opcodes.ASM5, mv);
		this.className = className;
	}

	@Override
	public void visit(int arg0, int access, String className, String signature, String superName, String[] interfaces) {
		isInterface = ((access & Opcodes.ACC_INTERFACE) != 0);
		isEnum = superName.equals("java/lang/Enum");

		shouldInstrument = !(isInterface || isEnum);
		if (shouldInstrument) {
			this.classId = ClassAnalyzer.registerClass(this.className);
		}
		super.visit(arg0, access, className, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		if (shouldInstrument) {
			if (InstrumentationProperties.INSTRUMENT_BRANCHES) {
				mv = new StaticBranchVisitor(mv, classId, className, name);
			}
			if (InstrumentationProperties.INSTRUMENT_LINES) {
				mv = new StaticLineVisitor(mv, className, classId);
			}
		}
		return mv;
	}
}