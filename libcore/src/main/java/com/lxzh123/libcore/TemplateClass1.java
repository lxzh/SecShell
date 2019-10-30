package com.lxzh123.libcore;


import androidx.annotation.Nullable;

import java.util.Comparator;

public class TemplateClass1<T extends Comparator, K extends Comparable> {
    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }
}
