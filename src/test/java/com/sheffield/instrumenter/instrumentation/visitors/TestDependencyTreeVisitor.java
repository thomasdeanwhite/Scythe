package com.sheffield.instrumenter.instrumentation.visitors;

import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.analysis.ClassNode;
import com.sheffield.instrumenter.analysis.DependencyTree;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Branch;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Line;
import com.sheffield.util.ClassNameUtils;
import com.sheffield.util.ClassUtils;
import org.junit.Before;
import org.junit.Test;
import test.classes.ExampleClass;
import test.classes.SubExampleClass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by thomas on 23/11/2016.
 */
public class TestDependencyTreeVisitor {

    Class ic = null;

    @Before
    public void setup(){
        try {
            ClassAnalyzer.softReset();
            ic = ClassTester.getSubInstrumentedTestClass();
        } catch (ClassNotFoundException e) {

        }
    }

    @Test
    public void instrumentClassIsFound() throws ClassNotFoundException {
        ic = ClassTester.getSubInstrumentedTestClass();
    }

    @Test
    public void checkChild () {

        ClassNode icNode = DependencyTree.getDependencyTree().getClassNode(
                SubExampleClass.class.getCanonicalName() + "::<init>"
        );

        ClassNode icNodeAbs = DependencyTree.getDependencyTree().getClassNode(
                SubExampleClass.class.getCanonicalName() + "::abs"
        );

        assertTrue("abs should be a dependency of <init>",
                icNode.getParent().contains(icNodeAbs));
    }

    @Test
    public void checkGrandChild () {

        ClassNode icNode = DependencyTree.getDependencyTree().getClassNode(
                SubExampleClass.class.getCanonicalName() + "::<init>"
        );

        ClassNode icNodeAbs = null;

        String childName = ClassNameUtils.standardise(
                SubExampleClass.class.getCanonicalName() + "::abs"
        );

        for (ClassNode cn : icNode.getParent()){
            if (cn.getClassName().equals(childName)){
                icNodeAbs = cn;

                break;
            }
        }

        ClassNode icNodeParentAbs = DependencyTree.getDependencyTree()
                .getClassNode(
                        ClassNameUtils.standardise(ExampleClass.class.getCanonicalName()
                                + "::abs")
        );

        assertTrue("super::abs should be a dependency of <init>",
                icNodeAbs.getParent().contains(icNodeParentAbs));
    }




}
