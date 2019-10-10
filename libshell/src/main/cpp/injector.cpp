#include "injector.h"
#include "common.h"
#include "utils.h"

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <string.h>
#include <errno.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/mman.h>

const char* PKG_NAME = "com/lxzh123/libshell";
#define JNIREG_CLASS "com/lxzh123/libshell/Helper"   //指定要注册的类

int g_sdk_int = 0;
bool g_isArt = false;
char g_priv_dir[256] = {0};
const char *g_file_dir = nullptr;
const char BUILD_CONFIG[] = "/BuildConfig";
#define MAGIC            ".shell"
const char* DAT_NAME =   "libcore.data";
const char* TAR_NAME =   "libcore.dex";

void inject_dex(JNIEnv *env, jobject ctx, const char *dex_path);

char *get_config_class_name(JNIEnv *env, jstring pkg_name) {
    int pkg_name_len = env->GetStringLength(pkg_name);
    char *config_name = (char *) env->GetStringUTFChars(pkg_name, NULL);

    strcat(config_name, BUILD_CONFIG);
    for (int i = 0; i < pkg_name_len; i++) {
        if (config_name[i] == '.') {
            config_name[i] = '/';
        }
    }

    return config_name;
}

extern "C"
JNIEXPORT jstring JNICALL Helper_init
        (JNIEnv *env, jclass, jobject ctx, jboolean injectDex) {
    jclass ApplicationClass = env->GetObjectClass(ctx);
    jmethodID getFilesDir = env->GetMethodID(ApplicationClass, "getFilesDir", "()Ljava/io/File;");
    jobject File_obj = env->CallObjectMethod(ctx, getFilesDir);
    jclass FileClass = env->GetObjectClass(File_obj);

    jmethodID getAbsolutePath = env->GetMethodID(FileClass, "getAbsolutePath",
                                                 "()Ljava/lang/String;");
    jstring data_file_dir = static_cast<jstring>(env->CallObjectMethod(File_obj, getAbsolutePath));

    jmethodID getPackageNameMethod = env->GetMethodID(ApplicationClass, "getPackageName",
                                                      "()Ljava/lang/String;");
    jstring pkg_name = static_cast<jstring >( env->CallObjectMethod(ctx, getPackageNameMethod));
    char *c_pkg_name = (char *) env->GetStringUTFChars(pkg_name, NULL);
    char *config_name = get_config_class_name(env, env->NewStringUTF(PKG_NAME));
//    char *config_name = get_config_class_name();
    LOGD("[+] pkgName:%s configName:%s", c_pkg_name, config_name);

    jclass BuildConfigClass = env->FindClass(config_name);
    jfieldID sdkDexNameFieldId = env->GetStaticFieldID(BuildConfigClass, "SDK_DEX_NAME", "Ljava/lang/String;");
    jfieldID sdkMixNameFieldId = env->GetStaticFieldID(BuildConfigClass, "SDK_MIX_NAME", "Ljava/lang/String;");

    jstring dexName = (jstring)env->GetStaticObjectField(BuildConfigClass, sdkDexNameFieldId);
    jstring mixName = (jstring)env->GetStaticObjectField(BuildConfigClass, sdkMixNameFieldId);
    LOGD("[+] dexName:%s mixName:%s", env->GetStringUTFChars(dexName, NULL), env->GetStringUTFChars(mixName, NULL));

    DAT_NAME = env->GetStringUTFChars(mixName, NULL);
    TAR_NAME = env->GetStringUTFChars(dexName, NULL);

    LOGD("[+] DAT_NAME:%s TAR_NAME:%s", DAT_NAME, TAR_NAME);

    g_file_dir = env->GetStringUTFChars(data_file_dir, NULL);
    //g_file_dir_backup=g_file_dir;
    LOGD("[+] FilesDir:%s", g_file_dir);
    env->DeleteLocalRef(data_file_dir);
    env->DeleteLocalRef(File_obj);
    env->DeleteLocalRef(FileClass);

    char priv_path[256] = {0}; // 加密dex的存储路径

    sprintf(g_priv_dir, "%s/%s", g_file_dir, MAGIC);
    sprintf(priv_path, "%s/%s", g_priv_dir, TAR_NAME);
    LOGD("[+] Helper_init g_priv_dir:%s, priv_path:%s", g_priv_dir, priv_path);
    if (access(g_priv_dir, F_OK) != 0) {
        if (mkdir(g_priv_dir, 0755) == -1) {
            LOGE("[-]mkdir %s error:%s", g_priv_dir, strerror(errno));
        }
    }
    LOGD("[+] start extract_file_and_decrypt");
    //从assets目录提取加密dex
    extract_file_and_decrypt(env, ctx, priv_path, DAT_NAME);
    if (injectDex) {
        inject_dex(env, ctx, priv_path);
    }
    return env->NewStringUTF(priv_path);

//    mem_loadDex(env, ctx, priv_path);
}

