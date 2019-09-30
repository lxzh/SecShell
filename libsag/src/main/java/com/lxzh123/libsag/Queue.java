package com.lxzh123.libsag;

import java.util.ArrayList;

/**
 * description 简易队列(非线程安全，仅供测试使用)
 * author      Created by lxzh
 * date        2019-09-03
 */
public class Queue<T> {
    private ArrayList<T> data;

    public Queue() {
        this.data = new ArrayList<>();
    }

    public void push(T item) {
        data.add(item);
    }

    public T pop() {
        if (data.size() > 0) {
            T item = data.get(0);
            data.remove(0);
            return item;
        }
        return null;
    }

    public T tryPop() {
        if (data.size() > 0) {
            return data.get(0);
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
