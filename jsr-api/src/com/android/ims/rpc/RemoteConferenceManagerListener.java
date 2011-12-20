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

package com.android.ims.rpc;

import android.os.RemoteException;
import android.util.Log;
import com.android.ims.ReasonInfoImpl;

import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.msrp.*;
import javax.microedition.ims.im.ConferenceManagerListener;
import java.util.ArrayList;
import java.util.List;

public class RemoteConferenceManagerListener extends IConferenceManagerListener.Stub {
    private static final String TAG = "RemoteConferenceManagerListener";

    private final List<ConferenceManagerListener> listeners = new ArrayList<ConferenceManagerListener>();
    private final com.android.ims.im.IMServiceImpl imServiceImpl;

    public RemoteConferenceManagerListener(final com.android.ims.im.IMServiceImpl imServiceImpl) {
        this.imServiceImpl = imServiceImpl;
    }

    public void addListener(ConferenceManagerListener listener) {
        Log.i(TAG, "addListener#");
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(ConferenceManagerListener listener) {
        Log.i(TAG, "removeListener#");
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    
    public void chatInvitationReceived(IChatInvitation chatInvitation) throws RemoteException {
        Log.i(TAG, "chatInvitationReceived#started");

        com.android.ims.im.ChatInvitationImpl chatInvitationImpl = new com.android.ims.im.ChatInvitationImpl(chatInvitation, imServiceImpl);

        for (ConferenceManagerListener listener : listeners) {
            Log.i(TAG, "chatInvitationReceived#listener notify");
            listener.chatInvitationReceived(chatInvitationImpl);
        }
        Log.i(TAG, "chatInvitationReceived#finish");
    }

    
    public void chatStarted(IChat chat) throws RemoteException {
        Log.i(TAG, "chatStarted#");

        com.android.ims.im.ChatImpl chatImpl = new com.android.ims.im.ChatImpl(chat, chat.getIMSession());

        for (ConferenceManagerListener listener : listeners) {
            listener.chatStarted(chatImpl);
        }
    }

    
    public void chatStartFailed(String sessionId, IReasonInfo reasonInfo) throws RemoteException {
        Log.i(TAG, "chatStartFailed#");

        ReasonInfoImpl reasonInfoImpl = new ReasonInfoImpl(reasonInfo);

        for (ConferenceManagerListener listener : listeners) {
            listener.chatStartFailed(sessionId, reasonInfoImpl);
        }
    }

    
    public void conferenceInvitationReceived(IConferenceInvitation conferenceInvitation) throws RemoteException {
        Log.i(TAG, "conferenceInvitationReceived#");

        com.android.ims.im.ConferenceInvitationImpl chatInvitationImpl = new com.android.ims.im.ConferenceInvitationImpl(conferenceInvitation, imServiceImpl);

        for (ConferenceManagerListener listener : listeners) {
            listener.conferenceInvitationReceived(chatInvitationImpl);
        }
    }

    
    public void conferenceStarted(IConference conference) throws RemoteException {
        Log.i(TAG, "conferenceStarted#");

        com.android.ims.im.ConferenceImpl conferenceImpl = new com.android.ims.im.ConferenceImpl(conference, conference.getIMSession());

        for (ConferenceManagerListener listener : listeners) {
            listener.conferenceStarted(conferenceImpl);
        }
    }

    
    public void conferenceStartFailed(String sessionId, IReasonInfo reasonInfo) throws RemoteException {
        Log.i(TAG, "conferenceStartFailed#");

        ReasonInfoImpl reasonInfoImpl = new ReasonInfoImpl(reasonInfo);

        for (ConferenceManagerListener listener : listeners) {
            listener.conferenceStartFailed(sessionId, reasonInfoImpl);
        }
    }

}
