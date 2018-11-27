package com.scythe.instrumenter.instrumentation;

import com.scythe.util.ClassNameUtils;
import java.util.HashMap;

public class ClassStore {
  private static HashMap<String, Class<?>> store = new HashMap<String, Class<?>>();
  /**
   *
   */
  private static final long serialVersionUID = -1002975153253026174L;

  public static void put(String name, Class<?> cl) {
    store.put(ClassNameUtils.standardise(name), cl);
  }

  public static boolean containsKey(String name) {
    name = ClassNameUtils.standardise(name);
    return store.containsKey(name);
  }

  public static Class<?> get(String name) {
    if (name == null) {
      return null;
    }
    if (store.containsKey(name)) {
      return store.get(name);
    }
    String newName = ClassNameUtils.standardise(name);
    if (store.containsKey(newName)) {
      return store.get(newName);
    }

    try {
      Class<?> c = ClassLoader.getSystemClassLoader().loadClass(ClassNameUtils.replaceSlashes(name));
      store.put(name, c);
      return c;
    } catch (ClassNotFoundException e) {
      //e.printStackTrace(ClassAnalyzer.out);
    } catch (NoClassDefFoundError e) {
      //e.printStackTrace(ClassAnalyzer.out);
    }
    return null;
  }
}
