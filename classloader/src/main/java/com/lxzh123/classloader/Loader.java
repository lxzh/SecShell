package com.lxzh123.classloader;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.ArrayMap;
import android.util.Log;

import com.lxzh123.classloader.reflect.Reflect;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public class Loader {
    private final static String TAG = "Loader";

    public final static String TARGET_APK_NAME = "libcore.data";

    public final static int SDK_VERSION = Build.VERSION.SDK_INT;

    public static void init(Context context) {
        printClassLoaderInfo(context);

        Object currentActivityThread = ActivityThreadCompat.instance();
        String packageName = context.getPackageName();//当前apk的包名
        ArrayMap mPackages = Reflect.on(currentActivityThread).field("mPackages").get();
        WeakReference wr = (WeakReference) mPackages.get(packageName);


        BaseDexClassLoader loader = new BaseDexClassLoader(new ByteBuffer[]{ByteBuffer.wrap(getApkData(context))},
                null, context.getClassLoader());
        Reflect.on(wr.get()).set("mClassLoader", loader);

        printClassLoaderInfo(context);
    }


    private static byte[] getApkData(Context context) {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;
        ByteArrayOutputStream bos = null;
        byte[] data = null;
        try {
            inputStream = new BufferedInputStream(assetManager.open(TARGET_APK_NAME));
            bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
            bos.flush();
            data = bos.toByteArray();
        } catch (IOException iex) {
            Log.e(TAG, "getApkData IOException iex=" + iex.getMessage());
            iex.printStackTrace();
            data = new byte[0];
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ex) {
                    Log.e(TAG, "close inputStream ex=" + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
        return data;
    }

    private static void printClassLoaderInfo(Context context) {
        Log.d(TAG, "printClassLoaderInfo");
        ClassLoader classLoader = context.getClassLoader();
        Log.d(TAG, "classLoader:" + classLoader);
        Class dexPathClassLoader = classLoader.getClass().getSuperclass();
        Log.d(TAG, "classLoader super:" + dexPathClassLoader);
    }
}