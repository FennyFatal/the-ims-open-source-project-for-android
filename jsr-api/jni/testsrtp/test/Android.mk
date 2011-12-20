LOCAL_PATH:= $(call my-dir)

# Build asrtpa_driver test
# ============================================================
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS       := optional test

LOCAL_MODULE            := srtpTest
LOCAL_SRC_FILES         := \
    srtp_driver.c \
    getopt_s.c \
    replay_driver.c \
    roc_driver.c \
    rdbx_driver.c \
    rtpw.c\
    rtp.c \
    dtls_srtp_driver.c \

LOCAL_C_INCLUDES        := $(LOCAL_PATH)/../include $(LOCAL_PATH)/../crypto/include

LOCAL_SHARED_LIBRARIES  := liblog libAsrtpa

include $(BUILD_STATIC_LIBRARY)
