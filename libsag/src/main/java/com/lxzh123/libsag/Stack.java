package com.lxzh123.libsag;

import java.util.ArrayList;

/**
 * description 简易栈(非线程安全，仅供测试使用)
 * author      Created by lxzh
 * date        2019-09-03
 */
public class Stack<T> {
    private ArrayList<T> data;

    public Stack() {
        this.data = new ArrayList<>();
    }

    public void push(T item) {
        data.add(item);
    }

    public T pop() {
        int size = data.size();
        if (size > 0) {
            T item = data.get(size - 1);
            data.remove(size - 1);
            return item;
        }
        return null;
    }

    public T tryPop() {
        int size = data.size();
        if (size > 0) {
            return data.get(size - 1);
        }
        return null;
    }

    public int size() {
        return data.size();
    }

    public boolean isEmpty() {
        return data.size() == 0;
    }
}
