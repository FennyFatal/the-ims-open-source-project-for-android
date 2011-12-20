LOCAL_PATH:= $(call my-dir)

# Copy the srtp library
# ============================================================
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS       := optional debug eng

LOCAL_MODULE            := libAsrtpa
LOCAL_SRC_FILES         := \
    crypto/cipher/aes.c \
    crypto/cipher/aes_cbc.c \
    crypto/cipher/aes_icm.c \
    crypto/cipher/cipher.c \
    crypto/cipher/null_cipher.c \
    crypto/hash/auth.c \
    crypto/hash/hmac.c \
    crypto/hash/null_auth.c \
    crypto/hash/sha1.c \
    crypto/kernel/alloc.c \
    crypto/kernel/crypto_kernel.c \
    crypto/kernel/key.c \
    crypto/math/datatypes.c \
    crypto/math/stat.c \
    crypto/replay/rdb.c \
    crypto/replay/rdbx.c \
    crypto/replay/ut_sim.c \
    crypto/rng/ctr_prng.c \
    crypto/rng/prng.c \
    crypto/rng/rand_source.c \
    srtp/srtp.c \

LOCAL_C_INCLUDES        := $(LOCAL_PATH)/include $(LOCAL_PATH)/crypto/include $(JNI_H_INCLUDE)
LOCAL_LDLIBS := -llog
#LOCAL_SHARED_LIBRARIES  := liblog
LOCAL_PRELINK_MODULE    := false

include $(BUILD_SHARED_LIBRARY)
