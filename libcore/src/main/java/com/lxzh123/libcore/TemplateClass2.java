package com.lxzh123.libcore;

import java.util.Comparator;

public class TemplateClass2<T extends Comparator, K extends Comparable> {
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
}
