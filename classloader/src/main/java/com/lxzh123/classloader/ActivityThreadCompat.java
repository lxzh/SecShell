package com.lxzh123.classloader;


import com.lxzh123.classloader.reflect.Reflect;
import com.lxzh123.classloader.reflect.ReflectException;

public class ActivityThreadCompat {

    private static Object sActivityThread;

    private static Class sClass;

    public synchronized static final Object instance() throws ReflectException {
        if (sActivityThread == null) {
            sActivityThread = Reflect.on("android.app.ActivityThread").call("currentActivityThread").get();
        }
        return sActivityThread;
    }
}
