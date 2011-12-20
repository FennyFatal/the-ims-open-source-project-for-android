#include <SystemClock.h>

#include "RtpAudioStream.h"

namespace ortp {

#define BUFFER_SIZE     512
#define HISTORY_SIZE    80
#define MEASURE_PERIOD  2000

int gRandom = -1;

using namespace android;

RtpAudioStream::RtpAudioStream()
{
    mCodec = NULL;
    mBuffer = NULL;
    mNext = NULL;
}

RtpAudioStream::~RtpAudioStream()
{
    delete mCodec;
    delete [] mBuffer;
    LOGD("stream is dead");
}

bool RtpAudioStream::set(int mode, AudioCodec *codec, int sampleRate,
    int sampleCount, int codecType, int dtmfType)
{
    if (mode < 0 || mode > LAST_MODE) {
        return false;
    }
    mMode = mode;

    mCodecMagic = (0x8000 | codecType) << 16;
    mDtmfMagic = (dtmfType == -1) ? 0 : (0x8000 | dtmfType) << 16;

    mTick = elapsedRealtime();
    mSampleRate = sampleRate / 1000;
    mSampleCount = sampleCount;
    mInterval = mSampleCount / mSampleRate;

    // Allocate jitter buffer.
    for (mBufferMask = 8; mBufferMask < mSampleRate; mBufferMask <<= 1);
    mBufferMask *= BUFFER_SIZE;
    mBuffer = new int16_t[mBufferMask];
    --mBufferMask;
    mBufferHead = 0;
    mBufferTail = 0;
    mLatencyTimer = 0;
    mLatencyScore = 0;

    // Initialize random bits.
    if (gRandom != -1) {
        read(gRandom, &mSequence, sizeof(mSequence));
        read(gRandom, &mTimestamp, sizeof(mTimestamp));
        read(gRandom, &mSsrc, sizeof(mSsrc));
    }

    mDtmfEvent = -1;
    mDtmfStart = 0;

    // Only take over these things when succeeded.
    if (codec) {
        mCodec = codec;
    }

    LOGD("stream is configured as %s %dkHz %dms mode %d",
        (codec ? codec->name : "RAW"), mSampleRate, mInterval, mMode);
    return true;
}

void RtpAudioStream::sendDtmf(int event)
{
    if (mDtmfMagic != 0) {
        mDtmfEvent = event << 24;
        mDtmfStart = mTimestamp + mSampleCount;
    }
}

bool RtpAudioStream::mix(int32_t *output, int head, int tail, int sampleRate)
{
    if (mMode == SEND_ONLY) {
        return false;
    }

    if (head - mBufferHead < 0) {
        head = mBufferHead;
    }
    if (tail - mBufferTail > 0) {
        tail = mBufferTail;
    }
    if (tail - head <= 0) {
        return false;
    }

    head *= mSampleRate;
    tail *= mSampleRate;

    if (sampleRate == mSampleRate) {
        for (int i = head; i - tail < 0; ++i) {
            output[i - head] += mBuffer[i & mBufferMask];
        }
    } else {
        // TODO: implement resampling.
        return false;
    }
    return true;
}

void RtpAudioStream::encode(int tick, RtpAudioStream *chain)
{
    if (tick - mTick >= mInterval) {
        // We just missed the train. Pretend that packets in between are lost.
        int skipped = (tick - mTick) / mInterval;
        mTick += skipped * mInterval;
        mSequence += skipped;
        mTimestamp += skipped * mSampleCount;
        LOGV("stream skips %d packets", skipped);
    }

    tick = mTick;
    mTick += mInterval;
    ++mSequence;
    mTimestamp += mSampleCount;

    if (mMode == RECEIVE_ONLY) {
        return;
    }

    // If there is an ongoing DTMF event, send it now.
    if (mDtmfEvent != -1) {
        int duration = mTimestamp - mDtmfStart;
        // Make sure duration is reasonable.
        if (duration >= 0 && duration < mSampleRate * 100) {
            duration += mSampleCount;
            int32_t buffer[4] = {
                htonl(mDtmfMagic | mSequence),
                htonl(mDtmfStart),
                mSsrc,
                htonl(mDtmfEvent | duration),
            };
            if (duration >= mSampleRate * 100) {
                buffer[3] |= htonl(1 << 23);
                mDtmfEvent = -1;
            }
////            sendto(mSocket, buffer, sizeof(buffer), MSG_DONTWAIT,
////                (sockaddr *)&mRemote, sizeof(mRemote));
            return;
        }
        mDtmfEvent = -1;
    }

    // It is time to mix streams.
    bool mixed = false;
    int32_t buffer[mSampleCount + 3];
    memset(buffer, 0, sizeof(buffer));
    while (chain) {
        if (chain != this &&
            chain->mix(buffer, tick - mInterval, tick, mSampleRate)) {
            mixed = true;
        }
        chain = chain->mNext;
    }
    if (!mixed) {
        if ((mTick ^ mLogThrottle) >> 10) {
            mLogThrottle = mTick;
            LOGV("stream no data");
        }
        return;
    }

    // Cook the packet and send it out.
    int16_t samples[mSampleCount];
    for (int i = 0; i < mSampleCount; ++i) {
        int32_t sample = buffer[i];
        if (sample < -32768) {
            sample = -32768;
        }
        if (sample > 32767) {
            sample = 32767;
        }
        samples[i] = sample;
    }
    if (!mCodec) {
        // Special case for device stream.
////        send(mSocket, samples, sizeof(samples), MSG_DONTWAIT);
        return;
    }

    buffer[0] = htonl(mCodecMagic | mSequence);
    buffer[1] = htonl(mTimestamp);
    buffer[2] = mSsrc;
    int length = mCodec->encode(&buffer[3], samples);
    if (length <= 0) {
        LOGV("stream encoder error");
        return;
    }
////    sendto(mSocket, buffer, length + 12, MSG_DONTWAIT, (sockaddr *)&mRemote,
////        sizeof(mRemote));
}

void RtpAudioStream::decode(int tick)
{
    char c;
    if (mMode == SEND_ONLY) {
////        recv(mSocket, &c, 1, MSG_DONTWAIT);
        return;
    }

    // Make sure mBufferHead and mBufferTail are reasonable.
    if ((unsigned int)(tick + BUFFER_SIZE - mBufferHead) > BUFFER_SIZE * 2) {
        mBufferHead = tick - HISTORY_SIZE;
        mBufferTail = mBufferHead;
    }

    if (tick - mBufferHead > HISTORY_SIZE) {
        // Throw away outdated samples.
        mBufferHead = tick - HISTORY_SIZE;
        if (mBufferTail - mBufferHead < 0) {
            mBufferTail = mBufferHead;
        }
    }

    // Adjust the jitter buffer if the latency keeps larger than two times of the
    // packet interval in the past two seconds.
    int score = mBufferTail - tick - mInterval * 2;
    if (mLatencyScore > score) {
        mLatencyScore = score;
    }
    if (mLatencyScore <= 0) {
        mLatencyTimer = tick;
        mLatencyScore = score;
    } else if (tick - mLatencyTimer >= MEASURE_PERIOD) {
        LOGV("stream reduces latency of %dms", mLatencyScore);
        mBufferTail -= mLatencyScore;
        mLatencyTimer = tick;
    }

    if (mBufferTail - mBufferHead > BUFFER_SIZE - mInterval) {
        // Buffer overflow. Drop the packet.
        LOGV("stream buffer overflow");
////        recv(mSocket, &c, 1, MSG_DONTWAIT);
        return;
    }

    // Receive the packet and decode it.
    int16_t samples[mSampleCount];
    int length = 0;
    if (!mCodec) {
        // Special case for device stream.
////        length = recv(mSocket, samples, sizeof(samples),
////            MSG_TRUNC | MSG_DONTWAIT) >> 1;
    } else {
        __attribute__((aligned(4))) uint8_t buffer[2048];
        sockaddr_storage remote;
        socklen_t len = sizeof(remote);

////        length = recvfrom(mSocket, buffer, sizeof(buffer),
////            MSG_TRUNC | MSG_DONTWAIT, (sockaddr *)&remote, &len);

        // Do we need to check SSRC, sequence, and timestamp? They are not
        // reliable but at least they can be used to identify duplicates?
        if (length < 12 || length > (int)sizeof(buffer) ||
            (ntohl(*(uint32_t *)buffer) & 0xC07F0000) != mCodecMagic) {
            LOGV("stream malformed packet");
            LOGV("stream malformed packet");
            return;
        }
        int offset = 12 + ((buffer[0] & 0x0F) << 2);
        if ((buffer[0] & 0x10) != 0) {
            offset += 4 + (ntohs(*(uint16_t *)&buffer[offset + 2]) << 2);
        }
        if ((buffer[0] & 0x20) != 0) {
            length -= buffer[length - 1];
        }
        length -= offset;
        if (length >= 0) {
            length = mCodec->decode(samples, &buffer[offset], length);
        }
    }
    if (length <= 0) {
        LOGV("stream decoder error");
        return;
    }

    if (tick - mBufferTail > 0) {
        // Buffer underrun. Reset the jitter buffer.
        LOGV("stream buffer underrun");
        if (mBufferTail - mBufferHead <= 0) {
            mBufferHead = tick + mInterval;
            mBufferTail = mBufferHead;
        } else {
            int tail = (tick + mInterval) * mSampleRate;
            for (int i = mBufferTail * mSampleRate; i - tail < 0; ++i) {
                mBuffer[i & mBufferMask] = 0;
            }
            mBufferTail = tick + mInterval;
        }
    }

    // Append to the jitter buffer.
    int tail = mBufferTail * mSampleRate;
    for (int i = 0; i < mSampleCount; ++i) {
        mBuffer[tail & mBufferMask] = samples[i];
        ++tail;
    }
    mBufferTail += mInterval;
}

void initRandom() {
    gRandom = open("/dev/urandom", O_RDONLY);
    if (gRandom == -1) {
        LOGE("urandom: %s", strerror(errno));
    }
}

} // namespace
