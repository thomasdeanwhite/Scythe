package com.scythe.instrumenter.instrumentation.modifiers;

import com.scythe.instrumenter.analysis.DependencyTree;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class DependencyTreeVisitor extends MethodVisitor {
  private DependencyTree depTree;
  private String className;
  private String methodName;
  private String classMethodId;

  public DependencyTreeVisitor(DependencyTree dt, MethodVisitor mv, String className, String methodName,
                               String desc, int access) {
    super(Opcodes.ASM5, mv);
    depTree = dt;
    this.className = className;
    this.methodName = methodName;

    classMethodId = DependencyTree.getClassMethodId(className, methodName);
  }

  @Override
  public void visitMethodInsn(int i, String s, String s1, String s2, boolean b) {
    super.visitMethodInsn(i, s, s1, s2, b);
    //if (!s.equals(className)){
    depTree.addDependency(DependencyTree.getClassMethodId(s, s1), classMethodId);
    //}
  }
}
