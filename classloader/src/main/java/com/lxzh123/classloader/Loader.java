package com.lxzh123.classloader;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.ArrayMap;
import android.util.Log;

import com.lody.turbodex.TurboDex;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Arrays;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class Loader {

    private final static String TAG = "Loader";

    public final static String ASSETS_RES_NAME = "libcore.data";
    public final static String TARGET_DEX_NAME = "libcore.dex";

    public final static int SDK_VERSION = Build.VERSION.SDK_INT;

    static {
        try {
            System.loadLibrary("seclib");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void init(Context context) {
        printClassLoaderInfo(context);
        TurboDex.enableTurboDex();

        boolean initInJni = true;
        boolean injectInJni = true;

        String path = prepare(context, initInJni, injectInJni);
        if(!injectInJni) {
            injectDex(context, path);
        }
//
//
//        Object currentActivityThread = ActivityThreadCompat.instance();
//        String packageName = context.getPackageName();//当前apk的包名
//        ArrayMap mPackages = Reflect.on(currentActivityThread).field("mPackages").get();
//        WeakReference wr = (WeakReference) mPackages.get(packageName);
//
//        CustDexClassLoader loader = new CustDexClassLoader(new ByteBuffer[]{ByteBuffer.wrap(getApkData(context))},
//                null, context.getClassLoader());
//        Reflect.on(wr.get()).set("mClassLoader", loader);

        printClassLoaderInfo(context);
    }

    private static String prepare(Context context, boolean initInJni, boolean injectInJni) {
        String dexPath;
        if(initInJni) {
            dexPath = Helper.init(context, injectInJni);
        } else {
            File filesDir = context.getFilesDir();
            dexPath = copyAssetsData(context, ASSETS_RES_NAME, TARGET_DEX_NAME, filesDir.getAbsolutePath());
        }
        Log.d(TAG, "prepare dexPath:" + dexPath);
        return dexPath;
    }

    public static void injectDex(Context context, String dexPath) {
        File optDir = context.getDir("dex", 0);
        PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();//获取加载当前类的ClassLoader
        DexClassLoader dexClassLoader = new DexClassLoader(dexPath, optDir.getAbsolutePath(), dexPath, pathClassLoader);
        try {
            //获取当前classLoader(PathClassLoader)的dexElements,默认一个数组中都只有1个dexFile
            Object[] dexElements1 = (Object[]) ClassLoaderUtil.getDexElements(ClassLoaderUtil.getPathList(pathClassLoader));
            System.out.println("dexElements1=" + Arrays.toString(dexElements1) + ",len=" + dexElements1.length);
            //获取DexClassLoader中的dexElements
            Object[] dexElements2 = (Object[]) ClassLoaderUtil.getDexElements(ClassLoaderUtil.getPathList(dexClassLoader));
            System.out.println("dexElements2=" + Arrays.toString(dexElements2) + ",len=" + dexElements2.length);
            /**
             *  合并dexElements
             *  1、热修复：将代表外部dex的dexElement放在了第0个位置
             *  2、dex分包：直接将外部dex的dexElements放在同一个数组中即可。
             */
            Object dexElements = ClassLoaderUtil.combineArray(dexElements1, dexElements2);
            System.out.println("合并后的dexElements=" + Arrays.toString((Object[]) dexElements));
            Object pathList = ClassLoaderUtil.getPathList(pathClassLoader);
            //将原先的dexElements
            ClassLoaderUtil.setField(pathList, pathList.getClass(), "dexElements", dexElements);
            Log.d(TAG, "injectDex SUCCEED");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static String copyAssetsData(Context context, String srcName, String dstName, String path) {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;
        File outFile = new File(path, dstName);
        FileOutputStream fos = null;
        try {
            if (outFile.exists()) {
                outFile.delete();
            }
            inputStream = new BufferedInputStream(assetManager.open(srcName));
            fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        } catch (IOException iex) {
            Log.e(TAG, "copyAssetsData IOException iex=" + iex.getMessage());
            iex.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ex) {
                    Log.e(TAG, "close inputStream ex=" + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (Exception ex) {
                    Log.e(TAG, "close inputStream ex=" + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
        return outFile.getAbsolutePath();
    }

    private static byte[] getApkData(Context context) {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;
        ByteArrayOutputStream bos = null;
        byte[] data = null;
        try {
            inputStream = new BufferedInputStream(assetManager.open(ASSETS_RES_NAME));
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