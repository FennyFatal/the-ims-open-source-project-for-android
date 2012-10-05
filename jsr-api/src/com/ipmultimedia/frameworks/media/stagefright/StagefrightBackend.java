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
