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

package com.android.ims.presence;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.android.ims.ServiceImpl;
import com.android.ims.configuration.AppConfiguration;
import com.android.ims.presence.rpc.RemotePresenceServiceListener;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.ReasonInfo;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.android.IError;
import javax.microedition.ims.android.presence.IPresenceService;
import javax.microedition.ims.android.presence.IPresenceSource;
import javax.microedition.ims.android.presence.IWatcher;
import javax.microedition.ims.android.presence.IWatcherInfoSubscriber;
import javax.microedition.ims.presence.*;

/**
 * Default implementation {@link PresenceService} 
 * 
 * @author Andrei Khomushko
 * 
 */
public class DefaultPresenceService extends ServiceImpl implements
        PresenceService, PresenceServiceListener {
    private static final String TAG = "PresenceServiceImpl";
    
    private final String localUserId; 
    private final IPresenceService presenceServicePeer;
    private final RemotePresenceServiceListener remotePresenceServiceListener;
    
    private PresenceServiceListener listener;
    
    public DefaultPresenceService(final IPresenceService presenceService, 
            final Context context, AppConfiguration configuration) 
            throws ImsException{
        super(context, configuration);
        
        assert presenceService != null;
        this.presenceServicePeer = presenceService;
        
        try {
            this.localUserId = presenceService.getUserId();
        } catch (RemoteException e) {
            throw new ImsException(e.getMessage(), e);
        }
        
        this.remotePresenceServiceListener = new RemotePresenceServiceListener(this, this);
        
        try {
            presenceServicePeer.addPresenceServiceListener(remotePresenceServiceListener);
        } catch (RemoteException e) {
            throw new ImsException(e.getMessage(), e);
        }
    }
    
    
    public void setListener(PresenceServiceListener listener) {
        this.listener = listener;
    }

    
    public String getAppIdInternally() throws RemoteException {
        return presenceServicePeer.getAppId();
    }

    
    public String getSchemeInternally() throws RemoteException {
        return presenceServicePeer.getAppId();
    }

    
    protected void closeInternally() throws RemoteException {
        removeRemoteListener();
        presenceServicePeer.close();
    }
    
    
    public void serviceClosed(PresenceService service, ReasonInfo reasonInfo) {
        Log.i(TAG, "serviceClosed#");
        if(listener != null) {
            listener.serviceClosed(this, reasonInfo);
        }
    }
    
    
    public PresenceSource createPresenceSource() throws ServiceClosedException,
            ImsException {
        Log.d(TAG, "createPresenceSource#start");
        if (!isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }

        final PresenceSource retValue = doCreatePresenceSource();
     
        Log.d(TAG, "createPresenceSource#end");
        return retValue;
    }
    
    public PresenceSource doCreatePresenceSource() throws ImsException{
        final PresenceSource retValue;
        
        IPresenceSource presenceSourcePeer = null;
        try {
            presenceSourcePeer = presenceServicePeer.createPresenceSource();
        } catch (RemoteException e) {
            throw new ImsException(e.getMessage(), e);
        }
        
        if(presenceSourcePeer == null) {
            throw new ImsException("Can't retrieve presenceSourcePeer");
        }
        
        DefaultPresenceSource presenceSource;
        try {
            presenceSource = new DefaultPresenceSource(presenceSourcePeer);
        } catch (InstantiationException e) {
            throw new ImsException("Can't instantiate DefaultPresenceSource", e);
        }
        addServiceCloseListener(presenceSource);
        retValue = presenceSource;
        
        return retValue;
    }
    
    
    public Watcher createListWatcher(String targetURI)
            throws ServiceClosedException, ImsException {
        Log.d(TAG, "createListWatcher#start");
        Watcher retValue = createWatcher(targetURI);
        Log.d(TAG, "createListWatcher#end");
        return retValue;
    }

    
    public Watcher createWatcher(String targetURI)
            throws ServiceClosedException, ImsException {
        Log.d(TAG, "createWatcher#start");
        if (!isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }
        
        if(targetURI == null) {
            throw new IllegalArgumentException("The targetURI argument is null");
        }
        final Watcher retValue;
        
        try {
            retValue = doCreateWatcher(targetURI);
        } catch (RemoteException e) {
            throw new ImsException(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new ImsException(e.getMessage(), e);
        }
        Log.d(TAG, "createWatcher#end");
        return retValue;
    }
    
    private Watcher doCreateWatcher(String targetURI) throws RemoteException, InstantiationException{
        final Watcher retValue;
        
        IExceptionHolder exceptionHolder = new IExceptionHolder();
        
        final IWatcher watcherPeer = presenceServicePeer.createWatcher(targetURI, exceptionHolder);

        if(exceptionHolder.getParcelableException() != null) {
            IError error = (IError)exceptionHolder.getParcelableException();
            throw new IllegalArgumentException(error.getMessage());
        }
        
        WatcherImpl watcher = new WatcherImpl(watcherPeer);
        addServiceCloseListener(watcher);
        
        retValue = watcher;
        
        return retValue;
    }

    
    public WatcherInfoSubscriber createWatcherInfoSubscriber()
            throws ServiceClosedException, ImsException {
        Log.d(TAG, "createWatcherInfoSubscriber#start");
        
        final WatcherInfoSubscriber watcherInfoSubscriber;
        
        if (!isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }
        
        final IWatcherInfoSubscriber infoSubscriber;
        try {
            infoSubscriber = presenceServicePeer.createWatcherInfoSubscriber();
        } catch (RemoteException e) {
            throw new ImsException(e.getMessage(), e);
        }
        
        try {
            watcherInfoSubscriber = new DefaultWatcherInfoSubscriber(infoSubscriber);
        } catch (InstantiationException e) {
            throw new ImsException(e.getMessage(), e);
        }
        
        Log.d(TAG, "createWatcherInfoSubscriber#end");
        return watcherInfoSubscriber;
    }

    
    public String getLocalUserId() {
        return localUserId;
    }
    
    private void removeRemoteListener() {
        try {
            presenceServicePeer.removePresenceServiceListener(remotePresenceServiceListener);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
