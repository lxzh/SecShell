package com.lxzh123.libshell;


import com.lxzh123.libshell.reflect.Reflect;
import com.lxzh123.libshell.reflect.ReflectException;

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
