LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS       := optional test

LOCAL_MODULE            := cryptoTest
LOCAL_SRC_FILES         := \
    aes_calc.c \
    datatypes_driver.c \
    stat_driver.c \
    sha1_driver.c \
    kernel_driver.c \
    cipher_driver.c \
    rand_gen.c \
    env.c \

LOCAL_C_INCLUDES        := $(LOCAL_PATH)/../../include $(LOCAL_PATH)/../include

LOCAL_SHARED_LIBRARIES  := liblog libSrtp

include $(BUILD_STATIC_LIBRARY)
