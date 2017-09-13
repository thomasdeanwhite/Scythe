package com.scythe.instrumenter.instrumentation.visitors;

import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.instrumenter.analysis.ClassAnalyzer;
import com.scythe.instrumenter.instrumentation.modifiers.ArrayBranchVisitor;
import com.scythe.instrumenter.instrumentation.modifiers.ArrayLineVisitor;
import com.scythe.instrumenter.instrumentation.objectrepresentation.BranchHit;
import com.scythe.instrumenter.instrumentation.objectrepresentation.LineHit;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ArrayClassVisitor extends ClassVisitor {

  private String className;
  public static final String COUNTER_VARIABLE_NAME = "__hitCounters";
  public static final String COUNTER_VARIABLE_DESC = "[I";

  public static final String DISTANCE_VARIABLE_NAME = "__distanceCounters";
  public static final String DISTANCE_VARIABLE_DESC = "[F";

  public static final String CHANGED_VARIABLE_NAME = "__changed";
  public static final String CHANGED_VARIABLE_DESC = "Z";
  public static final String COUNTER_METHOD_NAME = "__getHitCounters";
  public static final String COUNTER_METHOD_DESC = "()[I";

  public static final String DISTANCE_METHOD_NAME = "__getDistanceCounters";
  public static final String DISTANCE_METHOD_DESC = "()[F";

  public static final String RESET_COUNTER_METHOD_NAME = "__resetCounters";
  public static final String RESET_COUNTER_METHOD_DESC = "()V";
  public static final String INIT_METHOD_NAME = "__instrumentationInit";
  public static final String INIT_METHOD_DESC = "()V";
  public static final String CHANGED_METHOD_NAME = "classChanged";
  public static final String CHANGED_METHOD_DESC = "(Ljava/lang/String;)V";
  private AtomicInteger counter = new AtomicInteger(0);
  private AtomicInteger distanceCounter = new AtomicInteger(0);
  private List<BranchHit> branchHitCounterIds = new ArrayList<BranchHit>();
  private List<LineHit> lineHitCounterIds = new ArrayList<LineHit>();
  // isInterface represents whether or not the class we are visiting is an interface
  private boolean isInterface;
  private boolean isEnum;
  private boolean shouldInstrument;
  private int classId;

  public int newCounterId() {
    return counter.getAndIncrement();
  }

  public int newDistanceId() {
    return distanceCounter.getAndIncrement();
  }

  public void addBranchHit(BranchHit branch) {
    branchHitCounterIds.add(branch);
  }

  public int addLineHit(LineHit line) {
    for (LineHit lh : lineHitCounterIds){
      if (lh.getLine().getLineNumber() == line.getLine().getLineNumber()){

        //counter.getAndDecrement();
        return lh.getCounterId();
      }
    }

    lineHitCounterIds.add(line);
    return line.getCounterId();
  }

  public ArrayClassVisitor(ClassVisitor mv, String className) {
    super(Opcodes.ASM5, mv);
    this.className = className.replace('.', '/');
  }



  @Override
  public void visit(int arg0, int access, String className, String signature, String superName, String[] interfaces) {

    super.visit(arg0, access, className, signature, superName, interfaces);

    isInterface = ((access & Opcodes.ACC_INTERFACE) != 0);
    isEnum = superName.equals("java/lang/Enum");
    boolean isSynthetic = ((access & Opcodes.ACC_SYNTHETIC) != 0);

    shouldInstrument = !(isInterface || isEnum || isSynthetic);

    if (shouldInstrument) {
      // add hit counter array
      FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, COUNTER_VARIABLE_NAME,
              COUNTER_VARIABLE_DESC, null, null);
      fv.visitEnd();


      FieldVisitor fvd = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, DISTANCE_VARIABLE_NAME,
              DISTANCE_VARIABLE_DESC, null, null);
      fv.visitEnd();

      this.classId = ClassAnalyzer.registerClass(this.className);

      // add changed boolean
      if (InstrumentationProperties.USE_CHANGED_FLAG) {
        FieldVisitor changed = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, CHANGED_VARIABLE_NAME,
            CHANGED_VARIABLE_DESC, null, null);
        changed.visitEnd();
      }
