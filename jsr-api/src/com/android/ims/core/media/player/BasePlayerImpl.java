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

import javax.microedition.ims.core.media.MediaException;
import javax.microedition.ims.core.media.StreamMedia;
import javax.microedition.ims.media.PlayerExt;
import javax.microedition.ims.media.PlayerListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class BasePlayerImpl implements PlayerExt {

    protected static final HashMap<Character, Integer> sDtmfMap = new HashMap<Character, Integer>();
    static {
        sDtmfMap.put('1', 1);
        sDtmfMap.put('2', 2);
        sDtmfMap.put('3', 3);
        sDtmfMap.put('4', 4);
        sDtmfMap.put('5', 5);
        sDtmfMap.put('6', 6);
        sDtmfMap.put('7', 7);
        sDtmfMap.put('8', 8);
        sDtmfMap.put('9', 9);
        sDtmfMap.put('0', 0);
        sDtmfMap.put('*', 10);
        sDtmfMap.put('#', 11);
        sDtmfMap.put('A', 12);
        sDtmfMap.put('B', 13);
        sDtmfMap.put('C', 14);
        sDtmfMap.put('D', 15);
        sDtmfMap.put('a', 12);
        sDtmfMap.put('b', 13);
        sDtmfMap.put('c', 14);
        sDtmfMap.put('d', 15);
    }

    protected boolean mIsAudioPlayer;
    protected PlayerType mPlayerType;
    protected StreamMedia mStreamMedia;

    protected String mAuthKey;
    protected int mChannel;
    protected int mDtmfPayload;
    protected Context mContext;
    protected int mState = UNREALIZED;

    private List<PlayerListener> mListeners = Collections.synchronizedList(new ArrayList<PlayerListener>());

    @Override
    public synchronized void updateAuthKey(String authKey) {
        mAuthKey = authKey;
    }

    @Override
    public String getAuthKey() {
        return mAuthKey;
    }

    public synchronized void prefetch() throws MediaException {
        if (mState < REALIZED) {
            realize();
        }
        if (mState < PREFETCHED) {
            mState = PREFETCHED;
        }
    }

    public synchronized void deallocate() {
        checkClosed(false);
        if (mState < PREFETCHED) {
            return;
        }

        if (mState == STARTED) {
            close();
        }

        mState = REALIZED;
    }

    public int getState() {
        return mState;
    }

    public String getContentType() {
        return null;
    }

    @Override
    public void addPlayerListener(PlayerListener playerListener) {
        checkClosed(false);
        mListeners.add(playerListener);
    }

    @Override
    public void removePlayerListener(PlayerListener playerListener) {
        checkClosed(false);
        mListeners.remove(playerListener);
    }

    public int getChannel() {
        return mChannel;
    }

    public String getUri() {
        return null;
    }

    public int getCodec() {
        return -1;
    }

    public int getRtp() {
        return -1;
    }

    protected void checkClosed(boolean unrealized) {
        if (mState == CLOSED || (unrealized && mState == UNREALIZED)) {
            throw new IllegalStateException("Can't invoke the method at the " +
                    (mState == CLOSED ? "closed" : "unrealized") +
            " state");
        }
    }

    protected void sendEvent(String event, Object eventData) {
        if (mListeners.size() == 0 &&
            !event.equals(PlayerListener.END_OF_MEDIA) &&
            !event.equals(PlayerListener.CLOSED) &&
            !event.equals(PlayerListener.ERROR)) {
            return;
        }

     List<PlayerListener> listenersCopy = new ArrayList<PlayerListener>(mListeners);
         for (PlayerListener listener : listenersCopy) {
            listener.playerUpdate(this, event, eventData);
        }
    }
}
