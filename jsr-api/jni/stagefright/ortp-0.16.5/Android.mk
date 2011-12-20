LOCAL_PATH:= $(call my-dir)

# Build oRTP library
# ============================================================
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS       := optional debug eng

LOCAL_MODULE            := libOrtp
LOCAL_PRELINK_MODULE    := false

LOCAL_CFLAGS:= \
        -DHAVE_CONFIG_H -D_REENTRANT -DORTP_INET6 -DIS_ANDROID=1

LOCAL_SRC_FILES         := \
        src/str_utils.c         \
        src/port.c              \
        src/rtpparse.c          \
        src/rtpsession.c        \
        src/rtpsession_inet.c   \
        src/jitterctl.c         \
        src/rtpsignaltable.c    \
        src/rtptimer.c          \
        src/posixtimer.c        \
        src/ortp.c              \
        src/scheduler.c         \
        src/avprofile.c         \
        src/sessionset.c        \
        src/telephonyevents.c   \
        src/payloadtype.c       \
        src/rtcp.c              \
        src/utils.c             \
        src/rtcpparse.c         \
        src/event.c             \
        src/stun.c              \
        src/stun_udp.c          \
        src/srtp.c              \
        src/b64.c               \

LOCAL_C_INCLUDES        := $(LOCAL_PATH)/include
LOCAL_C_INCLUDES        += $(LOCAL_PATH)/../srtp-1.4.4/include
LOCAL_C_INCLUDES        += $(LOCAL_PATH)/../srtp-1.4.4/crypto/include
#LOCAL_C_INCLUDES        += external/openssl/include
LOCAL_C_INCLUDES        += $(JNI_H_INCLUDE)
#LOCAL_LDLIBS := -llog
LOCAL_LDLIBS := -llog -L$(LOCAL_PATH) -lcrypto -Lobj/local/armeabi/ -lSrtp
LOCAL_LD_FLAGS := -Wl,--rpath-link obj/local/armeabi/ -L$(LOCAL_PATH) -Lobj/local/armeabi/
LOCAL_SHARED_LIBRARIES  := libSrtp libcrypto

include $(BUILD_SHARED_LIBRARY)
