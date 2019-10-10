#include "utils.h"
#include "common.h"

#include <jni.h>
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

//ref to libmix:com.lxzh123.libmix.Mix:MASK
#define DECRYPT_MASK (0xAA)

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
            int size = AAsset_read(asset, buffer, 4096);
            if (size <= 0) {
                break;
            }
            fwrite(buffer, size, 1, file);
        }
        free(buffer);
        fclose(file);
        AAsset_close(asset);
        chmod(szDexPath, 493);
        return 1;
    }
} // extract_file

int
extract_file_and_decrypt(JNIEnv *env, jobject ctx, const char *szDexPath, const char *fileName) {
    jclass ApplicationClass = env->GetObjectClass(ctx);
    jmethodID getAssets = env->GetMethodID(ApplicationClass, "getAssets",
                                           "()Landroid/content/res/AssetManager;");
    jobject Assets_obj = env->CallObjectMethod(ctx, getAssets);
    AAssetManager *mgr = AAssetManager_fromJava(env, Assets_obj);
    if (mgr == NULL) {
        LOGE("[-] getAAssetManager failed");
        return 0;
    }
    AAsset *asset = AAssetManager_open(mgr, fileName, AASSET_MODE_STREAMING);
    if(!asset) {
        LOGE("[-] AAssetManager_open file:%s failed", fileName);
    }
    int bufferSize = AAsset_getLength(asset);

    FILE *file = fopen(szDexPath, "wb");
    if (access(szDexPath, F_OK) == 0) {
        int dexSize = ftell(file);
        LOGD("[+] Assets file size %s, existed file size:%d", szDexPath, dexSize);
    }
//
//    if (access(szDexPath, F_OK) == 0) {
//        LOGD("[+] File %s have existed", szDexPath);
//        return 0;
//    } else {

    LOGD("[+]Asset FileName:%s,extract path:%s,size:%d\n", fileName, szDexPath, bufferSize);
    unsigned char *buffer = (unsigned char *) malloc(4096);
    int max, l, r;
    while (true) {
        int size = AAsset_read(asset, buffer, 4096);
        if (size <= 0) {
            break;
        }
        if (size % 2 == 0) {
            max = size;
        } else {
            max = size - 1;
            buffer[max] = (unsigned char) (buffer[max] ^ DECRYPT_MASK);
        }
        LOGD("[+] decrypt and write i=%d", size);
        for (int i = 0; i < max; i += 2) {
            l = (unsigned char) (buffer[i] ^ DECRYPT_MASK);
            r = (unsigned char) (buffer[i + 1] ^ DECRYPT_MASK);
            buffer[i] = r;
            buffer[i + 1] = l;
        }
        fwrite(buffer, size, 1, file);
    }
    free(buffer);
    fclose(file);
    AAsset_close(asset);
    chmod(szDexPath, 493);
    return 1;
//    }
}