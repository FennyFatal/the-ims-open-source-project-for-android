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

package javax.microedition.ims.core.sipservice.subscribe;

import javax.microedition.ims.common.*;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.IncomingNotifyListener;
import javax.microedition.ims.core.sipservice.AbstractService;
import javax.microedition.ims.core.sipservice.RefreshHelper;
import javax.microedition.ims.core.sipservice.RefreshListener;
import javax.microedition.ims.core.sipservice.subscribe.listener.*;
import javax.microedition.ims.core.transaction.*;
import javax.microedition.ims.core.transaction.client.ClientTransaction;
import javax.microedition.ims.core.transaction.client.SubscribeCommonTransaction;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 25.5.2010
 * Time: 10.04.09
 */
public class SubscriptionImpl implements Subscription, Shutdownable {

    private static final DefaultTimeoutUnit TRANSACTION_TIMEOUT = new DefaultTimeoutUnit(
            RepetitiousTaskManager.TRANSACTION_TIMEOUT_INTERVAL,
            TimeUnit.MILLISECONDS
    );

    private final StackContext context;
    private final TransactionManager transactionManager;
    private final SubscribeService subscribeService;

    private final ListenerHolder<SubscriptionStateListener> subscriptionStateListenerHolder
            = new ListenerHolder<SubscriptionStateListener>(SubscriptionStateListener.class);

    private final ListenerHolder<IncomingNotifyListener> incomingNotifyListenerHolder =
            new ListenerHolder<IncomingNotifyListener>(IncomingNotifyListener.class);


    private final Dialog dialog;
    private final SubscriptionInfo info;
    private final String refreshTaskName;

    private final AtomicBoolean done = new AtomicBoolean(false);

    private final StateHolder stateHolder = new StateHolder();

    private final TransactionBuildUpListener<BaseSipMessage> subscribeListener =
            new TransactionBuildUpListener<BaseSipMessage>() {
                
                public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
                    assert TransactionType.SIP_SUBSCRIBE == event.getTransaction().getTransactionType();

                    if (dialog == event.getEntity()) {
                        handleTransactionCreate(event);
                    }
                    else {
                        assert dialog == event.getEntity() :
                                "Probably more than one simultaneous subscription detected. " +
                                        "You may rethink this code again and remove this assert";
                    }
                }

