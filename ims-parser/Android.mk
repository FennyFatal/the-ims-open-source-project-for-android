LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional debug eng

LOCAL_SRC_FILES := \
    src/javax/microedition/ims/messages/parser/body/BodyParser.rl \
    src/javax/microedition/ims/messages/parser/message/ChallengeParser.rl \
    src/javax/microedition/ims/messages/parser/sdp/FmtpParser.rl \
    src/javax/microedition/ims/messages/parser/message/MessageParser.rl \
    src/javax/microedition/ims/messages/parser/msrp/MsrpParser.rl \
    src/javax/microedition/ims/messages/parser/msrp/MsrpUriParser.rl \
    src/javax/microedition/ims/messages/parser/sdp/RtpMapParser.rl \
    src/javax/microedition/ims/messages/parser/sdp/SdpParser.rl \
    src/javax/microedition/ims/messages/parser/message/SipUriParser.rl \
    src/javax/microedition/ims/messages/parser/cpim/CpimParser.rl \
    $(call all-subdir-java-files)

LOCAL_STATIC_JAVA_LIBRARIES:= ims-common

LOCAL_MODULE := ims-parser

include $(BUILD_STATIC_JAVA_LIBRARY)
