package com.scythe.instrumenter.instrumentation.modifiers;

import com.scythe.instrumenter.instrumentation.objectrepresentation.Line;
import com.scythe.instrumenter.instrumentation.objectrepresentation.LineHit;
import com.scythe.instrumenter.instrumentation.visitors.ArrayClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ArrayLineVisitor extends MethodVisitor {
  private ArrayClassVisitor parent;
  private String className;
  private String methodName;

  public ArrayLineVisitor(ArrayClassVisitor parent, MethodVisitor mv, String className, String methodName) {
    super(Opcodes.ASM5, mv);
    this.className = className;
    this.parent = parent;
    this.methodName = methodName;
  }

  @Override
  public void visitLineNumber(int lineNumber, Label label) {
    int counterId = parent.newCounterId();

    counterId = parent.addLineHit(new LineHit(new Line(className, methodName, lineNumber), counterId));
    visitFieldInsn(Opcodes.GETSTATIC, className, ArrayClassVisitor.COUNTER_VARIABLE_NAME,
        ArrayClassVisitor.COUNTER_VARIABLE_DESC);
    visitLdcInsn(counterId);
    visitInsn(Opcodes.DUP2);
    visitInsn(Opcodes.IALOAD);
    visitLdcInsn(1);
    visitInsn(Opcodes.IADD);
    visitInsn(Opcodes.IASTORE);
    mv.visitLineNumber(lineNumber, label);
  }
}
