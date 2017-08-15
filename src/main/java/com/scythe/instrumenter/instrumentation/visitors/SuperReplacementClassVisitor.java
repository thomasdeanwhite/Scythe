package com.scythe.instrumenter.instrumentation.visitors;

import com.scythe.instrumenter.analysis.ClassAnalyzer;
import com.scythe.instrumenter.analysis.DependencyTree;
import com.scythe.instrumenter.instrumentation.InstrumentingClassLoader;
import com.scythe.instrumenter.instrumentation.modifiers.DependencyTreeVisitor;

import com.scythe.instrumenter.instrumentation.modifiers
		.SuperReplacementVisitor;
import com.scythe.util.ClassNameUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class SuperReplacementClassVisitor extends ClassVisitor {

	private String className;
	private String superClass = null;

	public SuperReplacementClassVisitor(ClassVisitor mv, String className) {
		super(Opcodes.ASM5, mv);
		this.className = ClassNameUtils.standardise(className);
	}

	@Override
	public void visit(int arg0, int access, String className, String sign, String superName, String[] arg5) {

        if (InstrumentingClassLoader.getInstance().shouldReplaceSuperClass
                (superName)){
            String newSuper = InstrumentingClassLoader.getInstance()
                    .superClassReplacement(superName);
            superName = newSuper;

        }
		super.visit(arg0, access, className, sign, superName, arg5);
		superClass = ClassNameUtils.standardise(superName);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = new SuperReplacementVisitor(super.visitMethod(access, name, desc, signature, exceptions), className, name, desc, access);

		return mv;
	}

	@Override
	public void visitEnd() {
		super.visitEnd();
	}

}
