LOCAL_PATH:= $(call my-dir)

subdirs := $(addprefix $(LOCAL_PATH)/,$(addsuffix /Android.mk, \
                srtp-1.4.4 \
                ortp-0.16.5 \
                libplayerbackend \
        ))

include $(subdirs)
