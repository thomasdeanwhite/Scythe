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

}
