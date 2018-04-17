package com.scythe.instrumenter.instrumentation.visitors;

import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.instrumenter.analysis.ClassAnalyzer;
import com.scythe.instrumenter.instrumentation.objectrepresentation.Branch;
import com.scythe.instrumenter.instrumentation.objectrepresentation.BranchHit;
import com.scythe.instrumenter.instrumentation.objectrepresentation.Line;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import test.classes.ExampleClass;

import static org.junit.Assert.*;

/**
 * Created by thomas on 23/11/2016.
 */
public class TestBranchArrayVisitor {

    private static final boolean WRITE_CLASS = true;

    Class ic = null;

    @Before
    public void setup() {
        ClassAnalyzer.softReset();
        try {
            if (WRITE_CLASS) {
                InstrumentationProperties.WRITE_CLASS = true;
                InstrumentationProperties.BYTECODE_DIR = "";
            }
            ic = ClassTester.getInstrumentedTestClass();


        } catch (ClassNotFoundException e) {
            ClassAnalyzer.out.println("Test Class 1 not found!");
        }
    }

    @Test
    public void instrumentClassIsFound() {
        try {
            ic = ClassTester.getInstrumentedTestClass();
        } catch (ClassNotFoundException e) {
            fail("Cannot find test class");
        }
    }

    @Test
    public void branchCoveredFalse()
            throws NoSuchMethodException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        Object o = ic.newInstance();

        Method m = ic.getDeclaredMethod("abs", new Class[]{int.class});

        m.setAccessible(true);
        Object r = m.invoke(o, 1);

        ClassAnalyzer.collectHitCounters(false);

        List<Branch> covered = ClassAnalyzer.getCoverableBranches
                (ExampleClass.class.getName());

        int trueHits = 0;
        int falseHits = 0;

        for (Branch b : covered) {
            if (b.getGoalId() == 3) {
                trueHits += b.getHits();
            } else if (b.getGoalId() == 4) {
                falseHits += b.getHits();
            }
        }

