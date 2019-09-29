package com.lxzh123.sag;

import android.util.Log;

import com.lxzh123.libsag.ILogger;

/**
 * description Logger$
 * author      Created by lxzh
 * date        2019-09-28
 */
class Logger implements ILogger {

    private int level = VERBOSE;
    private static volatile Logger instance;

    public static Logger get() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public void v(String TAG, String msg) {
        if (level <= VERBOSE) {
            Log.v(TAG, msg);
        }
    }

    @Override
    public void d(String TAG, String msg) {
        if (level <= DEBUG) {
            Log.d(TAG, msg);
        }
    }

    @Override
    public void i(String TAG, String msg) {
        if (level <= INFO) {
            Log.i(TAG, msg);
        }
    }

    @Override
    public void w(String TAG, String msg) {
        if (level <= WARN) {
            Log.v(TAG, msg);
        }
    }

    @Override
    public void e(String TAG, String msg) {
        if (level <= ERROR) {
            Log.e(TAG, msg);
        }
    }

    @Override
    public void a(String TAG, String msg) {
        if (level <= ASSERT) {
            Log.e(TAG, msg);
        }
    }
}
