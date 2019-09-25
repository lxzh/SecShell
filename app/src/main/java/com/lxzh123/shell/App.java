package com.lxzh123.shell;

import android.app.Application;
import android.content.Context;
import android.util.Log;

//import com.lxzh123.classloader.Loader;
import com.lxzh123.dexloader.DexLoader;
//import com.lxzh123.libshell.Shell;

public class App extends Application {

    private final static String TAG = App.class.getSimpleName();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
//            Shell.get().init(base);
            DexLoader.init(base);
//            Loader.init(base);
        } catch (Exception ex) {
            Log.e(TAG, "attachBaseContext Exception ex=" + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
