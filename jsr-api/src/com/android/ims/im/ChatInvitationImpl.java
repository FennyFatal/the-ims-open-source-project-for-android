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

import javax.microedition.ims.android.msrp.IChatInvitation;
import javax.microedition.ims.im.ChatInvitation;

public class ChatInvitationImpl implements ChatInvitation {
    private static final String TAG = "JSR - ChatInvitationImpl";
    
    private final IChatInvitation mChatInvitation;
    private final IMServiceImpl imServiceImpl;

    public ChatInvitationImpl(IChatInvitation mChatInvitation, IMServiceImpl imServiceImpl) {
        this.mChatInvitation = mChatInvitation;
        this.imServiceImpl = imServiceImpl;
    }

    /**
     * @see javax.microedition.ims.im.ChatInvitation#accept()
     */
    
    public void accept() {
        Log.i(TAG, "accept#started");

        if (!imServiceImpl.isOpen()) {
            throw new IllegalStateException("This invitation has expired");
        }
        if (isExpired()) {
            throw new IllegalStateException("This invitation has expired");
        }
        
        try {
            mChatInvitation.accept();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "accept#finish");
    }

    /**
     * @see javax.microedition.ims.im.ChatInvitation#reject()
     */
    
    public void reject() {
        Log.i(TAG, "reject#started");
        
        if (!imServiceImpl.isOpen()) {
            throw new IllegalStateException("This invitation has expired");
        }
        if (isExpired()) {
            throw new IllegalStateException("This invitation has expired");
        }
        
        try {
            mChatInvitation.reject();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        
        Log.i(TAG, "reject#finished");
    }

    /**
     * @see javax.microedition.ims.im.ChatInvitation#getSender()
     */
    
    public String getSender() {
        String sender = null;
        try {
            sender = mChatInvitation.getSender();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return sender;
    }

    /**
     * @see javax.microedition.ims.im.ChatInvitation#getSessionId()
     */
    
    public String getSessionId() {
        String sessionId = null;
        try {
            sessionId = mChatInvitation.getSessionId();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return sessionId;
    }

    /**
     * @see javax.microedition.ims.im.ChatInvitation#getSubject()
     */
    
    public String getSubject() {
        String subject = null;
        try {
            subject = mChatInvitation.getSubject();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return subject;
    }
    
    private boolean isExpired() {
        boolean expired = false;
        try {
            expired = mChatInvitation.isExpired();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return expired;
    }

}
