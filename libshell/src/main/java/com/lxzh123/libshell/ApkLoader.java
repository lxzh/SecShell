package com.lxzh123.libshell;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;

import com.lxzh123.libshell.reflect.RefInvoke;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

public class ApkLoader extends DexClassLoader {
    private final static String TAG = ApkLoader.class.getSimpleName();
    private Context mContext;
    private int mCookie;
    private String mDexFileName;

    public ApkLoader(Context context, String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
        this.mContext = context;
        this.mDexFileName = DexFile.class.getName();
        byte[] dexArrays = getApkData(context);
        ByteBuffer byteBuffer = ByteBuffer.wrap(dexArrays);
//        InMemoryDexClassLoader inMemoryDexClassLoader = new InMemoryDexClassLoader(byteBuffer, context.getClassLoader());
        this.mCookie = Helper.loadDex(dexArrays, (long)dexArrays.length);
//      this.mCookie = (Integer) RefInvoke.invokeStaticMethod(mDexFileName,
//                "openDexFile",
//                new Class[]{Object.class},
//                new Object[]{getApkData(context)});
        Log.d(TAG, "ApkLoader mCookie=" + mCookie);
    }

    private byte[] getApkData(Context context) {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;
        ByteArrayOutputStream bos = null;
        byte[] data = null;
        try {
            inputStream = new BufferedInputStream(assetManager.open(Helper.TARGET_APK_NAME));
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

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Log.d(TAG, "findClass name=" + name);
//        Class<?> clazz = null;
//        String[] as = getClassList(mCookie);
//        for (int i = 0; i < as.length; i++) {
//            Log.i(TAG, "findClass i=" + i + ",clzName=" + as[i]);
//            if (as[i].equals(name)) {
//                clazz = defineClass(as[i].replace('.', '/'),
//                        mContext.getClassLoader(),
//                        mCookie);
//            } else {
//                defineClass(as[i].replace('.', '/'),
//                        mContext.getClassLoader(),
//                        mCookie);
//            }
//        }
//        if (clazz == null) {
//            clazz = super.findClass(name);
//        }
//        return super.findClass(name);

        Class<?> cls = null;
        if (name.startsWith("com.lxzh123")) {
            String slashName = name.replace('.', '/');
            cls = defineClass(slashName, mContext.getClassLoader(), mCookie);
            if (null == cls) {
                cls = super.findClass(name);
            }
            return cls;
        } else {
            return super.findClass(name);
        }
    }

    private Class defineClass(String name, ClassLoader loader, int cookie) {
        if (Helper.SDK_VERSION <= Build.VERSION_CODES.KITKAT_WATCH) {
            return (Class) RefInvoke.invokeStaticMethod(mDexFileName,
                    "defineClassNative",
                    new Class[]{String.class, ClassLoader.class, int.class},
                    new Object[]{name, loader, cookie});
        } else if (Helper.SDK_VERSION <= Build.VERSION_CODES.LOLLIPOP) {
            return (Class) RefInvoke.invokeStaticMethod(mDexFileName,
                    "defineClassNative",
                    new Class[]{String.class, ClassLoader.class, long.class},
                    new Object[]{name, loader, (long)cookie});
        } else if(Helper.SDK_VERSION <= Build.VERSION_CODES.M) {
            return (Class) RefInvoke.invokeStaticMethod(mDexFileName,
                    "defineClassNative",
                    new Class[]{String.class, ClassLoader.class, long.class},
                    new Object[]{name, loader, (long)cookie});
        } else {
            return (Class) RefInvoke.invokeStaticMethod(mDexFileName,
                    "defineClassNative",
                    new Class[]{String.class, ClassLoader.class, int.class},
                    new Object[]{name, loader, cookie});
        }
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Log.d(TAG, "loadClass name=" + name + ", resolve=" + resolve);
        Class<?> clazz = super.loadClass(name, resolve);
        if (clazz == null) {
            Log.e(TAG, "loadClass fail, name=" + name);
        }
        return clazz;
    }
}
