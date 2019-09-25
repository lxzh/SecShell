#include "printlib.h"

#include <android/log.h>

#define TAG "printlib"
#define LOGI(FORMAT,...) __android_log_print(ANDROID_LOG_INFO, TAG, FORMAT, ##__VA_ARGS__);
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR, TAG, FORMAT, ##__VA_ARGS__);

extern "C" JNIEXPORT void JNICALL Java_com_lxzh123_shell_App_printObject
(JNIEnv * env, jclass /* this */, jobject obj) {
    jlong* addr = (jlong*)obj;
    LOGI("printObject:addr=(%lld %x) value=(%lld %x)", addr, addr, (*addr), (*addr));
}