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

package javax.microedition.ims.core.sipservice.refer;

import javax.microedition.ims.common.*;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.FirstMessageResolver;
import javax.microedition.ims.core.IMSStackException;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.*;
import javax.microedition.ims.core.sipservice.AbstractService;
import javax.microedition.ims.core.sipservice.Acceptable;
import javax.microedition.ims.core.sipservice.ReferState;
import javax.microedition.ims.core.sipservice.invite.InviteService;
import javax.microedition.ims.core.sipservice.invite.listener.MiddleManForServerMessageBuildingSupport;
import javax.microedition.ims.core.sipservice.invite.listener.ReferStateMiddleMan;
import javax.microedition.ims.core.sipservice.refer.listener.ReferServerListener;
import javax.microedition.ims.core.sipservice.refer.listener.ThirdPartySessionInitializationListener;
import javax.microedition.ims.core.sipservice.subscribe.*;
import javax.microedition.ims.core.sipservice.subscribe.listener.NotifyServerListener;
import javax.microedition.ims.core.transaction.*;
import javax.microedition.ims.core.transaction.client.NotifyClntTransaction;
import javax.microedition.ims.core.transaction.server.ReferSrvTransaction;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Request;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static javax.microedition.ims.core.transaction.TransactionType.*;

public class ReferServiceImpl<T> extends AbstractService implements ReferService, IncomingRequestHandler4ReferService<Request> {

    private final ListenerHolder<ReferStateListener> referStateListenerHolder =
            new ListenerHolder<ReferStateListener>(ReferStateListener.class);

    private final ListenerHolder<IncomingReferListener> incomingReferListenerHolder =
            new ListenerHolder<IncomingReferListener>(IncomingReferListener.class);

    private final ListenerHolder<IncomingNotifyListener> incomingNotifyListenerHolder =
            new ListenerHolder<IncomingNotifyListener>(IncomingNotifyListener.class);

    private final InviteService inviteService;
    private final ReferService transactionSafeView;

    private final AtomicBoolean done = new AtomicBoolean(false);

    private final TransactionBuildUpListener<BaseSipMessage> clientReferListener = new TransactionBuildUpListener<BaseSipMessage>() {
        
        public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
            assert SIP_REFER_CLIENT == event.getTransaction().getTransactionType();

            Dialog dialog = (Dialog) event.getEntity();
            final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(
                    new ReferStateMiddleMan<BaseSipMessage>(transaction, dialog, referStateListenerHolder)
            );
            //listener will un-subscribe automatically on transaction complete
//            listenerSupport.addRegistrationListener(
//                    new DialogCleanUpListener(listenerSupport, DIALOG, null) {
//                        
//                        protected void onDialogCleanUp(final Dialog DIALOG) {
//                            getStackContext().getDialogStorage().cleanUpDialog(DIALOG);
//                        }
//                    }
//            );
        }
    };

    private final TransactionBuildUpListener<BaseSipMessage> clientNotifyListener = new TransactionBuildUpListener<BaseSipMessage>() {
        
        public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
            assert SIP_NOTIFY_CLIENT == event.getTransaction().getTransactionType();

            Dialog dialog = (Dialog) event.getEntity();
            final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(
                    new ReferStateMiddleMan<BaseSipMessage>(transaction, dialog, referStateListenerHolder)
            );
            //listener will un-subscribe automatically on transaction complete
//            listenerSupport.addRegistrationListener(
//                    new DialogCleanUpListener(listenerSupport, DIALOG, null) {
//                        
//                        protected void onDialogCleanUp(final Dialog DIALOG) {
//                            getStackContext().getDialogStorage().cleanUpDialog(DIALOG);
//                        }
//                    }
//            );
        }
    };

    private final TransactionBuildUpListener<BaseSipMessage> serverReferListener =
            new TransactionBuildUpListener<BaseSipMessage>() {
                
                public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
                    assert SIP_REFER_SERVER == event.getTransaction().getTransactionType();

                    Dialog dialog = (Dialog) event.getEntity();
                    final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

                    //listener will un-subscribe automatically on transaction complete

                    final Acceptable<Refer> acceptable = new Acceptable<Refer>() {

                        
                        public void reject(Refer refer, int statusCode, String alternativeUserAddress) {
                            doReject(refer, statusCode, alternativeUserAddress);
                        }

                        
                        public void accept(Refer refer) {
                            //TODO temporary commented code : doAccept(DIALOG);
                            //uncomment this code

                            //doAccept(DIALOG);
                        }
                    };
                    transaction.addListener(
                            new ReferServerListener<BaseSipMessage>(
                                    dialog,
                                    TransactionUtils.wrap(acceptable, Acceptable.class),
                                    transaction,
                                    incomingReferListenerHolder
                            )
                    );
                    //listener will un-subscribe automatically on transaction complete
                    transaction.addListener(new MiddleManForServerMessageBuildingSupport(transaction, dialog));
                    //listener will un-subscribe automatically on transaction complete
                    transaction.addListener(
                            new ReferStateMiddleMan<BaseSipMessage>(transaction, dialog, referStateListenerHolder)
                    );
                    //listener will un-subscribe automatically on transaction complete
//            listenerSupport.addRegistrationListener(
//                    new DialogCleanUpListener(listenerSupport, DIALOG, false){
//                        
//                        protected void onDialogCleanUp(final Dialog DIALOG) {
//                             getStackContext().getDialogStorage().cleanUpDialog(DIALOG);
//                        }
//                    }
//            );
                }
            };

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
                    //listener will un-subscribe automatically on transaction complete
                    transaction.addListener(
                            new MiddleManForServerMessageBuildingSupport(transaction, dialog)
                    );
                    //listener will un-subscribe automatically on transaction complete
                    transaction.addListener(
                            new ReferStateMiddleMan<BaseSipMessage>(transaction, dialog, referStateListenerHolder)
                    );
                    //listener will un-subscribe automatically on transaction complete
