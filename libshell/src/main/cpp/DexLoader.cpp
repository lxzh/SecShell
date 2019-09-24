/*
*****************************************************************************
* File        :
* Description : dex_mem_loader dex内存加载
* Creation    : 2016.12
* Author      : ll-hack
* History     :
*
******************************************************************************
**/
#include "DexLoader.h"
#include "fake_dlfcn.h"
#include <string>
#include <sstream>
//#include <iostream>

using namespace std;

typedef const void* (*org_artDexFileOpenMemory19)(unsigned char const*base, unsigned int size, std::string const& location, unsigned int location_checksum, void* mem_map);

typedef const void* (*org_artDexFileOpenMemory21)(const uint8_t* base, size_t size, const std::string& location, uint32_t location_checksum, void* mem_map, std::string* error_msg);

typedef const void* (*org_artDexFileOpenMemory22)(const uint8_t* base, size_t size, const std::string& location, uint32_t location_checksum, void* mem_map, const void*oat_file, std::string* error_msg);

typedef const void* (*org_artDexFileOpenMemory23)(void* recookie, const uint8_t* base, size_t size, const std::string& location, uint32_t location_checksum, void* mem_map, const void*oat_dex_file, std::string* error_msg);

typedef const void* (*org_artDexFileOpenMemory24)(void* recookie, const uint8_t* base, size_t size, const std::string& location, uint32_t location_checksum, void* mem_map, const void*oat_dex_file, std::string* error_msg);


typedef void *(*orig_OpenCommon)(void *retcookie, const uint8_t *base, size_t size,
								 const std::string &location, uint32_t location_checksum,
								 const void *oat_dex_file, bool verify,
								 bool verify_checksum, std::string *error_msg, void *verify_result);

void* getLibartHandler(const char* libartName, int sdkVersion)
{
	if (sdkVersion < 23) {
		// Android L, art::JavaVMExt::AddWeakGlobalReference(art::Thread*, art::mirror::Object*)
		return dlopen(libartName, RTLD_LAZY | RTLD_GLOBAL);
	}else if (sdkVersion < 24) {
		// Android M, art::JavaVMExt::AddWeakGlobalRef(art::Thread*, art::mirror::Object*)
		return dlopen("libart.so", RTLD_LAZY | RTLD_GLOBAL);
	} else {
		// Android N and O, Google disallow us use dlsym;
		void *handle;
		void *jit_lib;
		if (sizeof(void *) == sizeof(uint64_t)) {
			handle = fake_dlopen("/system/lib64/libart.so", RTLD_NOW);
			jit_lib = fake_dlopen("/system/lib64/libart-compiler.so", RTLD_NOW);
		} else {
			handle = fake_dlopen("/system/lib/libart.so", RTLD_NOW);
			jit_lib = fake_dlopen("/system/lib/libart-compiler.so", RTLD_NOW);
		}
		LOGI("fake dlopen install: %p", handle);
        return handle;
	}
}

int sdkVersion()
{
    static bool done = false;
    static int sdkVersion = 0;
    if (!done)
    {
        done = true;
        char propValue[PROP_VALUE_MAX] = {0};
        __system_property_get("ro.build.version.sdk", propValue);
        sdkVersion = atoi(propValue);
    }

    return sdkVersion;
}

/**
 *  4.4 art
 */
const void* LoadByteArt19(void* libArthandler, const char* base, size_t size)
{
    std::string location = "";
    std::string err_msg;
	org_artDexFileOpenMemory19 func = (org_artDexFileOpenMemory19)dlsym(libArthandler, "_ZN3art7DexFile10OpenMemoryEPKhjRKSsjPNS_6MemMapE");
	if(!func){
		return NULL;
	}
    const Header* dex_header = reinterpret_cast<const Header*>(base);//dex buffer header
    const void* cookie = func((const unsigned char *) base, size, location, dex_header->checksum_, NULL);
	if (!cookie) {
		LOGI("LoadByte19 Failed");
    }
	else {
		LOGI("LoadByte19 : %x", cookie);
	}
	return cookie;
}

const void* LoadByteArt21(void* libArthandler, const char* base, size_t size)
{
	std::string location = "";
	std::string err_msg;
	org_artDexFileOpenMemory21 func = (org_artDexFileOpenMemory21)dlsym(libArthandler, "_ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPS9_");
	if(!func){
		return NULL;
	}
	const Header* dex_header = reinterpret_cast<const Header*>(base);
	const void *cookie = func((const unsigned char *) base, size, location, dex_header->checksum_, NULL, &err_msg);
	if (!cookie) {
		LOGI("LoadByte21 Failed %s", err_msg.c_str());
	}
	else {
		LOGI("LoadByte21 : %x", cookie);
	}
	return cookie;
}

const void* LoadByteArt22(void* libArthandler, const char* base, size_t size)
{
	std::string location = "";
	std::string err_msg;
	//sumsung
	org_artDexFileOpenMemory22 func = (org_artDexFileOpenMemory22)dlsym(libArthandler, "_ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_7OatFileES2_PS9_.constprop.183");
	if(!func){
		func = (org_artDexFileOpenMemory22)dlsym(libArthandler, "_ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_7OatFileES2_PS9_");
	}
	if(!func){
		return NULL;
	}
	const Header* dex_header = reinterpret_cast<const Header*>(base);
	const void *cookie = func((const unsigned char *) base, size, location, dex_header->checksum_, NULL, NULL, &err_msg);
	if (!cookie) {
		LOGI("LoadByte22 Failed %s", err_msg.c_str());
	}
	else {
		LOGI("LoadByte22 : %x", cookie);
	}
	return cookie;
}

