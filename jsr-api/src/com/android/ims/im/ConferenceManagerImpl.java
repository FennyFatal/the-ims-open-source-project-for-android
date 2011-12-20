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
import com.android.ims.rpc.RemoteConferenceManagerListener;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.android.msrp.IConferenceManager;
import javax.microedition.ims.im.ConferenceManager;
import javax.microedition.ims.im.ConferenceManagerListener;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation for ConferenceManager interface.
 */
public class ConferenceManagerImpl implements ConferenceManager {
    protected final String TAG = "JSR - ConferenceManagerImpl";
    
    private final IMServiceImpl imServiceImpl;
    private final IConferenceManager mConferenceManager;
    
    private final Set<String> sessionIds = new HashSet<String>();
    
    
    private ConferenceManagerListener mCurrentConferenceManagerListener;
    private final RemoteConferenceManagerListener remoteConferenceManagerListener;

    
    public ConferenceManagerImpl(IMServiceImpl imServiceImpl, IConferenceManager mConferenceManager) {
        this.imServiceImpl = imServiceImpl;
        this.mConferenceManager = mConferenceManager;
        
        
        this.remoteConferenceManagerListener = new RemoteConferenceManagerListener(imServiceImpl);
        try {
            mConferenceManager.addListener(remoteConferenceManagerListener);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        
    }
    
    /**
     * @see javax.microedition.ims.im.ConferenceManager#sendChatInvitation(java.lang.String, java.lang.String, java.lang.String)
     */
    
    public String sendChatInvitation(String sender, String recipient, String subject) throws ServiceClosedException, ImsException {
        String sessionId;
        Log.i(TAG, "sendChatInvitation#started");
        
        if (imServiceImpl.isClosed()) {
            throw new ServiceClosedException("Service is closed.");
        }
        if (sender != null && !UtilsMSRP.isValidUserIdentity(sender)) {
            throw new IllegalArgumentException("The sender argument is not null and not valid user identity");
        }
        if (!UtilsMSRP.isValidUserIdentity(recipient)) {
            throw new IllegalArgumentException("The recipient argument is not a valid user identity");
        }

        try {
            sessionId = mConferenceManager.sendChatInvitation(sender, recipient, subject);
            sessionIds.add(sessionId);
            
            Log.i(TAG, "sendChatInvitation#sent");
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException("The chat invitation could not be sent to the network. Error : " + e.getMessage(), e);
        }
        
        Log.i(TAG, "sendChatInvitation#finished");
        return sessionId;
    }

    /**
     * @see javax.microedition.ims.im.ConferenceManager#cancelInvitation(java.lang.String)
     */
    
    public void cancelInvitation(String sessionId) throws ServiceClosedException {
        Log.i(TAG, "cancelInvitation#started");
        
        if (imServiceImpl.isClosed()) {
            throw new ServiceClosedException("Service is closed.");
        }
        if (!sessionIds.contains(sessionId)) {
            throw new IllegalArgumentException("There is no pending invitation with the given identifier sent by the local user");
        }
        
        try {
            mConferenceManager.cancelInvitation(sessionId);
            Log.i(TAG, "cancelInvitation#sent");
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        
        Log.i(TAG, "cancelInvitation#finished");
    }

    /**
     * @see javax.microedition.ims.im.ConferenceManager#getMaxAllowedParticipants()
     */
    
    public int getMaxAllowedParticipants() {
        return UtilsMSRP.MAX_CONFERENCE_PARTICIPANTS;
    }

    /**
     * @see javax.microedition.ims.im.ConferenceManager#joinPredefinedConference(java.lang.String, java.lang.String)
     */
    
    public String joinPredefinedConference(String sender, String conferenceURI) throws ServiceClosedException, ImsException {
        String sessionId;
        Log.i(TAG, "joinPredefinedConference#started");
        
        if (imServiceImpl.isClosed()) {
            throw new ServiceClosedException("Service is closed.");
        }
        if (sender != null && !UtilsMSRP.isValidUserIdentity(sender)) {
            throw new IllegalArgumentException("The sender argument is not null and not valid user identity");
        }
        if (conferenceURI == null || conferenceURI.length() == 0) {
            throw new IllegalArgumentException("The conferenceURI argument is null or an empty string");
        }

        try {
            sessionId = mConferenceManager.joinPredefinedConference(sender, conferenceURI);
            sessionIds.add(sessionId);
            
            Log.i(TAG, "joinPredefinedConference#sent");
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException("The request to join a predefined conference could not be sent to the network. Error : " + e.getMessage(), e);
        }
        
        Log.i(TAG, "joinPredefinedConference#finished");
        return sessionId;
    }

    /**
     * @see javax.microedition.ims.im.ConferenceManager#sendConferenceInvitation(java.lang.String, java.lang.String[], java.lang.String)
     */
    
    public String sendConferenceInvitation(String sender, String[] recipients, String subject) throws ServiceClosedException, ImsException {
        String sessionId;
        Log.i(TAG, "sendConferenceInvitation#started");
        
        if (imServiceImpl.isClosed()) {
            throw new ServiceClosedException("Service is closed.");
        }
        if (sender != null && !UtilsMSRP.isValidUserIdentity(sender)) {
            throw new IllegalArgumentException("The sender argument is not null and not valid user identity");
        }
        if (recipients == null || recipients.length == 0) {
            throw new IllegalArgumentException("The recipients argument is null or an empty array");
        }
        for (String recipient : recipients) {
            if (!UtilsMSRP.isValidUserIdentity(recipient)) {
                throw new IllegalArgumentException("The recipients argument contains invalid user identities");
            }
        }
        if (recipients.length+1 > UtilsMSRP.MAX_CONFERENCE_PARTICIPANTS) {
            throw new IllegalArgumentException("The total number of participants including the sender exceeds the maximum allowed participants in a conference");
        }

        try {
            sessionId = mConferenceManager.sendConferenceInvitation(sender, recipients, subject);
            sessionIds.add(sessionId);
            
            Log.i(TAG, "sendConferenceInvitation#sent");
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException("The conference invitation could not be sent to the network. Error : " + e.getMessage(), e);
        }
        
        Log.i(TAG, "sendConferenceInvitation#finished");
        return sessionId;
    }

    /**
     * @see javax.microedition.ims.im.ConferenceManager#setListener(javax.microedition.ims.im.ConferenceManagerListener)
     */
    
    public void setListener(ConferenceManagerListener listener) {
        if (mCurrentConferenceManagerListener != null) {
            remoteConferenceManagerListener.removeListener(mCurrentConferenceManagerListener);
        }

        if (listener != null) {
            remoteConferenceManagerListener.addListener(listener);
        }
        mCurrentConferenceManagerListener = listener;
    }

}