//            listenerSupport.addRegistrationListener(
//                    new DialogCleanUpListener(listenerSupport, DIALOG, false){
//                        
//                        protected void onDialogCleanUp(final Dialog DIALOG) {
//                             getStackContext().getDialogStorage().cleanUpDialog(DIALOG);
//                        }
//                    }
//            );
                }
            };

    private final TemporaryStorage<Refer> pendingRefers = new TemporaryStorageImpl<Refer>(
            new TemporaryStorage.Callback<Refer>() {
                
                public void onDelete(final Refer element) {

                    stopSubscription(element.getDialog(), "SIP/2.0 603 Declined");

                    referStateListenerHolder.getNotifier().onReferenceTerminated(
                            new DefaultReferStateEvent(
                                    element.getDialog(),
                                    ReferState.REFERENCE_TERMINATED
                            )
                    );
                }
            },
            new DefaultTimeoutUnit(60, TimeUnit.SECONDS),
            getStackContext().getRepetitiousTaskManager()
    );

    public ReferServiceImpl(
            final StackContext stackContext,
            final TransactionManager transactionManager,
            final InviteService inviteService) {
        super(stackContext, transactionManager);

        this.inviteService = inviteService;

        transactionSafeView = TransactionUtils.wrap(this, ReferService.class);

        subscribeToTransactionManager();

        addIncomingReferListener(

                //TODO: unsubscribe on shutdown
                new IncomingReferListener() {
                    
                    public void referenceReceived(IncomingReferEvent event) {
                        pendingRefers.add(event.getRefer());

                        final RemoteState state = new RemoteStateDefault.RemoteStateBuilder().
                                value(RemoteStateValue.ACTIVE).build();

                        DefaultNotifyInfo notifyInfo =
                                new DefaultNotifyInfo(EventPackage.REFER, state, new String[]{"SIP/2.0 100 Trying"});

                        doNotifyByRefer(event.getRefer().getDialog(), notifyInfo);
                    }
                }
        );
    }

    public ReferService getTransactionSafeView() {
        return transactionSafeView;
    }

    
    public void refer(Refer refer) {
        assert TransactionUtils.isTransactionExecutionThread() :
                "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        assert !done.get();

        if (!done.get()) {
            refer.getDialog().setReferTo(refer);

            final Transaction transaction = getTransactionManager().lookUpTransaction(
                    refer.getDialog(),
                    null,
                    SIP_REFER_CLIENT
            );

            runAsynchronously((Transaction<Boolean, BaseSipMessage>) transaction, TRANSACTION_TIMEOUT);
        }
    }

    
    public void createSubscription(
            final Dialog dialogToObserve,
            final Refer refer,
            final ThirdPartySessionInitializationListener listener) throws IMSStackException {

        if (!pendingRefers.containsAndRemove(refer)) {
            throw new IMSStackException("Stale REFER " + refer.toString());
        }

        inviteService.addDialogStateListener(
                dialogToObserve,

                /**
                 * RFC3515
                 A minimal, but complete, implementation can respond with a single
                 SIP_NOTIFY containing either the body:

                 SIP/2.0 100 Trying

                 if the subscription is pending, the body:

                 SIP/2.0 200 OK

                 if the reference was successful, the body:

                 SIP/2.0 503 Service Unavailable

                 if the reference failed, or the body:

                 SIP/2.0 603 Declined

                 if the SIP_REFER request was accepted before approval to follow the
                 reference could be obtained and that approval was subsequently denied
                 (see Section 2.4.7).
                 */
                new DialogStateListenerAdapter<BaseSipMessage>() {

                    
                    public void onSessionAlerting(final DialogStateEvent<BaseSipMessage> event) {
                        super.onSessionAlerting(event);

                        final RemoteState state = new RemoteStateDefault.RemoteStateBuilder().
                                value(RemoteStateValue.ACTIVE).build();

                        DefaultNotifyInfo notifyInfo =
                                new DefaultNotifyInfo(EventPackage.REFER, state, new String[]{"SIP/2.0 100 Trying"});

                        doNotifyByRefer(refer.getDialog(), notifyInfo);
                    }

                    
                    public void onSessionStartFailed(final DialogStateEvent<BaseSipMessage> event) {
                        super.onSessionStartFailed(event);
                        stopSubscription(refer.getDialog(), "SIP/2.0 503 Service Unavailable");

                        listener.onSessionStartFailed();
                    }

                    
                    public void onSessionStarted(final DialogStateEvent<BaseSipMessage> event) {
                        super.onSessionStarted(event);
                        stopSubscription(refer.getDialog(), "SIP/2.0 200 OK");

                        listener.onSessionStarted();
                    }

                    /*
                        private String getBodyMessage(final DialogStateEvent<T> event) {
                        SipMessageUtil<T> sipMessageUtil = MessageUtilHolder.getSIPMessageUtil();
                        String responseLine = sipMessageUtil.getResponseLine(event.getTriggeringMessage());
                        return responseLine == null ? "SIP/2.0 100 Trying" : responseLine;
                    }*/
                }
        );
    }

    private void stopSubscription(final Dialog dialogToNotify, String bodyMessage) {

        final RemoteState state = new RemoteStateDefault.RemoteStateBuilder().
                value(RemoteStateValue.TERMINATED).reason(RemoteStateReason.NORESOURCE).build();

        DefaultNotifyInfo notifyInfo =
                new DefaultNotifyInfo(EventPackage.REFER, state, new String[]{bodyMessage});

        doNotifyByRefer(dialogToNotify, notifyInfo);
    }

    
    public void notifyByRefer(Dialog dialog, DefaultNotifyInfo notifyInfo) {
        doNotifyByRefer(dialog, notifyInfo);
    }

    private void doNotifyByRefer(Dialog dialog, DefaultNotifyInfo notifyInfo) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();

        if (!done.get()) {
            final NotifyClntTransaction transaction = getTransactionManager().newTransaction(
                    dialog,
                    null,
                    SIP_NOTIFY_CLIENT
            );

            transaction.setNotifyInfo(notifyInfo);

            runAsynchronously((Transaction<Boolean, BaseSipMessage>) transaction, TRANSACTION_TIMEOUT);
        }
    }

    
    public void handleIncomingRefer(final Request msg) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();
        assert msg != null && MessageType.SIP_REFER == MessageType.parse(msg.getMethod());

        Logger.log("ReferServiceImpl.handleIncomingRefer", "Handle incoming SIP_REFER message");
        if (!done.get()) {

//            final Dialog DIALOG = getStackContext().getDialogStorage().findDialogForMessage(msg);
//            assert DIALOG != null;
            //assert STATED == DIALOG.getState();

            //ClientIdentity localParty = getStackContext().getStackClientRegistry().findAddressee(msg.getTo().getUriBuilder().getShortURI());
            ClientIdentity localParty = getStackContext().getClientRouter().findAddressee(msg);
            assert localParty != null;

            final Dialog dialog = getStackContext().getDialogStorage().getDialogForIncomingMessage(localParty, msg);

            //DIALOG.putCustomParameter(ParamKey.RESPONSE_CODE, 200);
            dialog.getMessageHistory().addMessage(msg, true);
            //DIALOG.putCustomParameter(Dialog.ParamKey.INITIAL_MESSAGE, msg);
            //DIALOG.putCustomParameter(Dialog.ParamKey.LAST_MESSAGE, msg);

            final TransactionManager transactionManager = getTransactionManager();
            transactionManager.addListener(new FirstMessageResolver(SIP_REFER_SERVER.getName(), dialog, msg, transactionManager));

            final ReferSrvTransaction transaction = transactionManager.lookUpTransaction(
                    dialog,
                    null,
                    SIP_REFER_SERVER
            );

            runAsynchronously((Transaction<Boolean, BaseSipMessage>) transaction, TRANSACTION_TIMEOUT);
            Logger.log("ReferServiceImpl.handleIncomingRefer", "Transaction runned, DIALOG : " + dialog);
        }
        else {
            Logger.log("ReferServiceImpl.handleIncomingRefer", "Service already shutdowned");
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
            //assert STATED == DIALOG.getState();

            //DIALOG.putCustomParameter(ParamKey.RESPONSE_CODE, 200);
            dialog.getMessageHistory().addMessage(msg, true);
            //DIALOG.putCustomParameter(Dialog.ParamKey.INITIAL_MESSAGE, msg);
            //DIALOG.putCustomParameter(Dialog.ParamKey.LAST_MESSAGE, msg);

            final TransactionManager transactionManager = getTransactionManager();
            transactionManager.addListener(new FirstMessageResolver(SIP_NOTIFY_SERVER.getName(), dialog, msg, transactionManager));

            final Transaction transaction = transactionManager.newTransaction(
                    dialog,
                    null,
                    SIP_NOTIFY_SERVER
            );

            runAsynchronously(transaction, TRANSACTION_TIMEOUT);
        }
    }

    void doAccept(Dialog dialog) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();

        if (!done.get()) {
            getTransactionManager().findTransaction(dialog, SIP_REFER_SERVER).accept();
        }
    }

    void doReject(Refer refer, int statusCode, String alternativeUserAddress) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();

        if (!done.get()) {
            //Old code goes here
            //getTransactionManager().findTransaction(DIALOG, SIP_REFER_SERVER).reject(statusCode, alternativeUserAddress);

            if (pendingRefers.containsAndRemove(refer)) {
                stopSubscription(refer.getDialog(), "SIP/2.0 603 Declined");
            }
        }
    }


    
    public void addReferStateListener(Dialog dialog, ReferStateListener listener) {
        referStateListenerHolder.addListener(listener, dialog);
    }

    
    public void addReferStateListener(ReferStateListener listener) {
        referStateListenerHolder.addListener(listener);
    }

    
    public void removeReferStateListener(ReferStateListener listener) {
        referStateListenerHolder.removeListener(listener);
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


    
    public void addIncomingReferListener(IncomingReferListener listener) {
        incomingReferListenerHolder.addListener(listener);
    }

    
    public void addIncomingReferListener(Dialog dialog, IncomingReferListener listener) {
        incomingReferListenerHolder.addListener(listener, dialog);
    }

    
    public void removeIncomingReferListener(IncomingReferListener listener) {
        incomingReferListenerHolder.removeListener(listener);
    }


    
    public void shutdown() {
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Shutdowning ReferService");
        if (done.compareAndSet(false, true)) {
            unSubscribeFromTransactionManager();

            referStateListenerHolder.shutdown();
            incomingReferListenerHolder.shutdown();
            incomingNotifyListenerHolder.shutdown();

            ((Shutdownable) pendingRefers).shutdown();
        }
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "ReferService shutdown successfully");
    }

    private void subscribeToTransactionManager() {
        final TransactionManager trManager = getTransactionManager();
        trManager.addListener(clientReferListener, TransactionType.Name.SIP_REFER_CLIENT);
        trManager.addListener(serverReferListener, TransactionType.Name.SIP_REFER_SERVER);
        trManager.addListener(clientNotifyListener, TransactionType.Name.SIP_NOTIFY_CLIENT);
        trManager.addListener(serverNotifyListener, TransactionType.Name.SIP_NOTIFY_SERVER);
    }

    private void unSubscribeFromTransactionManager() {
        final TransactionManager trManager = getTransactionManager();
        trManager.removeListener(clientReferListener);
        trManager.removeListener(serverReferListener);
        trManager.removeListener(clientNotifyListener);
        trManager.removeListener(serverNotifyListener);
    }

}