void inject_dex(JNIEnv *env, jobject ctx, const char *dex_path) {
    LOGD("[+] inject_dex");
//    jstring dexPath = chars_to_jstring(env, dex_path);
    jstring dexPath = env->NewStringUTF(dex_path);

    LOGD("[+] inject_dex 0");
    jclass ApplicationClass = env->GetObjectClass(ctx);
    jmethodID getClassLoader = env->GetMethodID(ApplicationClass, "getClassLoader",
                                                "()Ljava/lang/ClassLoader;");
    LOGD("[+] inject_dex 1");
    jobject classLoader = env->CallObjectMethod(ctx, getClassLoader);
    jclass PathClassLoaderClass = env->GetObjectClass(classLoader);
    jclass DexClassLoaderClass = env->FindClass("dalvik/system/DexClassLoader");
    jclass BaseDexClassLoaderClass = env->GetSuperclass(PathClassLoaderClass);
//    jclass DexFileClass = env->FindClass("dalvik/system/DexFile");

    LOGD("[+] inject_dex 2");
    jmethodID getDir = env->GetMethodID(ApplicationClass, "getDir",
                                        "(Ljava/lang/String;I)Ljava/io/File;");
    jobject optDirFile = env->CallObjectMethod(ctx, getDir, env->NewStringUTF("dex"), 0);
    jclass FileClass = env->GetObjectClass(optDirFile);
    jmethodID getAbsolutePath = env->GetMethodID(FileClass, "getAbsolutePath",
                                                 "()Ljava/lang/String;");
    jstring optDir = static_cast<jstring>(env->CallObjectMethod(optDirFile, getAbsolutePath));

    LOGD("[+] inject_dex 3");
    //get pathList field id of BaseDexClassLoader
    jfieldID pathListId = env->GetFieldID(BaseDexClassLoaderClass, "pathList",
                                          "Ldalvik/system/DexPathList;");
    //get pathList in default classloader
    jobject pathList1 = env->GetObjectField(classLoader, pathListId);
    //get DexPathList Class
    jclass DexPathListClass = env->GetObjectClass(pathList1);
    //get dexElements field id of DexPathList
    jfieldID dexElementsId = env->GetFieldID(DexPathListClass, "dexElements",
                                             "[Ldalvik/system/DexPathList$Element;");
    //get dexElement array value in pathList1
    jobjectArray dexElement1 = static_cast<jobjectArray>(env->GetObjectField(pathList1,
                                                                             dexElementsId));
    int len = env->GetArrayLength(dexElement1);

    LOGD("[+] inject_dex 4");
    jmethodID DexClassLoaderInit = env->GetMethodID(DexClassLoaderClass, "<init>",
                                                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V");
    //create a DexClassLoader object to load the dex file
    jobject dexClassLoader = env->NewObject(DexClassLoaderClass, DexClassLoaderInit, dexPath,
                                            optDir, dexPath, classLoader);

    LOGD("[+] inject_dex 5");
    //get pathList in dexClassLoader
    jobject pathList2 = env->GetObjectField(dexClassLoader, pathListId);
    //get dexElement array value in pathList2
    jobjectArray dexElement2 = static_cast<jobjectArray>(env->GetObjectField(pathList2,
                                                                             dexElementsId));

    LOGD("[+] inject_dex 6");
    jclass ElementClass = env->GetObjectClass(env->GetObjectArrayElement(dexElement1, 0));
//    jclass ElementClass = env->FindClass("dalvik/system/DexPathList$Element");
    jobjectArray newElements = env->NewObjectArray(len + 1, ElementClass, NULL);
    for (int i = 0; i < len; ++i) {
        env->SetObjectArrayElement(newElements, i, env->GetObjectArrayElement(dexElement1, i));
    }
    env->SetObjectArrayElement(newElements, len, env->GetObjectArrayElement(dexElement2, 0));
    env->SetObjectField(pathList1, dexElementsId, newElements);

    LOGD("[+] inject_dex 7");
    env->DeleteLocalRef(newElements);
    env->DeleteLocalRef(ElementClass);
    env->DeleteLocalRef(dexElement1);
    env->DeleteLocalRef(dexElement2);
//    env->DeleteLocalRef(dexElementsId);
    env->DeleteLocalRef(pathList1);
    env->DeleteLocalRef(pathList2);
//    env->DeleteLocalRef(pathListId);
    env->DeleteLocalRef(DexPathListClass);
    env->DeleteLocalRef(FileClass);
    env->DeleteLocalRef(PathClassLoaderClass);
    env->DeleteLocalRef(ApplicationClass);
}

static JNINativeMethod gMethods[] = {
        {"init", "(Landroid/content/Context;Z)Ljava/lang/String;", (void *) Helper_init},
};

int jniRegisterNativeMethods(JNIEnv *env, const char *className, const JNINativeMethod *gMethods,
                             int numMethods) {
    LOGI("[+] jniRegisterNativeMethods");
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == NULL) {
        return -1;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        LOGE("[-] RegisterNatives failed");
        return -1;
    }
    return 0;
}

void init(JNIEnv *env) {
    LOGI("[+] init");
    jclass jclazz = env->FindClass("android/os/Build$VERSION");
    jfieldID SDK_INT = env->GetStaticFieldID(jclazz, "SDK_INT", "I");

    g_sdk_int = env->GetStaticIntField(jclazz, SDK_INT);
    LOGD("[+] sdk_int:%d", g_sdk_int);
    jclass System = env->FindClass("java/lang/System");
    jmethodID System_getProperty = env->GetStaticMethodID(System, "getProperty",
                                                          "(Ljava/lang/String;)Ljava/lang/String;");

    jstring vm_version_name = env->NewStringUTF("java.vm.version");
    jstring vm_version_value = (jstring) (env->CallStaticObjectMethod(System,
                                                                      System_getProperty,
                                                                      vm_version_name));

    char *cvm_version_value = (char *) env->GetStringUTFChars(vm_version_value, NULL);
    double version = atof(cvm_version_value);
    g_isArt = version >= 2 ? true : false;
    LOGD("[+] Android VmVersion:%f", version);

    env->ReleaseStringUTFChars(vm_version_value, cvm_version_value);
    env->DeleteLocalRef(System);
    env->DeleteLocalRef(vm_version_name);
    env->DeleteLocalRef(vm_version_value);
    jniRegisterNativeMethods(env, JNIREG_CLASS, gMethods, NELEM(gMethods));
    env->DeleteLocalRef(jclazz);
}

JNIEXPORT int JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGI("[+] JNI_OnLoad");
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    init(env);
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
    LOGI("[+] JNI_OnUnload");
}