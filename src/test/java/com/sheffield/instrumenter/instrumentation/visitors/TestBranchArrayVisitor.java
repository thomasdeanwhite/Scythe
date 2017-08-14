package com.sheffield.instrumenter.instrumentation.visitors;

import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Branch;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Line;
import org.junit.Before;
import org.junit.Test;
import test.classes.ExampleClass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by thomas on 23/11/2016.
 */
public class TestBranchArrayVisitor {

    Class ic = null;

    @Before
    public void setup(){
        try {
            ClassAnalyzer.softReset();
            ic = ClassTester.getInstrumentedTestClass();
        } catch (ClassNotFoundException e) {

        }
    }

    @Test
    public void instrumentClassIsFound() throws ClassNotFoundException {
        ic = ClassTester.getInstrumentedTestClass();
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

        for (Branch b : covered){
            if (b.getLineNumber() == 12){
                trueHits += b.getTrueHits();
                falseHits += b.getFalseHits();
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

        for (Branch b : covered){
            if (b.getLineNumber() == 12){
                trueHits += b.getTrueHits();
                falseHits += b.getFalseHits();
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
        int falseHits = 0;

        for (Branch b : covered){
            if (b.getLineNumber() == 12){
                trueHits += b.getTrueHits();
                falseHits += b.getFalseHits();
            }
        }

        assertEquals(2, falseHits);
        assertEquals(0, trueHits);
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
        int falseHits = 0;

        for (Branch b : covered){
            if (b.getLineNumber() == 12){
                trueHits += b.getTrueHits();
                falseHits += b.getFalseHits();
            }
        }

        assertEquals(1, falseHits);
        assertEquals(1, trueHits);
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

        for (Line l : covered){
            if (l.getHits() > 0){
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

        for (Line l : covered){
            if (l.getHits() > 0){
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

        for (Line l : covered){
            if (l.getHits() > 0){
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

        int trueHits = 0;
        int falseHits = 0;

        for (Branch b : covered){
            if (b.getLineNumber() == 12){
                trueHits += b.getTrueHits();
                falseHits += b.getFalseHits();
            }
        }

        assertEquals(1, falseHits);
        assertEquals(0, trueHits);
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

        for (Line l : covered){
            if (l.getHits() > 0){
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

        for (Line l : covered){
            if (l.getHits() > 0){
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
