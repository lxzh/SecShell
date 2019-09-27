package com.lxzh123.libcore;

public class OutSingleton {
    private static final OutSingleton ourInstance = new OutSingleton();

    public static OutSingleton getInstance() {
        return ourInstance;
    }

    private OutSingleton() {
    }
}
