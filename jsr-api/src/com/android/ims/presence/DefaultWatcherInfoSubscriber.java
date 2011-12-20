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

import android.os.RemoteException;
import android.util.Log;
import com.android.ims.ManagableConnection;
import com.android.ims.ReasonInfoImpl;
import com.android.ims.ServiceCloseListener;

import javax.microedition.ims.ReasonInfo;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.presence.IEvent;
import javax.microedition.ims.android.presence.IWatcherInfoSubscriber;
import javax.microedition.ims.android.presence.IWatcherInfoSubscriberListener;
import javax.microedition.ims.presence.Event;
import javax.microedition.ims.presence.WatcherInfo;
import javax.microedition.ims.presence.WatcherInfoSubscriber;
import javax.microedition.ims.presence.WatcherInfoSubscriberListener;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default implementation {@link WatcherInfoSubscriber}
 * 
 * @author Andrei Khomushko
 * 
 */
public class DefaultWatcherInfoSubscriber implements WatcherInfoSubscriber,
        ServiceCloseListener {

    private static final String TAG = "DefaultWatcherInfoSubscriber";
    private final AtomicReference<Boolean> done = new AtomicReference<Boolean>(
            false);
    
    private final IWatcherInfoSubscriber watcherInfoSubscriberPeer;
    private final IWatcherInfoSubscriberListener iWatcherListener;
    private WatcherInfoSubscriberListener listener;
    
    /**
     * Create DefaultWatcherInfoSubscriber
     * 
     * @throws IllegalArgumentException
     *             - if the watcherInfoSubscriber argument is null
     * @throws InstantiationException
     *             - if the object can't be instantiated
     *                         
     * @param watcherInfoSubscriber
     */
    public DefaultWatcherInfoSubscriber(IWatcherInfoSubscriber watcherInfoSubscriber) 
        throws InstantiationException{
        if(watcherInfoSubscriber == null) {
            throw new IllegalArgumentException(
            "The watcherInfoSubscriber argument is null");
        }
        this.watcherInfoSubscriberPeer = watcherInfoSubscriber;
        
        try {
            watcherInfoSubscriber.addListener(iWatcherListener = new RemoteWatcherInfoSubscriberListener());
        } catch (RemoteException e) {
            throw new InstantiationException("Cann't communicate with service");
        }

    }

    public void subscribe() throws ServiceClosedException, IOException {
        Log.d(TAG, "subscribe#started");
        
        if (done.get()) {
            throw new ServiceClosedException("Service already closed");
        }
        
        int state  = getState();
        if(state == STATE_PENDING_SUBSCRIBE || state == STATE_PENDING_UNSUBSCRIBE) {
            throw new IllegalStateException("The WatcherInfoSubscriber is in STATE_PENDING_SUBSCRIBE or STATE_PENDING_UNSUBSCRIBE");
        }
        
        try {
            watcherInfoSubscriberPeer.subscribe();
        } catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
        
        Log.d(TAG, "subscribe#ended");
    }

    public void unsubscribe() throws IOException {
        Log.d(TAG, "unsubscribe#started");
        
        int state = getState();
        if (state != STATE_ACTIVE) {
            throw new IllegalArgumentException(
                    "The Watcher is not in STATE_ACTIVE");
        }

        try {
            watcherInfoSubscriberPeer.unsubscribe();
        } catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
        
        Log.d(TAG, "unsubscribe#ended");
    }

    public void setListener(WatcherInfoSubscriberListener listener) {
        this.listener = listener;
    }

    public int getState() {
        int state;
        try {
            state = watcherInfoSubscriberPeer.getState();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            state = -1;
        }
        return state;
    }

    public void serviceClosed(ManagableConnection connection) {
        Log.d(TAG, "serviceClosed#started");
        removeRemoteListener();
        connection.removeServiceCloseListener(this);
        done.set(true);
        Log.d(TAG, "serviceClosed#ended");
    }
    
    private void removeRemoteListener() {
        try {
            watcherInfoSubscriberPeer.removeListener(iWatcherListener);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
    
    private class RemoteWatcherInfoSubscriberListener extends IWatcherInfoSubscriberListener.Stub {
        private static final String TAG = "RemoteWatcherInfoSubscriberListener";

        public void subscriptionStarted() throws RemoteException {
            Log.d(TAG, "subscriptionStarted#");
            if (listener != null) {
                listener.subscriptionStarted();
            }
        }
        
        public void subscriptionFailed(IReasonInfo reason) throws RemoteException {
            Log.d(TAG, "subscriptionFailed#reasonInfo = " + reason);
            if (listener != null) {
                ReasonInfo reasonInfo = new ReasonInfoImpl(reason);
                listener.subscriptionFailed(reasonInfo);
            }
        }

        public void subscriptionTerminated(IEvent iEvent) throws RemoteException {
            Log.d(TAG, "subscriptionTerminated#event = " + iEvent);
            if (listener != null) {
                Event event = PresenceUtils.createEvent(iEvent);
                listener.subscriptionTerminated(event);
            }
        }
        
        public void watcherInfoReceived(String watcherInfo) throws RemoteException {
            Log.d(TAG, "watcherInfoReceived#event = " + watcherInfo);
            
            if (listener != null) {
                WatcherInfo[] watcherInfos = parseWatcherInfos(watcherInfo);
                listener.watcherInfoReceived(watcherInfos);
            }
        }

        private WatcherInfo[] parseWatcherInfos(String watcherInfo) {
            WatcherInfo[] watcherInfos; 
            try {
                WatcherInfoSubscriberParser watcherInfoSubscriberParser = new WatcherInfoSubscriberParser(watcherInfo);
                watcherInfos = watcherInfoSubscriberParser.parse();
            } catch (IOException e) {
                Log.e(TAG, "parseWatcherInfos#e = " + e.getMessage());
                watcherInfos = new WatcherInfo[0];
            }
            
            return watcherInfos;
        }
    }    
}

