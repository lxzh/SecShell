package com.lxzh123.libshell;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.util.ArrayMap;

import com.lxzh123.libshell.reflect.Reflect;

import java.io.File;
import java.lang.ref.WeakReference;

public class Helper {
    private final static String TAG = "Helper";

    public final static String TARGET_APK_NAME = "libcore.data";
    public final static String TARGET_PKG_NAME = "com.lxzh123.libshell";

    static {
        System.loadLibrary("seclib");
    }

    public final static int SDK_VERSION = Build.VERSION.SDK_INT;

    private static String getSourceApkLibPath(Context context) {
        ApplicationInfo info = context.getApplicationInfo();
        return new File(info.dataDir, "lib").getPath();
    }

    public static void loadCore(Context context) {
        File sourceApk = context.getDir("source_dex", Context.MODE_PRIVATE);
        File odex = context.getDir("source_odex", Context.MODE_PRIVATE);
        File libs = context.getDir("source_lib", Context.MODE_PRIVATE);
        String odexPath = odex.getAbsolutePath();
        String libPath = libs.getAbsolutePath();
        String sourcePath = sourceApk.getAbsolutePath();

        Object currentActivityThread = ActivityThreadCompat.instance();
        String packageName = context.getPackageName();//当前apk的包名
        ArrayMap mPackages = Reflect.on(currentActivityThread).field("mPackages").get();
        WeakReference wr = (WeakReference) mPackages.get(packageName);

        ApkLoader dLoader = new ApkLoader(context,
                context.getPackageResourcePath(),
                odexPath,
                getSourceApkLibPath(context),
                ClassLoader.getSystemClassLoader());
        Reflect.on(wr.get()).set("mClassLoader", dLoader);
    }

    public static native int loadDex(byte[] dexBytes, long length);
}