//    } else {
//      ClassAnalyzer.out.println("\r " + this.className + " is an interface or enum!");
    }
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
    if (shouldInstrument && (access & Opcodes.ACC_ABSTRACT) == 0 && (access & Opcodes.ACC_SYNTHETIC) == 0) {
      if (InstrumentationProperties.USE_CHANGED_FLAG) {
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
      if (InstrumentationProperties.INSTRUMENT_BRANCHES) {
        mv = new ArrayBranchVisitor(this, mv, className, name, desc, access);
      }
      if (InstrumentationProperties.INSTRUMENT_LINES) {
        mv = new ArrayLineVisitor(this, mv, className, name);
      }
    }



    return mv;
  }

  @Override
  public void visitEnd() {
    // create visits to our own methods to collect hits, only if it's not an
    // interface
    if (shouldInstrument) {
      addGetCounterMethod(cv);
      addGetDistanceMethod(cv);
      addResetCounterMethod(cv);
      addInitMethod(cv);
      ClassAnalyzer.classAnalyzed(classId, branchHitCounterIds, lineHitCounterIds);
    }
    super.visitEnd();
  }

  private void addGetCounterMethod(ClassVisitor cv) {
    MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, COUNTER_METHOD_NAME, COUNTER_METHOD_DESC,
            null, null);
    mv.visitCode();
    mv.visitFieldInsn(Opcodes.GETSTATIC, className, COUNTER_VARIABLE_NAME, COUNTER_VARIABLE_DESC);
    mv.visitInsn(Opcodes.ARETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void addGetDistanceMethod(ClassVisitor cv) {
    MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, DISTANCE_METHOD_NAME, DISTANCE_METHOD_DESC,
            null, null);
    mv.visitCode();
    mv.visitFieldInsn(Opcodes.GETSTATIC, className, DISTANCE_VARIABLE_NAME,
            DISTANCE_VARIABLE_DESC);
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
    if(InstrumentationProperties.USE_CHANGED_FLAG) {
      mv.visitInsn(Opcodes.ICONST_0);
      mv.visitFieldInsn(Opcodes.PUTSTATIC, className, CHANGED_VARIABLE_NAME, CHANGED_VARIABLE_DESC);
    }
    mv.visitInsn(Opcodes.RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void addInitMethod(ClassVisitor cv) {
    MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, INIT_METHOD_NAME, INIT_METHOD_DESC, null,
        null);
    mv.visitCode();
    Label l = new Label();
    mv.visitFieldInsn(Opcodes.GETSTATIC, className, COUNTER_VARIABLE_NAME, COUNTER_VARIABLE_DESC);
    mv.visitJumpInsn(Opcodes.IFNONNULL, l);

    int count = counter.get();
    mv.visitLdcInsn(count);
    mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
    mv.visitFieldInsn(Opcodes.PUTSTATIC, className, COUNTER_VARIABLE_NAME, COUNTER_VARIABLE_DESC);
    mv.visitLabel(l);

    Label ld = new Label();

    mv.visitFieldInsn(Opcodes.GETSTATIC, className, DISTANCE_VARIABLE_NAME,
            DISTANCE_VARIABLE_DESC);
    mv.visitJumpInsn(Opcodes.IFNONNULL, ld);

    int distanceCount = distanceCounter.get();
    mv.visitLdcInsn(distanceCount);
    mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_FLOAT);
    mv.visitFieldInsn(Opcodes.PUTSTATIC, className, DISTANCE_VARIABLE_NAME,
            DISTANCE_VARIABLE_DESC);
    mv.visitLabel(ld);
    mv.visitInsn(Opcodes.RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

  }
}