        assertEquals(0, falseHits);
        assertEquals(1, trueHits);
    }

    @Test
    public void branchCoveredFalseTwice()
            throws NoSuchMethodException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        Object o = ic.newInstance();

        Method m = ic.getDeclaredMethod("abs", new Class[]{int.class});

        m.setAccessible(true);
        Object r = m.invoke(o, 1);
        r = m.invoke(o, 1);

        ClassAnalyzer.collectHitCounters(false);

        List<Branch> covered = ClassAnalyzer.getCoverableBranches
                (ExampleClass.class.getName());

        int trueHits = 0;
        int falseHits = 0;

        for (Branch b : covered) {
            if (b.getGoalId() == 3) {
                trueHits += b.getHits();
            } else if (b.getGoalId() == 4){
                falseHits += b.getHits();
            }
        }

        assertEquals(0, falseHits);
        assertEquals(2, trueHits);
    }

    @Test
    public void branchCoveredTrueTwice()
            throws NoSuchMethodException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        Object o = ic.newInstance();

        Method m = ic.getDeclaredMethod("abs", new Class[]{int.class});

        m.setAccessible(true);
        Object r = m.invoke(o, -1);
        r = m.invoke(o, -1);

        ClassAnalyzer.collectHitCounters(false);

        List<Branch> covered = ClassAnalyzer.getCoverableBranches
                (ExampleClass.class.getName());

        int trueHits = 0;

        for (Branch b : covered) {
            if (b.getGoalId() == 4) {
                trueHits += b.getHits();
            }
        }

        assertEquals(2, trueHits);
    }

    @Test
    public void branchCoveredBoth()
            throws NoSuchMethodException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        Object o = ic.newInstance();

        Method m = ic.getDeclaredMethod("abs", new Class[]{int.class});

        m.setAccessible(true);
        Object r = m.invoke(o, 1);
        r = m.invoke(o, -1);

        ClassAnalyzer.collectHitCounters(false);

        List<Branch> covered = ClassAnalyzer.getCoverableBranches
                (ExampleClass.class.getName());

        int trueHits = 0;

        for (Branch b : covered) {
            if (b.getGoalId() == 3 || b.getGoalId() == 4) {
                trueHits += b.getHits();
            }
        }
        assertEquals(2, trueHits);
    }


    @Test
    public void branchCoveredDistance()
            throws NoSuchMethodException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        Object o = ic.newInstance();

        Method m = ic.getDeclaredMethod("abs", new Class[]{int.class});

        m.setAccessible(true);
        Object r = m.invoke(o, 5);
        //r = m.invoke(o, -1);

        ClassAnalyzer.collectHitCounters(false);

        List<BranchHit> covered = ClassAnalyzer.getBranchDistances(
                ExampleClass.class.getName());

        assertTrue(covered.size() > 0);

        for (BranchHit b : covered) {
            if (b.getBranch().getHits() == 0 && b.getBranch().getLineNumber() == 12) {
                assertEquals(5, b.getDistance(), 0.0000001);
            }
        }
    }

    @Test
    public void branchCoveredDistanceIcmp()
            throws NoSuchMethodException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        Object o = ic.newInstance();

        Method m = ic.getDeclaredMethod("abs", new Class[]{int.class, int.class});

        m.setAccessible(true);
        Object r = m.invoke(o, 5, 1);
        //r = m.invoke(o, -1);

        ClassAnalyzer.collectHitCounters(false);

        List<BranchHit> covered = ClassAnalyzer.getBranchDistances(
                ExampleClass.class.getName());

        assertTrue(covered.size() > 0);

        for (BranchHit b : covered) {
            if (b.getBranch().getHits() == 0 && b.getBranch().getLineNumber() == 19) {
                assertEquals(4, b.getDistance(), 0.0000001);
            }
        }
    }

    @Test
    public void branchCoveredDistanceFalse()
            throws NoSuchMethodException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        Object o = ic.newInstance();

        Method m = ic.getDeclaredMethod("abs", new Class[]{int.class});

        m.setAccessible(true);
        Object r = m.invoke(o, -10);
        //r = m.invoke(o, -1);

        ClassAnalyzer.collectHitCounters(false);

        List<BranchHit> covered = ClassAnalyzer.getBranchDistances(
                ExampleClass.class.getName());

        assertTrue(covered.size() > 0);

        for (BranchHit b : covered) {
            if (b.getBranch().getHits() == 0 && b.getBranch().getLineNumber() == 12) {
                assertEquals(10, b.getDistance(), 0.0000001);
            }
        }
    }

    @Test
    public void branchCoveredDistanceBranchCollectCall()
            throws NoSuchMethodException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        Object o = ic.newInstance();

        ClassAnalyzer.collectHitCounters(false);

        Method m = ic.getDeclaredMethod("abs", new Class[]{int.class});

        m.setAccessible(true);
        Object r = m.invoke(o, -10);
        //r = m.invoke(o, -1);

        List<BranchHit> covered = ClassAnalyzer.getBranchDistances(
                ExampleClass.class.getName());

        assertTrue(covered.size() > 0);

        for (BranchHit b : covered) {
            b.collect();
            if (b.getBranch().getHits() == 0 && b.getBranch().getLineNumber() == 12) {
                assertEquals(10, b.getDistance(), 0.0000001);
            }
        }
    }


    @Test
    public void branchCoveredDistanceTwoParams()
            throws NoSuchMethodException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        Object o = ic.newInstance();

        ClassAnalyzer.collectHitCounters(false);

        Method m = ic.getDeclaredMethod("abs", new Class[]{int.class, int.class});

        m.setAccessible(true);
        Object r = m.invoke(o, -20, 10);
        //r = m.invoke(o, -1);

        List<BranchHit> covered = ClassAnalyzer.getBranchDistances(
                ExampleClass.class.getName());

        assertTrue(covered.size() > 0);

        for (BranchHit b : covered) {
            b.collect();
            if (b.getBranch().getHits() == 0 && b.getBranch().getLineNumber() == 19) {
                assertEquals(30, b.getDistance(), 0.0000001);
            }
        }
    }


    @Test
    public void branchCoveredDistanceSingleStackElement()
            throws NoSuchMethodException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        Object o = ic.newInstance();

        ClassAnalyzer.collectHitCounters(false);

        Method m = ic.getDeclaredMethod("isZero", new Class[]{float.class});

        m.setAccessible(true);
        Object r = m.invoke(o, 0f);
        //r = m.invoke(o, -1);

        List<BranchHit> covered = ClassAnalyzer.getBranchDistances(
                ExampleClass.class.getName());

        assertTrue(covered.size() > 0);

        for (BranchHit b : covered) {
            b.collect();
            if (b.getBranch().getHits() == 0 && b.getBranch().getLineNumber() == 28) {
                assertEquals(0, b.getDistance(), 0.0000001);
            }
        }
    }

    @Test
    public void branchCoveredDistanceSingleStackElementLong()
            throws NoSuchMethodException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        Object o = ic.newInstance();

        ClassAnalyzer.collectHitCounters(false);

        Method m = ic.getDeclaredMethod("isZeroLong", new Class[]{long.class});

        m.setAccessible(true);
        Object r = m.invoke(o, 0L);
        //r = m.invoke(o, -1);

        List<BranchHit> covered = ClassAnalyzer.getBranchDistances(
                ExampleClass.class.getName());

        assertTrue(covered.size() > 0);

        for (BranchHit b : covered) {
            b.collect();
            if (b.getBranch().getHits() == 0 && b.getBranch().getLineNumber() == 28) {
                assertEquals(0, b.getDistance(), 0.0000001);
            }
        }
    }


    @Test
    public void linesCoveredTrueBranchHit()
            throws NoSuchMethodException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        Object o = ic.newInstance();

        Method m = ic.getDeclaredMethod("abs", new Class[]{int.class});

        m.setAccessible(true);
        Object r = m.invoke(o, 1);

        ClassAnalyzer.collectHitCounters(false);

        List<Line> covered = ClassAnalyzer.getCoverableLines
                (ExampleClass.class.getName());


        List<Integer> coveredLines = Arrays.asList(new Integer[]{
                7, 9, 12, 13
        });

        int numLinesCovered = 0;

        for (Line l : covered) {
            if (l.getHits() > 0) {
                assertTrue("Line " + l.getLineNumber() + " should be covered",
                        coveredLines.contains(l.getLineNumber()));
                numLinesCovered++;
            } else {
                assertFalse("Line " + l.getLineNumber() + " should be " +
                                "covered",
                        coveredLines.contains(l.getLineNumber()));
            }
        }

        assertEquals(coveredLines.size(), numLinesCovered);

        assertTrue("There should be lines of code present", covered.size() > 0);
    }


    @Test
    public void linesCoveredFalseBranchHit()
            throws NoSuchMethodException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        Object o = ic.newInstance();

        Method m = ic.getDeclaredMethod("abs", new Class[]{int.class});

        m.setAccessible(true);
        Object r = m.invoke(o, -1);

        ClassAnalyzer.collectHitCounters(false);

        List<Line> covered = ClassAnalyzer.getCoverableLines
                (ExampleClass.class.getName());


        List<Integer> coveredLines = Arrays.asList(new Integer[]{
                7, 9, 12, 15
        });

        int numLinesCovered = 0;

        for (Line l : covered) {
            if (l.getHits() > 0) {
                assertTrue("Line " + l.getLineNumber() + " should be covered",
                        coveredLines.contains(l.getLineNumber()));
                numLinesCovered++;
            } else {
                assertFalse("Line " + l.getLineNumber() + " should be " +
                                "covered",
                        coveredLines.contains(l.getLineNumber()));
            }
        }

        assertEquals(coveredLines.size(), numLinesCovered);

        assertTrue("There should be lines of code present", covered.size() > 0);
    }

    @Test
    public void linesCoveredBothBrances()
            throws NoSuchMethodException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        Object o = ic.newInstance();

        Method m = ic.getDeclaredMethod("abs", new Class[]{int.class});

        m.setAccessible(true);
        Object r = m.invoke(o, 1);
        r = m.invoke(o, -1);

        ClassAnalyzer.collectHitCounters(false);

        List<Line> covered = ClassAnalyzer.getCoverableLines
                (ExampleClass.class.getName());


        List<Integer> coveredLines = Arrays.asList(new Integer[]{
                7, 9, 12, 13, 15
        });

        int numLinesCovered = 0;

        for (Line l : covered) {
            if (l.getHits() > 0) {
                assertTrue("Line " + l.getLineNumber() + " should be covered",
                        coveredLines.contains(l.getLineNumber()));
                numLinesCovered++;
            } else {
                assertFalse("Line " + l.getLineNumber() + " should be " +
                                "covered",
                        coveredLines.contains(l.getLineNumber()));
            }
        }

        assertEquals(coveredLines.size(), numLinesCovered);

        assertTrue("There should be lines of code present", covered.size() > 0);
    }

    @Test
    public void branchCoveredTrue()
            throws NoSuchMethodException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        Object o = ic.newInstance();

        Method m = ic.getDeclaredMethod("abs", new Class[]{int.class});

        m.setAccessible(true);
        Object r = m.invoke(o, -1);

        ClassAnalyzer.collectHitCounters(false);

        List<Branch> covered = ClassAnalyzer.getCoverableBranches
                (ExampleClass.class.getName());

        int hits = 0;

        for (Branch b : covered) {
            if (b.getLineNumber() == 12) {
                hits += b.getHits();
            }
        }

        assertEquals(1, hits);
    }


    @Test
    public void linesCoveredBothBranchesDelayed()
            throws NoSuchMethodException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        Object o = ic.newInstance();

        Method m = ic.getDeclaredMethod("abs", new Class[]{int.class});

        m.setAccessible(true);
        Object r = m.invoke(o, 1);

        ClassAnalyzer.collectHitCounters(false);

        int numLinesCovered = 0;

        List<Line> covered = ClassAnalyzer.getCoverableLines
                (ExampleClass.class.getName());

        List<Integer> coveredLines = Arrays.asList(new Integer[]{
                7, 9, 12, 13
        });

        for (Line l : covered) {
            if (l.getHits() > 0) {
                assertTrue("Line " + l.getLineNumber() + " should be covered",
                        coveredLines.contains(l.getLineNumber()));

                assertEquals("Line should only be hit once", 1, l.getHits());

                numLinesCovered++;
            } else {
                assertFalse("Line " + l.getLineNumber() + " should not be " +
                                "covered",
                        coveredLines.contains(l.getLineNumber()));
            }
        }

        r = m.invoke(o, -1);

        ClassAnalyzer.collectHitCounters(false);


        covered = ClassAnalyzer.getCoverableLines
                (ExampleClass.class.getName());


        coveredLines = Arrays.asList(new Integer[]{
                7, 9, 12, 13, 15
        });

        numLinesCovered = 0;

        for (Line l : covered) {
            if (l.getHits() > 0) {
                assertTrue("Line " + l.getLineNumber() + " should be covered",
                        coveredLines.contains(l.getLineNumber()));
                numLinesCovered++;
            } else {
                assertFalse("Line " + l.getLineNumber() + " should be " +
                                "covered",
                        coveredLines.contains(l.getLineNumber()));
            }
        }

        assertEquals(coveredLines.size(), numLinesCovered);

        assertTrue("There should be lines of code present", covered.size() > 0);
    }


}
