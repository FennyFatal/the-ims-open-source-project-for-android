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

import javax.microedition.ims.android.msrp.IFileInfo;
import javax.microedition.ims.android.msrp.IFilePushRequest;
import javax.microedition.ims.im.FileInfo;
import javax.microedition.ims.im.FilePushRequest;

public class FilePushRequestImpl implements FilePushRequest {
    private static final String TAG = "JSR - FilePushRequestImpl";

    private final IFilePushRequest mFilePushRequest;

    public FilePushRequestImpl(IFilePushRequest mFilePushRequest) {
        this.mFilePushRequest = mFilePushRequest;
    }

    
    public void accept() {
        Log.i(TAG, "accept#started");

        if (isExpired()) {
            throw new IllegalStateException("The request has expired");
        }
        
        try {
            mFilePushRequest.accept();
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
            mFilePushRequest.reject();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        
        Log.i(TAG, "reject#finished");
    }

    
    public String getRequestId() {
        Log.i(TAG, "getRequestId#started");
        String requestId = null;
        try {
            requestId = mFilePushRequest.getRequestId();
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
            sender = mFilePushRequest.getSender();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "getSender#finished");
        return sender;
    }

    
    public String[] getRecipients() {
        Log.i(TAG, "getRecipients#started");
        String[] recipients = null;
        try {
            recipients = mFilePushRequest.getRecipients();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "getRecipients#finished");
        return recipients;
    }

    
    public String getSubject() {
        Log.i(TAG, "getSubject#started");
        String subject = null;
        try {
            subject = mFilePushRequest.getSubject();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "getSubject#finished");
        return subject;
    }

    
    public FileInfo[] getFileInfos() {
        Log.i(TAG, "getFileInfos#started");
        IFileInfo[] iSrc = null;
        try {
            iSrc = mFilePushRequest.getFileInfos();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        
        FileInfo[] fileInfos = new FileInfo[iSrc.length];
        for (int i = 0; i < iSrc.length; i++) {
            fileInfos[i] = FileHelper.convertToFileInfo(iSrc[i]);
        }
        
        Log.i(TAG, "getFileInfos#finished");
        return fileInfos;
    }
    
    private boolean isExpired() {
        boolean expired = false;
        try {
            expired = mFilePushRequest.isExpired();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return expired;
    }
}
