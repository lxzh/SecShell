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
#ifndef _DEXLOADER_H
#define _DEXLOADER_H

#include "DexHeader.h"
#include "common.h"
#include <stdlib.h>
#include <dlfcn.h>
#include <stdio.h>

#include <jni.h>
#include <android/log.h>
#include <sys/system_properties.h>
#include <fcntl.h>

#ifdef __cplusplus
extern "C" {
#endif

int sdkVersion();
void* getLibartHandler(const char* libartName, int sdkVersion);
const void* LoadByteArt19(void* libArthandler, const char* base, size_t size);
const void* LoadByteArt21(void* libArthandler, const char* base, size_t size);
const void* LoadByteArt22(void* libArthandler, const char* base, size_t size);
const void* LoadByteArt23(void* libArthandler, const char* base, size_t size);
const void* LoadByteArt24(void* libArthandler, const char* base, size_t size);
const void* LoadByteArt26(void* libArthandler, const char* base, size_t size);

#ifdef __cplusplus
}
#endif
#endif

