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
import com.android.ims.rpc.RemoteIMSessionListener;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.android.msrp.IFileInfo;
import javax.microedition.ims.android.msrp.IIMSession;
import javax.microedition.ims.android.msrp.IMessage;
import javax.microedition.ims.im.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation for IMSession interface.
 */
public class IMSessionImpl implements IMSession {
    protected final String TAG = "JSR - IMSessionImpl";
    
    private final IIMSession mIMSession;
    
    private final RemoteIMSessionListener remoteIMSessionListener;
    private IMSessionListener mCurrentIMSessionListener;

    private final Set<String> transferIds = new HashSet<String>();
    
    private int maxSize = -1;
    private boolean closed = false;
    
    private String[] acceptedContentTypes = {"text/plain"};
    private Set<String> acceptedContentTypesSet = new HashSet<String>();
    
    public IMSessionImpl(final IIMSession mIMSession) {
        this.mIMSession = mIMSession;
        
        for (String acceptedContentType : acceptedContentTypes) {
            acceptedContentTypesSet.add(acceptedContentType);
        }
        
        
        this.remoteIMSessionListener = new RemoteIMSessionListener(this);
        try {
            mIMSession.addListener(remoteIMSessionListener);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        
    }

    /**
     * @see IMSession#close()
     */
    
    public void close() {
        Log.i(TAG, "close#started");
        if (!closed) {
            try {
                mIMSession.close();
                Log.i(TAG, "close#sent");
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            closed = true;
        }
        Log.i(TAG, "close#finished");
    }

    /**
     * @see IMSession#getAcceptedContentTypes()
     */
    
    public String[] getAcceptedContentTypes() {
        return acceptedContentTypes;
    }

    /**
     * @see IMSession#getMaxSize()
     */
    
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * @see IMSession#getSessionId()
     */
    
    public String getSessionId() {
        String sessionId = null;
        try {
            sessionId = mIMSession.getSessionId();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return sessionId;
    }

    /**
     * @see IMSession#sendComposingIndicator(int)
     */
    
    public void sendComposingIndicator(int timeout) throws ImsException {
        Log.i(TAG, "sendComposingIndicator#started");
        
        if (isClosed()) {
            throw new IllegalStateException("The IMSession is closed.");
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("The timeout argument is less than 0");
        }
        
        String messageWithComposingIndicator = ComposingIndicatorUtils.createMessage(timeout);
        
        ContentPart messageContent
            = new ContentPart(messageWithComposingIndicator.getBytes(), ComposingIndicatorUtils.COMPOSING_INDICATOR_CONTENT_TYPE);
        
        Message message = new Message();
        message.addContentPart(messageContent);
        
        IMessage iMessage = MessageHelper.convertToIMessage(message);
        
        try {
            mIMSession.sendComposingIndicator(iMessage);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException("The composing indicator message could not be sent to the network, message = " + e.getMessage(), e);
        }
        
        Log.i(TAG, "sendComposingIndicator#finished");
    }

    /**
     * @see IMSession#sendFiles(javax.microedition.ims.im.FileInfo[], boolean)
     */
    
    public String sendFiles(FileInfo[] files, boolean deliveryReport)
            throws IOException, ImsException, SecurityException {
        Log.i(TAG, "sendFiles#started");
        
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("The files argument is null or an empty array");
        }
        for (FileInfo file : files) {
            if (file == null) {
                throw new IllegalArgumentException("The files argument contains null values");
            }
        }
        
        if (isClosed()) {
            throw new IllegalStateException("The IMSession is closed.");
        }
        
        //TODO SecurityException
        //TODO IOException
        
        IFileInfo[] iFiles = new IFileInfo[files.length];
        for (int i = 0; i < files.length; i++) {
            iFiles[i] = FileHelper.convertToIFileInfo(files[i]);
        }
        
        String requestId;
        try {
            requestId = mIMSession.sendFiles(iFiles, deliveryReport);
            transferIds.add(requestId);
            Log.i(TAG, "sendFiles#sent");
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException("Cann't send files. Error : " + e.getMessage(), e);
        }

        Log.i(TAG, "sendFiles#finished");
        return requestId;
    }

    /**
     * @see IMSession#cancelFileTransfer(java.lang.String)
     */
    
    public void cancelFileTransfer(String identifier) {
        Log.i(TAG, "cancelFileTransfer#started");
        
        if (isClosed()) {
            throw new IllegalStateException("The IMSession is closed.");
        }
        
        if (!transferIds.contains(identifier)) {
            throw new IllegalArgumentException("There is no file transfer associated with the identifier");
        }
        
        try {
            mIMSession.cancelFileTransfer(identifier);
            Log.i(TAG, "sendFiles#sent");
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            //throw new ImsException("Cann't send files. Error : " + e.getMessage());
        }

        Log.i(TAG, "cancelFileTransfer#finished");
    }

    /**
     * @see IMSession#sendMessage(javax.microedition.ims.im.Message, boolean)
     */
    
    public void sendMessage(Message message, boolean deliveryReport) throws ImsException {
        Log.i(TAG, "sendMessage#started");
        
        if (isClosed()) {
            throw new IllegalStateException("The IMSession is closed.");
        }
        if (message == null) {
            throw new IllegalArgumentException("The message argument is null");
        }
        if (message.getContentParts() == null || message.getContentParts().length == 0) {
            throw new IllegalArgumentException("There are no content parts specified in the message argument.");
        }
        for (ContentPart contentPart : message.getContentParts()) {
            if (!acceptedContentTypesSet.contains(contentPart.getContentType())) {
                throw new IllegalArgumentException("The contentType of a content part is not one of the accepted content types");
            }
        }
        
        IMessage iMessage = MessageHelper.convertToIMessage(message);
        
        try {
            mIMSession.sendMessage(iMessage, deliveryReport);
            Log.i(TAG, "sendMessage#sent");
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException("The message could not be sent to the network.", e);
        }
        
        Log.i(TAG, "sendMessage#finished");
    }
    
    protected boolean isClosed() {
        return closed;
    }
    
    protected void setListener(ChatListener listener) {
        if (mCurrentIMSessionListener != null) {
            remoteIMSessionListener.removeListener(mCurrentIMSessionListener);
        }

        if (listener != null) {
            remoteIMSessionListener.addListener(listener);
        }
        mCurrentIMSessionListener = listener;
    }

}
