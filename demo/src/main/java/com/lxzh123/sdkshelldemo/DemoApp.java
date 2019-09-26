package com.lxzh123.sdkshelldemo;

import android.app.Application;

import com.lxzh123.injector.Loader;

//import com.lxzh123.libshell.Shell;

public class DemoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try{
//            Shell.get().init(this);
            Loader.init(this);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
