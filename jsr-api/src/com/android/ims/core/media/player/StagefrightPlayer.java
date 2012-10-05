/*
 * This software code is (c) 2010 T-Mobile USA, Inc. All Rights Reserved.
 *
 * Unauthorized redistribution or further use of this material is
 * prohibited without the express permission of T-Mobile USA, Inc. and
 * will be prosecuted to the fullest extent of the law.
 *
 * Removal or modification of these Terms and Conditions from the source
 * or binary code of this software is prohibited.  In the event that
 * redistribution of the source or binary code for this software is
 * approved by T-Mobile USA, Inc., these Terms and Conditions and the
 * above copyright notice must be reproduced in their entirety and in all
 * circumstances.
 *
 * No name or trademarks of T-Mobile USA, Inc., or of its parent company,
 * Deutsche Telekom AG or any Deutsche Telekom or T-Mobile entity, may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" AND "WITH ALL FAULTS" BASIS
 * AND WITHOUT WARRANTIES OF ANY KIND.  ALL EXPRESS OR IMPLIED
 * CONDITIONS, REPRESENTATIONS OR WARRANTIES, INCLUDING ANY IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT CONCERNING THIS SOFTWARE, ITS SOURCE OR BINARY CODE
 * OR ANY DERIVATIVES THEREOF ARE HEREBY EXCLUDED.  T-MOBILE USA, INC.
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE
 * OR ITS DERIVATIVES.  IN NO EVENT WILL T-MOBILE USA, INC. OR ITS
 * LICENSORS BE LIABLE FOR LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES,
 * HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT
 * OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF T-MOBILE USA,
 * INC. HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * THESE TERMS AND CONDITIONS APPLY SOLELY AND EXCLUSIVELY TO THE USE,
 * MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE, ITS SOURCE OR BINARY
 * CODE OR ANY DERIVATIVES THEREOF, AND ARE SEPARATE FROM ANY WRITTEN
 * WARRANTY THAT MAY BE PROVIDED WITH A DEVICE YOU PURCHASE FROM T-MOBILE
 * USA, INC., AND TO THE EXTENT PERMITTED BY LAW.
 */
package com.android.ims.core.media.player;

import android.content.Context;
import android.net.rtp.AudioCodec;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceView;
import com.android.ims.core.media.MediaDescriptorImpl;
import com.ipmultimedia.frameworks.media.stagefright.StagefrightBackend;

import javax.microedition.ims.core.media.MediaException;
import javax.microedition.ims.core.media.StreamMedia;
import javax.microedition.ims.media.*;

public class StagefrightPlayer extends BasePlayerImpl {

    private static final String TAG = "StagefrightPlayer";

    private StagefrightBackend mBackend;
    private AudioCodec mAudioCodec;
    private int mVideoCodec;
    private MediaDescriptorImpl mLocal;
    private MediaDescriptorImpl mRemote;
    private int mNativeStream = 0;

    public StagefrightPlayer(Context context, PlayerType playerType, StreamMedia streamMedia,
                             MediaDescriptorImpl localDescriptor, MediaDescriptorImpl remoteDescriptor,
                             AudioCodec codec, int channel,
                             String authKey, int dtmfPayload) throws MediaException {
        this(context, playerType, streamMedia, localDescriptor, remoteDescriptor,
                codec, -1, channel, authKey, dtmfPayload);
    }

    public void putOnHold(boolean onHold) {}
    public void putOnHold(boolean onHold, int direction) {}
    public boolean isOnHold() { return false; }

    public StagefrightPlayer(Context context, PlayerType playerType, StreamMedia streamMedia,
                             MediaDescriptorImpl localDescriptor, MediaDescriptorImpl remoteDescriptor,
                             int vdeoCodec, int channel, String authKey) throws MediaException {
        this(context, playerType, streamMedia, localDescriptor, remoteDescriptor,
                null, vdeoCodec, channel, authKey, -1);
    }

    private StagefrightPlayer(Context context, PlayerType playerType, StreamMedia streamMedia,
                              MediaDescriptorImpl localDescriptor, MediaDescriptorImpl remoteDescriptor,
                              AudioCodec auidoCodec, int videoCodec, int channel,
                              String authKey, int dtmfPayload) throws MediaException {
        mContext = context;
        mAudioCodec = auidoCodec;
        mVideoCodec = videoCodec;
        mDtmfPayload = dtmfPayload;
        mIsAudioPlayer = (auidoCodec != null);

        if (playerType == null) {
            throw new MediaException("Invalid player type");
        }
        mPlayerType = playerType;

        mLocal = localDescriptor;
        mRemote = remoteDescriptor;

        if (streamMedia == null) {
            throw new MediaException("Invalid streamMedia");
        }
        mStreamMedia = streamMedia;

        mAuthKey = authKey;
        mChannel = channel;
    }

