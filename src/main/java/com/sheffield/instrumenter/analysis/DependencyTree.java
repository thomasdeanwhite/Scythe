package com.sheffield.instrumenter.analysis;



import java.util.ArrayList;

public class DependencyTree {

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
        return root.findClassNode(className);
    }

    public ArrayList<ClassNode> getPackageNodes(String packageName) {
        return root.findPackages(packageName);
    }

    public void addDependency(String className, String childName) {
        className = convertString(className);
        childName = convertString(childName);
        ClassNode cn = root.findClassNode(className);

        if (cn == null) {
            cn = new ClassNode(className);
            root.addChild(cn);
        }

        ClassNode child = root.findClassNode(childName);
        if (child == null) {
            child = new ClassNode(childName);
        }

        cn.addChild(child);


    }

    public void clear() {
        root.clear();
    }

    public ClassNode getRoot() {
        return root;
    }

    public static String convertString(String s) {
        return s.replace("/", ".");
    }


}
