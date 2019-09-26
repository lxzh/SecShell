package com.lxzh123.classloader;

import java.io.File;

import dalvik.system.BaseDexClassLoader;

public class BinaryClassLoader extends BaseDexClassLoader {
    public BinaryClassLoader(String dexPath, File optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }
}
