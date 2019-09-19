#include "seclib.h"
#include "common.h"
#include <stdlib.h>
#include <dlfcn.h>
#include <stdio.h>
#include <string.h>

#include <android/log.h>

#define TAG "SEC_LIB"
#define LOGI(FORMAT,...) __android_log_print(ANDROID_LOG_INFO, TAG, FORMAT, ##__VA_ARGS__);
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR, TAG, FORMAT, ##__VA_ARGS__);


#define JNIREG_CLASS "com/lxzh123/libshell/Helper"//指定要注册的类

JNINativeMethod *dvm_dalvik_system_DexFile;
void (*openDexFile)(const u4* args, union JValue* pResult);

int lookup(JNINativeMethod *table, const char *name, const char *sig,
           void (**fnPtrout)(u4 const *, union JValue *)) {
    int i = 0;
    while (table[i].name != NULL)
    {
        LOGI("lookup %d %s" ,i,table[i].name);
        if ((strcmp(name, table[i].name) == 0)
            && (strcmp(sig, table[i].signature) == 0))
        {
            *fnPtrout = (void(*)(const u4 *, JValue *)) table[i].fnPtr;
            return 1;
        }
        i++;
    }
    return 0;
}

JNIEXPORT jint JNICALL Java_com_lxzh123_libshell_Helper_loadDex
        (JNIEnv* env, jclass jv, jbyteArray dexArray, jlong dexLen){
    u1 * olddata = (u1*)(env->GetByteArrayElements(dexArray, NULL));
    char* arr;
    arr = (char*)malloc(16 + dexLen);
    ArrayObject *ao=(ArrayObject*)arr;
    ao->length = dexLen;
    memcpy(arr+16, olddata, dexLen);
//    static_cast<uint32_t>(reinterpret_cast<uintptr_t>(ao));
    u4 args[] = { static_cast<uint32_t>(reinterpret_cast<uintptr_t>(ao)) };
//    u4 args[] = { (u4) ao };
    union JValue pResult;
    jint result;
    if(openDexFile != NULL) {
        openDexFile(args, &pResult);
    }else{
        result = -1;
    }
    result = static_cast<jint>(reinterpret_cast<uintptr_t>(pResult.l));
//    result = (jint) pResult.l;

    LOGI("loadDex dexLen=%lld result=%d", dexLen, result);
    return result;
}

jint JNICALL loadDex(JNIEnv *env,
                     jobject/* this */,
                     jbyteArray dexArray,
                     jlong dexLen) {
    u1 * olddata = (u1*)(env->GetByteArrayElements(dexArray, NULL));
    char* arr;
    arr = (char*)malloc(16 + dexLen);
    ArrayObject *ao=(ArrayObject*)arr;
    ao->length = dexLen;
    memcpy(arr+16, olddata, dexLen);
//    static_cast<uint32_t>(reinterpret_cast<uintptr_t>(ao));
    u4 args[] = { static_cast<uint32_t>(reinterpret_cast<uintptr_t>(ao)) };
//    u4 args[] = { (u4) ao };
    union JValue pResult;
    jint result;
    if(openDexFile != NULL) {
        openDexFile(args, &pResult);
    }else{
        result = -1;
    }
    result = static_cast<jint>(reinterpret_cast<uintptr_t>(pResult.l));
//    result = (jint) pResult.l;

    LOGI("loadDex dexLen=%lld result=%d", dexLen, result);
    return result;
}

static JNINativeMethod gMethods[] = {
        {"loadDex", "([BJ)I;", (void *) loadDex}
};

JNIEXPORT int JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    void *ldvm = (void*) dlopen("libdvm.so", RTLD_LAZY);
    dvm_dalvik_system_DexFile = (JNINativeMethod*) dlsym(ldvm, "dvm_dalvik_system_DexFile");
    LOGI("JNI_OnLoad");
    //openDexFile
    if(0 == lookup(dvm_dalvik_system_DexFile, "openDexFile", "([B)I",&openDexFile)) {
        openDexFile = NULL;
        LOGI("openDexFile method does not found ");
    }else{
        LOGI("openDexFile method found ! HAVE_BIG_ENDIAN");
    }
    LOGI("ENDIANNESS is %c" ,ENDIANNESS );
    LOGI("JNI_OnLoad -> GetEnv");
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    LOGI("JNI_OnLoad -> FindClass");
    jclass javaClass = env->FindClass(JNIREG_CLASS);
    if (javaClass == NULL) {
        return JNI_ERR;
    }
//    LOGI("JNI_OnLoad -> RegisterNatives");
//    if (env->RegisterNatives(javaClass, gMethods, NELEM(gMethods)) < 0) {
//        return JNI_ERR;
//    }
    LOGI("JNI_OnLoad -> return");
    return JNI_VERSION_1_6;
}