package com.lxzh123.libshell;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class InitProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        try {
            Core.init(this.getContext());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    @Nullable
    public Cursor query(@NonNull Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Nullable
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        return null;
    }

    public int delete(@NonNull Uri uri, String s, String[] strings) {
        return 0;
    }

    public int update(@NonNull Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
