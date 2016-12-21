package com.sheffield.instrumenter.analysis;

import com.sheffield.instrumenter.instrumentation.ClassReplacementTransformer;

import java.util.ArrayList;

/**
 * Created by thomas on 03/05/2016.
 */
public class ClassNode {
    private String className;
    private ArrayList<ClassNode> children;

    private ArrayList<ClassNode> parent;


    public ClassNode(String className) {
        className = DependencyTree.convertString(className);
        children = new ArrayList<ClassNode>();
        this.className = className;
        parent = new ArrayList<ClassNode>();
    }

    public void addParent(ClassNode cn){
        parent.add(cn);
    }

    public ArrayList<ClassNode> getParent(){
        return parent;
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

        ClassNode cn = DependencyTree.classNodeCache(className);

        if (cn != null){
            return cn;
        }

        className = DependencyTree.convertString(className);
        ArrayList<String> seen = new ArrayList<String>(children.size());
        return findClassNode(className, seen);
    }

    private ClassNode findClassNode(String className, ArrayList<String> seen) {
        for (int i = 0; i < children.size(); i++) {
            ClassNode cn = children.get(i);
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

    public ArrayList<ClassNode> getDependencies(){
        ArrayList<ClassNode> deps = new ArrayList<ClassNode>();
//        for (ClassNode cn : parent){
//            deps.addAll(getDependencies(cn, deps));
//        }

        getDependencies(this, deps);

        return deps;
    }

    private ArrayList<ClassNode> getDependencies(ClassNode parent, ArrayList<ClassNode> dependencies){
        if (dependencies.contains(parent) ||
                ClassReplacementTransformer.isForbiddenPackage(parent.getClassName())){
            return dependencies;
        }

        dependencies.add(parent);

        for (ClassNode cn : parent.getParent()) {
            getDependencies(cn, dependencies);
        }
        return dependencies;
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

    public String toString(ArrayList<String> seen) {
        String s = "";//[" + className + "]";
        if (seen.contains(className)) {
            return "";
        } else {
            s += className + "\n";
        }
        seen.add(className);
        for (ClassNode cn : getDependencies()) {
            if (seen.contains(cn.getClassName())) {
                continue;
            }
            s += cn.getClassName() + "\n";
            s += cn.toString(seen);

//            for (ClassNode cln : cn.getDependencies()){
//                s += cln.toString(seen);
//            }
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
            for (ClassNode cln : cn.getDependencies()){
                s += cln.toNomnoml(seen);
            }
            //seen.remove(cn.getClassName());
        }
        return s;
    }
}