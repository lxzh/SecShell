package com.lxzh123.libcore;

public class LIB {
    private static volatile LIB instance;

    public static LIB get() {
        if (instance == null) {
            synchronized (LIB.class) {
                if (instance == null) {
                    instance = new LIB();
                }
            }
        }
        return instance;
    }

    public int square(int x) {
        return InnerClass.innerMethod(x);
    }

}
