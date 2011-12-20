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
import javax.microedition.ims.android.presence.IPresentity;
import javax.microedition.ims.android.presence.IWatcher;
import javax.microedition.ims.android.presence.IWatcherListener;
import javax.microedition.ims.presence.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default implementation {@link Watcher}
 * 
 * @author Andrei Khomushko
 * 
 */
public class WatcherImpl implements Watcher, ServiceCloseListener {
    private static final String TAG = "WatcherImpl";
    private final AtomicReference<Boolean> done = new AtomicReference<Boolean>(
            false);

    private final IWatcherListener iWatcherListener;

    private final IWatcher watcherPeer;
    private final String targetURI;
    private WatcherListener listener;

    private List<Presentity> presentities = new ArrayList<Presentity>();
    private List<WatcherStateListener> stateListeners = new ArrayList<WatcherStateListener>();

    /**
     * Create WatcherImpl
     * 
     * @param watcherPeer
     * 
     * @throws IllegalArgumentException
     *             - if the watcherPeer argument is null
     * @throws InstantiationException
     *             - if the object can't be instantiated
     */
    public WatcherImpl(IWatcher watcherPeer) throws InstantiationException {
        if (watcherPeer == null) {
            throw new IllegalArgumentException(
                    "The watcherPeer argument is null");
        }
        this.watcherPeer = watcherPeer;

        try {
            this.targetURI = watcherPeer.getTargetURI();
        } catch (RemoteException e) {
            throw new InstantiationException("Cann't retrive target uri");
        }

        try {
            watcherPeer
                    .addListener(iWatcherListener = new RemoteWatcherListener());
        } catch (RemoteException e) {
            throw new InstantiationException("Cann't communicate with service");
        }
    }

    
    public Presentity[] getPresentities() {
        int state = getState();
        if (state == STATE_ACTIVE) {
            throw new IllegalStateException(
                    "The Watcher is not in STATE_ACTIVE");
        }

        return presentities.toArray(new Presentity[0]);
    }

    
    public int getState() {
        int state;
        try {
            state = watcherPeer.getState();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            state = -1;
        }
        return state;
    }

    
    public String getTargetURI() {
        return targetURI;
    }

    
    public void poll() throws ServiceClosedException, IOException {
        Log.d(TAG, "poll#started");
        if (done.get()) {
            throw new ServiceClosedException("Service already closed");
        }

        int state = getState();
        if (state != STATE_INACTIVE) {
            throw new IllegalArgumentException(
                    "The Watcher is not in STATE_INACTIVE");
        }

        try {
            watcherPeer.poll();
        } catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
        Log.d(TAG, "poll#ended");
    }

/* 
    public void poll(WatcherFilterSet filters) throws ServiceClosedException,
            IOException {
        Log.d(TAG, "poll#started");
        if (done.get()) {
            throw new ServiceClosedException("Service already closed");
        }

        if (filters == null) {
            throw new IllegalArgumentException("The filters argument is null");
        }

        if (!isContainFilters(filters)) {
            throw new IllegalArgumentException(
                    "The WatcherFilterSet does not contain any WatcherFilter");
        }

        int state = getState();
        if (state != STATE_INACTIVE) {
            throw new IllegalArgumentException(
                    "The Watcher is not in STATE_INACTIVE");
        }

        String filtersView = PresenceUtils
                .convertWatcherFilterToString(filters .getDOM() );
        Log.d(TAG, "poll#filtersView = " + filtersView);
        try {
            watcherPeer.pollWithFilter(filtersView);
        } catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
        Log.d(TAG, "poll#ended");
    }*/

