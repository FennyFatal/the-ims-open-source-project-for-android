#include <jni.h>

#define LOG_TAG "PlayerBackend"
#include <log.h>

#include "RtpAudioStream.h"


namespace ortp {

static const char *g_classname = "com/ipmultimedia/frameworks/media/stagefright/StagefrightBackend";
static const char *g_ssrc = "ssrc_changed";

extern void initRandom();

void ssrc_cb(RtpSession *session)
{
    LOGD("hey, the ssrc has changed!");
}

static jint JNICALL createSession(JNIEnv *env, jclass clasz, jboolean isReceiving)
{
    LOGI("%s", __FUNCTION__);

    RtpSession *session = rtp_session_new(isReceiving? RTP_SESSION_RECVONLY : RTP_SESSION_SENDONLY);
    if (session == NULL) {
        LOGE("Failed to create session");
        return 0;
    }

    rtp_session_set_scheduling_mode(session, 1);
    rtp_session_set_blocking_mode(session, 1);
    rtp_session_set_connected_mode(session, TRUE);
    rtp_session_set_payload_type(session, 0);

    return (int)session;
}

static void JNICALL closeSession(JNIEnv *env, jclass clasz, jint channel, jint nativeStream)
{
    LOGI("%s", __FUNCTION__);

    RtpSession *session = (RtpSession *)channel;

    RtpAudioStream *stream = (RtpAudioStream *)nativeStream;
    if (stream != NULL) {
        delete stream;
    }

    rtp_session_destroy(session);
}

static jboolean JNICALL setSource(JNIEnv *env, jclass clasz, jstring address,
        jint port, jboolean isAudioDevice, jint channel)
{
    LOGI("%s", __FUNCTION__);

    int jittcomp = 40;
    bool_t adapt = TRUE;
    const char *addr8 = env->GetStringUTFChars(address, NULL);
    RtpSession *session = (RtpSession *)channel;

    // receiver
    rtp_session_set_local_addr(session, addr8/*"0.0.0.0"*/, port);
    rtp_session_set_symmetric_rtp(session, TRUE);
    rtp_session_enable_adaptive_jitter_compensation(session, adapt);
    rtp_session_set_jitter_compensation(session, jittcomp);
    rtp_session_signal_connect(session, g_ssrc, (RtpCallback)ssrc_cb, 0);
    rtp_session_signal_connect(session, g_ssrc, (RtpCallback)rtp_session_reset, 0);

    env->ReleaseStringUTFChars(address, addr8);
    return true;
}

// TODO: implement me
static jint JNICALL setAudioCodec(JNIEnv *env, jclass clasz, jstring jCodecSpec, jboolean isReceiving, jint channel)
{
    LOGI("%s", __FUNCTION__);

    RtpSession *session = (RtpSession *)channel;
    RtpAudioStream *stream = NULL;
    AudioCodec *codec = NULL;

    int codecType = -1;
    char codecName[16];
    int sampleRate = -1;
    int sampleCount = -1;
    int dtmfType = -1;
    int mode = RtpAudioStream::NORMAL;

    if (!jCodecSpec) {
        return 0;
    }
    const char *codecSpec = env->GetStringUTFChars(jCodecSpec, NULL);
    if (!codecSpec) {
        return 0;
    }

    // Create audio codec.
    sscanf(codecSpec, "%d %[^/]%*c%d", &codecType, codecName, &sampleRate);
    codec = newAudioCodec(codecName);
    sampleCount = (codec ? codec->set(sampleRate, codecSpec) : -1);
    env->ReleaseStringUTFChars(jCodecSpec, codecSpec);
    if (sampleCount <= 0) {
        goto error;
    }

    // Create audio stream.
    if (isReceiving) {
        mode = RtpAudioStream::RECEIVE_ONLY;
    } else {
        mode = RtpAudioStream::SEND_ONLY;
    }
    stream = new RtpAudioStream();
    if (!stream->set(mode, codec, sampleRate, sampleCount, codecType, dtmfType)) {
        goto error;
    }

    return (int)stream;

error:
    delete stream;
    delete codec;
    return 0;
}

// TODO: implement me
static jint JNICALL setVideoCodec(JNIEnv *env, jclass clasz, jint codec, jboolean isReceiving, jint channel)
{
    LOGI("%s", __FUNCTION__);

    RtpSession *session = (RtpSession *)channel;

    return 0;
}

static jboolean JNICALL setDestination(JNIEnv *env, jclass clasz, jstring address,
        jstring jssrc, jint port, jboolean isAudioDevice, jint channel)
{
    LOGI("%s", __FUNCTION__);

    const char *addr8 = env->GetStringUTFChars(address, NULL);
    RtpSession *session = (RtpSession *)channel;

    // sender
    rtp_session_set_remote_addr(session, addr8, port);
    const char *ssrc = NULL;
    if (jssrc != NULL) {
        ssrc = env->GetStringUTFChars(jssrc, NULL);
        rtp_session_set_ssrc(session, atoi(ssrc));
    }

    if (jssrc != NULL) {
        env->ReleaseStringUTFChars(jssrc, ssrc);
    }
    env->ReleaseStringUTFChars(address, addr8);
    return true;
}

static jboolean JNICALL enableSrtp(JNIEnv *env, jclass clasz, jboolean isReceiving,
        /*jint cipherLen, jint authKeyLen, jint authTagLen, */jbyteArray key, jint channel)
{
    LOGI("%s", __FUNCTION__);

    srtp_t srtp;
    err_status_t status;
    srtp_policy_t policy;

    crypto_policy_set_aes_cm_128_hmac_sha1_32(&policy.rtp);
    crypto_policy_set_aes_cm_128_hmac_sha1_32(&policy.rtcp);

    jbyte *keyBytes = env->GetByteArrayElements(key, NULL);
    jsize size = env->GetArrayLength(key);
    policy.ssrc.type = ssrc_specific;
    policy.ssrc.value = 0xcafebabe;
    policy.key = (unsigned char *)keyBytes;
//    policy.key = (unsigned char *)malloc(sizeof(jbyte) * size);
//    memcpy(policy.key, keyBytes, sizeof(jbyte) * size);
    policy.next = NULL;
    env->ReleaseByteArrayElements(key, keyBytes, JNI_ABORT);

    status = ortp_srtp_create(&srtp, &policy);
    if (status) {
        return false;
    }

    RtpSession *session = (RtpSession *)channel;
    RtpTransport *rtpt;
    RtpTransport *rtcpt;
    srtp_transport_new(srtp, &rtpt, &rtcpt);
    rtp_session_set_transports(session, rtpt, rtcpt);

    return true;
}

static jboolean JNICALL disableSrtp(JNIEnv *env, jclass clasz, jboolean isReceiving, jint channel)
{
    LOGI("%s", __FUNCTION__);

    RtpSession *session = (RtpSession *)channel;
    srtp_t srtp = (srtp_t)session->rtp.tr->data;
    ortp_srtp_dealloc(srtp);
    rtp_session_set_transports(session, NULL, NULL);
    return true;
}

// TODO: implement me
static jboolean JNICALL start(JNIEnv *env, jclass clasz, jboolean isReceiving,
        jboolean isAudioDevice, jboolean isSrtp, jint channel, jint nativeStream)
{
    LOGI("%s", __FUNCTION__);

    RtpSession *session = (RtpSession *)channel;
    RtpAudioStream *stream = (RtpAudioStream *)nativeStream;

    return false;
}

// TODO: implement me
static jboolean JNICALL stop(JNIEnv *env, jclass clasz, jboolean isReceiving,
        jboolean isAudioDevice, jint channel, jint nativeStream)
{
    LOGI("%s", __FUNCTION__);

    RtpSession *session = (RtpSession *)channel;
    RtpAudioStream *stream = (RtpAudioStream *)nativeStream;

    return false;
}

// TODO: implement me
static jboolean JNICALL setDtmfPayload(JNIEnv *env, jclass clasz, jint payload, jint channel, jint nativeStream)
{
    LOGI("%s", __FUNCTION__);

    RtpSession *session = (RtpSession *)channel;
    RtpAudioStream *stream = (RtpAudioStream *)nativeStream;

    return false;
}

// TODO: implement me
static jboolean JNICALL sendDtmf(JNIEnv *env, jclass clasz, jint c, jboolean outBand, jint channel, jint nativeStream)
{
    LOGI("%s", __FUNCTION__);

    RtpSession *session = (RtpSession *)channel;
    RtpAudioStream *stream = (RtpAudioStream *)nativeStream;

    return false;
}

// TODO: implement me
static jboolean JNICALL startDtmf(JNIEnv *env, jclass clasz, jint c, jboolean outBand, jint channel, jint nativeStream)
{
    LOGI("%s", __FUNCTION__);

    RtpSession *session = (RtpSession *)channel;
    RtpAudioStream *stream = (RtpAudioStream *)nativeStream;

    return false;
}

// TODO: implement me
static jboolean JNICALL stopDtmf(JNIEnv *env, jclass clasz, jint channel, jint nativeStream)
{
    LOGI("%s", __FUNCTION__);

    RtpSession *session = (RtpSession *)channel;
    RtpAudioStream *stream = (RtpAudioStream *)nativeStream;

    return false;
}


static const JNINativeMethod nativeMethods[] = {
    // Common methods
    {"createSession", "(Z)I", (void *)createSession},
    {"close", "(II)V", (void *)closeSession},
    {"setSource", "(Ljava/lang/String;IZI)Z", (void *)setSource},
    {"setAudioCodec", "(Ljava/lang/String;ZI)I", (void *)setAudioCodec},
    {"setVideoCodec", "(IZI)I", (void *)setVideoCodec},
    {"setDestination", "(Ljava/lang/String;Ljava/lang/String;IZI)Z", (void *)setDestination},
    {"enableSrtp", "(Z[BI)Z", (void *)enableSrtp},
    {"start", "(ZZZII)Z", (void *)start},
    {"stop", "(ZZII)Z", (void *)stop},
    {"disableSrtp", "(ZI)Z", (void *)disableSrtp},
    {"setDtmfPayload", "(III)Z", (void *)setDtmfPayload},
    {"sendDtmf", "(IZII)Z", (void *)sendDtmf},
    {"startDtmf", "(IZII)Z", (void *)startDtmf},
    {"stopDtmf", "(II)Z", (void *)stopDtmf},
};

extern "C" jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    if (vm == NULL) {
        LOGE("%s didn\'t receive VM", __func__);
        return -1;
    }

