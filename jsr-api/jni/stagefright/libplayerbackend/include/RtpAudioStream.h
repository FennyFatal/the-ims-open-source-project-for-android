#define LOG_TAG "RtpAudioStream"
#include <log.h>

#include <ortp/ortp.h>
#include <ortp/srtp.h>

#include <AudioCodec.h>


namespace ortp {

class RtpAudioStream
{
public:
    RtpAudioStream();
    ~RtpAudioStream();
    bool set(int mode, AudioCodec *codec, int sampleRate,
        int sampleCount, int codecType, int dtmfType);

    void sendDtmf(int event);
    bool mix(int32_t *output, int head, int tail, int sampleRate);
    void encode(int tick, RtpAudioStream *chain);
    void decode(int tick);

    enum {
        NORMAL = 0,
        SEND_ONLY = 1,
        RECEIVE_ONLY = 2,
        LAST_MODE = 2,
    };

private:
    int mMode;
    AudioCodec *mCodec;
    uint32_t mCodecMagic;
    uint32_t mDtmfMagic;

    int mTick;
    int mSampleRate;
    int mSampleCount;
    int mInterval;
    int mLogThrottle;

    int16_t *mBuffer;
    int mBufferMask;
    int mBufferHead;
    int mBufferTail;
    int mLatencyTimer;
    int mLatencyScore;

    uint16_t mSequence;
    uint32_t mTimestamp;
    uint32_t mSsrc;

    int mDtmfEvent;
    int mDtmfStart;

    RtpAudioStream *mNext;

////    friend class RtpAudioGroup;
};

} // namespace
