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

package javax.microedition.ims.android.core;

import android.os.RemoteException;

import javax.microedition.ims.android.util.RemoteListenerHolder;
import javax.microedition.ims.common.EventPackage;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.dialog.IncomingNotifyListener;
import javax.microedition.ims.core.sipservice.subscribe.SubscribeService;
import javax.microedition.ims.core.sipservice.subscribe.Subscription;
import javax.microedition.ims.core.sipservice.subscribe.SubscriptionInfo;
import javax.microedition.ims.core.sipservice.subscribe.SubscriptionInfoImpl;
import javax.microedition.ims.core.sipservice.subscribe.listener.*;
import javax.microedition.ims.messages.history.MessageData;
import javax.microedition.ims.messages.history.MessageDataBuilder;
import javax.microedition.ims.messages.wrappers.sip.Request;
import java.util.concurrent.atomic.AtomicReference;


public class SubscriptionImpl extends ISubscription.Stub {
    private static final String TAG = "Service - SubscriptionImpl";

    private final IServiceMethod serviceMethod;
    //private final SubscribeService subscribeService;

    //private final String remoteParty;
    private final EventPackage event;

    private Mode subscriptionMode = Mode.UNDEFINED;

    private final Subscription stackSubscriptionObj;

    private final RemoteListenerHolder<ISubscriptionListener> listenerHolder = new RemoteListenerHolder<ISubscriptionListener>(ISubscriptionListener.class);

    private final AtomicReference<SubscriptionState> state = new AtomicReference<SubscriptionState>(SubscriptionState.STATE_INACTIVE);


    enum SubscriptionState {
        /**
         * The Subscription is not active.
         */
        STATE_INACTIVE(1),

        /**
         * A Subscription request is sent and the IMS engine is waiting for a response.
         */
        STATE_PENDING(2),

        /**
         * The Subscription is active.
         */
        STATE_ACTIVE(3);

        private int code;

        private SubscriptionState(int code) {
            this.code = code;
        }
    }


    enum Mode {
        UNDEFINED,
        SUBSCRIBE,
        POLL
    }


    private class SubscriptionStateListenerImpl extends SubscriptionStateAdapter {
        private IncomingNotifyListenerImpl notifyListener;

        
        public void onSubscriptionStarted(SubscriptionStateEvent event) {
            Logger.log(TAG, "SubscriptionStateListenerImpl.onSubscriptionStarted#started");

            setState(SubscriptionState.STATE_ACTIVE);

            notifySubscriptionStarted();

            notifyListener = new IncomingNotifyListenerImpl();

            stackSubscriptionObj.addSubscriptionNotifyListener(notifyListener);

            Logger.log(TAG, "SubscriptionStateListenerImpl.onSubscriptionStarted#finished");
        }

        
        public void onSubscriptionStartFailed(SubscriptionFailedEvent event) {
            Logger.log(TAG, "SubscriptionStateListenerImpl.onSubscriptionStartFailed#started");

            setState(SubscriptionState.STATE_INACTIVE);

            notifySubscriptionStartFailed();

            unSubscribe();

            Logger.log(TAG, "SubscriptionStateListenerImpl.onSubscriptionStartFailed#finished");
        }

        
        public void onSubscriptionTerminated(SubscriptionTerminatedEvent event) {
            Logger.log(TAG, "SubscriptionStateListenerImpl.onSubscriptionTerminated#started");

            setState(SubscriptionState.STATE_INACTIVE);

            notifySubscriptionTerminated();

            unSubscribe();

            Logger.log(TAG, "SubscriptionStateListenerImpl.onSubscriptionTerminated#finished");
        }

        private void unSubscribe() {
            stackSubscriptionObj.removeSubscriptionStateListener(this);
            if (notifyListener != null) {
                stackSubscriptionObj.removeSubscriptionNotifyListener(notifyListener);
            }
        }
    }


    private class IncomingNotifyListenerImpl implements IncomingNotifyListener {
        
        public void notificationReceived(final NotifyEvent event) {
            Logger.log(TAG, "IncomingNotifyListenerImpl.notificationReceived#started");

            if (subscriptionMode == Mode.POLL) {
                setState(SubscriptionState.STATE_INACTIVE);

                stackSubscriptionObj.unsubscribe();
            }

            Request lastRequest = event.getDialog().getMessageHistory().findLastRequestByMethod(MessageType.SIP_NOTIFY);
            MessageData messageData = MessageDataBuilder.buildMessageData(lastRequest, true);
            MessageImpl notify = new MessageImpl(messageData);

            notifySubscriptionNotify(notify);

            Logger.log(TAG, "IncomingNotifyListenerImpl.notificationReceived#finished");
        }
    }


