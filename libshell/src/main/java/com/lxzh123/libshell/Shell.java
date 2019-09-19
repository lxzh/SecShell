package com.lxzh123.libshell;

import android.content.Context;
import android.util.Log;

public class Shell {
    private final static String TAG = Shell.class.getSimpleName();

    private static volatile Shell instance;

    public static Shell get() {
        if (instance == null) {
            synchronized (Shell.class) {
                if (instance == null) {
                    instance = new Shell();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        try {
            Helper.loadCore(context);
        } catch (Exception ex) {
            Log.e(TAG, "loadCore Exception ex=" + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
