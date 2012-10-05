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

import javax.microedition.ims.common.ListenerHolder;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.common.Shutdownable;
import javax.microedition.ims.common.util.SIPUtil;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.FirstMessageResolver;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.DialogCallIDImpl;
import javax.microedition.ims.core.dialog.IncomingNotifyListener;
import javax.microedition.ims.core.sipservice.AbstractService;
import javax.microedition.ims.core.sipservice.subscribe.listener.NotifyServerListener;
import javax.microedition.ims.core.transaction.*;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Request;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static javax.microedition.ims.core.transaction.TransactionType.SIP_NOTIFY_SERVER;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 10-Dec-2009
 * Time: 15:40:16
 */
public class SubscribeServiceImpl
        extends AbstractService
        implements SubscribeService, IncomingRequestHandler4SubscribeService<Request> {

    private final ListenerHolder<IncomingNotifyListener> incomingNotifyListenerHolder =
            new ListenerHolder<IncomingNotifyListener>(IncomingNotifyListener.class);

    private final SubscriptionStorage subscriptionStorage;
    private final AtomicBoolean done = new AtomicBoolean(false);
    private final SubscribeService transactionSafeView;
    private int subscriptionExpirationTime;

    private final TransactionBuildUpListener<BaseSipMessage> serverNotifyListener =
            new TransactionBuildUpListener<BaseSipMessage>() {

                public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
                    assert SIP_NOTIFY_SERVER == event.getTransaction().getTransactionType();

                    Dialog dialog = (Dialog) event.getEntity();
                    final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

                    //listener will un-subscribe automatically on transaction complete
                    transaction.addListener(
                            new NotifyServerListener<BaseSipMessage>(dialog, transaction, incomingNotifyListenerHolder)
                    );
                }
            };


    public SubscribeServiceImpl(
            final StackContext stackContext,
            final TransactionManager transactionManager) {

        super(stackContext, transactionManager);

        this.subscriptionStorage = new SubscriptionStorageImpl(new SubscriptionStorageImpl.SubscriptionProvider() {

            public Subscription onDemand(
                    final ClientIdentity localParty,
                    final String remoteParty,
                    final SubscriptionInfo info) {

                Dialog dialog = stackContext.getDialogStorage().getDialog(
                        localParty,
                        remoteParty,
                        new DialogCallIDImpl(SIPUtil.newCallId())
                );

                return TransactionUtils.wrap(
                        new SubscriptionImpl(
                                stackContext,
                                transactionManager,
                                SubscribeServiceImpl.this,
                                dialog,
                                info
                        ),
                        Subscription.class,
                        Shutdownable.class
                );
            }
        });

        transactionManager.addListener(serverNotifyListener, TransactionType.Name.SIP_NOTIFY_SERVER);

        transactionSafeView = TransactionUtils.wrap(this, SubscribeService.class);

        subscriptionExpirationTime = (int)(stackContext.getConfig().getSubscriptionExpirationSeconds());
    }

    public SubscribeService getTransactionSafeView() {
        return transactionSafeView;
    }


    public void addIncomingNotifyListener(IncomingNotifyListener listener) {
        incomingNotifyListenerHolder.addListener(listener);
    }


    public void addIncomingNotifyListener(Dialog dialog, IncomingNotifyListener listener) {
        incomingNotifyListenerHolder.addListener(listener, dialog);
    }


    public void removeIncomingNotifyListener(IncomingNotifyListener listener) {
        incomingNotifyListenerHolder.removeListener(listener);
    }


    public Subscription lookUpSubscription(final ClientIdentity localParty, final String remoteParty, final SubscriptionInfo info) {
        if (done.get()) {
            throw new IllegalStateException("Cannot lookup retValue after SubscribeService shutdown.");
        }

        return subscriptionStorage.lookUp(localParty, remoteParty, info);
    }

    public Subscription findSubscription(
            final ClientIdentity localParty,
            final String remoteParty,
            final SubscriptionInfo info) {

        if (done.get()) {
            throw new IllegalStateException("Cannot find retValue after SubscribeService shutdown.");
        }

        SubscriptionKey subscriptionKey = new SubscriptionKeyDefaultImpl(localParty.getAppID(),/*remoteParty,*/ info.getEvent());

        return subscriptionStorage.find(subscriptionKey);
    }

    public Subscription lookUpDocumentChangesSubscription(
            final ClientIdentity localParty,
            final String remoteParty,
            final List<String> documentUris) {

        return subscriptionStorage.lookUp(
                localParty,
                remoteParty,
                SubscriptionHelper.createDocumentChangeSubscriptionInfo(documentUris)
        );
    }

    public int getExpirationTime() {
        return subscriptionExpirationTime;
    }

    public void unBind(final Subscription subscription) {
        subscriptionStorage.unBind(subscription);

        if (subscription instanceof Shutdownable) {
            ((Shutdownable) subscription).shutdown();
        }
    }

    public void handleIncomingNotify(final Request msg) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();
        assert msg != null && MessageType.SIP_NOTIFY == MessageType.parse(msg.getMethod());

        Logger.log("ReferServiceImpl.handleIncomingNotify", "Handle incoming SIP_NOTIFY message");
        if (!done.get()) {

            final Dialog dialog = getStackContext().getDialogStorage().findDialogForMessage(msg);
            assert dialog != null;

            dialog.getMessageHistory().addMessage(msg, true);

            final TransactionManager transactionManager = getTransactionManager();
            transactionManager.addListener(new FirstMessageResolver(SIP_NOTIFY_SERVER.getName(), dialog, msg, transactionManager));

            final Transaction transaction = transactionManager.newTransaction(dialog, null, SIP_NOTIFY_SERVER);
            runAsynchronously(transaction, TRANSACTION_TIMEOUT);
        }
    }


    public void shutdown() {

        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Shutdowning SubscribeService");

        if (done.compareAndSet(false, true)) {
            ((Shutdownable) subscriptionStorage).shutdown();
            incomingNotifyListenerHolder.shutdown();
            getTransactionManager().removeListener(serverNotifyListener);
        }
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "SubscribeService shutdown successfully");
    }
}
