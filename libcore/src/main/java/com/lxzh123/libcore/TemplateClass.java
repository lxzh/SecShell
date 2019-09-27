package com.lxzh123.libcore;

import androidx.annotation.Nullable;

import java.util.Comparator;

public class TemplateClass<T extends Comparator> {
    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }
}
