package com.lxzh123.mkstubs;

import java.io.IOException;

public class MKStubs {
    private volatile static MKStubs instance;

    public static MKStubs get() {
        if (instance == null) {
            synchronized (MKStubs.class) {
                if (instance == null) {
                    instance = new MKStubs();
                }
            }
        }
        return instance;
    }

    private MKStubs() {
    }

    public void make(Params params) throws IOException {
        Main main = new Main();
        main.process(params);
    }
}
