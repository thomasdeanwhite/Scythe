package com.sheffield.instrumenter.instrumentation;

import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.instrumentation.visitors.ArrayClassVisitor;
import com.sheffield.instrumenter.instrumentation.visitors.DependencyTreeClassVisitor;
import com.sheffield.instrumenter.instrumentation.visitors.StaticClassVisitor;
import com.sheffield.util.ArrayUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;

public class InstrumentingClassLoader extends URLClassLoader {

    private static InstrumentingClassLoader instance;
    private ClassLoader classLoader;
    private ClassReplacementTransformer crt = new ClassReplacementTransformer();
    private boolean shouldInstrument;
    private MockClassLoader loader;
    private ArrayList<ClassInstrumentingInterceptor> classInstrumentingInterceptors;

    private boolean buildDependencyTree = false;

    public interface ClassInstrumentingInterceptor {
        ClassVisitor intercept(ClassVisitor parent, String className);
    }

    public void setBuildDependencyTree(boolean b) {
        buildDependencyTree = true;
    }

    public void addClassInstrumentingInterceptor(ClassInstrumentingInterceptor cii) {
        ClassAnalyzer.out.printf("- Added ClassInstrumentingInterceptor: %s.\n", cii.getClass().getName());
        classInstrumentingInterceptors.add(cii);
    }

    public void removeClassInstrumentingInterceptor(ClassInstrumentingInterceptor cii) {
        classInstrumentingInterceptors.remove(cii);
    }

    public void setShouldInstrument(boolean shouldInstrument) {
        this.shouldInstrument = shouldInstrument;
    }

    private InstrumentingClassLoader(URL[] urls) {
        super(urls);
        ClassAnalyzer.out.println("Created InstrumentingClassLoader with URLS "+ Arrays.toString(urls));
        loader = new MockClassLoader(urls);
        this.classLoader = getClass().getClassLoader();
        classInstrumentingInterceptors = new ArrayList<ClassInstrumentingInterceptor>();
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
        String className = name.replace('/', '.');
        if (ClassStore.containsKey(className)) {
            return ClassStore.get(className);
        }
        if ("".equals(className)) {
            throw new ClassNotFoundException();
        }
        // check if the class comes from the Java API (i.e. javax/swing or java.io.*)
        if (!crt.shouldInstrumentClass(className)) {
            Class<?> cl = findLoadedClass(className);
            if (cl != null) {
                return cl;
            }
            return super.loadClass(className, resolve);
        }
        // this is most likely a testing class or something similar, but still requires being loaded by this class loader
        if (!shouldInstrument) {
            try {
                InputStream stream = getInputStreamForClass(name);
                byte[] bytes = IOUtils.toByteArray(stream);
                Class<?> cl = defineClass(className, bytes, 0, bytes.length);
                ClassStore.put(className, cl);
                return cl;
            } catch (final IOException e) {

            }
            return super.loadClass(className, resolve);
        }
        InputStream stream = null;
        ByteArrayOutputStream out = null;
        try {

            stream = getInputStreamForClass(name);
            ClassWriter writer = new CustomLoaderClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS,
                    this);
            ClassVisitor cw = writer;
            for (ClassInstrumentingInterceptor cii : classInstrumentingInterceptors) {
                ClassVisitor newVisitor = cii.intercept(cw, name);
                if (newVisitor != null) {
                    cw = newVisitor;
                }
            }

            ClassVisitor cv = Properties.INSTRUMENTATION_APPROACH == Properties.InstrumentationApproach.STATIC
                    ? new StaticClassVisitor(cw, name) : new ArrayClassVisitor(cw, name);

            if (buildDependencyTree) {
                cv = new DependencyTreeClassVisitor(cv, name);
            }
            byte[] bytes = crt.transform(name, IOUtils.toByteArray(stream), cv, writer);
            if (Properties.WRITE_CLASS) {
                String outputDir = Properties.BYTECODE_DIR + "/"
                        + name.replace(".", "/");
                File output = new File(outputDir + ".class");

//                if (lastIndex > -1) {
//                    outputDir = outputDir.substring(0, lastIndex);
//                    File folder = new File(outputDir);
//                    folder.mkdirs();
//                    output = new File(outputDir.substring(outputDir.lastIndexOf(".") + 1) + ".class");
//                }

                if (output.getParentFile() != null){
                    output.getParentFile().mkdirs();
                }

                output.createNewFile();
                FileOutputStream outFile = new FileOutputStream(output);
                outFile.write(bytes);
                outFile.flush();
                outFile.close();
            }

            Class<?> cl = null;
            try {
                cl = defineClass(className, bytes, 0, bytes.length);
            } catch (final Throwable e) {
                e.printStackTrace(ClassAnalyzer.out);
            }

            ClassStore.put(className, cl);
            if (resolve) {
                resolveClass(cl);
            }
            return cl;
        } catch (final IOException e) {
            throw new ClassNotFoundException("Couldn't instrument class" + e.getLocalizedMessage());
        } catch (final IllegalClassFormatException e) {
            throw new ClassNotFoundException("Couldn't instrument class" + e.getLocalizedMessage());
        } catch (final Exception e) {
            e.printStackTrace();
            throw new ClassNotFoundException("Couldn't instrument class " + e.getLocalizedMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (stream != null) {
                    stream.close();
                }
            } catch (final IOException e) {
                // not much we can do here
            }
        }
    }

    public Class<?> loadOriginalClass(String name) throws ClassNotFoundException {
        name = name.replace("/", ".");
        try {

            Class<?> cl = loader.loadOriginalClass(name);
            return cl;
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        throw new ClassNotFoundException();

    }

    private InputStream getInputStreamForClass(String name) throws ClassNotFoundException {
        String path = name.replace(".", "/") + ".class";
        InputStream stream = getResourceAsStream(path);
        if (stream != null) {
            return stream;
        }
        throw new ClassNotFoundException("Could not find class on classpath");
    }

    public static InstrumentingClassLoader getInstance() {
        if (instance == null) {
            URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            URL[] urls = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();
            instance = new InstrumentingClassLoader(ArrayUtils.combineArrays(URL.class, true, urls, loader.getURLs()));
        }
        return instance;
    }

    private class MockClassLoader extends URLClassLoader {
        public MockClassLoader(URL[] urls) {
            super(urls);
        }

        private Class<?> loadOriginalClass(String name) throws IOException, ClassNotFoundException {
            InputStream stream;
            Class<?> cl = findLoadedClass(name);
            if (!crt.shouldInstrumentClass(name) || !shouldInstrument) {
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
            if (!crt.shouldInstrumentClass(name) || !shouldInstrument) {
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