    public void realize() throws MediaException {
        checkClosed(false);

        if (mState >= REALIZED) {
            return;
        }
        if (mBackend != null) {
            close();
        }

        mBackend = new StagefrightBackend();

        if (mPlayerType == PlayerType.PLAYER_RECEIVING || mChannel == -1) {
            mChannel = mBackend.createSession(mPlayerType == PlayerType.PLAYER_RECEIVING);
            if (mChannel == 0) {
                // Failed to create RTP/RTCP session
                mChannel = -1;
                throw new MediaException("Failed to create RTP/RTCP session");
            }
        }

        mState = REALIZED;

        if (mIsAudioPlayer) {
            String codecSpec = String.format("%d %s %s",
                    mAudioCodec.type, mAudioCodec.rtpmap, mAudioCodec.fmtp);
            mNativeStream = mBackend.setAudioCodec(codecSpec, mPlayerType == PlayerType.PLAYER_RECEIVING, mChannel);
        } else {
            mNativeStream = mBackend.setVideoCodec(mVideoCodec, mPlayerType == PlayerType.PLAYER_RECEIVING, mChannel);
        }
        if (mPlayerType == PlayerType.PLAYER_RECEIVING) {
            mBackend.setSource(mLocal.getConnectionAddress(), mLocal.getPort(), mIsAudioPlayer, mChannel);
        } else if (mPlayerType == PlayerType.PLAYER_SENDING) {
            if (mIsAudioPlayer) {
                if (mDtmfPayload > 0) {
                    mBackend.setDtmfPayload(mDtmfPayload, mChannel, mNativeStream);
                }
            }
            mBackend.setDestination(mRemote.getConnectionAddress(), null, mRemote.getPort(), mIsAudioPlayer, mChannel);
        }
    }

    public void start() throws MediaException {
        if (mPlayerType == PlayerType.PLAYER_RECEIVING && !mStreamMedia.canRead()) {
            throw new MediaException("StreamMedia could not be read");
        } else if (mPlayerType == PlayerType.PLAYER_SENDING && !mStreamMedia.canWrite()) {
            throw new MediaException("StreamMedia could not be written");
        }

        if (mState >= STARTED) {
            return;
        }

        if (mState < REALIZED) {
            realize();
        }

        if (mState < PREFETCHED) {
            prefetch();
        }

        if (mBackend != null) {
            boolean isSrtpEnabled = false;
            if (mAuthKey != null) {
                byte[] key = Base64.decode(mAuthKey.getBytes(), Base64.DEFAULT);
//                int authKeyLength = 4;
                isSrtpEnabled = mBackend.enableSrtp(mPlayerType == PlayerType.PLAYER_RECEIVING,
                        /*30, 20, authKeyLength, */key, mChannel);
                Log.i(TAG, "enableStrp: " + isSrtpEnabled);
            }
            mBackend.start(mPlayerType == PlayerType.PLAYER_RECEIVING, mIsAudioPlayer, isSrtpEnabled,
                    mChannel, mNativeStream);
        }
        mState = STARTED;
    }

    public void stop() throws MediaException {
        checkClosed(false);

        if (mState < STARTED) {
            return;
        }

        if (mBackend != null) {
            mBackend.stop(mPlayerType == PlayerType.PLAYER_RECEIVING, mIsAudioPlayer, mChannel, mNativeStream);
            if (mAuthKey != null) {
                mBackend.disableSrtp(mPlayerType == PlayerType.PLAYER_RECEIVING, mChannel);
            }
        }

        mState = PREFETCHED;
    }

    public void close() {
        if (mState == CLOSED) {
            return;
        }
        if (mState == STARTED) {
            try {
                stop();
            } catch (MediaException e) {
                Log.e(TAG, "Exception in close", e);
            }
        }

        if (mBackend != null) {
            mBackend.do_close(mChannel, mNativeStream);
            mBackend = null;
        }
        mChannel = -1;
        mState = CLOSED;

        sendEvent(PlayerListener.CLOSED, null);
    }


    public void sendDtmf(char c) {
        if (mBackend != null) {
            mBackend.sendDtmf(sDtmfMap.get(c), mDtmfPayload > 0, mChannel, mNativeStream);
        }
    }

    public void startDtmf(char c) {
        if (mBackend != null) {
            mBackend.startDtmf(sDtmfMap.get(c), mDtmfPayload > 0, mChannel, mNativeStream);
        }
    }

    public void stopDtmf() {
        if (mBackend != null) {
            mBackend.stopDtmf(mChannel, mNativeStream);
        }
    }

    public void setSurfaces(SurfaceView local, SurfaceView remote, int channel) {
        // TODO: Implement me
    }

    public void setCallback(int channel, IVideoCallback callback) {
        // TODO: Implement me
    }

    public void resetVideoCodec(int codec, int size, int channel) {
        // TODO: Implement me
    }
}
