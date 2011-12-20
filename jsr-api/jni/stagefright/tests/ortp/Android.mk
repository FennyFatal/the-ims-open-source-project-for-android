LOCAL_PATH:= $(call my-dir)


# Build test app
# ============================================================
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS       := optional test

LOCAL_SRC_FILES         := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME      := OrtpTest

include $(BUILD_PACKAGE)
