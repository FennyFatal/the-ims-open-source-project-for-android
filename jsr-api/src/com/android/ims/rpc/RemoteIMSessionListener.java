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
import com.android.ims.im.ComposingIndicatorUtils;
import com.android.ims.im.ComposingIndicatorUtils.ComposingIndicatorInfo;
import com.android.ims.im.FilePushRequestImpl;
import com.android.ims.im.IMSessionImpl;
import com.android.ims.im.MessageHelper;

import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.msrp.IFilePushRequest;
import javax.microedition.ims.android.msrp.IIMSessionListener;
import javax.microedition.ims.android.msrp.IMessage;
import javax.microedition.ims.im.IMSessionListener;
import javax.microedition.ims.im.Message;
import java.util.ArrayList;
import java.util.List;

public class RemoteIMSessionListener extends IIMSessionListener.Stub {
    private static final String TAG = "RemoteIMSessionListener";
    
    private final List<IMSessionListener> listeners = new ArrayList<IMSessionListener>();
    
    private final IMSessionImpl imSessionImpl;
    

    public RemoteIMSessionListener(IMSessionImpl imSessionImpl) {
        this.imSessionImpl = imSessionImpl;
    }

    public void composingIndicatorReceived(String sender, String messageBody) throws RemoteException {
        Log.i(TAG, "composingIndicatorReceived#");
        
        ComposingIndicatorInfo indicatorInfo = ComposingIndicatorUtils.parseMessage(messageBody);
        
        for (IMSessionListener listener : listeners) {
            listener.composingIndicatorReceived(imSessionImpl, sender, indicatorInfo.getTimeout());
        }
    }

    public void fileReceived(String requestId, String fileId, String filePath) throws RemoteException {
        Log.i(TAG, "fileReceived#");
        
        for (IMSessionListener listener : listeners) {
            listener.fileReceived(imSessionImpl, requestId, fileId, filePath);
        }
    }

    public void fileReceiveFailed(String requestId, String fileId, IReasonInfo reason) throws RemoteException {
        Log.i(TAG, "fileReceiveFailed#");
        
        ReasonInfoImpl reasonInfoImpl = new ReasonInfoImpl(reason);
        
        for (IMSessionListener listener : listeners) {
            listener.fileReceiveFailed(imSessionImpl, requestId, fileId, reasonInfoImpl);
        }
    }

    public void fileSendFailed(String requestId, String fileId, IReasonInfo reason) throws RemoteException {
        Log.i(TAG, "fileSendFailed#");
        
        ReasonInfoImpl reasonInfoImpl = new ReasonInfoImpl(reason);
        
        for (IMSessionListener listener : listeners) {
            listener.fileSendFailed(imSessionImpl, requestId, fileId, reasonInfoImpl);
        }
    }

    public void fileSent(String requestId, String fileId) throws RemoteException {
        Log.i(TAG, "fileSent#");
        
        for (IMSessionListener listener : listeners) {
            listener.fileSent(imSessionImpl, requestId, fileId);
        }
    }

    public void fileTransferFailed(String requestId, IReasonInfo reason) throws RemoteException {
        Log.i(TAG, "fileTransferFailed#");
        
        ReasonInfoImpl reasonInfoImpl = new ReasonInfoImpl(reason);
        
        for (IMSessionListener listener : listeners) {
            listener.fileTransferFailed(imSessionImpl, requestId, reasonInfoImpl);
        }
    }

    public void fileTransferProgress(String requestId, String fileId, long bytesTransferred, long bytesTotal) throws RemoteException {
        Log.i(TAG, "fileTransferProgress#");
        
        for (IMSessionListener listener : listeners) {
            listener.fileTransferProgress(imSessionImpl, requestId, fileId, bytesTransferred, bytesTotal);
        }
    }

    public void incomingFilePushRequest(IFilePushRequest filePushRequest) throws RemoteException {
        Log.i(TAG, "incomingFilePushRequest#");
        
        FilePushRequestImpl filePushRequestImpl = new FilePushRequestImpl(filePushRequest); 
        
        for (IMSessionListener listener : listeners) {
            listener.incomingFilePushRequest(imSessionImpl, filePushRequestImpl);
        }
    }
    
    public void messageReceived(IMessage message) throws RemoteException {
        Log.i(TAG, "messageReceived#");
        
        Message messageImpl = MessageHelper.convertToMessage(message);
        
        for (IMSessionListener listener : listeners) {
            listener.messageReceived(imSessionImpl, messageImpl);
        }
    }

    public void messageSendFailed(String messageId, IReasonInfo reason) throws RemoteException {
        Log.i(TAG, "messageSendFailed#");
        
        ReasonInfoImpl reasonInfoImpl = new ReasonInfoImpl(reason);
        
        for (IMSessionListener listener : listeners) {
            listener.messageSendFailed(imSessionImpl, messageId, reasonInfoImpl);
        }
    }

    public void messageSent(String messageId) throws RemoteException {
        Log.i(TAG, "messageSent#");
        
        for (IMSessionListener listener : listeners) {
            listener.messageSent(imSessionImpl, messageId);
        }
    }

    public void sessionClosed(IReasonInfo reason) throws RemoteException {
        Log.i(TAG, "sessionClosed#");
        
        ReasonInfoImpl reasonInfoImpl = new ReasonInfoImpl(reason);
        
        for (IMSessionListener listener : listeners) {
            listener.sessionClosed(imSessionImpl, reasonInfoImpl);
        }
    }

    public void systemMessageReceived(IMessage message) throws RemoteException {
        Log.i(TAG, "systemMessageReceived#");
        
        Message messageImpl = MessageHelper.convertToMessage(message);
        
        for (IMSessionListener listener : listeners) {
            listener.systemMessageReceived(imSessionImpl, messageImpl);
        }
    }

    public void addListener(IMSessionListener listener) {
        Log.i(TAG, "addListener#");
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(IMSessionListener listener) {
        Log.i(TAG, "removeListener#");
        if (listener != null) {
            listeners.remove(listener);
        }
    }
}
