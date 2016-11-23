package com.sheffield.instrumenter.analysis;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestDependencyTree {

	@Test(timeout = 10000)
	public void testChild (){
		DependencyTree dt = DependencyTree.getDependencyTree();
		dt.clear();
		dt.addDependency("foo/Bar", "bar/Foo");

		ClassNode cn = dt.getClassNode("bar/Foo");

		assertNotNull(cn);
		assertEquals("bar/Foo", cn.getClassName());

	}

	@Test(timeout = 10000)
	public void testGrandChild (){
		DependencyTree dt = DependencyTree.getDependencyTree();
		dt.clear();
		dt.addDependency("foo/Bar", "step");

		dt.addDependency("step", "bar/Foo");

		ClassNode cn = dt.getClassNode("bar/Foo");

		assertNotNull(cn);
		assertEquals("bar/Foo", cn.getClassName());
	}

	@Test(timeout = 10000)
	public void testRecursion (){
		DependencyTree dt = DependencyTree.getDependencyTree();
		dt.clear();
		dt.addDependency("foo/Bar", "step");

		dt.addDependency("step", "foo/Bar");

		ClassNode cn = dt.getClassNode("invalid");

		assertNull(cn);
	}

	@Test(timeout = 10000)
	public void testSecondChild (){
		DependencyTree dt = DependencyTree.getDependencyTree();
		dt.clear();
		dt.addDependency("foo/Bar", "step");


		dt.addDependency("foo/Bar", "bar/Foo");

		ClassNode cn = dt.getClassNode("bar/Foo");

		assertNotNull(cn);
		assertEquals("bar/Foo", cn.getClassName());
	}

	@Test//(timeout = 10000)
	public void testDoubleRecursion (){
		DependencyTree dt = DependencyTree.getDependencyTree();
		dt.clear();
		dt.addDependency("foo/Bar", "step");
		dt.addDependency("step", "step2");
		dt.addDependency("step2", "foo/Bar");
		dt.addDependency("foo/Bar", "step2");
		dt.addDependency("foo/Bar", "bar/Foo");
		ClassNode cn = dt.getClassNode("bar/Foo");

		assertNotNull(cn);
		assertEquals("bar/Foo", cn.getClassName());
	}
	
}
