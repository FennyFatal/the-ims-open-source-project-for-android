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

package javax.microedition.ims.android.presence;

import android.os.RemoteException;
import android.util.Log;

import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.presence.IEvent.EventType;
import javax.microedition.ims.android.util.RemoteListenerHolder;
import javax.microedition.ims.common.EventPackage;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.dialog.IncomingNotifyListener;
import javax.microedition.ims.core.sipservice.subscribe.SubscribeService;
import javax.microedition.ims.core.sipservice.subscribe.Subscription;
import javax.microedition.ims.core.sipservice.subscribe.SubscriptionInfo;
import javax.microedition.ims.core.sipservice.subscribe.SubscriptionInfoImpl;
import javax.microedition.ims.core.sipservice.subscribe.listener.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default implementation IWatcherInfoSubscriber.aidl
 *
 * @author Andrei Khomushko
 */
class WatcherInfoSubscriberImpl extends IWatcherInfoSubscriber.Stub implements
        SubscriptionStateListener, IncomingNotifyListener {
    private static final String TAG = "WatcherInfoSubscriberImpl";

    private final RemoteListenerHolder<IWatcherInfoSubscriberListener> listenerHolder = new RemoteListenerHolder<IWatcherInfoSubscriberListener>(
            IWatcherInfoSubscriberListener.class);

    private final SubscribeService subscribeServicePeer;

    private final AtomicReference<SubscriptionState> state = new AtomicReference<SubscriptionState>(
            SubscriptionState.STATE_INACTIVE);
    private final ClientIdentity localParty;
    private final String remoteParty;
    private SubscriptionInfo subscriptionInfo;

    WatcherInfoSubscriberImpl(
            final ClientIdentity localParty,
            final String remoteParty,
            final SubscribeService subscribeService) {

        if (subscribeService == null) {
            throw new IllegalArgumentException(
                    "The subscribeService argument is null");
        }

        if (localParty == null) {
            throw new IllegalArgumentException(
                    "The localParty argument is null");
        }

        if (remoteParty == null) {
            throw new IllegalArgumentException(
                    "The remoteParty argument is null");
        }

        this.subscribeServicePeer = subscribeService;
        this.localParty = localParty;
        this.remoteParty = remoteParty;
        this.subscriptionInfo = new SubscriptionInfoImpl(EventPackage.PRESENCE_WINFO);
    }

    public void subscribe() throws RemoteException {
        Logger.log(TAG, "subscribe#start");

        assert state.get() == SubscriptionState.STATE_INACTIVE : "Subscription is in not suitable state to initiate subscribe#";

        Subscription subscription = subscribeServicePeer.lookUpSubscription(localParty, remoteParty, subscriptionInfo);
        assert subscription != null;

        subscription.addSubscriptionStateListener(this);
        subscription.addSubscriptionNotifyListener(this);

        subscription.subscribe();

        setState(SubscriptionState.STATE_PENDING_SUBSCRIBE);

        Logger.log(TAG, "subscribe#end");
    }

    public void onSubscriptionStarted(SubscriptionStateEvent event) {
        Logger.log(TAG, "onSubscriptionStarted#event = " + event);

        setState(SubscriptionState.STATE_ACTIVE);
        try {
            listenerHolder.getNotifier().subscriptionStarted();
        }
        catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public void onSubscriptionStartFailed(SubscriptionFailedEvent event) {
        Logger.log(TAG, "onSubscriptionStartFailed#event = " + event);
        setState(SubscriptionState.STATE_INACTIVE);

        final IReasonInfo reasonInfo = SubscriptionUtil.createReasonInfo(event);
        try {
            listenerHolder.getNotifier().subscriptionFailed(reasonInfo);
        }
        catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public void unsubscribe() throws RemoteException {
        Logger.log(TAG, "unsubscribe#start");

        assert state.get() == SubscriptionState.STATE_ACTIVE : "Subscription is in not suitable state to initiate unsubscribe#";

        Subscription subscription = subscribeServicePeer.findSubscription(localParty, remoteParty, subscriptionInfo);
        assert subscription != null;

        subscription.unsubscribe();

        setState(SubscriptionState.STATE_PENDING_UNSUBSCRIBE);

        Logger.log(TAG, "unsubscribe#finish");
    }

    public void onSubscriptionRefreshed(SubscriptionStateEvent event) {
        Logger.log(TAG, "onSubscriptionRefreshed#event = " + event);
    }

    public void onSubscriptionRefreshFailed(SubscriptionFailedEvent event) {
        Logger.log(TAG, "onSubscriptionRefreshFailed#event = " + event);

        IEvent iEvent = new IEvent(EventType.EVENT_DEACTIVATED, -1);
        terminateSubscription(iEvent);
    }

    public void onSubscriptionTerminated(SubscriptionTerminatedEvent event) {
        Logger.log(TAG, "onSubscriptionTerminated#event = " + event);

        IEvent iEvent = SubscriptionUtil.createIEvent(event);
        terminateSubscription(iEvent);
    }

    private void terminateSubscription(IEvent iEvent) {
        setState(SubscriptionState.STATE_INACTIVE);

        /** AK Subscription subsystem by oneself unbinds inactive subscription */
/*        Subscription subscription = subscribeServicePeer
                .findSubscription(dialog);
        assert subscription != null;
        
        subscription.removeSubscriptionStateListener(this);
        subscription.removeSubscriptionNotifyListener(this);

        subscribeServicePeer.unBind(subscription);
*/
        try {
            listenerHolder.getNotifier().subscriptionTerminated(iEvent);
        }
        catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public void notificationReceived(NotifyEvent event) {
        Logger.log(TAG, "notificationReceived#event = " + event);

        String[] notifyBodyMessages = event.getNotifyInfo().getNotifyBodyMessages();
        assert notifyBodyMessages.length == 1;

        try {
            listenerHolder.getNotifier().watcherInfoReceived(notifyBodyMessages[0]);
        }
        catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public int getState() throws RemoteException {
        return state.get().getCode();
    }

    private void setState(SubscriptionState newState) {
        state.set(newState);
    }

    public void addListener(IWatcherInfoSubscriberListener listener)
            throws RemoteException {
        listenerHolder.addListener(listener);
    }

    public void removeListener(IWatcherInfoSubscriberListener listener)
            throws RemoteException {
        listenerHolder.removeListener(listener);
    }
}
