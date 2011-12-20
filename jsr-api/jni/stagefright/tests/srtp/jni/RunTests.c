#include <jni.h>
#include <string.h>
//#include <cassert>

#include <log.h>

#include "tests.h"
#define LOG_TAG "srtp_RunTests"


static const char *g_classname = "com/tmobile/test/srtptest/MainTestActivity";

static jboolean JNICALL runDtls(JNIEnv *env, jclass clasz)
{
    LOGI("%s", __FUNCTION__);
    return dtls_srtp_driver() == 0;
}

static jboolean JNICALL runRdbx(JNIEnv *env, jclass clasz)
{
    LOGI("%s", __FUNCTION__);
    return rdbx_driver(0, 1) == 0;
}

static jboolean JNICALL runReplay(JNIEnv *env, jclass clasz, jint num_trials)
{
    LOGI("%s", __FUNCTION__);
    return replay_driver(num_trials) == 0;
}

static jboolean JNICALL runRoc(JNIEnv *env, jclass clasz, jint num_trials)
{
    LOGI("%s", __FUNCTION__);
    return roc_driver(num_trials) == 0;
}

// TODO: implement me
static jboolean JNICALL runRtpw(JNIEnv *env, jclass clasz)
{
    LOGI("%s", __FUNCTION__);
//    return rtpw(argc, argv) == 0;
    return 1;
}

static jboolean JNICALL runSrtp(JNIEnv *env, jclass clasz)
{
    LOGI("%s", __FUNCTION__);
    return srtp_driver() == 0;
}

static jboolean JNICALL runAes(JNIEnv *env, jclass clasz, jstring key, jstring phrase, jstring cipher)
{
    LOGI("%s", __FUNCTION__);
    char *key8 = (char *) (*env)->GetStringUTFChars(env, key, NULL);
    char *phrase8 = (char *) (*env)->GetStringUTFChars(env, phrase, NULL);
    char *cipher8 = (char *) (*env)->GetStringUTFChars(env, cipher, NULL);
    int result = aes_calc(key8, phrase8, cipher8);
    (*env)->ReleaseStringUTFChars(env, key, key8);
    (*env)->ReleaseStringUTFChars(env, phrase, phrase8);
    (*env)->ReleaseStringUTFChars(env, cipher, cipher8);
    return result == 0;
}

static jboolean JNICALL runCipher(JNIEnv *env, jclass clasz)
{
    LOGI("%s", __FUNCTION__);
    return cipher_driver(0, 1, 0) == 0;
}

static jboolean JNICALL runDatatypes(JNIEnv *env, jclass clasz)
{
    LOGI("%s", __FUNCTION__);
    return datatypes_driver() == 0;
}

static jboolean JNICALL runEnv(JNIEnv *env, jclass clasz)
{
    LOGI("%s", __FUNCTION__);
    return envTest() == 0;
}

static jboolean JNICALL runKernel(JNIEnv *env, jclass clasz)
{
    LOGI("%s", __FUNCTION__);
    return kernel_driver(1, 0) == 0;
}

static jboolean JNICALL runRand(JNIEnv *env, jclass clasz, jint bytes)
{
    LOGI("%s", __FUNCTION__);
    return rand_gen(bytes, 0) == 0;
}

static jboolean JNICALL runSha1(JNIEnv *env, jclass clasz)
{
    LOGI("%s", __FUNCTION__);
    return sha1_driver() == 0;
}

static jboolean JNICALL runStat(JNIEnv *env, jclass clasz)
{
    LOGI("%s", __FUNCTION__);
    return stat_driver() == 0;
}


static const JNINativeMethod nativeMethods[] = {
    // Common methods
    {"runDtlsTest", "()Z", (void *) runDtls },
    {"runRdbxTest", "()Z", (void *) runRdbx},
    {"runReplayTest", "(I)Z", (void *) runReplay},
    {"runRocTest", "(I)Z", (void *) runRoc},
    {"runRtpwTest", "()Z", (void *) runRtpw},
    {"runSrtpTest", "()Z", (void *) runSrtp},
    {"runAesTest", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z", (void *) runAes},
    {"runCipherTest", "()Z", (void *) runCipher},
    {"runDatatypesTest", "()Z", (void *) runDatatypes},
    {"runEnvTest", "()Z", (void *) runEnv},
    {"runKernelTest", "()Z", (void *) runKernel},
    {"runRandTest", "(I)Z", (void *) runRand},
    {"runSha1Test", "()Z", (void *) runSha1},
    {"runStatTest", "()Z", (void *) runStat},
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv* env;
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("%s couldn\'t get JNI env", __func__);
        return -1;
    }

    LOGI("JNI_OnLoad SRTP Test Library");

    jclass k = (*env)->FindClass(env, g_classname);

    if(k == NULL) {
        LOGE("Native registration unable to find class '%s'", g_classname);
        return JNI_FALSE;
    }

    // We register native methods in order to skip javah and use conveniently named functions.
    // Just this native initialization function uses name-mangling based method discovery.
    (*env)->RegisterNatives(env, k, nativeMethods, sizeof(nativeMethods) / sizeof(nativeMethods[0]));

    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved)
{
    JNIEnv *env;
    jclass k;

    jint r = (*vm)->GetEnv (vm, (void **)&env, JNI_VERSION_1_4);

    /* !!! FindClass returns valid value !!! */
    k = (*env)->FindClass (env, g_classname);

    (*env)->UnregisterNatives(env, k);
    LOGI("Remove SRTP Test Library\n");
}
