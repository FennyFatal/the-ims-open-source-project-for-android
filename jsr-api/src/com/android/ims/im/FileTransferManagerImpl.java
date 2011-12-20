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
import com.android.ims.rpc.RemoteFileTransferManagerListener;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.android.msrp.IFileInfo;
import javax.microedition.ims.android.msrp.IFileSelector;
import javax.microedition.ims.android.msrp.IFileTransferManager;
import javax.microedition.ims.im.FileInfo;
import javax.microedition.ims.im.FileSelector;
import javax.microedition.ims.im.FileTransferManager;
import javax.microedition.ims.im.FileTransferManagerListener;
import java.io.IOException;

/**
 * Implementation for FileTransferManager interface.
 */
public class FileTransferManagerImpl implements FileTransferManager {
    protected final String TAG = "JSR - FileTransferManagerImpl";
    
    private final IMServiceImpl imServiceImpl;
    private final IFileTransferManager mFileTransferManager;
    
    private FileTransferManagerListener mCurrentFileTransferManagerListener;
    private final RemoteFileTransferManagerListener remoteFileTransferManagerListener;

    
    public FileTransferManagerImpl(IMServiceImpl imServiceImpl, IFileTransferManager mFileTransferManager) {
        this.imServiceImpl = imServiceImpl;
        this.mFileTransferManager = mFileTransferManager;
        
        
        this.remoteFileTransferManagerListener = new RemoteFileTransferManagerListener();
        try {
            mFileTransferManager.addListener(remoteFileTransferManagerListener);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        
    }

    
    public String sendFiles(String sender, String[] recipients, String subject,
            FileInfo[] files, boolean deliveryReport)
            throws ServiceClosedException, IOException, ImsException,
            SecurityException {
        
        Log.i(TAG, "sendFiles#started");

        if (imServiceImpl.isClosed()) {
            throw new ServiceClosedException("Service is closed.");
        }
        if (sender != null && !UtilsMSRP.isValidUserIdentity(sender)) {
            throw new IllegalArgumentException("The sender argument is neither null nor a valid user identity");
        }
        
        if (recipients == null || recipients.length == 0) {
            throw new IllegalArgumentException("The recipients argument is null or an empty array");
        }
        for (String recipient : recipients) {
            if (!UtilsMSRP.isValidUserIdentity(recipient)) {
                throw new IllegalArgumentException("the recipients argument contains invalid user identities");
            }
        }

        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("The files argument is null or an empty array");
        }
        for (FileInfo file : files) {
            if (file == null) {
                throw new IllegalArgumentException("The files argument contains null values");
            }
        }

        IFileInfo[] iFiles = new IFileInfo[files.length];
        for (int i = 0; i < files.length; i++) {
            iFiles[i] = FileHelper.convertToIFileInfo(files[i]);
        }

        String requestId;
        try {
            requestId = mFileTransferManager.sendFiles(sender, recipients, subject, iFiles, deliveryReport);
            Log.i(TAG, "sendFiles#sent");
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException("Cann't send files. Error : " + e.getMessage(), e);
        }
        
        Log.i(TAG, "sendFiles#finished");
        return requestId;
    }

    
    public String requestFiles(String requestSender, String requestRecipient, String subject,
            FileSelector[] files)
            throws ServiceClosedException, ImsException {
        
        Log.i(TAG, "requestFiles#started");

        if (imServiceImpl.isClosed()) {
            throw new ServiceClosedException("Service is closed.");
        }
        if (requestSender != null && !UtilsMSRP.isValidUserIdentity(requestSender)) {
            throw new IllegalArgumentException("The requestSender argument is neither null nor a valid user identity");
        }
        if (!UtilsMSRP.isValidUserIdentity(requestRecipient)) {
            throw new IllegalArgumentException("The requestRecipient argument is not a valid user identity");
        }
        
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("The files argument is null or an empty array");
        }
        for (FileSelector file : files) {
            if (file == null) {
                throw new IllegalArgumentException("The files argument contains null values");
            }
        }

        IFileSelector[] iFiles = new IFileSelector[files.length];
        for (int i = 0; i < files.length; i++) {
            iFiles[i] = FileHelper.convertToIFileSelector(files[i]);
        }

        String requestId;
        try {
            requestId = mFileTransferManager.requestFiles(requestSender, requestRecipient, subject, iFiles);
            Log.i(TAG, "requestFiles#sent");
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException("Cann't request files. Error : " + e.getMessage(), e);
        }
        
        Log.i(TAG, "requestFiles#finished");
        return requestId;
    }
    
    
    public void cancel(String identifier) {
        Log.i(TAG, "cancel#started");
        
        try {
            if (!mFileTransferManager.isAvailable4Cancel(identifier)) {
                throw new IllegalArgumentException("There is no file transfer associated with the identifier");
            }
            
            mFileTransferManager.cancel(identifier);
            Log.i(TAG, "cancel#sent");
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
//            throw new IllegalStateException("Cann't cancel large message. Error : " + e.getMessage());
        }
        
        Log.i(TAG, "cancel#finished");
    }

    
    public void setListener(FileTransferManagerListener listener) {
        if (mCurrentFileTransferManagerListener != null) {
            remoteFileTransferManagerListener.removeListener(mCurrentFileTransferManagerListener);
        }

        if (listener != null) {
            remoteFileTransferManagerListener.addListener(listener);
        }
        mCurrentFileTransferManagerListener = listener;
    }

}
