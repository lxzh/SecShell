package com.lxzh123.sdkshellapp;

import com.lxzh123.libcore.LIB;

/**
 * Compatible with android4.x version
 */
public class CoreStub {
    public static int init(int param) {
        return LIB.get().square(param);
    }
}
