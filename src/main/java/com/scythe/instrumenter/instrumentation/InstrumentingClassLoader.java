package com.scythe.instrumenter.instrumentation;

import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.instrumenter.analysis.ClassAnalyzer;
import com.scythe.instrumenter.instrumentation.visitors.ArrayClassVisitor;
import com.scythe.instrumenter.instrumentation.visitors.DependencyTreeClassVisitor;
import com.scythe.instrumenter.instrumentation.visitors.MutationClassVisitor;
import com.scythe.instrumenter.instrumentation.visitors.StaticClassVisitor;
import com.scythe.instrumenter.instrumentation.visitors.SuperReplacementClassVisitor;
import com.scythe.util.ArrayUtils;
import com.scythe.util.ClassNameUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class InstrumentingClassLoader extends URLClassLoader {

  private static InstrumentingClassLoader instance;
  private ClassLoader classLoader;
  private ClassReplacementTransformer crt = new ClassReplacementTransformer();
  private MockClassLoader loader;
  private ArrayList<ClassInstrumentingInterceptor> classInstrumentingInterceptors;

  private HashMap<String, String> superClassReplacements = new HashMap
      <String, String>();

  private boolean buildDependencyTree = false;

  private boolean visitMutants = false;

  private InstrumentingClassLoader(URL[] urls) {
    super(urls);
    ClassAnalyzer.out.println("Created InstrumentingClassLoader with URLS " + Arrays.toString(urls));
    Thread.currentThread().setContextClassLoader(this);
    loader = new MockClassLoader(urls);
    this.classLoader = getClass().getClassLoader();
    classInstrumentingInterceptors = new ArrayList<ClassInstrumentingInterceptor>();
  }

  public static InstrumentingClassLoader getInstance() {
    if (instance == null) {
      URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
      URL[] urls = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();
      instance = new InstrumentingClassLoader(ArrayUtils.combineArrays(URL.class, true, urls, loader.getURLs()));
    }
    return instance;
  }

  public void addSuperClassReplacement(String superClass, String replacement) {
    ClassAnalyzer.out.println("- Replacing class [" + superClass +
        "->" + replacement + "]");
    superClassReplacements.put(ClassNameUtils.standardise(superClass),
        ClassNameUtils.standardise(replacement));
  }

  public boolean shouldReplaceSuperClass(String superClass) {
    return superClassReplacements.containsKey(superClass);
  }

  public String superClassReplacement(String superClass) {
    return superClassReplacements.get(superClass);
  }

  public void setVisitMutants(boolean b) {
    visitMutants = b;
  }

  public void setBuildDependencyTree(boolean b) {
    buildDependencyTree = b;
  }

  public void addClassInstrumentingInterceptor(ClassInstrumentingInterceptor cii) {
    ClassAnalyzer.out.printf("- Added ClassInstrumentingInterceptor: %s.\n", cii.getClass().getName());
    classInstrumentingInterceptors.add(cii);
  }

  public void removeClassInstrumentingInterceptor(ClassInstrumentingInterceptor cii) {
    classInstrumentingInterceptors.remove(cii);
  }

  @Override
  public void addURL(URL u) {
    super.addURL(u);
    loader.addURL(u);
    // Add url to system class loader.
  }

  public ClassReplacementTransformer getClassReplacementTransformer() {
    return crt;
  }

  /**
   * * Add a inmemory representation of a class. * @param name * name of the class * @param bytes * class definition
   */

  @Override
  public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    String className = ClassNameUtils.replaceSlashes(name);
    if (ClassStore.containsKey(className)) {
      return ClassStore.get(className);
    }
    if ("".equals(className)) {
      throw new ClassNotFoundException("Empty class name given");
    }

    InputStream stream = null;
    try {

      stream = getInputStreamForClass(name);
      byte[] original = IOUtils.toByteArray(stream);
      byte[] bytes = modifyBytes(className, original);

      Class<?> cl = null;
      try {
        if (!crt.isForbiddenPackage(name)) {
          cl = defineClass(className, bytes, 0, bytes.length);
        } else {
          cl = super.loadClass(className, resolve);
        }
      } catch (final Throwable e) {
        // either we cannot define classes ourself, or the class declaration caused an error
        cl = super.loadClass(className, resolve);
      }

      ClassStore.put(className, cl);
      if (resolve) {
        resolveClass(cl);
      }
      return cl;
    } catch (final NoSuchFileException e) {
      return super.loadClass(name, resolve);
    } catch (final IOException e) {
      e.printStackTrace(ClassAnalyzer.out);
      throw new ClassNotFoundException("Couldn't instrument class" + e.getLocalizedMessage());
    } catch (final IllegalClassFormatException e) {
      e.printStackTrace(ClassAnalyzer.out);
      throw new ClassNotFoundException("Couldn't instrument class" + e.getLocalizedMessage());
    } catch (final Exception e) {
      e.printStackTrace(ClassAnalyzer.out);
      throw new ClassNotFoundException("Couldn't instrument class " + e.getLocalizedMessage());
    } finally {
      try {
        if (stream != null) {
          stream.close();
        }
      } catch (final IOException e) {
        // not much we can do here
      }
    }
  }

  public byte[] modifyBytes(String name, byte[] original)
      throws ClassNotFoundException, IllegalClassFormatException, IOException {
    String className = ClassNameUtils.replaceSlashes(name);
    if ("".equals(className)) {
      throw new ClassNotFoundException("Empty class name given");
    }
    ByteArrayOutputStream out = null;

    ClassWriter writer = new CustomLoaderClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, this);
    ClassVisitor cw = writer;

    if (superClassReplacements.size() > 0) {
      cw = new SuperReplacementClassVisitor(cw, name);
    }

    for (ClassInstrumentingInterceptor cii :
        classInstrumentingInterceptors) {
      ClassVisitor newVisitor = cii.intercept(cw, name);
      if (newVisitor != null) {
        cw = newVisitor;
      }
    }

    ClassVisitor cv = InstrumentationProperties.INSTRUMENTATION_APPROACH == InstrumentationProperties.InstrumentationApproach.STATIC
        ? new StaticClassVisitor(cw, name) : new ArrayClassVisitor(cw, name);

    if (buildDependencyTree) {
      cv = new DependencyTreeClassVisitor(cv, name);
    }

    if (visitMutants) {
      cv = new MutationClassVisitor(cv);
    }

    byte[] bytes = crt.transform(name, original, cv, writer);

    if (InstrumentationProperties.WRITE_CLASS) {
      File output = new File(InstrumentationProperties.BYTECODE_DIR, ClassNameUtils.replaceDots(name) + ".class");

      if (output.getParentFile() != null && !output.getParentFile().exists()) {
        output.getParentFile().mkdirs();
        ClassAnalyzer.out.println("- Created new Folder: " + output.getParentFile().getAbsolutePath());
      }

      output.createNewFile();
      FileOutputStream outFile = new FileOutputStream(output);
      outFile.write(bytes);
      outFile.flush();
      outFile.close();
    }

    return bytes;
  }

  public Class<?> loadOriginalClass(String name) throws ClassNotFoundException {
    name = name.replace("/", ".");
    try {

      Class<?> cl = loader.loadOriginalClass(name);
      return cl;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    throw new ClassNotFoundException();

  }

  private InputStream getInputStreamForClass(String name) throws NoSuchFileException {
    String path = name.replace(".", "/") + ".class";
    InputStream stream = getResourceAsStream(path);
    if (stream != null) {
      return stream;
    }
    throw new NoSuchFileException("Could not find class on classpath");
  }

  public interface ClassInstrumentingInterceptor {
    ClassVisitor intercept(ClassVisitor parent, String className);
  }

  private class MockClassLoader extends URLClassLoader {
    public MockClassLoader(URL[] urls) {
      super(urls);
    }

    private Class<?> loadOriginalClass(String name) throws IOException, ClassNotFoundException {
      InputStream stream;
      Class<?> cl = findLoadedClass(name);
      if (!crt.shouldInstrumentClass(name)) {
        return super.loadClass(name);
      }
      if (cl == null) {
        stream = getInputStreamForClass(name);
        byte[] bytes = IOUtils.toByteArray(stream);
        cl = defineClass(name, bytes, 0, bytes.length);
      }
      return cl;
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      String localMessage = "";
      if (!crt.shouldInstrumentClass(name)) {
        return super.loadClass(name, resolve);
      }
      try {
        return loadOriginalClass(name);
      } catch (IOException e) {
        localMessage = e.getLocalizedMessage();
        e.printStackTrace(ClassAnalyzer.out);
      }
      throw new ClassNotFoundException(localMessage);
    }

    @Override
    protected void addURL(URL u) {
      super.addURL(u);
    }
  }
}
