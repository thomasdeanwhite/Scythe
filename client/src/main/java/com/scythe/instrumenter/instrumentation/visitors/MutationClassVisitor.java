package com.scythe.instrumenter.instrumentation.visitors;

import com.scythe.instrumenter.instrumentation.modifiers.MutationMethodVisitor;
import com.scythe.instrumenter.mutation.MutationProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class MutationClassVisitor extends ClassVisitor {
  public static List<Integer> activeMutantIds = new ArrayList<Integer>();
  public static String typeName = Type.getInternalName(MutationClassVisitor.class);

  public MutationClassVisitor(ClassVisitor mv) throws IOException {
    super(Opcodes.ASM5, mv);
    Path p = Paths.get(MutationProperties.ACTIVE_MUTANT_FILE);
    if (Files.exists(p, LinkOption.NOFOLLOW_LINKS)) {
      for (String id : Files.readAllLines(p)) {
        try {
          activeMutantIds.add(Integer.parseInt(id));
        } catch (NumberFormatException e) {
          throw new IOException("Mutant " + id + " is not an integer");
        }
      }
    } else {
      throw new IOException("Active Mutant File does not exist! " + MutationProperties.ACTIVE_MUTANT_FILE);
    }
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    // TODO Auto-generated method stub
    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
    mv = new MutationMethodVisitor(mv);
    return mv;
  }

}
