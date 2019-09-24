package com.lxzh123.dexloader;

import android.content.Context;
import android.os.Build;

public class DexLoader {
    private final static String TAG = "DexLoader";

    public final static String TARGET_APK_NAME = "libcore.aar";
    public final static String TARGET_PKG_NAME = "com.lxzh123.libshell";

    static {
        System.loadLibrary("dexload");
    }

    public final static int SDK_VERSION = Build.VERSION.SDK_INT;

    public static native void attachBaseContext(Context context);
}