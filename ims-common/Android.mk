LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional debug eng

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_MODULE := ims-common

include $(BUILD_STATIC_JAVA_LIBRARY)
