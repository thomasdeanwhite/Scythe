package com.sheffield.util;

/**
 * Created by thomas on 30/08/2016.
 */
public class ClassNameUtils {

    public static String replaceDots(String className){
        return className.replace(".", "/");
    }

    public static String replaceSlashes(String className){
        return className.replace("/", ".");
    }

    public static String standardise(String className){
        return replaceDots(className).trim();
    }

    public static String getPackageName(String className){
        className = className.trim();
        if (className.contains("/")){
            return className.substring(0, className.lastIndexOf("/"));
        }
        if (className.toLowerCase().endsWith(".class") || className.toLowerCase().endsWith(".java"))
            className = className.substring(0, className.lastIndexOf("."));

        if (className.contains(".")) {
            return className.substring(0, className.lastIndexOf("."));
        }

        return "";

    }

}
