/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lxzh123.classloader;

import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Base class for common functionality between various dex-based
 * {@link ClassLoader} implementations.
 */
public class CustDexClassLoader extends ClassLoader {
    private final static String TAG = "CustDexClassLoader";

//    /**
//     * Hook for customizing how dex files loads are reported.
//     * <p>
//     * This enables the framework to monitor the use of dex files. The
//     * goal is to simplify the mechanism for optimizing foreign dex files and
//     * enable further optimizations of secondary dex files.
//     * <p>
//     * The reporting happens only when new instances of CustDexClassLoader
//     * are constructed and will be active only after this field is set with
//     * {@link CustDexClassLoader#setReporter}.
//     */
//    /* @NonNull */ private static volatile Object reporter = null;

    //    private final DexPathList pathList;
    private Object pathList;
    private Method findClassMethod;
    private Method findResourceMethod;
    private Method findResourcesMethod;
    private Method findLibraryMethod;
    private Class<?> ReporterClass;

    /**
     * Array of ClassLoaders that can be used to load classes and resources that the code in
     * {@code pathList} may depend on. This is used to implement Android's
     * <a href=https://developer.android.com/guide/topics/manifest/uses-library-element>
     * shared libraries</a> feature.
     * <p>The shared library loaders are always checked before the {@code pathList} when looking
     * up classes and resources.
     *
     * <p>{@code null} if the class loader has no shared library.
     *
     * @hide
     */
    protected final ClassLoader[] sharedLibraryLoaders;

