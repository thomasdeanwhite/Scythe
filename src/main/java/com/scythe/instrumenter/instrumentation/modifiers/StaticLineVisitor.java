package com.scythe.instrumenter.instrumentation.modifiers;

import com.scythe.instrumenter.analysis.ClassAnalyzer;
import com.scythe.instrumenter.instrumentation.visitors.StaticClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class StaticLineVisitor extends MethodVisitor {
  private int classId;

  public StaticLineVisitor(MethodVisitor arg0, String className, int classId) {
    super(Opcodes.ASM5, arg0);
    this.classId = classId;
  }

  @Override
  public void visitLineNumber(int lineNumber, Label label) {
    ClassAnalyzer.lineFound(classId, lineNumber);
    visitLdcInsn(classId);
    visitLdcInsn(lineNumber);
    visitMethodInsn(Opcodes.INVOKESTATIC, StaticClassVisitor.ANALYZER_CLASS, "lineExecuted",
        "(II)V", false);
    mv.visitLineNumber(lineNumber, label);
  }

}
