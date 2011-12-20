package com.android.ims.core.media.player;

import android.content.Context;

import javax.microedition.ims.core.media.MediaException;
import javax.microedition.ims.core.media.StreamMedia;
import javax.microedition.ims.media.PlayerExt;
import javax.microedition.ims.media.PlayerListener;
import java.util.ArrayList;
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

    private List<PlayerListener> mListeners = new ArrayList<PlayerListener>();

    public void updateAuthKey(String authKey) {
        mAuthKey = authKey;
    }

    public String getAuthKey() {
        return mAuthKey;
    }

    public void prefetch() throws MediaException {
        if (mState < REALIZED) {
            realize();
        }
        if (mState < PREFETCHED) {
            mState = PREFETCHED;
        }
    }

    public void deallocate() {
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

    public void addPlayerListener(PlayerListener playerListener) {
        checkClosed(false);
        mListeners.add(playerListener);
    }

    public void removePlayerListener(PlayerListener playerListener) {
        checkClosed(false);
        mListeners.remove(playerListener);
    }

    public int getChannel() {
        return mChannel;
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

        for (PlayerListener listener : mListeners) {
            listener.playerUpdate(this, event, eventData);
        }
    }

}
