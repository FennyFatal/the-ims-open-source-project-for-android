LOCAL_PATH:= $(call my-dir)

# Make the stagefright player backend library
# ============================================================
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional debug eng

LOCAL_MODULE := libStagefrightPlayerBackend
LOCAL_PRELINK_MODULE    := false

LOCAL_SRC_FILES         := \
    PlayerBackend.cpp \
    AudioCodec.cpp \
    G711Codec.cpp \
    RtpAudioStream.cpp \
#    RtpAudioGroup.cpp \

LOCAL_C_INCLUDES        := $(LOCAL_PATH)/include
LOCAL_C_INCLUDES        += $(LOCAL_PATH)/../ortp-0.16.5/include
LOCAL_C_INCLUDES        += $(LOCAL_PATH)/../srtp-1.4.4/include
LOCAL_C_INCLUDES        += $(LOCAL_PATH)/../srtp-1.4.4/crypto/include
LOCAL_C_INCLUDES        += $(JNI_H_INCLUDE)
LOCAL_C_INCLUDES        += frameworks/base/voip/jni/rtp
LOCAL_C_INCLUDES        += frameworks/base/include

#LOCAL_CFLAGS := $(LOCAL_PATH)/uptime.o
LOCAL_LDLIBS := -llog -L$(LOCAL_PATH) -lrtp_jni -L$(LOCAL_PATH) -lutils
LOCAL_SHARED_LIBRARIES  := \
    liblog \
    libOrtp \
    libSrtp \
    libutils \
    libmedia \
    librtp_jni \
##    libstagefright \

include $(BUILD_SHARED_LIBRARY)