    private boolean isContainFilters(WatcherFilterSet filters)
            throws IOException {
        // TODO use filters.getDom to get filters size
        return false;
    }

    
    public void setListener(WatcherListener listener) {
        this.listener = listener;
    }

    
    public void subscribe() throws ServiceClosedException, IOException {
        Log.d(TAG, "subscribe#started");
        if (done.get()) {
            throw new ServiceClosedException("Service already closed");
        }

        int state = getState();
        if (state == STATE_PENDING_SUBSCRIBE
                || state == STATE_PENDING_UNSUBSCRIBE) {
            throw new IllegalArgumentException(
                    "The Watcher is in STATE_PENDING_SUBSCRIBE or STATE_PENDING_UNSUBSCRIBE");
        }

        try {
            watcherPeer.subscribe();
        } catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
        Log.d(TAG, "subscribe#ended");
    }

/*  
    public void subscribe(WatcherFilterSet filters)
            throws ServiceClosedException, IOException {
        Log.d(TAG, "subscribe#started");
        if (done.get()) {
            throw new ServiceClosedException("Service already closed");
        }

        if (filters == null) {
            throw new IllegalArgumentException("The filters argument is null");
        }

        if (!isContainFilters(filters)) {
            throw new IllegalArgumentException(
                    "The WatcherFilterSet does not contain any WatcherFilter");
        }

        int state = getState();
        if (state == STATE_PENDING_SUBSCRIBE
                || state == STATE_PENDING_UNSUBSCRIBE) {
            throw new IllegalArgumentException(
                    "The Watcher is in STATE_PENDING_SUBSCRIBE or STATE_PENDING_UNSUBSCRIBE");
        }

        String filterView = PresenceUtils
                .convertWatcherFilterToString(filters .getDOM() );
        Log.d(TAG, "subscribe#filterView = " + filterView);
        try {
            watcherPeer.subscribeWithFilter(filterView);
        } catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
        Log.d(TAG, "subscribe#ended");
    }*/

    
    public void unsubscribe() throws IOException {
        Log.d(TAG, "unsubscribe#started");

        int state = getState();
        if (state != STATE_ACTIVE) {
            throw new IllegalArgumentException(
                    "The Watcher is not in STATE_ACTIVE");
        }

        try {
            watcherPeer.unsubscribe();
        } catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
        Log.d(TAG, "unsubscribe#ended");
    }

    
    public void serviceClosed(ManagableConnection connection) {
        Log.d(TAG, "serviceClosed#started");
        removeRemoteListener();
        connection.removeServiceCloseListener(this);
        stateListeners.clear();
        done.set(true);
        Log.d(TAG, "serviceClosed#ended");
    }

    private void removeRemoteListener() {
        try {
            watcherPeer.removeListener(iWatcherListener);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void presenceInfoReceived(Presentity[] newPresentities) {
        for (Presentity presentity : newPresentities) {
            presentities.add(presentity);
            stateListeners.add((WatcherStateListener) presentity);
        }

        if (listener != null) {
            listener.presenceInfoReceived(this, newPresentities);
        }
    }

    private void subscriptionFailed(ReasonInfo reasonInfo) {
        Log.d(TAG, "subscriptionFailed#");

        for (WatcherStateListener listener : stateListeners) {
            listener.subscriptionFailed();
        }

        if (listener != null) {
            listener.subscriptionFailed(this, reasonInfo);
        }
    }

    private void subscriptionStarted() {
        Log.d(TAG, "subscriptionStarted#");

        for (WatcherStateListener listener : stateListeners) {
            listener.subscriptionStarted();
        }

        if (listener != null) {
            listener.subscriptionStarted(this);
        }
    }

    private void subscriptionTerminated(Event event) {
        Log.d(TAG, "subscriptionTerminated#");

        for (WatcherStateListener listener : stateListeners) {
            listener.subscriptionTerminated(event);
        }

        if (listener != null) {
            listener.subscriptionTerminated(this, event);
        }
    }

    
    public String toString() {
        return "WatcherImpl [done=" + done + ", listener=" + listener
                + ", targetURI=" + targetURI + "]";
    }

    private class RemoteWatcherListener extends IWatcherListener.Stub {
        private static final String TAG = "RemoteWatcherListener";

        
        public void presenceInfoReceived(IPresentity[] iPresentities)
                throws RemoteException {
            Log.d(TAG,
                    "presenceInfoReceived#iPresentities = "
                            + Arrays.toString(iPresentities));

            final List<Presentity> presentities = new ArrayList<Presentity>();

            for (IPresentity iPresentity : iPresentities) {
                try {
                    PresentityParser parser = new PresentityParser(iPresentity.getPidfDoc());
                    Presentity presentity = parser.parse();

                    presentities.add(presentity);
                } catch (IOException e) {
                    Log.e(TAG,
                            String.format("pidf = %s, e = %s",
                                    iPresentity.getPidfDoc(), e.getMessage()),
                            e);
                }
            }

            if (!presentities.isEmpty()) {
                WatcherImpl.this.presenceInfoReceived(presentities
                        .toArray(new DefaultPresentity[0]));
            }
        }

        
        public void subscriptionFailed(IReasonInfo reasonInfo)
                throws RemoteException {
            Log.d(TAG, "subscriptionFailed#reasonInfo = " + reasonInfo);
            WatcherImpl.this.subscriptionFailed(new ReasonInfoImpl(reasonInfo));
        }

        
        public void subscriptionStarted() throws RemoteException {
            Log.d(TAG, "subscriptionStarted#");
            WatcherImpl.this.subscriptionStarted();
        }

        
        public void subscriptionTerminated(IEvent event) throws RemoteException {
            Log.d(TAG, "subscriptionTerminated#event = " + event);
            Event createEvent = PresenceUtils.createEvent(event);
            WatcherImpl.this.subscriptionTerminated(createEvent);
        }
    }
}
