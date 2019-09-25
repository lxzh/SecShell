LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := dexload

LOCAL_SRC_FILES := packer.cpp byte_load.cpp utils.cpp fake_dlfcn.cpp


LOCAL_CFLAGS := -Wall
# LOCAL_CFLAGS +=-fpermissive
LOCAL_CFLAGS += -DNO_WINDOWS_BRAINDEATH -Werror-pointer-arith
LOCAL_LDLIBS :=-llog -landroid
include $(BUILD_SHARED_LIBRARY)


NDK_APP_DST_DIR := ../../../libs/$(TARGET_ARCH_ABI)