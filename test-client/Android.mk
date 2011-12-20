LOCAL_PATH := $(call my-dir)

ifeq ($(IMS_SETTINGS), 1)
    $(shell ln -f -s AndroidManifest-wLaunch.xml $(LOCAL_PATH)/AndroidManifest.xml)
else
    $(shell ln -f -s AndroidManifest-woLaunch.xml $(LOCAL_PATH)/AndroidManifest.xml)
endif

$(shell touch $(LOCAL_PATH)/AndroidManifest.xml)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional debug eng
LOCAL_JAVA_LIBRARIES := jsr-api
   
# Build all java files in the java subdirectory
LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_PACKAGE_NAME := TestClient

LOCAL_PROGUARD_ENABLED := disabled
 
include $(BUILD_PACKAGE)

