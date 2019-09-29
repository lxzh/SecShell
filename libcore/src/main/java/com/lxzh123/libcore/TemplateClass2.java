package com.lxzh123.libcore;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Lang.LangClass;

public class TemplateClass2<T extends Comparator, K extends Comparable> {
    public T value1;
    public K value2;
    public Object value3;
    public int[] value4;
    public String[] value5;
    public Set<Integer> value61;
    public Set<String> value62;
    public Set<int[]> value63;
    public Set<? extends OutClass> value7;
    public List<String> value8;
    public List<String[]> value9;
    public List<List<String[]>> value10;
    public Map<String, String> value11;
    public Map<String[], String> value12;
    public Map<int[], List<String>> value13;
    public Map<T, List<Map<Set<Map<String, int[]>>, List<Object>>>> value14;

    public TemplateClass2(T a, K b, Object obj) {
    }

    public T getValue1() {
        T a = null;
        return a;
    }

    public K getValue2() {
        K a = null;
        return a;
    }

    public K getValue2(T obj1, Object obj3) {
        K a = null;
        return a;
    }

    public LangClass getValue3(Object obj1, T obj2, K obj3, LangClass obj4, int[] obj5, String obj6) {
        return null;
    }
}
