package com.lxzh123.sdkshelldemo;

import android.app.Application;

import com.lxzh123.libshell.Loader;

public class DemoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try{
            Loader.init(this);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
