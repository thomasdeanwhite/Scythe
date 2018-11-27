package com.scythe.instrumenter.instrumentation.modifiers;

import com.scythe.instrumenter.mutation.MutantHandler;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MutationMethodVisitor extends MethodVisitor {

  private boolean configVisited = false;
  private boolean pushVisited = false;

  public MutationMethodVisitor(MethodVisitor mv) {
    super(Opcodes.ASM5, mv);
  }

  @Override
  public void visitJumpInsn(int opcode, Label label) {
    int op = opcode;
    switch (opcode) {
      case Opcodes.IF_ICMPEQ:
      case Opcodes.IF_ICMPNE:
        if (configVisited && pushVisited) {
          visitMethodInsn(Opcodes.INVOKESTATIC, MutantHandler.type, MutantHandler.enabledMethodName,
              MutantHandler.enabledMethodDescriptor, false);
          if (opcode == Opcodes.IF_ICMPNE) {
            op = Opcodes.IFEQ;
          } else {
            op = Opcodes.IFNE;
          }
        }
        break;
      case Opcodes.IFNE:
      case Opcodes.IFEQ:
        if (configVisited) {
          visitLdcInsn(0);
          visitMethodInsn(Opcodes.INVOKESTATIC, MutantHandler.type, MutantHandler.enabledMethodName,
              MutantHandler.enabledMethodDescriptor, false);
          if (opcode == Opcodes.IFEQ) {
            op = Opcodes.IFNE;
          } else {
            op = Opcodes.IFEQ;
          }
          break;
        }
    }
    mv.visitJumpInsn(op, label);
    configVisited = false;
    pushVisited = false;
  }

  @Override
  public void visitFieldInsn(int opcode, String owner, String name, String desc) {
    if (opcode == Opcodes.GETSTATIC && owner.equals("major/mutation/Config") && name.equals("__M_NO")
        && desc.equals("I")) {
      configVisited = true;
    } else {
      super.visitFieldInsn(opcode, owner, name, desc);
    }
  }

  @Override
  public void visitIntInsn(int opcode, int operand) {
    if (configVisited) {
      switch (opcode) {
        case Opcodes.SIPUSH:
        case Opcodes.BIPUSH:
          pushVisited = true;
          break;
        default:
          configVisited = false;
      }
    }
    super.visitIntInsn(opcode, operand);
  }

  @Override
  public void visitLdcInsn(Object arg0) {
    if (configVisited) {
      pushVisited = true;
    } else {
      configVisited = false;
    }
    super.visitLdcInsn(arg0);
  }

  @Override
  public void visitInsn(int opcode) {
    if (configVisited) {
      switch (opcode) {
        case Opcodes.ICONST_1:
        case Opcodes.ICONST_2:
        case Opcodes.ICONST_3:
        case Opcodes.ICONST_4:
        case Opcodes.ICONST_5:
          pushVisited = true;
          break;
        default:
          configVisited = false;
      }
    }
    super.visitInsn(opcode);
  }

  @Override
  public void visitVarInsn(int opcode, int var) {
    if (configVisited) {
      if (opcode == Opcodes.ILOAD) {
        pushVisited = true;
      } else {
        pushVisited = false;
      }
    }
    super.visitVarInsn(opcode, var);
  }

}
