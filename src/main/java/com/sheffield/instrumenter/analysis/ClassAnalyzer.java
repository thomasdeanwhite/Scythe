package com.sheffield.instrumenter.analysis;

import com.google.gson.Gson;
import com.sheffield.instrumenter.FileHandler;
import com.sheffield.instrumenter.InstrumentationProperties;
import com.sheffield.instrumenter.InstrumentationProperties.InstrumentationApproach;
import com.sheffield.instrumenter.analysis.task.AbstractTask;
import com.sheffield.instrumenter.analysis.task.Task;
import com.sheffield.instrumenter.analysis.task.TaskTimer;
import com.sheffield.instrumenter.instrumentation.ClassStore;
import com.sheffield.instrumenter.instrumentation.LoggingUncaughtExceptionHandler;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Branch;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.BranchHit;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Line;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.LineHit;
import com.sheffield.instrumenter.instrumentation.visitors.ArrayClassVisitor;
import com.sheffield.instrumenter.testcase.TestCaseWrapper;
import com.sheffield.output.Csv;
import com.sheffield.util.ClassNameUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ClassAnalyzer {

  private static ArrayList<ThrowableListener> throwableListeners;

  private static ArrayList<String> branchesToCover;

  public static PrintStream out = System.out;

  private static Map<Integer, Map<Integer, LineHit>> lines;

  private static Map<Integer, Map<Integer, BranchHit>> branches;

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

  static {
    Thread.currentThread().setUncaughtExceptionHandler(new LoggingUncaughtExceptionHandler());
    throwableListeners = new ArrayList<ThrowableListener>();

    branchesToCover = new ArrayList<String>();

    branches = new HashMap<Integer, Map<Integer, BranchHit>>();

    branchesDistance = new ArrayList<String>();
    classIds = new HashMap<Integer, String>();
    classNames = new HashMap<String, Integer>();
    branchTypes = new HashMap<String, BranchType>();
    branchDistance = new HashMap<String, Float>();
    lines = new HashMap<Integer, Map<Integer, LineHit>>();
    distancesWaiting = new ArrayList<String>();

    callFrequencies = new HashMap<String, Integer>();

    changedClasses = new ArrayList<Class<?>>();
  }

  public static void reset() {
    branches.clear();
    lines.clear();
    changedClasses.clear();
  }

  public static void setBranches(Map<Integer, Map<Integer, BranchHit>> b) {
    for (Integer i : b.keySet()) {
      Map<Integer, BranchHit> b2 = b.get(i);
      for (Integer i2 : b2.keySet()) {
        BranchHit bh = b2.get(i2);
        registerClass(bh.getBranch().getClassName(), i);
        if (i >= classId) {
          classId = i + 1;
        }
        if (branches.get(i) != null && branches.get(i).get(i2) != null) {
          bh.getBranch().falseHit(branches.get(i).get(i2).getBranch().getFalseHits());
          bh.getBranch().trueHit(branches.get(i).get(i2).getBranch().getTrueHits());
        }
      }
    }

    branches = b;
  }

  public static void setLines(Map<Integer, Map<Integer, LineHit>> l) {
    for (Integer i : l.keySet()) {
      Map<Integer, LineHit> b2 = l.get(i);
      for (Integer i2 : b2.keySet()) {
        LineHit bh = b2.get(i2);
        registerClass(bh.getLine().getClassName(), i);
        if (i >= classId) {
          classId = i + 1;
        }
        if (lines.get(i) != null && lines.get(i).get(i2) != null) {
          bh.getLine().hit(lines.get(i).get(i2).getLine().getHits());
        }
      }
    }

    lines = l;
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

    String clazz = className;

    if (classNames.containsKey(className)) {
      return classNames.get(className);
    }

    if (classNames.containsKey(clazz)) {
      classId = classNames.get(clazz);
    }

    classIds.put(classId, className);
    classNames.put(className, classId);

    return classId;
  }

  public static int branchFound(int classId, int lineNumber) {
    return branchFound(classId, lineNumber, getNewBranchId());
  }

  public static int branchFound(int classId, int lineNumber, int branchId) {
    if (!branches.containsKey(classId)) {
      branches.put(classId, new HashMap<Integer, BranchHit>());
    }
    branches.get(classId).put(branchId, new BranchHit(new Branch(classIds.get(classId), lineNumber), 0, 0));
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
      branches.get(classId).put(branchId, new BranchHit(new Branch(classIds.get(classId), 0), 0, 0));
    }
    if (bh != null) {
      if (hit) {
        bh.getBranch().trueHit(1);
      } else {
        bh.getBranch().falseHit(1);
      }
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

  public static double branchCoverage() {
    int branchesTotal = 0;
    int branchesExecuted = 0;
    for (int classId : branches.keySet()) {
      for (BranchHit b : branches.get(classId).values()) {
        if (b.getBranch().getFalseHits() > 0) {
          branchesExecuted++;
        }
        if (b.getBranch().getTrueHits() > 0) {
          branchesExecuted++;
        }
        branchesTotal += 2;
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
   * Returns distance between negative and positive branch hit. 0 is a positive hit, 1 is as far away from positive as possible.
   *
   * @param branch
   * @return
   */
  public static double getBranchDistance(String branch) {
    if (!branchDistance.containsKey(branch)) {
      return 1;
    }
    return branchDistance.get(branch);
  }

  public static synchronized List<BranchHit> getBranchesExecuted() {
    List<BranchHit> branchesHit = new ArrayList<BranchHit>();
    for (int classId : branches.keySet()) {
      for (BranchHit b : branches.get(classId).values()) {
        if (b.getBranch().getTrueHits() > 0 || b.getBranch().getFalseHits() > 0) {
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
        if (b.getBranch().getFalseHits() > 0) {
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
    lines.get(classId).put(lineNumber, new LineHit(new Line(classIds.get(classId), lineNumber), -1));
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
    LineHit lh = new LineHit(new Line(classIds.get(classId), lineNumber), -1);
    lines.get(classId).put(lineNumber, lh);
    return lh;
  }

  public static double lineCoverage() {
    int totalLines = 0;
    int coveredLines = 0;
    int classId = -1;
    for (Iterator<Integer> it = lines.keySet().iterator(); it.hasNext();) {
      classId = it.next();
      for (LineHit lh : lines.get(classId).values()) {
        if (lh.getLine().getHits() > 0) {
          coveredLines++;
        }
        totalLines++;
      }

    }
    return coveredLines / (double) totalLines;
  }

  public static String getReport() {
    double bCoverage = branchCoverage();
    return "\t@ Branches Discovered: " + getAllBranches().size() + "\n\t@ Branches Covered: "
        + getBranchesExecuted().size() + "\n\t@ Branch Coverage: " + bCoverage;

  }

  public static Csv toCsv() {
    int totalLines = 0;
    int coveredLines = 0;
    for (int s : lines.keySet()) {
      Map<Integer, LineHit> lh = lines.get(s);
      for (int i : lh.keySet()) {
        totalLines++;
        if (lh.get(i).getLine().getHits() > 0) {
          coveredLines++;
        }
      }
    }

    Csv csv = new Csv();

    List<BranchHit> branchesExecuted = getBranchesExecuted();

    int exec = 0;

    for (BranchHit bh : branchesExecuted) {
      if (bh.getBranch().getTrueHits() > 0) {
        exec++;
      }

      if (bh.getBranch().getFalseHits() > 0) {
        exec++;
      }
    }

    int allBranches = getAllBranches().size() * 2;
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
    for (int s : lines.keySet()) {
      Map<Integer, LineHit> lh = lines.get(s);
      totalLines += lh.size();
      for (int i : lh.keySet()) {
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

  public static void classAnalyzed(int classId, List<BranchHit> branchHitCounterIds, List<LineHit> lineHitCounterIds) {
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
    return lines.get(classId).containsKey(i) ? lines.get(classId).get(i).getLine() : null;
  }

  private static BranchHit findBranchWithCounterId(int classId, int i) {
    for (BranchHit bh : branches.get(classId).values()) {
      if (bh.getFalseCounterId() == i || bh.getTrueCounterId() == i) {
        return bh;
      }
    }
    return null;
  }

  public static void classChanged(String changedClass) {
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
    while (collectingHitCounters) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    Task timerTask = new CollectHitCountersTimer();
    if (InstrumentationProperties.INSTRUMENTATION_APPROACH == InstrumentationApproach.ARRAY) {
      collectingHitCounters = true;
      if (InstrumentationProperties.LOG) {
        TaskTimer.taskStart(timerTask);
      }
      List<Class<?>> classes = changedClasses;
      if (!InstrumentationProperties.USE_CHANGED_FLAG) {
        classes = new ArrayList<Class<?>>();
        for (int classId : lines.keySet()) {
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

      for (int c = 0; c < classes.size(); c++) {
        Class<?> cl = classes.get(c);
        try {
          Method getCounters = cl.getDeclaredMethod(ArrayClassVisitor.COUNTER_METHOD_NAME, new Class<?>[] {});
          getCounters.setAccessible(true);
          int[] counters = (int[]) getCounters.invoke(null, new Object[] {});
          if (counters != null) {
            for (int i = 0; i < counters.length; i++) {

              Object o = classNames.get(cl.getName());
              if (o == null) {
                o = classNames.get(ClassNameUtils.standardise(cl.getName()));
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
                if (branch.getTrueCounterId() == i) {
                  branch.getBranch().trueHit(counters[i]);
                  // if (superClassId >= 0) {
                  // for (BranchHit bh : branches.get(superClassId)){
                  // if (bh)
                  // }
                  // }
                } else {
                  branch.getBranch().falseHit(counters[i]);
                }
                if (InstrumentationProperties.TRACK_ACTIVE_TESTCASE) {
                  branch.getBranch().addCoveringTest(activeTestCase);
                }
              }

              String className = cl.getName();

            }
          }
          if (reset) {
            resetHitCounters(cl);
          }
        } catch (Exception e) {
          e.printStackTrace(out);
        }
      }
      if (InstrumentationProperties.LOG) {
        TaskTimer.taskEnd(timerTask);
      }
      collectingHitCounters = false;
    }
  }

  public static int getClassId(String className) {
    return classNames.get(ClassNameUtils.standardise(className));
  }

  public static ArrayList<LineHit> getLinesCovered() {

    collectHitCounters(false);

    ArrayList<LineHit> coveredLines = new ArrayList<LineHit>();

    for (Integer i : lines.keySet()) {
      Map<Integer, LineHit> h = lines.get(i);

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
      Method resetCounters = cl.getDeclaredMethod(ArrayClassVisitor.RESET_COUNTER_METHOD_NAME, new Class[] {});
      resetCounters.setAccessible(true);
      resetCounters.invoke(null, new Object[] {});
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

    className = className.replace(".", "/");
    int classId;
    try {
      classId = classNames.get(className);
    } catch (NullPointerException e) {
      className = className.replace("/", ".");
      try {
        classId = classNames.get(className);
      } catch (NullPointerException e2) {
        return new ArrayList<Line>();
      }
    }

    if (!lines.containsKey(classId)) {
      return Collections.<Line> emptyList();
    }
    List<Line> coverableLines = new ArrayList<Line>();
    Collection<LineHit> lhs = lines.get(classId).values();
    for (LineHit lh : lhs) {
      coverableLines.add(lh.getLine());
    }
    return coverableLines;
  }

  public static List<Branch> getCoverableBranches(String className) {
    if (className == null) {
      return new ArrayList<Branch>();
    }

    className = className.replace(".", "/");
    int classId = 0;
    try {
      classId = classNames.get(className);
    } catch (NullPointerException e) {
      try {
        className = className.replace("/", ".");
        classId = classNames.get(className);
      } catch (NullPointerException e2) {
        return new ArrayList<Branch>();
      }
    }
    if (!branches.containsKey(classId)) {
      return Collections.<Branch> emptyList();
    }
    List<Branch> coverableBranches = new ArrayList<Branch>();
    for (BranchHit bh : branches.get(classId).values()) {
      coverableBranches.add(bh.getBranch());
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
}
