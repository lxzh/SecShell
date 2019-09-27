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