package com.lxzh123.dexloader;

import android.content.Context;
import android.os.Build;
import android.util.Log;

public class DexLoader {
    private final static String TAG = "DexLoader";

    public final static String TARGET_APK_NAME = "libcore.aar";
    public final static String TARGET_PKG_NAME = "com.lxzh123.libshell";

    static {
        System.loadLibrary("dexload");
    }

    public final static int SDK_VERSION = Build.VERSION.SDK_INT;

    public static void init(Context context) {
        printClassLoaderInfo(context);
        attachBaseContext(context);
        printClassLoaderInfo(context);
    }

    private static void printClassLoaderInfo(Context context) {
        Log.d(TAG, "printClassLoaderInfo");
        ClassLoader classLoader = context.getClassLoader();
        Log.d(TAG, "classLoader:"+classLoader);
        Class dexPathClassLoader = classLoader.getClass().getSuperclass();
        Log.d(TAG, "classLoader super:"+dexPathClassLoader);
    }

    public static native void attachBaseContext(Context context);

    public static void printNativeObject(Object object) {
        if (object != null) {
            Log.d(TAG, "object(" + object.getClass() + ")" + object.toString());
        }
    }
}