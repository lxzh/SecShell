package com.lxzh123.sdkshellapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.lxzh123.libcore.LIB;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.text);

        int x = 5;
        int square = LIB.get().square(5);

        textView.setText("square(" + x + ")=" + square);
    }
}