    JNIEnv* env = NULL;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_4) != JNI_OK) {
        LOGE("%s couldn\'t get JNI env", __func__);
        return -1;
    }

    LOGI("JNI_OnLoad Stagefright Native Player Backend");

    jclass clazz = env->FindClass(g_classname);
    if (clazz == NULL) {
        LOGE("Native registration unable to find class '%s'", g_classname);
        return JNI_FALSE;
    }

    // Init random
    initRandom();

    // Init oRTP library
    ortp_init();
    ortp_scheduler_init();

    // Init SRTP library
    int err = ortp_srtp_init();
    if (err != 0) {
        LOGE("Failed to init SRTP");
    }

    // We register native methods in order to skip javah and use conveniently named functions.
    // Just this native initialization function uses name-mangling based method discovery.
    env->RegisterNatives(clazz, nativeMethods, sizeof(nativeMethods) / sizeof(nativeMethods[0]));

    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved)
{
    JNIEnv *env = NULL;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_4) != JNI_OK) {
        LOGE("%s couldn\'t get JNI env", __func__);
        return;
    }

    // Shutdown SRTP library
    int err = ortp_srtp_deinit();
    if (err != 0) {
        LOGE("Failed to shutdown SRTP");
    }

    // Shutdown oRTP library
    ortp_exit();

    jclass clazz = env->FindClass(g_classname);
    env->UnregisterNatives(clazz);
    LOGI("Remove oRTP Library\n");
}

} // namespace
