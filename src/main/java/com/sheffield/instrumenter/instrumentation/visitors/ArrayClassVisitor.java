package com.sheffield.instrumenter.instrumentation.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.instrumentation.modifiers.ArrayBranchVisitor;
import com.sheffield.instrumenter.instrumentation.modifiers.ArrayLineVisitor;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.BranchHit;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.LineHit;

public class ArrayClassVisitor extends ClassVisitor {

	private String className;
	public static final String COUNTER_VARIABLE_NAME = "__hitCounters";
	public static final String COUNTER_VARIABLE_DESC = "[I";
	public static final String CHANGED_VARIABLE_NAME = "__changed";
	public static final String CHANGED_VARIABLE_DESC = "Z";
	public static final String COUNTER_METHOD_NAME = "__getHitCounters";
	public static final String COUNTER_METHOD_DESC = "()[I";
	public static final String RESET_COUNTER_METHOD_NAME = "__resetCounters";
	public static final String RESET_COUNTER_METHOD_DESC = "()V";
	public static final String INIT_METHOD_NAME = "__instrumentationInit";
	public static final String INIT_METHOD_DESC = "()V";
	public static final String CHANGED_METHOD_NAME = "classChanged";
	public static final String CHANGED_METHOD_DESC = "(Ljava/lang/String;)V";
	private AtomicInteger counter = new AtomicInteger(0);
	private List<BranchHit> branchHitCounterIds = new ArrayList<BranchHit>();
	private List<LineHit> lineHitCounterIds = new ArrayList<LineHit>();
	// itf represents whether or not the class we are visiting is an interface
	private boolean itf;
	private int classId;
	
	public int newCounterId() {
		return counter.getAndIncrement();
	}

	public void addBranchHit(BranchHit branch) {
		branchHitCounterIds.add(branch);
	}

	public void addLineHit(LineHit line) {
		lineHitCounterIds.add(line);
	}

	public ArrayClassVisitor(ClassVisitor mv, String className) {
		super(Opcodes.ASM5, mv);
		this.className = className.replace('.', '/');
		this.classId = ClassAnalyzer.registerClass(className);
	}

	@Override
	public void visit(int arg0, int access, String arg2, String arg3, String arg4, String[] arg5) {
		super.visit(arg0, access, arg2, arg3, arg4, arg5);
		itf = (access & Opcodes.ACC_INTERFACE) != 0;
		if (!itf) {
			// add hit counter array
			FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, COUNTER_VARIABLE_NAME,
					COUNTER_VARIABLE_DESC, null, null);
			fv.visitEnd();

			// add changed boolean
			if (Properties.USE_CHANGED_FLAG) {
				FieldVisitor changed = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, CHANGED_VARIABLE_NAME,
						CHANGED_VARIABLE_DESC, null, null);
				changed.visitEnd();
			}
		}
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		if (!itf && (access & Opcodes.ACC_ABSTRACT) == 0 && Properties.USE_CHANGED_FLAG) {
			// add call to ClassAnalyzer.changed
			mv.visitFieldInsn(Opcodes.GETSTATIC, className, CHANGED_VARIABLE_NAME, CHANGED_VARIABLE_DESC);
			Label l = new Label();
			mv.visitJumpInsn(Opcodes.IFGT, l);
			mv.visitLdcInsn(className);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, StaticClassVisitor.ANALYZER_CLASS, CHANGED_METHOD_NAME,
					CHANGED_METHOD_DESC, false);
			mv.visitInsn(Opcodes.ICONST_1);
			mv.visitFieldInsn(Opcodes.PUTSTATIC, className, CHANGED_VARIABLE_NAME, CHANGED_VARIABLE_DESC);
			mv.visitLabel(l);
		}
		if ((access & Opcodes.ACC_STATIC) != 0 || "<init>".equals(name)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, className, INIT_METHOD_NAME, INIT_METHOD_DESC, false);
		}
		if (Properties.INSTRUMENT_BRANCHES) {
			mv = new ArrayBranchVisitor(this, mv, className, name, desc, access);
		}
		if (Properties.INSTRUMENT_LINES) {
			mv = new ArrayLineVisitor(this, mv, className);
		}

		return mv;
	}

	@Override
	public void visitEnd() {
		// create visits to our own methods to collect hits, only if it's not an interface
		if (!itf) {
			addGetCounterMethod(cv);
			addResetCounterMethod(cv);
			addInitMethod(cv);
			ClassAnalyzer.classAnalyzed(classId, branchHitCounterIds, lineHitCounterIds);
		}
		super.visitEnd();
	}

	private void addGetCounterMethod(ClassVisitor cv) {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, COUNTER_METHOD_NAME,
				COUNTER_METHOD_DESC, null, null);
		mv.visitCode();
		mv.visitFieldInsn(Opcodes.GETSTATIC, className, COUNTER_VARIABLE_NAME, COUNTER_VARIABLE_DESC);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private void addResetCounterMethod(ClassVisitor cv) {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, RESET_COUNTER_METHOD_NAME,
				RESET_COUNTER_METHOD_DESC, null, null);
		mv.visitCode();
		mv.visitFieldInsn(Opcodes.GETSTATIC, className, COUNTER_VARIABLE_NAME, COUNTER_VARIABLE_DESC);
		mv.visitInsn(Opcodes.ARRAYLENGTH);
		mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
		mv.visitFieldInsn(Opcodes.PUTSTATIC, className, COUNTER_VARIABLE_NAME, COUNTER_VARIABLE_DESC);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private void addInitMethod(ClassVisitor cv) {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, INIT_METHOD_NAME, INIT_METHOD_DESC,
				null, null);
		mv.visitCode();
		Label l = new Label();
		mv.visitFieldInsn(Opcodes.GETSTATIC, className, COUNTER_VARIABLE_NAME, COUNTER_VARIABLE_DESC);
		mv.visitJumpInsn(Opcodes.IFNONNULL, l);
		int count = counter.get();
		mv.visitLdcInsn(count);
		mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
		mv.visitFieldInsn(Opcodes.PUTSTATIC, className, COUNTER_VARIABLE_NAME, COUNTER_VARIABLE_DESC);
		mv.visitLabel(l);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

	}

}
