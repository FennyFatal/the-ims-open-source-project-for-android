LOCAL_PATH:= $(call my-dir)


# Make the shared test library
# ============================================================
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS       := optional test

LOCAL_MODULE            := libSrtpTest
LOCAL_PRELINK_MODULE    := false
LOCAL_SRC_FILES         := jni/RunTests.c
LOCAL_C_INCLUDES        := $(LOCAL_PATH)/jni/include $(JNI_H_INCLUDE)
LOCAL_LDLIBS := -llog
LOCAL_SHARED_LIBRARIES  := liblog libSrtp
LOCAL_STATIC_LIBRARIES  := \
    cryptoTest \
    srtpTest

include $(BUILD_SHARED_LIBRARY)


# Build test app
# ============================================================
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS       := optional test

LOCAL_SRC_FILES         := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME      := SrtpTest

include $(BUILD_PACKAGE)
