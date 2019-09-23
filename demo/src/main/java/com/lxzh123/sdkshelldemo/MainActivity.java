package com.lxzh123.sdkshelldemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.lxzh123.libcore.LIB;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LIB.get().square(5);
    }
}
