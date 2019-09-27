//
// Created by ljf12 on 2019/9/17.
//

#ifndef JNITEST_JNILIB_H
#define JNITEST_JNILIB_H

#include <jni.h>
#include <stdlib.h>

#define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))

//__BEGIN_DECLS

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved);
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved);

//__END_DECLS

#endif //JNITEST_JNILIB_H
