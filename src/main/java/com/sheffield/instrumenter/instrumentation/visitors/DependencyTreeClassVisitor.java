package com.sheffield.instrumenter.instrumentation.visitors;

import com.sheffield.instrumenter.analysis.DependencyTree;
import com.sheffield.instrumenter.instrumentation.modifiers.DependencyTreeVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class DependencyTreeClassVisitor extends ClassVisitor {

	private String className;
	private DependencyTree depTree;

	public DependencyTreeClassVisitor(ClassVisitor mv, String className) {
		super(Opcodes.ASM5, mv);
		this.className = className.replace('.', '/');
		depTree = DependencyTree.getDependencyTree();
		//depTree.clear();
	}

	@Override
	public void visit(int arg0, int access, String arg2, String arg3, String arg4, String[] arg5) {
		super.visit(arg0, access, arg2, arg3, arg4, arg5);

	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = new DependencyTreeVisitor(depTree, super.visitMethod(access, name, desc, signature, exceptions), className, name, desc, access);
		return mv;
	}

	@Override
	public void visitEnd() {
		super.visitEnd();
	}

}