    /**
     * Constructs an instance.
     * <p>
     * dexFile must be an in-memory representation of a full dexFile.
     *
     * @param dexFiles          the array of in-memory dex files containing classes.
     * @param librarySearchPath the list of directories containing native
     *                          libraries, delimited by {@code File.pathSeparator}; may be {@code null}
     * @param parent            the parent class loader
     * @hide
     */
    public CustDexClassLoader(ByteBuffer[] dexFiles, String librarySearchPath, ClassLoader parent) {
        super(parent);
        Log.d(TAG, "CustDexClassLoader");
        this.sharedLibraryLoaders = null;
        try {
            Log.d(TAG, "CustDexClassLoader classloader:" + parent);
            Class<?> DexPathListClass = Class.forName("dalvik.system.DexPathList");
//            ReporterClass = Class.forName("dalvik.system.DexPathList$Reporter");
            Constructor<?> constructor = DexPathListClass.getConstructor(ClassLoader.class, String.class);
            this.pathList = constructor.newInstance(this, librarySearchPath);
            Log.d(TAG, "CustDexClassLoader pathList:" + pathList.toString());

            Class<?> PathClassLoaderClass = parent.getClass();
            Log.d(TAG, "CustDexClassLoader PathClassLoaderClass:" + PathClassLoaderClass.toString());
            Class<?> OriginBaseClassLoaderClass = parent.getClass().getSuperclass();
            Log.d(TAG, "CustDexClassLoader OriginBaseClassLoaderClass:" + OriginBaseClassLoaderClass.toString());
            Field pathListField = OriginBaseClassLoaderClass.getDeclaredField("pathList");

            pathListField.setAccessible(true);
            Object oldPathList = pathListField.get(parent);
            Log.d(TAG, "CustDexClassLoader oldPathList:" + oldPathList.toString());

            Field dexElementsField = DexPathListClass.getDeclaredField("dexElements");
            Log.d(TAG, "CustDexClassLoader dexElementsField:" + dexElementsField.toString());
            dexElementsField.setAccessible(true);
            Object oldDexElements = dexElementsField.get(oldPathList);
            Log.d(TAG, "CustDexClassLoader oldDexElements:" + oldDexElements.toString());

            Class<?> ElementClass = Class.forName("dalvik.system.DexPathList$Element");
            Class<?> ElementArrayClass = oldDexElements.getClass();
            Log.d(TAG, "CustDexClassLoader ElementArrayClass:" + ElementArrayClass.toString());

            int elementLen = Array.getLength(oldDexElements);
            Object newDexElements = Array.newInstance(ElementClass, elementLen + 1);
            for (int k = 0; k < elementLen; ++k) {
                Array.set(newDexElements, k, Array.get(oldDexElements, k));
            }

//            Object[] null_elements = null;
//            Class<?> DexFileClass = Class.forName("dalvik.system.DexFile");
//            Log.d(TAG, "CustDexClassLoader DexFileClass:" + DexFileClass.toString());
//            for (Constructor constructor : DexFileClass.getDeclaredConstructors()) {
//                Log.d(TAG, "Constructor : " + constructor.toString());
//            }
//            for (Method method : DexFileClass.getDeclaredMethods()) {
//                Log.d(TAG, "method : " + method.toString());
//            }
//            Constructor<?> DexFileConstructor = DexFileClass.getConstructor(ByteBuffer[].class, ClassLoader.class, ElementArrayClass);
//            Log.d(TAG, "CustDexClassLoader DexFileConstructor:" + DexFileConstructor.toString());

//            DexFile dex = new DexFile(dexFiles, this, null_elements);
//            // Capture class loader context from *before* `dexElements` is set (see comment below).
//            String classLoaderContext = dex.isBackedByOatFile()
//                    ? null : DexFile.getClassLoaderContext(definingContext, null_elements);
//            dexElements = new Element[] { new Element(dex) };

            Method initByteBufferDexPathMethod = DexPathListClass.getMethod("initByteBufferDexPath", ByteBuffer[].class);
            initByteBufferDexPathMethod.invoke(pathList, dexFiles);
            findClassMethod = DexPathListClass.getMethod("findClass", String.class, List.class);
            findResourceMethod = DexPathListClass.getMethod("findResource", String.class);
            findResourcesMethod = DexPathListClass.getMethod("findResources", String.class);
            findLibraryMethod = DexPathListClass.getMethod("findLibrary", String.class);
        } catch (Exception ex) {
            Log.e(TAG, "CustDexClassLoader Exception:" + ex.toString());
            ex.printStackTrace();
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // First, check whether the class is present in our shared libraries.
        if (sharedLibraryLoaders != null) {
            for (ClassLoader loader : sharedLibraryLoaders) {
                try {
                    return loader.loadClass(name);
                } catch (ClassNotFoundException ignored) {
                }
            }
        }
        // Check whether the class in question is present in the dexPath that
        // this classloader operates on.
        List<Throwable> suppressedExceptions = new ArrayList<Throwable>();

        Class c = null;
        try {
            c = (Class) findClassMethod.invoke(pathList, name, suppressedExceptions);
        } catch (Exception ex) {

        }
//        Class c = pathList.findClass(name, suppressedExceptions);
        if (c == null) {
            ClassNotFoundException cnfe = new ClassNotFoundException(
                    "Didn't find class \"" + name + "\" on path: " + pathList);
            for (Throwable t : suppressedExceptions) {
                cnfe.addSuppressed(t);
            }
            throw cnfe;
        }
        return c;
    }

    @Override
    protected URL findResource(String name) {
        if (sharedLibraryLoaders != null) {
            for (ClassLoader loader : sharedLibraryLoaders) {
                URL url = loader.getResource(name);
                if (url != null) {
                    return url;
                }
            }
        }
        URL rst = null;
        try {
            rst = (URL) findResourceMethod.invoke(pathList, name);
        } catch (Exception ex) {

        }
//        return pathList.findResource(name);
        return rst;
    }

    @Override
    protected Enumeration<URL> findResources(String name) {
//        Enumeration<URL> myResources = pathList.findResources(name);
        Enumeration<URL> myResources = null;
        try {
            myResources = (Enumeration<URL>) findResourcesMethod.invoke(pathList, name);
        } catch (Exception ex) {

        }
        if (sharedLibraryLoaders == null) {
            return myResources;
        }

        Enumeration<URL>[] tmp =
                (Enumeration<URL>[]) new Enumeration<?>[sharedLibraryLoaders.length + 1];
        // This will add duplicate resources if a shared library is loaded twice, but that's ok
        // as we don't guarantee uniqueness.
        for (int i = 0; i < sharedLibraryLoaders.length; i++) {
            try {
                tmp[i] = sharedLibraryLoaders[i].getResources(name);
            } catch (IOException e) {
                // Ignore.
            }
        }
        tmp[sharedLibraryLoaders.length] = myResources;
        return new CompoundEnumeration<>(tmp);
    }

    @Override
    public String findLibrary(String name) {
        String library = null;
        try {
            library = (String) findLibraryMethod.invoke(pathList, name);
        } catch (Exception ex) {

        }
        return library;
//        return pathList.findLibrary(name);
    }

    /**
     * Returns package information for the given package.
     * Unfortunately, instances of this class don't really have this
     * information, and as a non-secure {@code ClassLoader}, it isn't
     * even required to, according to the spec. Yet, we want to
     * provide it, in order to make all those hopeful callers of
     * {@code myClass.getPackage().getName()} happy. Thus we construct
     * a {@code Package} object the first time it is being requested
     * and fill most of the fields with dummy values. The {@code
     * Package} object is then put into the {@code ClassLoader}'s
     * package cache, so we see the same one next time. We don't
     * create {@code Package} objects for {@code null} arguments or
     * for the default package.
     *
     * <p>There is a limited chance that we end up with multiple
     * {@code Package} objects representing the same package: It can
     * happen when when a package is scattered across different JAR
     * files which were loaded by different {@code ClassLoader}
     * instances. This is rather unlikely, and given that this whole
     * thing is more or less a workaround, probably not worth the
     * effort to address.
     *
     * @param name the name of the class
     * @return the package information for the class, or {@code null}
     * if there is no package information available for it
     */
    @Override
    protected synchronized Package getPackage(String name) {
        if (name != null && !name.isEmpty()) {
            Package pack = super.getPackage(name);

            if (pack == null) {
                pack = definePackage(name, "Unknown", "0.0", "Unknown",
                        "Unknown", "0.0", "Unknown", null);
            }

            return pack;
        }

        return null;
    }
//
//    public String getLdLibraryPath() {
//        StringBuilder result = new StringBuilder();
//        for (File directory : pathList.getNativeLibraryDirectories()) {
//            if (result.length() > 0) {
//                result.append(':');
//            }
//            result.append(directory);
//        }
//
//        return result.toString();
//    }

    @Override
    public String toString() {
        String sharedLibs = "";
        if (sharedLibraryLoaders != null) {
            for (Object obj : sharedLibraryLoaders) {
                sharedLibs += obj + ",";
            }
        }
        return getClass().getName() + "[" + pathList + "; parent=(" + getParent()
                + "), shared-libs=(" + sharedLibs + ")]";
    }
//
//    /**
//     * Sets the reporter for dex load notifications.
//     * Once set, all new instances of CustDexClassLoader will report upon
//     * constructions the loaded dex files.
//     *
//     * @param newReporter the new Reporter. Setting null will cancel reporting.
//     */
//    public static void setReporter(Object newReporter) {
//        reporter = newReporter;
//    }
//
//    public static Object getReporter() {
//        return reporter;
//    }
}
