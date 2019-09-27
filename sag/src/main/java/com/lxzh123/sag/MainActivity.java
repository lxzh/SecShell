package com.lxzh123.sag;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PermissionInfo;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.lxzh123.libsag.Sag;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import dalvik.system.DexClassLoader;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends Activity {
    private final static String TAG = "MainActivity";

    private final static String[] PERMISSIONS = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private final static int requestPermissionCode = 1000;

    private boolean hasWriteSDCardPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions(PERMISSIONS, requestPermissionCode);
    }

    private void requestRequiredPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, requestPermissionCode);
        } else {
            Log.d(TAG, "write external storage is permit");
            startParseJar();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == requestPermissionCode) {
            requestRequiredPermission();
        }
    }

    private void startParseJar() {
        Log.d(TAG, "startParseJar");
        new Thread() {
            @Override
            public void run() {
                super.run();
//                String outPath = getFilesDir().getAbsolutePath();
                String outPath = Environment.getExternalStorageDirectory().toString() + File.separator + "sag";
                String fileName = copyAssetsData(getApplicationContext(), "classes.jar", "classes.jar", outPath);
                String optDir = getDir("dex", 0).getAbsolutePath();
                Log.d(TAG, "generateSdkApi:outPath=" + outPath + " fileName=" + fileName + " optDir=" + optDir);
                DexClassLoader classLoader = new DexClassLoader(fileName, optDir, null, getBaseContext().getClassLoader());
                Sag.generateSdkApi(outPath, fileName, classLoader);
            }
        }.start();
    }

    private static String copyAssetsData(Context context, String srcName, String dstName, String path) {
        Log.d(TAG, "copyAssetsData:srcName=" + srcName + " path=" + path);
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;
        File pathFile = new File(path);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        File outFile = new File(path, dstName);
        FileOutputStream fos = null;
        try {
            if (outFile.exists()) {
                outFile.delete();
            }
            inputStream = new BufferedInputStream(assetManager.open(srcName));
            fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        } catch (IOException iex) {
            Log.e(TAG, "copyAssetsData IOException iex=" + iex.getMessage());
            iex.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ex) {
                    Log.e(TAG, "close inputStream ex=" + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (Exception ex) {
                    Log.e(TAG, "close inputStream ex=" + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
        return outFile.getAbsolutePath();
    }
}
