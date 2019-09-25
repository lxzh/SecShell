//
// Created by ljf12 on 2019/9/17.
//

#ifndef JNITEST_JNILIB_H
#define JNITEST_JNILIB_H

#include <jni.h>
#include <stdlib.h>
#include <string>

#define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))

#ifdef __cplusplus
extern "C" {
#endif

//__BEGIN_DECLS

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved);
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved);

//__END_DECLS

/*
 * Class:     com_lxzh123_shell_App
 * Method:    printObject
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_com_lxzh123_shell_App_printObject
(JNIEnv *, jclass , jobject);

#ifdef __cplusplus
}
#endif
#endif //JNITEST_JNILIB_H
