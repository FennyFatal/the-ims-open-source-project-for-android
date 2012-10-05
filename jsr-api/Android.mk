LOCAL_PATH:= $(call my-dir)

# install the /system/etc/permissions file.
include $(CLEAR_VARS)
LOCAL_MODULE := jsr-api.xml
LOCAL_MODULE_TAGS := optional debug eng
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/permissions
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional debug eng

LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_SRC_FILES := $(filter-out %/MainTestActivity.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/OrtpTestActivity.java,$(LOCAL_SRC_FILES))

LOCAL_STATIC_JAVA_LIBRARIES:= ims-service

LOCAL_MODULE := jsr-api

include $(BUILD_JAVA_LIBRARY)

include $(LOCAL_PATH)/jni/Android.mk
