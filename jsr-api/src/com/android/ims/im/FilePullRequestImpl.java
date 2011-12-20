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

import javax.microedition.ims.ImsException;
import javax.microedition.ims.android.msrp.IFilePullRequest;
import javax.microedition.ims.android.msrp.IFileSelector;
import javax.microedition.ims.im.FilePullRequest;
import javax.microedition.ims.im.FileSelector;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FilePullRequestImpl implements FilePullRequest {
    private static final String TAG = "JSR - FilePullRequestImpl";
    
    private final IFilePullRequest mFilePullRequest;
    
    private final Map<String, IFileSelectorWrapper> iFileSelectorMap = new HashMap<String, IFileSelectorWrapper>();

    public FilePullRequestImpl(IFilePullRequest mFilePullRequest) {
        this.mFilePullRequest = mFilePullRequest;
    }

    
    public void accept() throws ImsException {
        Log.i(TAG, "accept#started");

        if (isExpired()) {
            throw new IllegalStateException("The request has expired");
        }
        for (IFileSelectorWrapper wrapper : iFileSelectorMap.values()) {
            if (!wrapper.isPathSet()) {
                throw new ImsException("A file path has not been set for all file selectors in the request");
            }
        }
        
        try {
            mFilePullRequest.accept();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "accept#finish");
    }

    
    public void reject() {
        Log.i(TAG, "reject#started");
        
        if (isExpired()) {
            throw new IllegalStateException("The request has expired");
        }
        
        try {
            mFilePullRequest.reject();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        
        Log.i(TAG, "reject#finished");
    }

    
    public String getRequestId() {
        Log.i(TAG, "getRequestId#started");
        String requestId = null;
        try {
            requestId = mFilePullRequest.getRequestId();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "getRequestId#finished");
        return requestId;
    }

    
    public String getSender() {
        Log.i(TAG, "getSender#started");
        String sender = null;
        try {
            sender = mFilePullRequest.getSender();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "getSender#finished");
        return sender;
    }

    
    public String getRecipient() {
        Log.i(TAG, "getRecipient#started");
        String recipient = null;
        try {
            recipient = mFilePullRequest.getRecipient();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "getRecipient#finished");
        return recipient;
    }

    
    public String getSubject() {
        Log.i(TAG, "getSubject#started");
        String subject = null;
        try {
            subject = mFilePullRequest.getSubject();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "getSubject#finished");
        return subject;
    }

    
    public FileSelector[] getFileSelectors() {
        Log.i(TAG, "getFileSelectors#started");
        IFileSelector[] iSrc = null;
        try {
            iSrc = mFilePullRequest.getFileSelectors();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        
        FileSelector[] fileSelectors = new FileSelector[iSrc.length];
        for (int i = 0; i < iSrc.length; i++) {
            fileSelectors[i] = FileHelper.convertToFileSelector(iSrc[i]);
            
            if (iFileSelectorMap.containsKey(iSrc[i].getFileId())) {
                throw new IllegalStateException("Map already contains value. Wrong keys.");
            }
            iFileSelectorMap.put(iSrc[i].getFileId(), new IFileSelectorWrapper(iSrc[i]));
        }
        
        Log.i(TAG, "getFileSelectors#finished");
        return fileSelectors;
    }

    
    public void setFilePath(FileSelector fileSelector, String filePath)
            throws IOException, SecurityException {
        
        Log.i(TAG, "setFilePath#started");

        if (fileSelector == null) {
            throw new IllegalArgumentException("The fileSelector argument is null");
        }
        if (!iFileSelectorMap.containsKey(fileSelector.getFileId())) {
            throw new IllegalArgumentException("The file indicated by the fileSelector argument was not included in this FilePullRequest");
        }
        //TODO IOException
        //TODO SecurityException
        
        IFileSelectorWrapper iFileSelectorWrapper = iFileSelectorMap.get(fileSelector.getFileId());
        
        iFileSelectorWrapper.pathSet();
        
        try {
            mFilePullRequest.setFilePath(iFileSelectorWrapper.getiFileSelector(), filePath);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "setFilePath#finished");
    }
    
    
    private class IFileSelectorWrapper {
        private IFileSelector iFileSelector;
        private boolean pathSet;
        
        public IFileSelectorWrapper(IFileSelector iFileSelector) {
            this.iFileSelector = iFileSelector;
            this.pathSet = false;
        }
        
        public void pathSet() {
            pathSet = true;
        }

        public boolean isPathSet() {
            return pathSet;
        }

        public IFileSelector getiFileSelector() {
            return iFileSelector;
        }
    }
    
    private boolean isExpired() {
        boolean expired = false;
        try {
            expired = mFilePullRequest.isExpired();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return expired;
    }

}
