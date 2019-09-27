package com.lxzh123.libcore;

public interface OutAnnotation {
    boolean equals(Object obj);

    int hashCode();

    String toString();

    Class<? extends OutAnnotation> annotationType();
}