const void* LoadByteArt23(void* libArthandler, const char* base, size_t size)
{
	std::string location = "";
	std::string err_msg;
	void* retcookie = malloc(0x78);
	memset(retcookie, 0, 0x78);
	org_artDexFileOpenMemory23 func = (org_artDexFileOpenMemory23)dlsym(libArthandler, "_ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_10OatDexFileEPS9_");
	if(!func){
		return NULL;
	}
	const Header* dex_header = reinterpret_cast<const Header*>(base);
	func(retcookie,(const unsigned char *) base, size, location, dex_header->checksum_, NULL, NULL, &err_msg);
	if (*(int*)retcookie==0) {
		LOGI("LoadByte23 Failed %s", err_msg.c_str());
	}
	else {
		LOGI("LoadByte23 : %x", retcookie);
	}
	return retcookie;
}

const void *LoadByteArt24(void *libArthandler, const char *base, size_t size)
{
	std::string location = "";
	std::string err_msg;
	void *retcookie = malloc(0x78);
	memset(retcookie, 0, 0x78);

	// #define SEARCH_SYMBOL_Nougat		//												     _ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_10OatDexFileEPS9_																																	_ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_10OatDexFileEPS9_
	org_artDexFileOpenMemory24 func = (org_artDexFileOpenMemory24)fake_dlsym(libArthandler, "_ZN3art7DexFile10OpenMemoryEPKhmRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_10OatDexFileEPS9_");

	//xiaomi art32:_ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_10OatDexFileEPS9_
	//xiaomi art64:_ZN3art7DexFile10OpenMemoryEPKhmRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_10OatDexFileEPS9_
	//Androidart32:_ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_10OatDexFileEPS9_
	//Androidart64:_ZN3art7DexFile10OpenMemoryEPKhmRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_10OatDexFileEPS9_
	LOGI("[+]LoadByteArt24 fake_dlsym openMemory succeed");
	if (!func)
	{
		LOGE("[-]sdk_int:%d dlsym openMemory failed:%s", 24, dlerror());
#ifndef SEARCH_SYMBOL_Nougat
		return NULL;
#else  // ifndef SEARCH_SYMBOL_Nougat
		LOGD("[+]try search symbol from elf file");
        func = (org_artDexFileOpenMemory23)get_addr_symbol("/system/lib/libart.so",
                                                           "_ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_10OatDexFileEPS9_");

        if (!func)
        {
            LOGE("[-]search symbol openMemory uniptr failed");
            return NULL;
        }
#endif // ifndef SEARCH_SYMBOL_Nougat
	}
	LOGI("[+]LoadByteArt24 invoke func");
	const Header *dex_header = reinterpret_cast<const Header *>(base);
	const void *value = func(retcookie,
					   (const unsigned char *)base,
					   size,
					   location,
					   dex_header->checksum_,
					   NULL,
					   NULL,
					   &err_msg);
	void *a = retcookie;

	// LOGD改变了retcookie？？ 所以先用a备份
	LOGI("[+]openMemory value:%p,*(int*)retcookie:%x,*(jlong*)retcookie:%llx",
		 value,
		 *(int *)retcookie,
		 *(jlong *)a);

	return (void *)(*(jlong *)retcookie);
} // mem_loadDex_byte24

// For Andoird oreo 8.0 and 8.1
// Reserved

/*
 * void* mem_loadDex_byte26(void* artHandle, const char* base, size_t size)
 * {
 *
 * std::string location="";
 * std::string err_msg;
 * void* retcookie = malloc(0x78);
 * memset(retcookie, 0, 0x78);
 *
 #define OREO_TARGET_STRING			"_ZN3art7DexFile10OpenCommonEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPKNS_10OatDexFileEbbPS9_PNS0_12VerifyResultE"
 * orig_OpenCommon func = (orig_OpenCommon)dlsym(artHandle,OREO_TARGET_STRING );
 * if (!func) {
 *  LOGE("[-]sdk_int:%d dlsym openCommon failed:%s",g_sdk_int,dlerror());
 *  return NULL;
 *
 *    // LOGD("[+]try search symbol %s from elf
 * file",(char*)OREO_TARGET_STRING);
 *    //
 * func=(org_artDexFileOpenMemory23)get_addr_symbol("/system/lib/libart.so",OREO_TARGET_STRING);
 *    // if (!func) {
 *    //  LOGE("[-]search symbol %s addr failed",OREO_TARGET_STRING);
 *    //  return NULL;
 *    // }
 * }
 * else {
 *  LOGD("[+]sdk_int:%d,dlsym openCommon :%p",g_sdk_int,func);
 *  return NULL;
 * }
 * const Header* dex_header = reinterpret_cast<const Header*>(base);
 * void* value=func(retcookie,(const unsigned char *)base, size, location,
 * dex_header->checksum_, NULL,false,false, &err_msg,NULL);
 * void* a=retcookie;
 * //LOGD改变了retcookie？？ 所以先用a备份
 * LOGD("[+]openCommon
 * value:%p,*(int*)retcookie:%x,*(jlong*)retcookie:%llx",value,*(int*)retcookie,*(jlong*)a);
 *
 * return (void*)(*(jlong*)retcookie);
 *
 * }
 */


