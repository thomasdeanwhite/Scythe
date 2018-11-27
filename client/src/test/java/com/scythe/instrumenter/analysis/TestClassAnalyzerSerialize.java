package com.scythe.instrumenter.analysis;

import com.scythe.instrumenter.instrumentation.objectrepresentation.Line;
import com.scythe.instrumenter.instrumentation.objectrepresentation.LineHit;
import com.scythe.instrumenter.instrumentation.visitors.ClassTester;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.internal.util.reflection.FieldSetter;
import test.classes.ExampleClass;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class TestClassAnalyzerSerialize {

  private static final String TEST_CLASS_NAME = "org.mock.testclass.TestClass1";
  private static final String TEST_METHOD_NAME = "method1";

  private static final int MOCK_LINES_TO_INSERT = 100;

  private ConcurrentMap<Integer, Map<Integer, LineHit>> lines = new ConcurrentHashMap<>();
  private static File tmpFile;
  private static int classId;

  @BeforeClass
  public static void init() throws Exception {
    if (tmpFile == null) {
      tmpFile = File.createTempFile("serialized", ".tmp");
      tmpFile.deleteOnExit();
    }
    classId = ClassAnalyzer.registerClass(TEST_CLASS_NAME);
  }

  @Before
  public void setup() throws Exception {
    lines.put(classId, new HashMap<>());
    for (int i = 0; i < MOCK_LINES_TO_INSERT; i++) {
      Line l = new Line(TEST_CLASS_NAME, TEST_METHOD_NAME, i);
      LineHit lh = new LineHit(l, i);
      lines.get(classId).put(i, lh);
    }
    ClassAnalyzer.reset();
  }

  @Test
  public void testSerialize() throws Exception {
    FieldSetter.setField(null, ClassAnalyzer.class.getDeclaredField("lines"), lines);
    ClassAnalyzer.serialize(tmpFile);
    ClassAnalyzer.reset();
    assertEquals(0, ClassAnalyzer.getCoverableLines(TEST_CLASS_NAME).size());
    ClassAnalyzer.deserialize(tmpFile);
    assertEquals(MOCK_LINES_TO_INSERT, ClassAnalyzer.getCoverableLines(TEST_CLASS_NAME).size());
  }

  @Test
  public void testSerializeClasses() throws Exception {
    ArrayList<Class<?>> changedClasses = new ArrayList<>();
    changedClasses.add(MockClass.class);
    FieldSetter
        .setField(null, ClassAnalyzer.class.getDeclaredField("changedClasses"), changedClasses);
    ClassAnalyzer.serialize(tmpFile);
    ClassAnalyzer.softReset();
    assertEquals(0, ClassAnalyzer.getChangedClasses().size());
    ClassAnalyzer.deserialize(tmpFile);
    assertEquals(1, ClassAnalyzer.getChangedClasses().size());
  }

  private static class MockClass extends ExampleClass{
    public static void __resetCounters(){
    }
    public static int[] __getHitCounters(){
      return new int[0];
    }
    public static void __instrumentationInit(){}
  }

}
