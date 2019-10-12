package com.lxzh123.libsag;

/**
 * description Logger interface
 * author      Created by lxzh
 * date        2019-09-28
 */
public interface ILogger {
    static final int VERBOSE = 2;
    static final int DEBUG = 3;
    static final int INFO = 4;
    static final int WARN = 5;
    static final int ERROR = 6;
    static final int ASSERT = 7;

    ILogger setLevel(int level);

    void v(String TAG, String msg);

    void d(String TAG, String msg);

    void i(String TAG, String msg);

    void w(String TAG, String msg);

    void e(String TAG, String msg);

    void a(String TAG, String msg);
}
