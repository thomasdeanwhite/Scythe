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

    public class ClassNode {
        private String className;
        private ArrayList<ClassNode> children;

        public ClassNode(String className) {
            className = convertString(className);
            children = new ArrayList<ClassNode>();
            this.className = className;
        }

        public void addChild(ClassNode child) {
            if (!children.contains(child)) {
                children.add(child);
            }
        }

        public String getClassName() {
            return className;
        }

        public ClassNode findClassNode(String className) {
            className = convertString(className);
            ArrayList<String> seen = new ArrayList<String>(children.size());
            return findClassNode(className, seen);
        }

        private ClassNode findClassNode(String className, ArrayList<String> seen) {
            for (ClassNode cn : children) {
                if (seen.contains(cn.getClassName())) {
                    continue;
                }
                seen.add(cn.getClassName());
                if (cn.getClassName().equals(className)) {
                    return cn;
                } else {
                    ClassNode result = cn.findClassNode(className, seen);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return null;

        }

        public ArrayList<ClassNode> findPackages(String packageName) {
            ArrayList<String> seen = new ArrayList<String>(children.size());

            return findPackages(packageName, seen);
        }

        private ArrayList<ClassNode> findPackages(String packageName, ArrayList<String> seen) {
            ArrayList<ClassNode> results = new ArrayList<ClassNode>();
            for (ClassNode cn : children) {
                if (seen.contains(cn.getClassName())) {
                    continue;
                }
                seen.add(cn.getClassName());
                if (cn.getClassName().startsWith(packageName)) {
                    results.add(cn);
                }
                results.addAll(cn.findPackages(packageName, seen));
            }
            return results;

        }

        public ArrayList<ClassNode> getChildren() {
            return new ArrayList<ClassNode>(children);
        }

        public void clear() {
            ArrayList<ClassNode> childrenBackup = new ArrayList<ClassNode>(children);
            children.clear();
            for (ClassNode cn : childrenBackup) {
                cn.clear();
            }
        }

        @Override
        public String toString() {
            ArrayList<String> seen = new ArrayList<String>(children.size());
            return toString(seen);

        }

        public String toNomnoml() {
            ArrayList<String> seen = new ArrayList<String>(children.size());
            return toNomnoml(seen);

        }

        private String toString(ArrayList<String> seen) {
            String s = "";//[" + className + "]";
            if (seen.contains(className)) {
                return "";
            } else {
                s += className + "\n";
            }
            seen.add(className);
            for (ClassNode cn : children) {
                if (seen.contains(cn.getClassName())) {
                    continue;
                }
                s += cn.getClassName() + "\n";
                s += cn.toString(seen);
                //seen.remove(cn.getClassName());
            }
            return s;
        }

        private String toNomnoml(ArrayList<String> seen) {
            String s = "";//[" + className + "]";
            if (seen.contains(className)) {
                return "";
            }
            seen.add(className);
            for (ClassNode cn : children) {
                if (seen.contains(cn.getClassName())) {
                    continue;
                }
                s += "\n[" + className + "]<--[" + cn.getClassName() + "]\n";
                s += cn.toNomnoml(seen);
                //seen.remove(cn.getClassName());
            }
            return s;
        }
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

    private static String convertString(String s) {
        return s.replace("/", ".");
    }


}