    public SubscriptionImpl(
            final ClientIdentity localParty,
            final String remoteParty,
            final EventPackage event,
            final SubscribeService subscribeService) {

        //this.remoteParty = remoteParty;
        this.event = event;
        //this.subscribeService = subscribeService;

        SubscriptionInfo subscriptionDescr = new SubscriptionInfoImpl(event, subscribeService.getExpirationTime(), "");
        stackSubscriptionObj = subscribeService.lookUpSubscription(localParty, remoteParty, subscriptionDescr);

        this.serviceMethod = new SubscriptionServiceMethod(
                remoteParty,
                stackSubscriptionObj.getDialog().getMessageHistory()
        );
    }


    public void subscribe() throws RemoteException {
        Logger.log(TAG, "subscribe#started");

        setState(SubscriptionState.STATE_PENDING);

        stackSubscriptionObj.addSubscriptionStateListener(new SubscriptionStateListenerImpl());

        subscriptionMode = Mode.SUBSCRIBE;

        stackSubscriptionObj.subscribe();

        Logger.log(TAG, "subscribe#finished");
    }

    
    public void poll() throws RemoteException {
        Logger.log(TAG, "poll#started");

        setState(SubscriptionState.STATE_PENDING);

        stackSubscriptionObj.addSubscriptionStateListener(new SubscriptionStateListenerImpl());

        subscriptionMode = Mode.POLL;

        stackSubscriptionObj.subscribe();

        Logger.log(TAG, "poll#finished");
    }

    
    public void unsubscribe() {
        Logger.log(TAG, "unsubscribe#started");

        setState(SubscriptionState.STATE_PENDING);

        stackSubscriptionObj.unsubscribe();

        Logger.log(TAG, "unsubscribe#finished");
    }

    
    public String getEvent() throws RemoteException {
        return event.stringValue();
    }

    
    public int getState() throws RemoteException {
        return state.get().code;
    }

    
    public void addListener(ISubscriptionListener listener) throws RemoteException {
        Logger.log(TAG, "addListener: listener: " + listener);

        if (listener != null) {
            listenerHolder.addListener(listener);
        }
    }

    
    public void removeListener(ISubscriptionListener listener) throws RemoteException {
        Logger.log(TAG, "removeListener: listener: " + listener);

        if (listener != null) {
            listenerHolder.removeListener(listener);
        }
    }

    
    public IServiceMethod getServiceMethod() throws RemoteException {
        return serviceMethod;
    }

    private void setState(SubscriptionState subscriptionState) {
        state.set(subscriptionState);
    }

    private void notifySubscriptionStarted() {
        Logger.log(TAG, "notifySubscriptionStarted#started");
        try {
            listenerHolder.getNotifier().subscriptionStarted();
        }
        catch (RemoteException e) {
            e.printStackTrace();
            Logger.log(TAG, e.getMessage());
        }
        Logger.log(TAG, "notifySubscriptionStarted#finished");
    }

    private void notifySubscriptionStartFailed() {
        Logger.log(TAG, "notifySubscriptionStartFailed#started");
        try {
            listenerHolder.getNotifier().subscriptionStartFailed();
        }
        catch (RemoteException e) {
            e.printStackTrace();
            Logger.log(TAG, e.getMessage());
        }
        Logger.log(TAG, "notifySubscriptionStartFailed#finished");
    }

    private void notifySubscriptionTerminated() {
        Logger.log(TAG, "notifySubscriptionTerminated#started");
        try {
            listenerHolder.getNotifier().subscriptionTerminated();
        }
        catch (RemoteException e) {
            e.printStackTrace();
            Logger.log(TAG, e.getMessage());
        }
        Logger.log(TAG, "notifySubscriptionTerminated#finished");
    }

    private void notifySubscriptionNotify(IMessage notify) {
        Logger.log(TAG, "notifySubscriptionNotify#started");
        try {
            listenerHolder.getNotifier().subscriptionNotify(notify);
        }
        catch (RemoteException e) {
            e.printStackTrace();
            Logger.log(TAG, e.getMessage());
        }
        Logger.log(TAG, "notifySubscriptionNotify#finished");
    }

}
