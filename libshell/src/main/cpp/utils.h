#ifndef _BANGCLE_UTILS_H
#define _BANGCLE_UTILS_H
#include <jni.h>

int extract_file(JNIEnv *env, jobject ctx, const char *szDexPath, const char *fileName);
int extract_file_and_decrypt(JNIEnv *env, jobject ctx, const char *szDexPath, const char *fileName);
#endif
