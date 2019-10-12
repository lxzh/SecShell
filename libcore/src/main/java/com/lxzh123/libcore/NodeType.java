package com.lxzh123.libcore;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({1, 2, 3})
@Retention(RetentionPolicy.SOURCE)
public @interface NodeType {
}
