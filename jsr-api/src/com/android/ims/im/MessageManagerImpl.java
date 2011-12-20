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
import com.android.ims.rpc.RemoteMessageManagerListener;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.android.msrp.IMessage;
import javax.microedition.ims.android.msrp.IMessageManager;
import javax.microedition.ims.im.Message;
import javax.microedition.ims.im.MessageManager;
import javax.microedition.ims.im.MessageManagerListener;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation for MessageManager interface.
 */
public class MessageManagerImpl implements MessageManager {
    protected final String TAG = "JSR - MessageManagerImpl";
    
    private final IMServiceImpl imServiceImpl;
    private final IMessageManager mMessageManager;
    
    private final Set<String> largeMessageIds = new HashSet<String>();
    
    
    private MessageManagerListener mCurrentMessageManagerListener;
    private final RemoteMessageManagerListener remoteMessageManagerListener;

    
    public MessageManagerImpl(IMServiceImpl imServiceImpl, IMessageManager mMessageManager) {
        this.imServiceImpl = imServiceImpl;
        this.mMessageManager = mMessageManager;
        
        
        this.remoteMessageManagerListener = new RemoteMessageManagerListener();
        try {
            mMessageManager.addListener(remoteMessageManagerListener);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        
    }
    
    /**
     * @see javax.microedition.ims.im.MessageManager#sendMessage(javax.microedition.ims.im.Message, boolean)
     */
    
    public void sendMessage(Message message, boolean deliveryReport) throws ServiceClosedException, ImsException {
        Log.i(TAG, "sendMessage#started");
        
        if (imServiceImpl.isClosed()) {
            throw new ServiceClosedException("Service is closed.");
        }
        if (message == null) {
            throw new IllegalArgumentException("The message argument is null.");
        }
        if (message.getRecipients() == null || message.getRecipients().length == 0) {
            throw new IllegalArgumentException("There are no recipients specified in the message argument.");
        }
        if (message.getContentParts() == null || message.getContentParts().length == 0) {
            throw new IllegalArgumentException("There are no content parts specified in the message argument.");
        }
        
        IMessage iMessage = MessageHelper.convertToIMessage(message);
        
        try {
            mMessageManager.sendMessage(iMessage, deliveryReport);
            Log.i(TAG, "sendMessage#sent");
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException("Cann't send message. Error : " + e.getMessage(), e);
        }
        
        Log.i(TAG, "sendMessage#finished");
    }

    /**
     * @see javax.microedition.ims.im.MessageManager#sendLargeMessage(javax.microedition.ims.im.Message, boolean)
     */
    
    public void sendLargeMessage(Message message, boolean deliveryReport) throws ServiceClosedException, ImsException {
        Log.i(TAG, "sendLargeMessage#started");

        if (imServiceImpl.isClosed()) {
            throw new ServiceClosedException("Service is closed.");
        }
        if (message == null) {
            throw new IllegalArgumentException("The message argument is null.");
        }
        if (message.getRecipients() == null || message.getRecipients().length == 0) {
            throw new IllegalArgumentException("There are no recipients specified in the message argument.");
        }
        if (message.getContentParts() == null || message.getContentParts().length == 0) {
            throw new IllegalArgumentException("There are no content parts specified in the message argument.");
        }
        
        IMessage iMessage = MessageHelper.convertToIMessage(message);
        
        largeMessageIds.add(message.getMessageId());
        
        try {
            mMessageManager.sendLargeMessage(iMessage, deliveryReport);
            Log.i(TAG, "sendLargeMessage#sent");
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException("Cann't send large message. Error : " + e.getMessage(), e);
        }
        
        Log.i(TAG, "sendLargeMessage#finished");
    }

    /**
     * @see javax.microedition.ims.im.MessageManager#cancel(java.lang.String)
     */
    
    public void cancel(String messageId) {
        Log.i(TAG, "cancel#started");
        
        if (!largeMessageIds.contains(messageId)) {
            throw new IllegalArgumentException("There is no large message transfer associated with the messageId");
        }
        
        try {
            mMessageManager.cancel(messageId);
            Log.i(TAG, "cancel#sent");
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
//            throw new IllegalStateException("Cann't cancel large message. Error : " + e.getMessage());
        }
        
        Log.i(TAG, "cancel#finished");
    }

    /**
     * @see javax.microedition.ims.im.MessageManager#setListener(javax.microedition.ims.im.MessageManagerListener)
     */
    
    public void setListener(MessageManagerListener listener) {
        if (mCurrentMessageManagerListener != null) {
            remoteMessageManagerListener.removeListener(mCurrentMessageManagerListener);
        }

        if (listener != null) {
            remoteMessageManagerListener.addListener(listener);
        }
        mCurrentMessageManagerListener = listener;
    }

}
