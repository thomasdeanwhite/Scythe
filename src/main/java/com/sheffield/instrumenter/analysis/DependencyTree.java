package com.sheffield.instrumenter.analysis;


import com.sheffield.util.ClassNameUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class DependencyTree {

    private HashMap<String, HashMap<String, ClassNode>> nodes = new HashMap<String, HashMap<String, ClassNode>>();

    public static String getClassMethodId(String className, String methodName){
        return className + "::" + methodName + "";
    }

    public static String getClassName(String classMethodId){
        if (classMethodId == null){
            return null;
        }
        return classMethodId.split("::")[0];
    }

    public static String getMethodName(String classMethodId){
        if (classMethodId == null || !classMethodId.contains("::")){
            return null;
        }
        return classMethodId.split("::")[1];
    }

    private static DependencyTree depTree;

    public static DependencyTree getDependencyTree() {
        if (depTree == null) {
            depTree = new DependencyTree();
        }
        return depTree;
    }

    private ClassNode root;

    private DependencyTree() {
        root = new ClassNode("root");
    }

    public ClassNode getClassNode(String className) {
        return root.findClassNode(ClassNameUtils.standardise(className));
    }

    public ArrayList<ClassNode> getPackageNodes(String packageName) {
        packageName = ClassNameUtils.standardise(packageName);
        return root.findPackages(packageName);
    }

    public void addDependency(String className, String childName) {
        className = convertString(className);
        childName = convertString(childName);
        ClassNode cn = null;

        String claName = getClassName(className);

        if (nodes.containsKey(claName))
            cn = nodes.get(claName).get(getMethodName(className));
        else
            nodes.put(claName, new HashMap<String, ClassNode>());

        if (cn == null)
            cn = root.findClassNode(className);

        if (cn == null) {
            cn = new ClassNode(className);
            root.addChild(cn);
            nodes.get(claName).put(getMethodName(className), cn);
        }

        String chiName = getClassName(childName);

        ClassNode child = null;

        if (nodes.containsKey(chiName))
            child = nodes.get(chiName).get(getMethodName(childName));
        else
            nodes.put(chiName, new HashMap<String, ClassNode>());



        if (child == null)
            root.findClassNode(childName);

        if (child == null) {
            child = new ClassNode(childName);
            nodes.get(chiName).put(getMethodName(childName), child);
        }

        child.addParent(cn);
        cn.addChild(child);


    }

    public ArrayList<ClassNode> getDependencies(String className) {
        className = ClassNameUtils.standardise(className);
        return root.findClassNode(className).getDependencies();
    }

    public void clear() {
        root.clear();
        nodes.clear();
    }

    public ClassNode getRoot() {
        return root;
    }

    public static String convertString(String s) {
        return ClassNameUtils.standardise(s);
    }


}
