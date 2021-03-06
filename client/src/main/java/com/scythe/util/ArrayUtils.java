package com.scythe.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by david on 07/04/16.
 */
public class ArrayUtils {
  public static <T> T[] combineArrays(Class<T> type, T[] first, T[] second) {
    return combineArrays(type, false, first, second);
  }

  public static <T> T[] combineArrays(Class<T> type, boolean unique, T[]... arrays) {
    int size = 0;
    for (T[] arr : arrays) {
      size += arr.length;
    }
    T[] result = (T[]) Array.newInstance(type, size);
    int current = 0;
    for (int i = 0; i < arrays.length; i++) {
      T[] arr = arrays[i];
      for (T el : arr) {
        result[current] = el;
        current++;
      }
    }
    if (unique) {

      Set<T> set = new HashSet<T>();
      set.addAll(Arrays.asList(result));
      result = set.toArray((T[]) Array.newInstance(type, set.size()));
    }
    return result;
  }

  public static <T> List<T> createList(T... objects) {
    List<T> list = new ArrayList<T>();
    for (T object : objects) {
      list.add(object);
    }
    return list;
  }

}
