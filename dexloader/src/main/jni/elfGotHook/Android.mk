LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := elfHooker
LOCAL_CFLAGS := -pie -fPIE
LOCAL_LDFLAGS := -pie -fPIE
LOCAL_LDLIBS := -llog -lEGL
LOCAL_CPPFLAGS := -std=c++14
LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_SRC_FILES := hooker.cpp       \
                   elf_reader.cpp   \
                   tools.cpp

include $(BUILD_SHARED_LIBRARY)
