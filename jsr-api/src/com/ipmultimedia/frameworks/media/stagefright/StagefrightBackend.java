package com.ipmultimedia.frameworks.media.stagefright;

public class StagefrightBackend {

    static {
        System.loadLibrary("Srtp");
        System.loadLibrary("Ortp");
        System.loadLibrary("StagefrightPlayerBackend");
    }

    public StagefrightBackend() {
    }

    public native int createSession(boolean isReceiving);

    private synchronized native void close(int session, int nativeStream);

    public void do_close(int session, int nativeStream) {
        close(session, nativeStream);
    }

    public native boolean setSource(String host, int port, boolean isAudioDevice, int session);
    public native int setAudioCodec(String codec, boolean isReceiving, int session);
    public native int setVideoCodec(int codec, boolean isReceiving, int session);
    public native boolean setDestination(String host, String ssrc, int port, boolean isAudioDevice, int session);
    public native boolean enableSrtp(boolean isReceiving, /*int cipherLen, int authKeyLen,
            int authTagLen, */byte[] key, int session);
    public native void disableSrtp(boolean isReceiving, int session);

    public native boolean start(boolean isReceiving, boolean isAudioDevice, boolean isSrtp, int session, int nativeStream);
    public native boolean stop(boolean isReceiving, boolean isAudioDevice, int session, int nativeStream);
    public native void setDtmfPayload(int payload, int session, int nativeStream);
    public native void sendDtmf(int c, boolean outBand, int session, int nativeStream);
    public native void startDtmf(int c, boolean outBand, int session, int nativeStream);
    public native void stopDtmf(int session, int nativeStream);
}
