package com.lxzh123.dexloader;

public class Jni {
    public static native void printNativeObject(Object object);
    public static native void printNativeClass(Class object);
    public static native void printNativeMethod(java.lang.reflect.Method object);
    public static native void method(java.io.File dir, boolean isDirectory, java.io.File zip);

}
