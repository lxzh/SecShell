package com.lxzh123.shell;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class App extends Application {

    private final static String TAG = App.class.getSimpleName();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            Loader.init(base);
        } catch (Exception ex) {
            Log.e(TAG, "attachBaseContext Exception ex=" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "onTerminate");
    }
}
