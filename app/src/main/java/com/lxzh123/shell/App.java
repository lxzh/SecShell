package com.lxzh123.shell;

import android.app.Application;
import android.content.Context;
import android.util.Log;

//import com.lxzh123.dexloader.DexLoader;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import com.lxzh123.injector.Loader;
//import com.lxzh123.libshell.Shell;

public class App extends Application {

    private final static String TAG = App.class.getSimpleName();

    static {
        System.loadLibrary("printlib");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
//            printDexFile(base);
//            Shell.get().init(base);
//            DexLoader.init(base);
            Loader.init(base);
        } catch (Exception ex) {
            Log.e(TAG, "attachBaseContext Exception ex=" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void printDexFile(Context context) {
        try {
            ClassLoader parent = context.getClassLoader();
            Log.d(TAG, "BaseDexClassLoader classloader:" + parent);
            Class<?> DexPathListClass = Class.forName("dalvik.system.DexPathList");

            Class<?> PathClassLoaderClass = parent.getClass();
            Log.d(TAG, "BaseDexClassLoader PathClassLoaderClass:" + PathClassLoaderClass.toString());
            Class<?> OriginBaseClassLoaderClass = parent.getClass().getSuperclass();
            Log.d(TAG, "BaseDexClassLoader OriginBaseClassLoaderClass:" + OriginBaseClassLoaderClass.toString());
            Field pathListField = OriginBaseClassLoaderClass.getDeclaredField("pathList");

            pathListField.setAccessible(true);
            Object oldPathList = pathListField.get(parent);
            Log.d(TAG, "BaseDexClassLoader oldPathList:" + oldPathList.toString());

            Field dexElementsField = DexPathListClass.getDeclaredField("dexElements");
            Log.d(TAG, "BaseDexClassLoader dexElementsField:" + dexElementsField.toString());
            dexElementsField.setAccessible(true);
            Object oldDexElements = dexElementsField.get(oldPathList);
            Log.d(TAG, "BaseDexClassLoader oldDexElements:" + oldDexElements.toString());

            Object dexElementOne = ((Object[])oldDexElements)[0];
            printObject(dexElementOne);
            Log.d(TAG, "dexElementOne addr:"+System.identityHashCode(dexElementOne));
            Class<?> ElementClass = dexElementOne.getClass();
            Field dexFileField = ElementClass.getDeclaredField("dexFile");
            dexFileField.setAccessible(true);
            Class<?> DexFileClass = Class.forName("dalvik.system.DexFile");

            Object dexFile = dexFileField.get(dexElementOne);
            printObject(dexFile);
            Log.d(TAG, "dexFile addr:"+System.identityHashCode(dexFile));
            Field mCookieField = DexFileClass.getDeclaredField("mCookie");
            mCookieField.setAccessible(true);
            Object mCookie = mCookieField.get(dexFile);
            printObject(mCookie);
            Log.d(TAG, "mCookie addr:"+System.identityHashCode(mCookie));

//            Class<?> ElementClass = Class.forName("dalvik.system.DexPathList$Element");
            Class<?> ElementArrayClass = oldDexElements.getClass();
            Log.d(TAG, "BaseDexClassLoader ElementArrayClass:" + ElementArrayClass.toString());

            int elementLen = Array.getLength(oldDexElements);
            Object newDexElements = Array.newInstance(ElementClass, elementLen + 1);
            for (int k = 0; k < elementLen; ++k) {
                Array.set(newDexElements, k, Array.get(oldDexElements, k));
            }

//            Object[] null_elements = null;
//            Class<?> DexFileClass = Class.forName("dalvik.system.DexFile");
//            Log.d(TAG, "BaseDexClassLoader DexFileClass:" + DexFileClass.toString());
//            for (Constructor constructor : DexFileClass.getDeclaredConstructors()) {
//                Log.d(TAG, "Constructor : " + constructor.toString());
//            }
//            for (Method method : DexFileClass.getDeclaredMethods()) {
//                Log.d(TAG, "method : " + method.toString());
//            }
//            Constructor<?> DexFileConstructor = DexFileClass.getConstructor(ByteBuffer[].class, ClassLoader.class, ElementArrayClass);
//            Log.d(TAG, "BaseDexClassLoader DexFileConstructor:" + DexFileConstructor.toString());

//            DexFile dex = new DexFile(dexFiles, this, null_elements);
//            // Capture class loader context from *before* `dexElements` is set (see comment below).
//            String classLoaderContext = dex.isBackedByOatFile()
//                    ? null : DexFile.getClassLoaderContext(definingContext, null_elements);
//            dexElements = new Element[] { new Element(dex) };

//            Method initByteBufferDexPathMethod = DexPathListClass.getMethod("initByteBufferDexPath", ByteBuffer[].class);
//            initByteBufferDexPathMethod.invoke(pathList, dexFiles);
//            Method findClassMethod = DexPathListClass.getMethod("findClass", String.class, List.class);
//            Method findResourceMethod = DexPathListClass.getMethod("findResource", String.class);
//            Method findResourcesMethod = DexPathListClass.getMethod("findResources", String.class);
//            Method findLibraryMethod = DexPathListClass.getMethod("findLibrary", String.class);
        } catch (Exception ex) {
            Log.e(TAG, "BaseDexClassLoader Exception:" + ex.toString());
            ex.printStackTrace();
        }
    }

    public static native void printObject(Object object);
}
