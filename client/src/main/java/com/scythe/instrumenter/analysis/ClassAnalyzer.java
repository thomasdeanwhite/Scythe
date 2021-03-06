package com.scythe.instrumenter.analysis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.scythe.instrumenter.FileHandler;
import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.instrumenter.InstrumentationProperties.InstrumentationApproach;
import com.scythe.instrumenter.analysis.task.AbstractTask;
import com.scythe.instrumenter.analysis.task.Task;
import com.scythe.instrumenter.analysis.task.TaskTimer;
import com.scythe.instrumenter.instrumentation.ClassStore;
import com.scythe.instrumenter.instrumentation.LoggingUncaughtExceptionHandler;
import com.scythe.instrumenter.instrumentation.objectrepresentation.Branch;
import com.scythe.instrumenter.instrumentation.objectrepresentation.BranchHit;
import com.scythe.instrumenter.instrumentation.objectrepresentation.Line;
import com.scythe.instrumenter.instrumentation.objectrepresentation.LineHit;
import com.scythe.instrumenter.instrumentation.visitors.ArrayClassVisitor;
import com.scythe.instrumenter.testcase.TestCaseWrapper;
import com.scythe.output.Csv;
import com.scythe.util.ClassNameUtils;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClassAnalyzer {

  private static ArrayList<ThrowableListener> throwableListeners;

  private static ArrayList<String> branchesToCover;

  public static PrintStream out = System.out;

  private static ConcurrentHashMap<Integer, Map<Integer, LineHit>> lines;

  private static ConcurrentHashMap<Integer, Map<Integer, BranchHit>> branches;

  private static Map<Integer, String> classIds;
  private static Map<String, Integer> classNames;

  private static ArrayList<String> distancesWaiting;

  private static ArrayList<String> branchesDistance;

  private static HashMap<String, BranchType> branchTypes;

  private static HashMap<String, Integer> callFrequencies;

  private static HashMap<String, Float> branchDistance;

  private static final float BRANCH_DISTANCE_ADDITION = 50f;

  private static ArrayList<Class<?>> changedClasses;

  private static TestCaseWrapper activeTestCase;

  public static Map<String, Integer> getClassMapping() {
    return classNames;
  }

  static {
    Thread.currentThread().setUncaughtExceptionHandler(new LoggingUncaughtExceptionHandler());
    throwableListeners = new ArrayList<ThrowableListener>();

    branchesToCover = new ArrayList<String>();

    branches = new ConcurrentHashMap<Integer, Map<Integer, BranchHit>>();

    branchesDistance = new ArrayList<String>();
    classIds = new HashMap<Integer, String>();
    classNames = new HashMap<String, Integer>();
    branchTypes = new HashMap<String, BranchType>();
    branchDistance = new HashMap<String, Float>();
    lines = new ConcurrentHashMap<Integer, Map<Integer, LineHit>>();
    distancesWaiting = new ArrayList<String>();

    callFrequencies = new HashMap<String, Integer>();

    changedClasses = new ArrayList<Class<?>>();
  }

  public static ArrayList<LineHit> getTotalLines() {

    ArrayList<LineHit> lin = new ArrayList<LineHit>();
    HashMap<Integer, Map<Integer, LineHit>> linesCopy = new HashMap<Integer, Map<Integer, LineHit>>(
        lines);
    for (Integer i : linesCopy.keySet()) {
      for (Integer j : linesCopy.get(i).keySet()) {
        lin.add(linesCopy.get(i).get(j));
      }
    }
    return lin;
  }

  /**
   * Renamed to getRawBranches
   */
  @Deprecated
  public static Map<Integer, Map<Integer, BranchHit>> getTotalBranches() {
    return branches;
  }

  public static Map<Integer, Map<Integer, LineHit>> getRawLines() {
    return new HashMap<>(lines);
  }

  public static Map<Integer, Map<Integer, BranchHit>> getRawBranches() {
    return new HashMap<>(branches);
  }

  public static void reset() {
    List<Field> fields = Arrays.asList(ClassAnalyzer.class.getDeclaredFields()).stream().filter(
        f -> Collection.class.isAssignableFrom(f.getType()) ||
            Map.class.isAssignableFrom(f.getType()))
        .collect(Collectors.toList());
    for(Field f : fields){
      try {
        Object obj = f.get(null);
        Method clear = f.getType().getMethod("clear");
        clear.invoke(obj, null);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch(NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }

  public static void softReset() {
    for (Map<Integer, BranchHit> mb : branches.values()) {
      for (BranchHit b : mb.values()) {
        b.reset();
      }
    }

    for (Map<Integer, LineHit> lb : lines.values()) {
      for (LineHit l : lb.values()) {
        l.reset();
      }
    }

    ArrayList<Class<?>> changed = new ArrayList<Class<?>>(changedClasses);

    for (int i = 0; i < changed.size(); i++) {
      resetHitCounters(changed.get(i));
    }

    changedClasses.clear();
  }

  public static void setBranches(Map<Integer, Map<Integer, BranchHit>> b) {
//        for (Integer i : b.keySet()) {
//            Map<Integer, BranchHit> b2 = b.get(i);
//            for (Integer i2 : b2.keySet()) {
//                BranchHit bh = b2.get(i2);
//                registerClass(bh.getBranch().getClassName(), i);
//                if (i >= classId) {
//                    classId = i + 1;
//                }
//                if (branches.get(i) != null && branches.get(i).get(i2) != null) {
//                    bh.getBranch().falseHit(branches.get(i).get(i2).getBranch().getFalseHits());
//                    bh.getBranch().trueHit(branches.get(i).get(i2).getBranch().getTrueHits());
//                }
//            }
//        }
//
//        branches = new ConcurrentHashMap<>(b);
  }

  public static void setLines(Map<Integer, Map<Integer, LineHit>> l) {
    ConcurrentHashMap<Integer, Map<Integer, LineHit>> newLines = new ConcurrentHashMap<>();
    for (Integer i : l.keySet()) {
      Map<Integer, LineHit> b2 = l.get(i);
      for (Integer i2 : b2.keySet()) {
        LineHit bh = b2.get(i2);

        int newId = registerClass(bh.getLine().getClassName(), i);

        newLines.put(newId, l.get(i));
      }
    }

    lines = newLines;
  }

  public static void resetCoverage() {
    for (int classId : branches.keySet()) {
      for (BranchHit bh : branches.get(classId).values()) {
        bh.reset();
      }
    }

    for (int classId : lines.keySet()) {
      for (LineHit lh : lines.get(classId).values()) {
        lh.reset();
      }
    }
    if (InstrumentationProperties.INSTRUMENTATION_APPROACH == InstrumentationApproach.ARRAY
        && InstrumentationProperties.USE_CHANGED_FLAG) {
      for (Class<?> cl : changedClasses) {
        try {
          Field changed = cl.getDeclaredField("__changed");
          changed.setAccessible(true);
          changed.set(cl, false);
        } catch (Exception e) {
          e.printStackTrace(out);
        }
      }
    }
    changedClasses.clear();
  }

  public static void addThrowableListener(ThrowableListener tl) {
    throwableListeners.add(tl);
  }

  public static void throwableThrown(Throwable throwable) {
    for (ThrowableListener t : throwableListeners) {
      t.throwableThrown(throwable);
    }
  }

  public static void setOut(PrintStream stream) {
    out = stream;
  }

  private static int branchId = 0;

  private static int getNewBranchId() {
    return ++branchId;
  }

  private static int classId = -1;

  public static int registerClass(String className) {
    return registerClass(className, ++classId);
  }

  public static int registerClass(String className, int classId) {

    String clazz = ClassNameUtils.standardise(className);

    if (classNames.containsKey(clazz)) {
      return classNames.get(clazz);
    }

    if (classIds.containsKey(classId)) {
      return registerClass(className);
    }
    classIds.put(classId, clazz);
    classNames.put(clazz, classId);
    lines.put(classId, new HashMap<Integer, LineHit>());
    return classId;
  }

  public static int branchFound(int classId, int lineNumber) {
    return branchFound(classId, lineNumber, getNewBranchId());
  }

  public static int branchFound(int classId, int lineNumber, int branchId) {
    if (!branches.containsKey(classId)) {
      branches.put(classId, new HashMap<Integer, BranchHit>());
    }
    branches.get(classId)
        .put(branchId, new BranchHit(new Branch(classIds.get(classId), "<>", lineNumber), 0, 0));
    branches.get(classId).get(branchId).getBranch().setGoalId(branchId);
    return branchId;
  }

  public static BranchType getBranchType(String branch) {
    return branchTypes.get(branch);
  }

  public static void branchDistanceFound(String branch, BranchType type) {
    if (!branchTypes.containsKey(branch)) {
      branchTypes.put(branch, type);
    }
    if (!branchesDistance.contains(branch)) {
      branchesDistance.add(branch);
    }

  }

  public static synchronized void branchExecuted(boolean hit, int classId, int branchId) {
    if (branches.get(classId) == null) {
      branches.put(classId, new HashMap<Integer, BranchHit>());
    }

    BranchHit bh = branches.get(classId).get(branchId);
    if (bh == null) {
      branches.get(classId)
          .put(branchId, new BranchHit(new Branch(classIds.get(classId), "<>", 0), 0, 0));
    }
    if (bh != null) {
      bh.getBranch().hit(1);
      if (bh.getBranch().getClassName() == null) {
        bh.getBranch().setClassName(new Exception().getStackTrace()[1].getClassName());
      }
      if (InstrumentationProperties.TRACK_ACTIVE_TESTCASE) {
        bh.getBranch().addCoveringTest(activeTestCase);
      }
      Class<?> cl = ClassStore.get(bh.getBranch().getClassName());
      if (!changedClasses.contains(cl)) {
        changedClasses.add(cl);
      }
    }
  }

  public static void branchExecutedDistance(int i, int j, String branch) {
    if (!distancesWaiting.contains(branch)) {
      distancesWaiting.add(branch);
    }
    calculateBranchDistance(branch, i, j);
  }

  public static void branchExecutedDistance(float i, float j, String branch) {
    if (!distancesWaiting.contains(branch)) {
      distancesWaiting.add(branch);
    }
    calculateBranchDistance(branch, i, j);
  }

  public static void branchExecutedDistance(double i, double j, String branch) {
    if (!distancesWaiting.contains(branch)) {
      distancesWaiting.add(branch);
    }
    calculateBranchDistance(branch, (float) i, (float) j);

  }

  public static void branchExecutedDistance(long i, long j, String branch) {
    if (!distancesWaiting.contains(branch)) {
      distancesWaiting.add(branch);
    }
    calculateBranchDistance(branch, i, j);
  }

  public static void branchExecutedDistance(short i, short j, String branch) {
    if (!distancesWaiting.contains(branch)) {
      distancesWaiting.add(branch);
    }
    calculateBranchDistance(branch, i, j);

  }

  public static double getBranchCoverage() {
    int branchesTotal = 0;
    int branchesExecuted = 0;
    Set<Integer> keys = new HashMap<>(branches).keySet();
    for (int classId : keys) {
      Collection<BranchHit> values = new HashMap<>(branches.get(classId)).values();
      for (BranchHit b : values) {
        if (b.getBranch().getHits() > 0) {
          branchesExecuted++;
        }
        branchesTotal++;
      }
    }

    return branchesExecuted / (double) branchesTotal;
  }

  public static double calculateBranchDistance(String branch, float b1, float b2) {

    b1 += BRANCH_DISTANCE_ADDITION;
    b2 += BRANCH_DISTANCE_ADDITION;

    BranchType bt = branchTypes.get(branch);

    if (bt == null) {
      return 1d;
    }

    float bd = 0;

    switch (bt) {
      case BRANCH_E:
        bd = Math.abs(b1 - b2);
        break;
      case BRANCH_GE:
        bd = b1 - b2;
        break;
      case BRANCH_GT:
        bd = b1 - b2;
        break;
      case BRANCH_LE:
        bd = b2 - b1;
        break;
      case BRANCH_LT:
        bd = b2 - b1;
        break;
    }
    bd = Math.abs(bd / Float.MAX_VALUE);
    bd = Math.min(1f, Math.max(0f, bd));
    bd = (float) Math.pow(bd, 0.005);
    branchDistance.put(branch, bd);

    return bd;

  }

  /**
   * Returns distance between negative and positive branch hit. 0 is a positive hit, 1 is as far
   * away from positive as possible.
   */
  public static double getBranchDistance(String branch) {
    if (!branchDistance.containsKey(branch)) {
      return 1;
    }
    return branchDistance.get(branch);
  }

  public static synchronized List<BranchHit> getBranchesExecuted() {
    List<BranchHit> branchesHit = new ArrayList<BranchHit>();

    Iterator<Integer> iter = new HashMap<>(branches).keySet().iterator();

    while (iter.hasNext()) {
      Integer classId = iter.next();
      for (BranchHit b : branches.get(classId).values()) {
        if (b.getBranch().getHits() > 0) {
          branchesHit.add(b);
        }
      }
    }
    return branchesHit;
  }

  public static synchronized List<BranchHit> getBranchesNotExecuted() {
    List<BranchHit> branchesHit = new ArrayList<BranchHit>();
    for (int classId : branches.keySet()) {
      for (BranchHit b : branches.get(classId).values()) {
        if (b.getBranch().getHits() == 0) {
          branchesHit.add(b);
        }
      }
    }
    return branchesHit;
  }

  public static synchronized List<BranchHit> getAllBranches() {
    List<BranchHit> branchesHit = new ArrayList<BranchHit>();
    for (int classId : branches.keySet()) {
      for (BranchHit b : branches.get(classId).values()) {
        branchesHit.add(b);
      }
    }
    return branchesHit;
  }

  public static void lineFound(int classId, int lineNumber) {
    if (!lines.containsKey(classId)) {
      lines.put(classId, new HashMap<Integer, LineHit>());
    }

    for (LineHit lh : lines.get(classId).values()) {
      if (lh.getLine().getLineNumber() == lineNumber) {
        return;
      }
    }

    lines.get(classId).put(lineNumber,
        new LineHit(new Line(classIds.get(classId), "<>", lineNumber), lineNumber));
    lines.get(classId).get(lineNumber).getLine().setGoalId(lineNumber);
  }

  public static void lineExecuted(int classId, int lineNumber) {
    if (!lines.containsKey(classId)) {
      lines.put(classId, new HashMap<Integer, LineHit>());
    }
    LineHit lh = findOrCreateLine(classId, lineNumber);
    lh.getLine().hit(1);
    if (lh.getLine().getClassName() == null) {
      lh.getLine().setClassName(new Exception().getStackTrace()[1].getClassName());
    }
    if (InstrumentationProperties.TRACK_ACTIVE_TESTCASE) {
      lh.getLine().addCoveringTest(activeTestCase);
    }
    Class<?> cl = ClassStore.get(lh.getLine().getClassName());
    if (!changedClasses.contains(cl)) {
      changedClasses.add(cl);
    }
  }

  private static LineHit findOrCreateLine(int classId, int lineNumber) {
    if (lines.get(classId).containsKey(lineNumber)) {
      return lines.get(classId).get(lineNumber);
    }
    LineHit lh = new LineHit(new Line(classIds.get(classId), "<>", lineNumber), lineNumber);
    lines.get(classId).put(lineNumber, lh);
    return lh;
  }

  public static String getReport() {
    return "\n\t@ Lines:\n\t\t total: " + getTotalLines().size() + "\n\t\t covered: "
        + getLinesCovered().size() + "\n\t\t coverage "
        + getLineCoverage();

  }

  public static Csv toCsv() {
    int totalLines = getTotalLines().size();
    int coveredLines = getLinesCovered().size();

//        Set<Integer> set = new HashMap<>(lines).keySet();
//
//        for (int s : set) {
//            Map<Integer, LineHit> lh = lines.get(s);
//            for (int i : lh.keySet()) {
//                totalLines++;
//                int hits = lh.get(i).getLine().getHits();
//
//                assert hits >= 0;
//
//                if (hits > 0) {
//                    coveredLines++;
//                }
//            }
//        }

    Csv csv = new Csv();

    List<BranchHit> branchesExecuted = getBranchesExecuted();

    int exec = 0;

    for (BranchHit bh : branchesExecuted) {
      if (bh.getBranch().getHits() > 0) {
        exec++;
      }
    }

    int allBranches = getAllBranches().size();
    double bCoverage = exec / (float) allBranches;

    // *2 for true/false hits
    csv.add("branchesTotal", "" + allBranches);
    csv.add("branchesCovered", "" + exec);
    csv.add("branchCoverage", "" + bCoverage);
    csv.add("linesTotal", "" + totalLines);
    csv.add("linesCovered", "" + coveredLines);
    csv.add("lineCoverage", "" + ((float) coveredLines / (float) totalLines));

    return csv;

  }

  public static float getLineCoverage() {
    int totalLines = 0;
    int coveredLines = 0;
    Set<Integer> keys = new HashMap<>(lines).keySet();
    for (int s : keys) {
      Map<Integer, LineHit> lh = new HashMap<>(lines).get(s);
      totalLines += lh.size();
      Set<Integer> keys2 = new HashMap<>(lh).keySet();
      for (int i : keys2) {
        if (lh.get(i).getLine().getHits() > 0) {
          coveredLines++;
        }
      }
    }

    if (totalLines == 0) {
      return 0f;
    }

    return ((float) coveredLines / (float) totalLines);
  }

  public static double getLineCoverage(String className) {
    int classId = classNames.get(ClassNameUtils.standardise(className));
    Map<Integer, LineHit> classLines = lines.get(classId);
    int linesCovered = 0;
    int totalLines = classLines.size();
    if (totalLines == 0) {
      return 0;
    }
    if (classLines != null) {
      linesCovered = classLines.values().stream().mapToInt(l -> l.getLine().getHits() > 0 ? 1 : 0)
          .sum();
    }
    return ((double) linesCovered) / totalLines;
  }

  public static double getBranchCoverage(String className) {
    int classId = classNames.get(ClassNameUtils.standardise(className));
    Map<Integer, BranchHit> classBranches = branches.get(classId);
    int branchesCovered = 0;
    int totalBranches = classBranches.size();
    if (totalBranches == 0) {
      return 0;
    }
    if (classBranches != null) {
      branchesCovered = classBranches.values().stream()
          .mapToInt(b -> b.getBranch().getHits() > 0 ? 1 : 0).sum();
    }
    return ((double) branchesCovered) / totalBranches;
  }

  public static void output(String file, String file2, String forbidden) {

    Gson g = new Gson();
    if (forbidden == null) {
      forbidden = "";
    }
    String[] forbid = forbidden.split(",");

    for (int i = 0; i < forbid.length; i++) {
      forbid[i] = ClassNameUtils.standardise(forbid[i]);
    }

    try {

      HashMap<Integer, Map<Integer, BranchHit>> outputBranches = new HashMap<Integer, Map<Integer, BranchHit>>();
      HashMap<Integer, Map<Integer, LineHit>> outputLines = new HashMap<Integer, Map<Integer, LineHit>>();

      for (Integer classId : lines.keySet()) {
        String className = "";
        try {
          className = ClassNameUtils.standardise(classIds.get(classId));
        } catch (NullPointerException e) {
          continue;
        }

        boolean fbd = false;

        for (String s : forbid) {
          if (className.equals(s)) {
            fbd = true;
            break;
          }
        }

        if (!fbd) {
          outputBranches.put(classId, branches.get(classId));
          outputLines.put(classId, lines.get(classId));
        }
      }

      FileHandler.writeToFile(new File(file), g.toJson(outputBranches));
      FileHandler.writeToFile(new File(file2), g.toJson(outputLines));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void classAnalyzed(int classId, List<BranchHit> branchHitCounterIds,
      List<LineHit> lineHitCounterIds) {
    lines.put(classId, new HashMap<Integer, LineHit>());
    for (LineHit lh : lineHitCounterIds) {
      lh.getLine().setGoalId(lh.getLine().getLineNumber());
      lines.get(classId).put(lh.getCounterId(), lh);
    }
    branches.put(classId, new HashMap<Integer, BranchHit>());
    for (BranchHit b : branchHitCounterIds) {
      int branchId = getNewBranchId();
      b.getBranch().setGoalId(branchId);
      branches.get(classId).put(branchId, b);
    }
  }

  private static Line findLineWithCounterId(int classId, int i) {
    if (!lines.containsKey(classId)) {
      lines.put(classId, new HashMap<>());
    }
    return lines.get(classId).containsKey(i) ? lines.get(classId).get(i).getLine() : null;
  }

  private static BranchHit findBranchWithCounterId(int classId, int i) {
    if (!branches.containsKey(classId)) {
      branches.put(classId, new HashMap<>());
    }
    for (BranchHit bh : branches.get(classId).values()) {
      if (bh.getCounterId() == i) {
        return bh;
      }
    }
    return null;
  }

  private static BranchHit findBranchDistanceWithCounterId(int classId, int
      i) {
    if (!branches.containsKey(classId)) {
      branches.put(classId, new HashMap<>());
    }
    for (BranchHit bh : branches.get(classId).values()) {
      if (bh.getDistanceId() == i) {
        return bh;
      }
    }
    return null;
  }

  public static void classChanged(String changedClass) {

    changedClass = ClassNameUtils.standardise(changedClass);

    Class<?> cl = ClassStore.get(changedClass);
    if (cl != null) {
      changedClasses.add(cl);
    }
  }

  public static boolean collectingHitCounters = false;

  @Deprecated
  /**
   * Please use collectHitCounters(boolean reset)
   */
  public static void collectHitCounters() {
    collectHitCounters(true);
  }

  public static void collectHitCounters(boolean reset) {
//        while (collectingHitCounters) {
//            try {
//                Thread.sleep(50);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    Task timerTask = new CollectHitCountersTimer();
    if (InstrumentationProperties.INSTRUMENTATION_APPROACH == InstrumentationApproach.ARRAY) {
      collectingHitCounters = true;
      if (InstrumentationProperties.LOG) {
        TaskTimer.taskStart(timerTask);
      }
      List<Class<?>> classes = new ArrayList<Class<?>>(changedClasses);
      if (!InstrumentationProperties.USE_CHANGED_FLAG) {
        classes = new ArrayList<Class<?>>();
        for (int classId : classIds.keySet()) {
          Class<?> c = ClassStore.get(classIds.get(classId));
          if (c == null) {
            try {
              c = ClassLoader.getSystemClassLoader().loadClass(classIds.get(classId));
            } catch (ClassNotFoundException e) {
              e.printStackTrace();
            }
          }
          classes.add(c);
        }
        changedClasses.addAll(classes);

      }

      for (Class<?> c : changedClasses) {
        try {
          collectHitCountersForClass(c, reset);

        } catch (Exception e) {
          e.printStackTrace(out);
        } catch (Error e) {
          e.printStackTrace(out);
        }
      }
      if (InstrumentationProperties.LOG) {
        TaskTimer.taskEnd(timerTask);
      }
      collectingHitCounters = false;
    }
  }

  public static void collectHitCountersForClass(Class<?> cl, boolean reset)
      throws NoSuchMethodException, InvocationTargetException,
      IllegalAccessException {
    Method getCounters = cl
        .getDeclaredMethod(ArrayClassVisitor.COUNTER_METHOD_NAME, new Class<?>[]{});
    getCounters.setAccessible(true);
    int[] counters = (int[]) getCounters.invoke(null, new Object[]{});
    if (counters != null) {
      for (int i = 0; i < counters.length; i++) {

        Object o = classNames.get(cl.getName());
        if (o == null) {
          o = classNames.get(ClassNameUtils.standardise(cl.getName()));
        }

        if (o == null) {
          registerClass(ClassNameUtils.standardise(cl.getName()));
          o = classNames.get(ClassNameUtils.standardise(cl.getName()));
        }

        if (o == null) {
          continue;
        }

        int classId = (Integer) o;
        Line line = findLineWithCounterId(classId, i);
        if (line != null) {
          line.hit(counters[i]);

          if (InstrumentationProperties.TRACK_ACTIVE_TESTCASE) {
            line.addCoveringTest(activeTestCase);
          }
        }
        BranchHit branch = findBranchWithCounterId(classId, i);
        if (branch != null) {
          if (branch.getCounterId() == i) {
            branch.getBranch().hit(counters[i]);
            // if (superClassId >= 0) {
            // for (BranchHit bh : branches.get(superClassId)){
            // if (bh)
            // }
            // }
          }
          if (InstrumentationProperties.TRACK_ACTIVE_TESTCASE) {
            branch.getBranch().addCoveringTest(activeTestCase);
          }
        }

      }
    }

    Method getDistance = cl
        .getDeclaredMethod(ArrayClassVisitor.DISTANCE_METHOD_NAME, new Class<?>[]{});
    getDistance.setAccessible(true);
    float[] distances = (float[]) getDistance.invoke(null, new
        Object[]{});
    if (distances != null) {
      for (int i = 0; i < distances.length; i++) {

        Object o = classNames.get(cl.getName());
        if (o == null) {
          o = classNames.get(ClassNameUtils.standardise(cl.getName()));
        }

        if (o == null) {
          registerClass(ClassNameUtils.standardise(cl.getName()));
          o = classNames.get(ClassNameUtils.standardise(cl.getName()));
        }

        if (o == null) {
          continue;
        }

        int classId = (Integer) o;
        BranchHit branch = findBranchDistanceWithCounterId
            (classId, i);
        if (branch != null) {
          branch.setDistance(Math.abs(distances[i]));
        }

      }
    }
    if (reset) {
      resetHitCounters(cl);
    }
  }

  public static int getClassId(String className) {

    className = ClassNameUtils.standardise(className);

    if (classNames.containsKey(className)) {
      return classNames.get(className);
    }
    return -1;
  }

  public static ArrayList<LineHit> getLinesCovered() {
    collectHitCounters(false);

    ArrayList<LineHit> coveredLines = new ArrayList<LineHit>();

    Iterator<Integer> iter = new HashMap<>(lines).keySet().iterator();

    while (iter.hasNext()) {

      Integer i = iter.next();

      Map<Integer, LineHit> h = new HashMap<>(lines.get(i));

      for (LineHit l : h.values()) {
        if (l.getLine().getHits() > 0) {
          coveredLines.add(l);
        }
      }
    }

    return coveredLines;

  }

  public static void resetHitCounters(Class<?> cl) {
    try {
      Method resetCounters = cl
          .getDeclaredMethod(ArrayClassVisitor.RESET_COUNTER_METHOD_NAME, new Class[]{});
      resetCounters.setAccessible(true);
      resetCounters.invoke(null, new Object[]{});
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

  }

  public static List<Line> getCoverableLines(String className) {
    if (className == null) {
      return new ArrayList<Line>();
    }

    className = ClassNameUtils.standardise(className);

    if (!classNames.containsKey(className)) {
      return new ArrayList<Line>();
    }

    int classId = classNames.get(className);

    List<Line> coverableLines = new ArrayList<Line>();
    if (lines.get(classId) == null) {
      return coverableLines;
    }
    Collection<LineHit> lhs = lines.get(classId).values();
    for (LineHit lh : lhs) {
      coverableLines.add(lh.getLine());
    }
    return coverableLines;
  }

  public static List<Line> getCoverableLines(String className, String methodName) {
    List<String> method = new ArrayList<String>();

    method.add(methodName);

    return getCoverableLines(className, method);
  }

  public static List<Line> getCoverableLines(String className, List<String> methodNames) {
    if (className == null || methodNames == null || methodNames.size() == 0) {
      return new ArrayList<Line>();
    }

    className = ClassNameUtils.standardise(className);

    if (!classNames.containsKey(className)) {
      return new ArrayList<Line>();
    }

    int classId = classNames.get(className);

    if (!lines.containsKey(classId)) {
      return Collections.<Line>emptyList();
    }
    List<Line> coverableLines = new ArrayList<Line>();
    for (LineHit lh : lines.get(classId).values()) {
      if (methodNames.contains(lh.getLine().getMethodName())) {
        coverableLines.add(lh.getLine());
      }
    }
    return coverableLines;
  }

  public static List<Branch> getCoverableBranches(String className) {
    if (className == null) {
      return new ArrayList<Branch>();
    }

    className = ClassNameUtils.standardise(className);

    if (!classNames.containsKey(className)) {
      return new ArrayList<Branch>();
    }

    int classId = classNames.get(className);

    if (!branches.containsKey(classId)) {
      return Collections.<Branch>emptyList();
    }
    List<Branch> coverableBranches = new ArrayList<Branch>();
    for (BranchHit bh : branches.get(classId).values()) {
      coverableBranches.add(bh.getBranch());
    }
    return coverableBranches;
  }

  public static List<BranchHit> getBranchDistances(String className) {
    if (className == null) {
      return new ArrayList<BranchHit>();
    }

    className = ClassNameUtils.standardise(className);

    if (!classNames.containsKey(className)) {
      return new ArrayList<BranchHit>();
    }

    int classId = classNames.get(className);

    if (!branches.containsKey(classId)) {
      return Collections.<BranchHit>emptyList();
    }
    List<BranchHit> coverableBranches = new ArrayList<BranchHit>();
    for (BranchHit bh : branches.get(classId).values()) {
      coverableBranches.add(bh);
    }
    return coverableBranches;
  }

  public static List<Branch> getCoverableBranches(String className, String methodName) {
    List<String> method = new ArrayList<String>();

    method.add(methodName);

    return getCoverableBranches(className, method);
  }

  public static List<Branch> getCoverableBranches(String className, List<String> methodNames) {
    if (className == null || methodNames == null || methodNames.size() == 0) {
      return new ArrayList<Branch>();
    }

    className = ClassNameUtils.standardise(className);
    if (!classNames.containsKey(className)) {
      return new ArrayList<Branch>();
    }

    int classId = classNames.get(className);

    if (!branches.containsKey(classId)) {
      return Collections.<Branch>emptyList();
    }
    List<Branch> coverableBranches = new ArrayList<Branch>();
    for (BranchHit bh : branches.get(classId).values()) {
      if (methodNames.contains(bh.getBranch().getMethodName())) {
        coverableBranches.add(bh.getBranch());
      }
    }
    return coverableBranches;
  }

  public static List<Class<?>> getChangedClasses() {
    return new ArrayList<Class<?>>(changedClasses);
  }

  private static final class CollectHitCountersTimer extends AbstractTask {

    @Override
    public String asString() {
      return "Collecting hit counters";
    }
  }

  public static TestCaseWrapper getActiveTestCase() {
    return activeTestCase;
  }

  public static void setActiveTestCase(TestCaseWrapper activeTestCase) {
    ClassAnalyzer.activeTestCase = activeTestCase;
  }

  public static void serialize(File destination) {
    Gson gson = getGson();
    try {
      JsonWriter writer = new JsonWriter(new FileWriter(destination));
      gson.toJson(new ClassAnalyzer(), ClassAnalyzer.class, writer);
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void deserialize(File source) {
    Gson gson = getGson();
    try {
      gson.fromJson(new JsonReader(new FileReader(source)), ClassAnalyzer.class);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private static Gson getGson() {
    GsonBuilder builder = new GsonBuilder();
    if (InstrumentationProperties.DISABLE_HTML_ESCAPE) {
      builder.disableHtmlEscaping();
    }
    if (InstrumentationProperties.PRETTY_PRINT_COVERAGE) {
      builder.setPrettyPrinting();
    }
    CoverageSerializer cs = new CoverageSerializer();
    return builder.registerTypeAdapter(ClassAnalyzer.class, cs)
        .excludeFieldsWithModifiers(Modifier.TRANSIENT)
        .create();
  }

}
