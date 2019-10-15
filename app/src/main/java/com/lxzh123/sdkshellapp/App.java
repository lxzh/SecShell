package com.lxzh123.sdkshellapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.lxzh123.libshell.Core;

import java.lang.reflect.Method;

public class App extends Application {

    private final static String TAG = App.class.getSimpleName();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.d(TAG, "attachBaseContext");
        try {
            Core.init(base);
        } catch (Exception ex) {
            Log.e(TAG, "attachBaseContext Exception ex=" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Class clz = null;
        Log.d(TAG, "onCreate");
        while (clz == null) {
            try {
                clz = Class.forName("com.lxzh123.libcore.LIB");
                if (clz != null) {
                    Log.d(TAG, "App.onCreate Class.forName success");
                    Method getMethod = clz.getDeclaredMethod("get");
                    Method squareMethod = clz.getDeclaredMethod("square", int.class);
                    Object LIBObj = getMethod.invoke(clz, null);
                    Object square = squareMethod.invoke(LIBObj, 5);
                    Log.d(TAG, "call LIBObj.square:" + square);
                } else {
                    Log.d(TAG, "App.onCreate Class.forName failed");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                Thread.sleep(200);
            } catch (Exception ex) {
                Log.d(TAG, "Class.forName sleep:200ms");
            }
        }

        int square = CoreStub.init(5);
        Log.d(TAG, "call lib.square:" + square);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "onTerminate");
    }
}
