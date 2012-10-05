package com.android.ims.core.media.player;

import android.content.Context;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.rtp.RtpStream;
import android.view.SurfaceView;
import android.util.Base64;
import android.util.Log;
import com.android.ims.core.media.MediaDescriptorImpl;
import com.ipmultimedia.frameworks.media.stagefright.StagefrightBackend;

import java.net.InetAddress;
import javax.microedition.ims.core.media.MediaException;
import javax.microedition.ims.core.media.StreamMedia;
import javax.microedition.ims.media.*;


public class AndroidVoipPlayer extends BasePlayerImpl {
    private static final boolean DEBUG = true;
    private static final String TAG = "AndroidVoipPlayer";

    private AudioCodec mCodec;
    private AudioGroup mAudioGroup = null;
    private AudioStream mAudioStream;
    private MediaDescriptorImpl mRemote;
    private MediaDescriptorImpl mLocal;

    public AndroidVoipPlayer(Context context, PlayerType playerType, StreamMedia streamMedia,
             MediaDescriptorImpl localDescriptor, MediaDescriptorImpl remoteDescriptor,
             AudioCodec codec, int channel, String authKey,
             int dtmfPayload) throws MediaException {
        this(context, playerType, streamMedia, localDescriptor, remoteDescriptor,
                codec, -1, channel, authKey, dtmfPayload);
    }

    public AndroidVoipPlayer(Context context, PlayerType playerType, StreamMedia streamMedia,
            MediaDescriptorImpl localDescriptor, MediaDescriptorImpl remoteDescriptor,
            int vdeoCodec, int channel, String authKey) throws MediaException {
        this(context, playerType, streamMedia, localDescriptor, remoteDescriptor,
                null, vdeoCodec, channel, authKey, -1);
    }

    private AndroidVoipPlayer(Context context, PlayerType playerType, StreamMedia streamMedia,
            MediaDescriptorImpl localDescriptor, MediaDescriptorImpl remoteDescriptor,
            AudioCodec auidoCodec, int videoCodec, int channel,
            String authKey, int dtmfPayload) throws MediaException {
        mContext = context;
        mCodec = auidoCodec;
        //mVideoCodec = videoCodec;
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
        if(mRemote == null) return;
        checkClosed(false);

        if (mState >= REALIZED) {
            return;
        }

        if (mAudioStream != null) {
            close();
        }

        logd("mLocal.getConnectionAddress() = " + mLocal.getConnectionAddress());
        try{
            mAudioStream = new AudioStream(InetAddress.getByName(mLocal.getConnectionAddress()));
        }catch ( Exception e) {
            Log.e(TAG, "unknown host " + mLocal.getConnectionAddress());
        }
        mCodec = AudioCodec.PCMU; // Now we only support PCMU
        mAudioStream.setCodec(mCodec);

        mState = REALIZED;

        if (mIsAudioPlayer ) {

            if (mRemote != null) {

            logd("AudioPlayer: " + mRemote.getConnectionAddress() + ":" + mRemote.getPort());
            try {
                mAudioStream.associate(InetAddress.getByName(mRemote.getConnectionAddress()), mRemote.getPort());
            }catch ( java.net.UnknownHostException e) {
                Log.e(TAG, "unknown host " + e);
            }
            }
        } else {
            // TODO: Implement Me
            throw new MediaException("Video player does not support yet!");
        }

        if (mPlayerType == PlayerType.PLAYER_RECEIVING) {
            //mAudioStream.setMode(RtpStream.MODE_RECEIVE_ONLY);
        } else if (mPlayerType == PlayerType.PLAYER_SENDING) {
            if (mDtmfPayload > 0) {
                mAudioStream.setDtmfType(mDtmfPayload);
            }
            //mAudioStream.setMode(RtpStream.MODE_SEND_ONLY);
        }
        mAudioStream.setMode(RtpStream.MODE_NORMAL);

        //mAudioGroup = new AudioGroup();
        if (mAudioGroup == null) {
            mAudioGroup = new AudioGroup();
        }
    }

    public void start() throws MediaException {
        if(mRemote == null) return;
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

        mAudioStream.join(mAudioGroup);
        mAudioGroup.setMode(AudioGroup.MODE_NORMAL);

        mState = STARTED;
    }

    public void stop() throws MediaException {
        if(mRemote == null) return;
        checkClosed(false);

        if (mState < STARTED) {
            return;
        }

        if (mAudioStream != null) {
            mAudioStream.join(null);
        }

        mState = PREFETCHED;
    }

    public void putOnHold(boolean onHold) {
        if(mRemote == null) return;
        // TODO: Implement Me
    }

    public void putOnHold(boolean onHold, int direction) {
        if(mRemote == null) return;
        // TODO: Implement Me
    }

    public boolean isOnHold() {
        // TODO: Implement Me
        return false;
    }

    public void close() {
        if(mRemote == null) return;
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

        if (mAudioGroup != null) {
            mAudioGroup.clear();
            mAudioGroup = null;
        }

        if (mAudioStream != null) {
            mAudioStream.release();
            mAudioStream = null;
        }

        mChannel = -1;
        mState = CLOSED;

        sendEvent(PlayerListener.CLOSED, null);
    }

    public void sendDtmf(char c) {
        if(mRemote == null) return;
        // TODO: Implement Me
    }

    public void startDtmf(char c) {
        if(mRemote == null) return;
        // TODO: Implement Me
    }

    public void stopDtmf() {
        if(mRemote == null) return;
        // TODO: Implement Me
    }

    public void setSurfaces(SurfaceView local, SurfaceView remote, int channel) {
        if(mRemote == null) return;
        // TODO: Implement Me
    }

    public void setCallback(int channel, IVideoCallback callback) {
        if(mRemote == null) return;
        // TODO: Implement Me
    }

    public void resetVideoCodec(int codec, int size, int channel) {
        if(mRemote == null) return;
        // TODO: Implement Me
    }

    protected void checkClosed(boolean unrealized) {
        if(mRemote == null) return;
        if (mState == CLOSED || (unrealized && mState == UNREALIZED)) {
                throw new IllegalStateException("Can't invoke the method at the " +
                (mState == CLOSED ? "closed" : "unrealized") + " state");
        }
    }

    private void logd(String messgae) {
        if (DEBUG) {
            Log.d(TAG, messgae);
        }
    }
}
