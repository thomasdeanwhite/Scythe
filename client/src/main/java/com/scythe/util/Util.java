package com.scythe.util;

import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.instrumenter.analysis.ClassAnalyzer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

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

  public static void setField(Field f, Object obj)
      throws IllegalArgumentException, IllegalAccessException {
    Class<?> cl = f.getType();
    if(!f.isAccessible()){
      f.setAccessible(true);
    }
    String value = obj == null ? "" : obj.toString();
    boolean set = false;
    if (cl.isAssignableFrom(Number.class) || cl.isPrimitive()) {
      if (cl.equals(Long.class) || cl.equals(long.class)) {
        try {
          Long l = Long.parseLong(value);
          f.setLong(null, l);
          set = true;
        } catch (NumberFormatException e) {
          Double fl = Double.parseDouble(value);
          f.setLong(null, (long) fl.doubleValue());
          set = true;
        }
      } else if (cl.equals(Double.class) || cl.equals(double.class)) {
        Double d = Double.parseDouble(value);
        f.setDouble(null, d);
        set = true;
      } else if (cl.equals(Float.class) || cl.equals(float.class)) {
        Float fl = Float.parseFloat(value);
        f.setFloat(null, fl);
        set = true;
      } else if (cl.equals(Integer.class) || cl.equals(int.class)) {
        Double fl = Double.parseDouble(value);
        f.setInt(null, (int) fl.doubleValue());
        set = true;
      } else if (cl.equals(Boolean.class) || cl.equals(boolean.class)) {
        Boolean bl = Boolean.parseBoolean(value);
        f.setBoolean(null, bl);
        set = true;
      }
    } else if (cl.isAssignableFrom(String.class)) {
      f.set(null, value);
      set = true;
    }
    if (f.getType().isEnum()) {
      f.set(null, Enum.valueOf((Class<Enum>) f.getType(), value.toUpperCase()));
      set = true;
    }
    if(!set) {
      f.set(null, obj);
    }
  }

}
