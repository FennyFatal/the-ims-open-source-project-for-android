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

package com.android.ims.im;

import android.os.RemoteException;
import android.util.Log;
import com.android.ims.core.media.util.UtilsMSRP;
import com.android.ims.rpc.RemoteChatListener;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.android.msrp.IChat;
import javax.microedition.ims.android.msrp.IIMSession;
import javax.microedition.ims.im.Chat;
import javax.microedition.ims.im.ChatListener;

/**
 * Implementation for Chat interface.
 */
public class ChatImpl extends IMSessionImpl implements Chat {
    protected final String TAG = "JSR - ChatImpl";
    
    private final IChat mChat;
    

    private ChatListener mCurrentChatListener;
    private final RemoteChatListener remoteChatListener;
    
    
    public ChatImpl(IChat mChat, IIMSession mIMSession) {
        super(mIMSession);
        this.mChat = mChat;
        
        
        this.remoteChatListener = new RemoteChatListener();
        try {
            mChat.addListener(remoteChatListener);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        
    }

    /**
     * @see Chat#extendToConference(java.lang.String[])
     */
    
    public void extendToConference(String[] additionalParticipants) throws ImsException {
        if (isClosed()) {
            throw new IllegalStateException("The Chat is closed.");
        }
        if (additionalParticipants == null || additionalParticipants.length == 0) {
            throw new IllegalArgumentException("The additionalParticipants argument is null or an empty array");
        }
        for (String additionalParticipant : additionalParticipants) {
            if (!UtilsMSRP.isValidUserIdentity(additionalParticipant)) {
                throw new IllegalArgumentException("The additionalParticipants argument contains invalid user identities");
            }
        }
        if (additionalParticipants.length+1 > UtilsMSRP.MAX_CONFERENCE_PARTICIPANTS) {
            throw new IllegalArgumentException("The total number of participants including the original participants exceeds the maximum allowed participants in a conference");
        }
        
        try {
            mChat.extendToConference(additionalParticipants);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException("The request could not be sent to the network, message = " + e.getMessage(), e);
        }
    }

    /**
     * @see Chat#setListener(ChatListener)
     */
    
    public void setListener(ChatListener listener) {
        super.setListener(listener);
        
        if (mCurrentChatListener != null) {
            remoteChatListener.removeListener(mCurrentChatListener);
        }

        if (listener != null) {
            remoteChatListener.addListener(listener);
        }
        mCurrentChatListener = listener;
    }

}
