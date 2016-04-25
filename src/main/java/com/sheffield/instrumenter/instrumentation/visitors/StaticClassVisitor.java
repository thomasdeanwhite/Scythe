package com.sheffield.instrumenter.instrumentation.visitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.instrumentation.modifiers.StaticBranchVisitor;
import com.sheffield.instrumenter.instrumentation.modifiers.StaticLineVisitor;

public class StaticClassVisitor extends ClassVisitor {
	public static final String ANALYZER_CLASS = Type.getInternalName(ClassAnalyzer.class);
	private String className;
	private int classId; 
	
	public StaticClassVisitor(ClassVisitor mv, String className) {
		super(Opcodes.ASM5, mv);
		this.className = className;
		this.classId = ClassAnalyzer.registerClass(className);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		if (Properties.INSTRUMENT_BRANCHES) {
			mv = new StaticBranchVisitor(mv, classId, className, name);
		}
		if (Properties.INSTRUMENT_LINES) {
			mv = new StaticLineVisitor(mv, className, classId);
		}
		return mv;
	}
}