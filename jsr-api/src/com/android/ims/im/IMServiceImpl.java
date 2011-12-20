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

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.android.ims.ServiceImpl;
import com.android.ims.configuration.AppConfiguration;
import com.android.ims.rpc.RemoteIMServiceListener;

import javax.microedition.ims.android.msrp.IIMService;
import javax.microedition.ims.im.*;

/**
 * Implementation for IMService interface.
 */
public class IMServiceImpl extends ServiceImpl implements IMService {
    
    private final IIMService mIMService;
    
    private ConferenceManagerImpl conferenceManagerImpl;
    private DeferredMessageManagerImpl deferredMessageManagerImpl;
    private FileTransferManagerImpl fileTransferManagerImpl;
    private HistoryManagerImpl historyManagerImpl;
    private MessageManagerImpl messageManagerImpl;
    
    private boolean closed = false;
    
    private IMServiceListener mCurrentIMServiceListener;
    private final RemoteIMServiceListener remoteIMServiceListener;


    public IMServiceImpl(IIMService mIMService, Context mContext, AppConfiguration configuration) {
        super(mContext, configuration);
        
        assert mIMService != null;
        this.mIMService = mIMService;
        
        
        this.remoteIMServiceListener = new RemoteIMServiceListener(this);
        try {
            mIMService.addListener(remoteIMServiceListener);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        
    }

    /**
     * @see javax.microedition.ims.im.IMService#getConferenceManager()
     */
    
    public ConferenceManager getConferenceManager() {
        if (conferenceManagerImpl == null) {
            Log.i(TAG, "getting ConferenceManager");
            try {
                conferenceManagerImpl = new ConferenceManagerImpl(this, mIMService.getConferenceManager());
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }        
        return conferenceManagerImpl;
    }

    /**
     * @see javax.microedition.ims.im.IMService#getDeferredMessageManager()
     */
    
    public DeferredMessageManager getDeferredMessageManager() {
        if (deferredMessageManagerImpl == null) {
            Log.i(TAG, "getting DeferredMessageManager");
            try {
                deferredMessageManagerImpl = new DeferredMessageManagerImpl(this, mIMService.getDeferredMessageManager());
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }        
        return deferredMessageManagerImpl;
    }

    /**
     * @see javax.microedition.ims.im.IMService#getFileTransferManager()
     */
    
    public FileTransferManager getFileTransferManager() {
        if (fileTransferManagerImpl == null) {
            Log.i(TAG, "getting FileTransferManager");
            try {
                fileTransferManagerImpl = new FileTransferManagerImpl(this, mIMService.getFileTransferManager());
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }        
        return fileTransferManagerImpl;
    }

    /**
     * @see javax.microedition.ims.im.IMService#getHistoryManager()
     */
    
    public HistoryManager getHistoryManager() {
        if (historyManagerImpl == null) {
            Log.i(TAG, "getting HistoryManager");
            try {
                historyManagerImpl = new HistoryManagerImpl(this, mIMService.getHistoryManager());
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }        
        return historyManagerImpl;
    }

    /**
     * @see javax.microedition.ims.im.IMService#getMessageManager()
     */
    
    public MessageManager getMessageManager() {
        if (messageManagerImpl == null) {
            Log.i(TAG, "getting MessageManager");
            try {
                messageManagerImpl = new MessageManagerImpl(this, mIMService.getMessageManager());
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }        
        return messageManagerImpl;
    }

    /**
     * @see javax.microedition.ims.im.IMService#getLocalUserId()
     */
    
    public String getLocalUserId() {
        String localUserId = null;
        try {
            localUserId = mIMService.getLocalUserId();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return localUserId;
    }

    /**
     * @see javax.microedition.ims.im.IMService#setListener(javax.microedition.ims.im.IMServiceListener)
     */
    
    public void setListener(IMServiceListener listener) {
        if (mCurrentIMServiceListener != null) {
            remoteIMServiceListener.removeListener(mCurrentIMServiceListener);
        }

        if (listener != null) {
            remoteIMServiceListener.addListener(listener);
        }
        mCurrentIMServiceListener = listener;
    }

    public boolean isClosed() {
        return closed;
    }

    
    protected void closeInternally() throws RemoteException {
        closed = true;
        mIMService.close();
    }

    
    protected String getAppIdInternally() throws RemoteException {
        return mIMService.getAppId();
    }

    
    protected String getSchemeInternally() throws RemoteException {
        return mIMService.getSheme();
    }

}
