package com.scythe.util;

import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.instrumenter.analysis.ClassAnalyzer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Util {
  public static void writeClass(String className, byte[] bytes) {
    File output = new File(InstrumentationProperties.BYTECODE_DIR, ClassNameUtils.replaceDots(className) + ".class");

    if (output.getParentFile() != null && !output.getParentFile().exists()) {
      output.getParentFile().mkdirs();
      ClassAnalyzer.out.println("- Created new Folder: " + output.getParentFile().getAbsolutePath());
    }
    try {
      output.createNewFile();
      FileOutputStream outFile = new FileOutputStream(output);
      outFile.write(bytes);
      outFile.flush();
      outFile.close();
    }catch(IOException e){
      System.err.println("Unable to create persistent class file. Perhaps the file could not be created");
      e.printStackTrace();
    }
  }
}