                private void handleTransactionCreate(TransactionBuildUpEvent<BaseSipMessage> event) {
                    assert TransactionType.SIP_SUBSCRIBE == event.getTransaction().getTransactionType();
                    assert dialog == event.getEntity();

                    final Dialog dialog = (Dialog) event.getEntity();
                    final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

                    transaction.addListener(
                            new TransactionListenerAdapter<BaseSipMessage>() {

                                public void onTransactionInit(
                                        final TransactionEvent<BaseSipMessage> transactionEvent) {

                                    final SubscribeTransactionDescription transactionDescription
                                            = getTransactionDescription(transactionEvent);

                                    State expectedState = State.NO_SUBSCRIPTION;
                                    if (transactionDescription != null &&
                                            SubscribeTransactionDescription.Type.REFRESH == transactionDescription.getType()) {

                                        expectedState = State.SUBSCRIBED;
                                    }


                                    final boolean stateChanged =
                                            stateHolder.compareAndTransitNextState(
                                                    expectedState,
                                                    true,
                                                    transactionDescription
                                            );

                                    assert stateChanged : "Can not proceed to next state. Current state " +
                                            stateHolder.getState() + " expected state " + expectedState;
                                }
                            }
                    );

                    transaction.addListener(new TransactionListenerAdapter<BaseSipMessage>() {
                        
                        public void onTransactionComplete(
                                final TransactionEvent<BaseSipMessage> event,
                                final TransactionResult.Reason reason) {

                            super.onTransactionComplete(event, reason);
                            final Boolean success = event.getTransaction().getTransactionValue().getValue();

                            notifyListeners(success, dialog, info);

                            final boolean stateChanged = stateHolder.compareAndTransitNextState(
                                    State.SUBSCRIBING,
                                    success,
                                    getTransactionDescription(event)
                            );
                            assert stateChanged : "Can not proceed to next state. Current state " +
                                    stateHolder.getState() + " expected state " + State.SUBSCRIBING;

                        }

                        private void notifyListeners(Boolean success, final Dialog dialog, final SubscriptionInfo info) {
                            if (State.SUBSCRIBED == stateHolder.getState()) {
                                if (success) {

                                    final SubscriptionStateEvent subscriptionStateEvent = new DefaultSubscriptionStateEvent(
                                            dialog,
                                            info
                                    );

                                    subscriptionStateListenerHolder.getNotifier().
                                            onSubscriptionRefreshed(subscriptionStateEvent);
                                }
                                else {


                                    final SubscriptionFailedEvent subscriptionStateEvent = new DefaultSubscriptionFailedEvent(
                                            dialog,
                                            info,
                                            "fake",
                                            0
                                    );

                                    subscriptionStateListenerHolder.getNotifier().
                                            onSubscriptionRefreshFailed(subscriptionStateEvent);
                                }
                            }
                            else {
                                if (success) {
                                    final SubscriptionStateEvent subscriptionStateEvent = new DefaultSubscriptionStateEvent(
                                            dialog,
                                            info
                                    );

                                    subscriptionStateListenerHolder.getNotifier().
                                            onSubscriptionStarted(subscriptionStateEvent);
                                }
                                else {
                                    //TODO AK change hardcoded parameters
                                    final SubscriptionFailedEvent subscriptionStateEvent = new DefaultSubscriptionFailedEvent(
                                            dialog,
                                            info,
                                            "no resoureces",
                                            -1
                                    );

                                    subscriptionStateListenerHolder.getNotifier().
                                            onSubscriptionStartFailed(subscriptionStateEvent);
                                }
                            }
                        }
                    });

                    transaction.addListener(
                            new RefreshListener(
                                    context.getRepetitiousTaskManager(),
                                    refreshTaskName,
                                    Dialog.ParamKey.SUBSCRIPTION_EXPIRES,
                                    dialog,
                                    new RefreshHelper.Refresher() {
                                        
                                        public void refresh(final long timeOutInMillis) {
                                            refreshSubscription();
                                        }
                                    },
                                    info.getExpirationSeconds()
                            )
                    );
                }
            };


    private final TransactionBuildUpListener<BaseSipMessage> unsubscribeListener =
            new TransactionBuildUpListener<BaseSipMessage>() {
                
                public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
                    assert TransactionType.SIP_UNSUBSCRIBE == event.getTransaction().getTransactionType();

                    if (dialog == event.getEntity()) {
                        handleTransactionCreate(event);
                    }
                    else {
                        assert dialog == event.getEntity() :
                                "Probably more than one simultaneous subscription detected. " +
                                        "You may rethink this code again and remove this assert";
                    }
                }

                private void handleTransactionCreate(TransactionBuildUpEvent<BaseSipMessage> event) {
                    assert TransactionType.SIP_UNSUBSCRIBE == event.getTransaction().getTransactionType();
                    assert dialog == event.getEntity();

                    final Dialog dialog = (Dialog) event.getEntity();
                    final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

                    transaction.addListener(new TransactionListenerAdapter<BaseSipMessage>() {
                        public void onTransactionInit(TransactionEvent<BaseSipMessage> transactionEvent) {
                            final boolean stateChanged = stateHolder.compareAndTransitNextState(
                                    State.SUBSCRIBED,
                                    true,
                                    getTransactionDescription(transactionEvent)
                            );
                            assert stateChanged : "Can not proceed to next state. Current state " +
                                    stateHolder.getState() + " expected state " + State.SUBSCRIBED;
                        }
                    });

                    transaction.addListener(new TransactionListenerAdapter<BaseSipMessage>() {
                        
                        public void onTransactionComplete(
                                final TransactionEvent<BaseSipMessage> event,
                                final TransactionResult.Reason reason) {

                            super.onTransactionComplete(event, reason);

                            /* finalizeSubscription(new RemoteStateDefault.RemoteStateBuilder()
                            .value(RemoteStateValue.TERMINATED).reason(RemoteStateReason.NORESOURCE).build());*/
                        }

                    });


                    transaction.addListener(new TransactionListenerAdapter<BaseSipMessage>() {
                        
                        public void onTransactionInit(final TransactionEvent event) {
                            dialog.putCustomParameter(Dialog.ParamKey.SUBSCRIPTION_EXPIRES, 0L);
                        }
                    });
                }
            };

    private SubscribeTransactionDescription getTransactionDescription(
            final TransactionEvent<BaseSipMessage> transactionEvent) {

        SubscribeTransactionDescription retValue = null;

        if (transactionEvent.getTransaction() instanceof CommonSIPTransaction) {
            final CommonSIPTransaction commonSIPTransaction =
                    (CommonSIPTransaction) transactionEvent.getTransaction();

            final TransactionDescription transactionDescription = commonSIPTransaction.getDescription();
            if (transactionDescription instanceof SubscribeTransactionDescription) {
                retValue = (SubscribeTransactionDescription) transactionDescription;
            }
        }

        return retValue;
    }

    private final IncomingNotifyListener notifyResender;

    /*
    The SUBSCRIBE method [5] is used by a user agent to establish a subscription
    for the purpose of receiving notifications (via the NOTIFY method)
    about a particular event. A successful subscription establishes a dialog between
    the UAC and the UAS. The subscription request contains an Expires (see
    Section 6.4.7) header field, which indicates the desired duration of the existence
    of the subscription. After this time period passes, the subscription is automatically
    terminated. The subscription can be refreshed by sending another
    SUBSCRIBE within the dialog before the expiration time
     */

    public SubscriptionImpl(
            final StackContext context,
            final TransactionManager transactionManager,
            final SubscribeService subscribeService,
            final Dialog dialog,
            final SubscriptionInfo info) {
        this.context = context;
        this.transactionManager = transactionManager;
        this.subscribeService = subscribeService;

        dialog.putCustomParameter(Dialog.ParamKey.SUBSCRIBE_INFO, info);
        this.dialog = dialog;
        this.info = info;

        this.refreshTaskName = "Subscription_" + dialog.getCallId() + "_" + hashCode();

        subscribeTransactionListeners();

        subscribeService.addIncomingNotifyListener(dialog, notifyResender = new IncomingNotifyListener() {
            
            public void notificationReceived(final NotifyEvent event) {
                assert event.getType() == dialog;
                incomingNotifyListenerHolder.getNotifier().notificationReceived(event);

                final RemoteState subscriptionState = event.getNotifyInfo().getNotifySubscriptionState();
                if (RemoteStateValue.TERMINATED == subscriptionState.getValue()) {
                    finalizeSubscription(subscriptionState);
                }
            }
        });

        addSubscriptionStateListener(new SubscriptionStateAdapter() {
            
            public void onSubscriptionTerminated(SubscriptionTerminatedEvent event) {
                removeSubscriptionStateListener(this);
                subscribeService.removeIncomingNotifyListener(notifyResender);
            }
        });
    }

    
    public SubscriptionInfo getDescription() {
        return info;
    }

    
    public Dialog getDialog() {
        return dialog;
    }

    /*
    The SUBSCRIBE method [5] is used by a user agent to establish a subscription
    for the purpose of receiving notifications (via the NOTIFY method)
    about a particular event. A successful subscription establishes a dialog between
    the UAC and the UAS. The subscription request contains an Expires (see
    Section 6.4.7) header field, which indicates the desired duration of the existence
    of the subscription. After this time period passes, the subscription is automatically
    terminated. The subscription can be refreshed by sending another
    SUBSCRIBE within the dialog before the expiration time
     */

    public void subscribe() {
        doSubscription(
                TransactionType.SIP_SUBSCRIBE,
                dialog,
                new SubscribeTransactionDescriptionImpl(info, SubscribeTransactionDescription.Type.SUBSCRIBE)
        );
    }

    /*
    There is no "UNSUBSCRIBE" method used in SIP, instead a SUBSCRIBE with
    Expires:0 requests the termination of a subscription and hence the dialog. A
    terminated subscription (either due to timeout out or a termination request) will
    result in a final NOTIFY indicating that the subscription has been terminated
    (see Section 4.1.9 on NOTIFY).
     */

    public void unsubscribe() {
        doUnsubscribe();
    }

    private void doUnsubscribe() {
        /*final Dialog transactionEntity = context.getDialogStorage().getDialog(
                dialog.getLocalParty(),
                dialog.getRemoteParty(),
                new DialogCallIDImpl(SIPUtil.newCallId())
        );

        transactionEntity.putCustomParameter(Dialog.ParamKey.SUBSCRIBE_INFO, info);
        transactionEntity.putCustomParameter(Dialog.ParamKey.SUBSCRIPTION_EXPIRES, 0);*/

        doSubscription(
                TransactionType.SIP_UNSUBSCRIBE,
                dialog,
                new SubscribeTransactionDescriptionImpl(info, SubscribeTransactionDescription.Type.UNSUBSCRIBE)
        );
        RefreshHelper.cancelRefresh(context.getRepetitiousTaskManager(), refreshTaskName);
    }

    
    public void addSubscriptionNotifyListener(final IncomingNotifyListener listener) {
        if (!done.get()) {
            incomingNotifyListenerHolder.addListener(listener);
        }
    }

    
    public void addSubscriptionStateListener(final SubscriptionStateListener listener) {
        if (!done.get()) {
            subscriptionStateListenerHolder.addListener(listener);
        }
    }

    
    public void removeSubscriptionNotifyListener(final IncomingNotifyListener listener) {
        incomingNotifyListenerHolder.removeListener(listener);
    }

    
    public void removeSubscriptionStateListener(SubscriptionStateListener listener) {
        subscriptionStateListenerHolder.removeListener(listener);
    }

    private void refreshSubscription() {
        Logger.log("Starting subscription refresh...");
        doSubscription(
                TransactionType.SIP_SUBSCRIBE,
                dialog,
                new SubscribeTransactionDescriptionImpl(info, SubscribeTransactionDescription.Type.REFRESH)
        );
    }


    private void doSubscription(
            final TransactionType<ClientTransaction, ? extends SubscribeCommonTransaction> transactionType,
            final Dialog transactionEntity,
            final TransactionDescription transactionDescription) {

        assert TransactionType.SIP_SUBSCRIBE == transactionType ||
                TransactionType.SIP_UNSUBSCRIBE == transactionType;

        if (!done.get()) {

            final Transaction<Boolean, BaseSipMessage> transaction =
                    transactionManager.newTransaction(transactionEntity, transactionDescription, transactionType);

            AbstractService.runAsynchronously(transaction, TRANSACTION_TIMEOUT);
        }
        else {
            assert false : "Subscription already shutdown";
        }
    }

    private void finalizeSubscription(final RemoteState remoteState) {

        final SubscriptionTerminatedEvent subscriptionStateEvent = new DefaultSubscriptionTerminatedEvent(
                dialog,
                info,
                remoteState.getReason(),
                remoteState.getRetryAfter()
        );

        subscriptionStateListenerHolder.getNotifier().onSubscriptionTerminated(subscriptionStateEvent);


        stateHolder.transitFinalState();
        shutdown();
    }

    private void subscribeTransactionListeners() {
        transactionManager.addListener(subscribeListener, TransactionType.Name.SIP_SUBSCRIBE, dialog);
        transactionManager.addListener(unsubscribeListener, TransactionType.Name.SIP_UNSUBSCRIBE, dialog);
    }

    private void unsubscribeTransactionListeners() {
        transactionManager.removeListener(subscribeListener);
        transactionManager.removeListener(unsubscribeListener);
    }

    
    public void shutdown() {
        subscriptionStateListenerHolder.shutdown();
        incomingNotifyListenerHolder.shutdown();
        subscribeService.removeIncomingNotifyListener(notifyResender);

        unsubscribeTransactionListeners();

        RefreshHelper.cancelRefresh(context.getRepetitiousTaskManager(), refreshTaskName);
    }

}
