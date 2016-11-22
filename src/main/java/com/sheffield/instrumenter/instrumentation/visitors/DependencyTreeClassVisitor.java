package com.sheffield.instrumenter.instrumentation.visitors;

import com.sheffield.instrumenter.analysis.DependencyTree;
import com.sheffield.instrumenter.instrumentation.modifiers.DependencyTreeVisitor;
import com.sheffield.util.ClassNameUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class DependencyTreeClassVisitor extends ClassVisitor {

	private String className;
	private DependencyTree depTree;
	private String superClass = null;

	public DependencyTreeClassVisitor(ClassVisitor mv, String className) {
		super(Opcodes.ASM5, mv);
		this.className = ClassNameUtils.standardise(className);
		depTree = DependencyTree.getDependencyTree();
		//depTree.clear();
	}

	@Override
	public void visit(int arg0, int access, String className, String sign, String superName, String[] arg5) {
		super.visit(arg0, access, className, sign, superName, arg5);
		superClass = ClassNameUtils.standardise(superName);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = new DependencyTreeVisitor(depTree, super.visitMethod(access, name, desc, signature, exceptions), className, name, desc, access);

		if ((access & Opcodes.ACC_SYNTHETIC) != 0){
			String classMethodId = DependencyTree.getClassMethodId(className, name);
			String superMethodId = DependencyTree.getClassMethodId(superClass, name);

			depTree.addDependency(superMethodId, classMethodId);

		}

		return mv;
	}

	@Override
	public void visitEnd() {
		super.visitEnd();
	}

}
