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
import com.android.ims.im.LargeMessageRequestImpl;
import com.android.ims.im.MessageHelper;

import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.msrp.ILargeMessageRequest;
import javax.microedition.ims.android.msrp.IMessage;
import javax.microedition.ims.android.msrp.IMessageManagerListener;
import javax.microedition.ims.im.Message;
import javax.microedition.ims.im.MessageManagerListener;
import java.util.ArrayList;
import java.util.List;

public class RemoteMessageManagerListener extends IMessageManagerListener.Stub {
    private static final String TAG = "RemoteMessageManagerListener";

    private final List<MessageManagerListener> listeners = new ArrayList<MessageManagerListener>();

    public RemoteMessageManagerListener() {
    }

    public void addListener(MessageManagerListener listener) {
        Log.i(TAG, "addListener#");
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(MessageManagerListener listener) {
        Log.i(TAG, "removeListener#");
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    
    public void incomingLargeMessage(ILargeMessageRequest largeMessageRequest) throws RemoteException {
        Log.i(TAG, "incomingLargeMessage#");
        
        LargeMessageRequestImpl largeMessageRequestImpl = new LargeMessageRequestImpl(largeMessageRequest);
        
        for (MessageManagerListener listener : listeners) {
            listener.incomingLargeMessage(largeMessageRequestImpl);
        }
    }

    
    public void messageReceived(IMessage message) throws RemoteException {
        Log.i(TAG, "messageReceived#");
        
        Message messageImpl = MessageHelper.convertToMessage(message);
        
        for (MessageManagerListener listener : listeners) {
            listener.messageReceived(messageImpl);
        }
    }

    
    public void messageReceiveFailed(String messageId, IReasonInfo reasonInfo) throws RemoteException {
        Log.i(TAG, "messageReceiveFailed#");
        
        ReasonInfoImpl reasonInfoImpl = new ReasonInfoImpl(reasonInfo);
        
        for (MessageManagerListener listener : listeners) {
            listener.messageReceiveFailed(messageId, reasonInfoImpl);
        }
    }

    
    public void messageSendFailed(String messageId, IReasonInfo reasonInfo) throws RemoteException {
        Log.i(TAG, "messageSendFailed#");
        
        ReasonInfoImpl reasonInfoImpl = new ReasonInfoImpl(reasonInfo);
        
        for (MessageManagerListener listener : listeners) {
            listener.messageSendFailed(messageId, reasonInfoImpl);
        }
    }

    
    public void messageSent(String messageId) throws RemoteException {
        Log.i(TAG, "messageSent#");
        
        for (MessageManagerListener listener : listeners) {
            listener.messageSent(messageId);
        }
    }

    
    public void transferProgress(String messageId, int bytesTransferred, int bytesTotal) throws RemoteException {
        Log.i(TAG, "transferProgress#");
        
        for (MessageManagerListener listener : listeners) {
            listener.transferProgress(messageId, bytesTransferred, bytesTotal);
        }
    }
    
}
