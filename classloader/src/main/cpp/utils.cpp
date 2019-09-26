#include "utils.h"
#include "common.h"

#include <jni.h>
#include <dlfcn.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <dirent.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/mman.h>

int extract_file(JNIEnv *env, jobject ctx, const char *szDexPath, const char *fileName) {
    if (access(szDexPath, F_OK) == 0) {
        LOGD("[+]File %s have existed", szDexPath);
        return 0;
    } else {
        AAssetManager *mgr;
        jclass ApplicationClass = env->GetObjectClass(ctx);
        jmethodID getAssets =
                env->GetMethodID(ApplicationClass, "getAssets",
                                 "()Landroid/content/res/AssetManager;");
        jobject Assets_obj = env->CallObjectMethod(ctx, getAssets);
        mgr = AAssetManager_fromJava(env, Assets_obj);
        if (mgr == NULL) {
            LOGE("[-]getAAssetManager failed");
            return 0;
        }
        AAsset *asset = AAssetManager_open(mgr, fileName, AASSET_MODE_STREAMING);
        FILE *file = fopen(szDexPath, "wb");
        int bufferSize = AAsset_getLength(asset);
        LOGD("[+]Asset FileName:%s,extract path:%s,size:%d\n", fileName, szDexPath, bufferSize);
        void *buffer = malloc(4096);
        while (true) {
            int numBytesRead = AAsset_read(asset, buffer, 4096);
            if (numBytesRead <= 0) {
                break;
            }
            fwrite(buffer, numBytesRead, 1, file);
        }
        free(buffer);
        fclose(file);
        AAsset_close(asset);
        chmod(szDexPath, 493);
        return 1;
    }
} // extract_file

jstring chars_to_jstring(JNIEnv* env, const char* ptr) {
    //定义java String类 strClass
    jclass strClass = (env)->FindClass("Ljava/lang/String;");
    //获取String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String
    jmethodID ctorID = (env)->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    //建立byte数组
    jbyteArray bytes = (env)->NewByteArray(strlen(ptr));
    //将char* 转换为byte数组
    (env)->SetByteArrayRegion(bytes, 0, strlen(ptr), (jbyte*) ptr);
    // 设置String, 保存语言类型,用于byte数组转换至String时的参数
    jstring encoding = (env)->NewStringUTF("GB2312");
    //将byte数组转换为java String,并输出
    return (jstring) (env)->NewObject(strClass, ctorID, bytes, encoding);
}

char* jstring_to_chars(JNIEnv* env, jstring jstr) {
    char* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("GB2312");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char*) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}