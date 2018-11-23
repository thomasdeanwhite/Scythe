package com.scythe.ant;

import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.instrumenter.instrumentation.InstrumentingClassLoader;
import com.scythe.util.ClassNameUtils;
import com.scythe.util.Util;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;

public class InstrumentTask extends Task {

  private InstrumentingClassLoader icl;
  private List<FileSet> files = new ArrayList<>();
  private File bytecodeDir;

  public void init() {
    icl = InstrumentingClassLoader.getInstance();
  }

  public void setDestdir(File bytecodeDir) {
    this.bytecodeDir = bytecodeDir;
  }

  public void addFileset(FileSet files) {
    this.files.add(files);
  }

  public void validate() {
    if (bytecodeDir == null) {
      throw new BuildException("Destination directory must be supplied", getLocation());
    }
    if (files.size() < 1) {
      throw new BuildException("There is no target filesets", getLocation());
    }
    if (getProject() == null) {
      throw new BuildException("Project is null, somehow!");
    }
  }

  public void execute() throws BuildException {
    validate();
    if (!bytecodeDir.exists()) {
      bytecodeDir.mkdirs();
    }

    InstrumentationProperties.WRITE_CLASS = true;
    InstrumentationProperties.BYTECODE_DIR = bytecodeDir.getAbsolutePath();

    files.forEach(path -> {
      for (Resource includedFile : path) {
        try {
          File f = new File(path.getDir(), includedFile.getName());
          System.out.println(f.getAbsolutePath());
          if (f.isDirectory()) {
            return;
          }
          byte[] uninstrumented = IOUtils.toByteArray(includedFile.getInputStream());
          int length = includedFile.getName().length();
          String className = ClassNameUtils.standardise(includedFile.getName().substring(0, length - ".class".length()));
          byte[] instrumented = icl.modifyBytes(className, uninstrumented);

          Util.writeClass(className, instrumented);
        } catch (IOException e) {
          e.printStackTrace();
        } catch (IllegalClassFormatException e) {
          e.printStackTrace();
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
      }
    });
  }
}
