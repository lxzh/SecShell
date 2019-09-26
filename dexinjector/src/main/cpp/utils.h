#ifndef _BANGCLE_UTILS_H
#define _BANGCLE_UTILS_H
#include <jni.h>
#include <fcntl.h>

int extract_file(JNIEnv *env, jobject ctx, const char *szDexPath, const char *fileName);
void *get_module_base(pid_t pid, const char *module_name);
void *get_addr_symbol(char *module_name, char *target_symbol);
jstring chars_to_jstring(JNIEnv* env, const char* pat);
char* jstring_to_chars(JNIEnv* env, jstring jstr);
#endif
