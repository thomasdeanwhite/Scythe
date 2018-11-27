package com.scythe.instrumenter.instrumentation;

import com.scythe.instrumenter.analysis.ClassAnalyzer;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.NoSuchFileException;
import org.apache.commons.io.IOUtils;

public class MockClassLoader extends URLClassLoader {
  private ClassReplacementTransformer crt;

  public MockClassLoader(URL[] urls, ClassReplacementTransformer crt) {
    super(urls);
    this.crt = crt;
  }

  public Class<?> loadOriginalClass(String name) throws IOException, ClassNotFoundException {
    InputStream stream;
    Class<?> cl = findLoadedClass(name);
    if(ClassReplacementTransformer.isForbiddenPackage(name)){
      return super.loadClass(name, false);
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

  private InputStream getInputStreamForClass(String name) throws NoSuchFileException {
    String path = name.replace(".", "/") + ".class";
    InputStream stream = getResourceAsStream(path);
    if (stream != null) {
      return stream;
    }
    throw new NoSuchFileException("Could not find class on classpath");
  }
}
