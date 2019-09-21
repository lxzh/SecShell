#include "seclib.h"
#include "common.h"
#include "DexLoader.h"
#include <stdlib.h>
#include <dlfcn.h>
#include <stdio.h>
#include <string.h>
#include <vector>
#include <android/log.h>
//#include <bits/unique_ptr.h>

//#define TAG "SEC_LIB"
//#define LOGI(FORMAT,...) __android_log_print(ANDROID_LOG_INFO, TAG, FORMAT, ##__VA_ARGS__);
//#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR, TAG, FORMAT, ##__VA_ARGS__);

#define JNIREG_CLASS "com/lxzh123/libshell/Helper"   //指定要注册的类

JNINativeMethod *dvm_dalvik_system_DexFile;
void (*openDexFile)(const u4* args, union JValue* pResult);

bool isArtMode()
{
    static bool done = false;
    static bool isArt = false;
    if (!done)
    {
        done = true;
        char propValue[PROP_VALUE_MAX] = {0};
        __system_property_get("persist.sys.dalvik.vm.lib.2", propValue);
        if (propValue[0] == '\x0')
            __system_property_get("persist.sys.dalvik.vm.lib", propValue);
        isArt = ( !strcmp(propValue, "libart.so") );
    }

    return isArt;
}

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


jint ArtLoadByte(const char* base, size_t size)
{
    const void* cookie = NULL;
    jint result = 0;
    int sdkVer = sdkVersion();
    std::vector<const void*> dex_files;
    void* libartHandler = getLibartHandler("libart.so",sdkVer);
    if(!libartHandler){
        return -1;
    }
    switch (sdkVer)
    {
        case 19:
            LOGI("art19");
            cookie = LoadByteArt19(libartHandler, base, size);
            if (cookie){
                result = static_cast<jint>(reinterpret_cast<uintptr_t>(cookie));
            }
            break;
        case 21:
            LOGI("art21");
            cookie = LoadByteArt21(libartHandler, base, size);
            if (cookie){
//                dex_files.get()->push_back(cookie);
//                dex_files.push_back(cookie);
                result = static_cast<jlong>(reinterpret_cast<uintptr_t>(cookie));
            }
            break;
        case 22:
            LOGI("art22");
            cookie = LoadByteArt22(libartHandler, base, size);
            if (cookie){
                dex_files.push_back(cookie);
                result = static_cast<jlong>(reinterpret_cast<uintptr_t>(cookie));
//                result = static_cast<jlong>(reinterpret_cast<uintptr_t>(dex_files));
            }
            break;
        case 23:
            LOGI("art23");
            cookie = LoadByteArt23(libartHandler, base, size);
            if (cookie){
                dex_files.push_back(cookie);
                result = static_cast<jlong>(reinterpret_cast<uintptr_t>(cookie));
//                result = static_cast<jlong>(reinterpret_cast<uintptr_t>(dex_files));
            }
            break;
        case 24:
        case 25:
            LOGI("art24 or art25");
            cookie = LoadByteArt24(libartHandler, base, size);
            if (cookie){
                dex_files.push_back(cookie);
                result = static_cast<jlong>(reinterpret_cast<uintptr_t>(cookie));
//                result = static_cast<jlong>(reinterpret_cast<uintptr_t>(dex_files));
            }
//            c_dex_cookie = NULL;
            break;
        default:
            LOGI("LoadByteArt sdk:%d not implement!", sdkVer);
            break;
    }
    return result;
}

jint JNICALL loadDex(JNIEnv *env,
                     jobject/* this */,
                     jbyteArray dexArray,
                     jlong dexLen) {
    LOGI("loadDex");
    jint result;
    u1 * olddata = (u1*)(env->GetByteArrayElements(dexArray, NULL));
    if (isArtMode()==true){
        result = ArtLoadByte((const char*)olddata, (size_t)dexLen);
        LOGI("loadDex art cookie:0x%x", result);
        return result;
    }
    LOGI("loadDex ->1");
    char* arr;
    arr = (char*)malloc(16 + dexLen);
    ArrayObject *ao=(ArrayObject*)arr;
    ao->length = dexLen;
    memcpy(arr+16, olddata, dexLen);
//    static_cast<uint32_t>(reinterpret_cast<uintptr_t>(ao));
    u4 args[] = { static_cast<uint32_t>(reinterpret_cast<uintptr_t>(ao)) };
//    u4 args[] = { (u4) ao };
    union JValue pResult;

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
        {"loadDex",     "([BJ)I",              (void*)loadDex},
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
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
    LOGI("JNI_OnLoad -> RegisterNatives");
    jint rst = env->RegisterNatives(javaClass, gMethods, NELEM(gMethods));
    if (rst < 0) {
        LOGI("JNI_OnLoad -> RegisterNatives Error %d", rst);
        return JNI_ERR;
    }
    LOGI("JNI_OnLoad -> return");
    return JNI_VERSION_1_6;
}