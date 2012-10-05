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

package javax.microedition.ims.core;

import javax.microedition.ims.common.*;
import javax.microedition.ims.core.auth.AuthorizationRegistry;
import javax.microedition.ims.core.connection.ConnectionDataListener;
import javax.microedition.ims.core.connection.ConnectionDataProvider;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.DialogStorageEvent;
import javax.microedition.ims.core.dialog.DialogStorageListener;
import javax.microedition.ims.core.dialog.DialogStorageListenerAdapter;
import javax.microedition.ims.core.dispatcher.*;
import javax.microedition.ims.core.env.ConnectionManager;
import javax.microedition.ims.core.msrp.IncomingMsrpMessageHandler;
import javax.microedition.ims.core.msrp.MSRPService;
import javax.microedition.ims.core.msrp.MSRPServiceImpl;
import javax.microedition.ims.core.presence.PresenceService;
import javax.microedition.ims.core.presence.PresenceServiceImpl;
import javax.microedition.ims.core.sipservice.invite.DialogStateException;
import javax.microedition.ims.core.sipservice.invite.IncomingRequestHandler;
import javax.microedition.ims.core.sipservice.invite.InviteService;
import javax.microedition.ims.core.sipservice.invite.InviteServiceImpl;
import javax.microedition.ims.core.sipservice.options.DefaultOptionsService;
import javax.microedition.ims.core.sipservice.options.OptionsService;
import javax.microedition.ims.core.sipservice.pagemessage.PageMessageService;
import javax.microedition.ims.core.sipservice.pagemessage.PageMessageServiceImpl;
import javax.microedition.ims.core.sipservice.publish.PublishService;
import javax.microedition.ims.core.sipservice.publish.PublishServiceImpl;
import javax.microedition.ims.core.sipservice.refer.ReferService;
import javax.microedition.ims.core.sipservice.refer.ReferServiceImpl;
import javax.microedition.ims.core.sipservice.register.RegisterEvent;
import javax.microedition.ims.core.sipservice.register.RegisterServiceImpl;
import javax.microedition.ims.core.sipservice.register.RegistrationListenerAdapter;
import javax.microedition.ims.core.sipservice.subscribe.SubscribeService;
import javax.microedition.ims.core.sipservice.subscribe.SubscribeServiceImpl;
import javax.microedition.ims.core.transaction.*;
import javax.microedition.ims.core.xdm.XDMMessage;
import javax.microedition.ims.core.xdm.XDMService;
import javax.microedition.ims.core.xdm.XDMServiceImpl;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.ParamHeader;
import javax.microedition.ims.messages.wrappers.sip.Request;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 10-Dec-2009
 * Time: 15:25:29
 */
public class SIPIMSStack extends AbstractIMSStack<IMSMessage> {
    private static final String STACK_IS_ALREADY_SHUTDOWN_MESSAGE = "Stack is already shutdown";

    private final StackContextExt context;
    private final AtomicReference<TransactionManagerImpl> transactionManager = new AtomicReference<TransactionManagerImpl>(null);
    private final AtomicReference<IMSStackMessageDispatcherRegistry> dispatcherRegistry =
            new AtomicReference<IMSStackMessageDispatcherRegistry>(null);

    private final AtomicReference<RegisterServiceImpl> registerServiceImpl = new AtomicReference<RegisterServiceImpl>(null);

    //SIP_INVITE service
    private final AtomicReference<InviteServiceImpl> inviteService = new AtomicReference<InviteServiceImpl>(null);
    //SIP_REFER service
    private final AtomicReference<ReferServiceImpl> referService = new AtomicReference<ReferServiceImpl>(null);

    private final AtomicReference<SubscribeServiceImpl> subscribeService = new AtomicReference<SubscribeServiceImpl>(null);
    //SIP_REFER service
    private final AtomicReference<PageMessageServiceImpl> pageMessageService = new AtomicReference<PageMessageServiceImpl>(null);

    private final AtomicReference<XDMService> xdmService = new AtomicReference<XDMService>(null);

    private final AtomicReference<PresenceServiceImpl> presenceService = new AtomicReference<PresenceServiceImpl>(null);

    private final AtomicReference<MSRPServiceImpl> msrpService = new AtomicReference<MSRPServiceImpl>(null);

    private final AtomicReference<DefaultOptionsService> optionsService = new AtomicReference<DefaultOptionsService>(null);
    private final AtomicReference<PublishServiceImpl> publishService = new AtomicReference<PublishServiceImpl>(null);


    private final AtomicReference<AuthorizationRegistry> authorizationRegistry = new AtomicReference<AuthorizationRegistry>(null);
    private final AtomicBoolean done = new AtomicBoolean(false);

    private final IncomingMsrpMessageHandler<MsrpMessage> incomingMsrpRequestHandler = new IncomingMsrpMessageHandler<MsrpMessage>() {


        public void handleIncomingSendMessage(final MsrpMessage msg) {
            assert !done.get() : STACK_IS_ALREADY_SHUTDOWN_MESSAGE;
            assert msg != null /*&& MessageType.MSRP_SEND == MessageType.parse(msg.getEntityType().name())*/;

            if (!done.get()) {
                msrpService.get().handleIncomingSendMessage(msg);
            }
        }


        public void handleIncomingReportMessage(final MsrpMessage msg) {
            assert !done.get() : STACK_IS_ALREADY_SHUTDOWN_MESSAGE;
            assert msg != null && MessageType.MSRP_REPORT == MessageType.parse(msg.getType().name());

            if (!done.get()) {
                msrpService.get().handleIncomingReportMessage(msg);
            }
        }

    };

    private final IncomingRequestHandler<Request> incomingSipRequestHandler = new IncomingRequestHandler<Request>() {
        public void handleIncomingInvite(final Request msg) throws DialogStateException {
            assert !done.get() : STACK_IS_ALREADY_SHUTDOWN_MESSAGE;
            assert msg != null && MessageType.SIP_INVITE == MessageType.parse(msg.getMethod());

            if (!done.get()) {
                try {
                    inviteService.get().handleIncomingInvite(msg);
                } catch (DialogStateException e) {
                    if (e.getError() == DialogStateException.Error.ADDRESSEE_NOT_FOUND)
                        registerServiceImpl.get().notifyToDeregister();
                    throw e;
                }
            }
        }


        public void handleIncomingReInvite(final Request msg) throws DialogStateException {
            assert !done.get() : STACK_IS_ALREADY_SHUTDOWN_MESSAGE;
            assert msg != null && MessageType.SIP_INVITE == MessageType.parse(msg.getMethod());

            if (!done.get()) {
                inviteService.get().handleIncomingReInvite(msg);
            }
        }


        public void handleIncomingBye(final Request msg) {
            assert !done.get() : STACK_IS_ALREADY_SHUTDOWN_MESSAGE;
            assert msg != null && MessageType.SIP_BYE == MessageType.parse(msg.getMethod());

            if (!done.get()) {
                inviteService.get().handleIncomingBye(msg);
            }
        }


        public void handleIncomingNotify(Request msg) {
            assert !done.get() : STACK_IS_ALREADY_SHUTDOWN_MESSAGE;
            assert msg != null && MessageType.SIP_NOTIFY == MessageType.parse(msg.getMethod());

            if (!done.get()) {

                final ParamHeader eventHeader = msg.getEvent();

                if (eventHeader != null) {

                    String eventHeaderValue = eventHeader.getValue();

                    if (eventHeaderValue != null) {

                        EventPackage eventPackage = EventPackage.parse(eventHeaderValue);

                        if (eventPackage != null) {
                            if (eventPackage == EventPackage.REFER) {
                                referService.get().handleIncomingNotify(msg);
                            }
                            else {
                                subscribeService.get().handleIncomingNotify(msg);
                            }
                        }
                        else {
                            final String errMsg = "'Notify' message contains unsupported value in 'event' HEADER: '" +
                                    eventHeaderValue + "'";
                            assert false : errMsg;
                        }
                    }
                    else {
                        assert false : "'Notify' message without 'event' HEADER";
                    }
                }
                else {
                    assert false : "'Notify' without 'event' HEADER";
                }
            }
        }


        public void handleIncomingRefer(Request msg) {
            assert !done.get() : STACK_IS_ALREADY_SHUTDOWN_MESSAGE;
            assert msg != null && MessageType.SIP_REFER == MessageType.parse(msg.getMethod());

            if (!done.get()) {
                referService.get().handleIncomingRefer(msg);
            }
        }

        public void handleIncomingCancel(Request msg) throws DialogStateException {
            assert !done.get() : STACK_IS_ALREADY_SHUTDOWN_MESSAGE;
            assert msg != null && MessageType.SIP_CANCEL == MessageType.parse(msg.getMethod());

            if (!done.get()) {
                inviteService.get().handleIncomingCancel(msg);
            }
        }

        public void handleIncomingPageMessage(Request msg) throws DialogStateException {
            assert !done.get() : STACK_IS_ALREADY_SHUTDOWN_MESSAGE;
            assert msg != null && MessageType.SIP_MESSAGE == MessageType.parse(msg.getMethod());

            if (!done.get()) {
                pageMessageService.get().handleIncomingPageMessage(msg);
            }
        }


        public void handleIncomingOptionsMessage(Request msg) {
            assert !done.get() : STACK_IS_ALREADY_SHUTDOWN_MESSAGE;
            assert msg != null && MessageType.SIP_OPTIONS == MessageType.parse(msg.getMethod());

            if (!done.get()) {
                optionsService.get().handleIncomingOptionsMessage(msg);
            }
        }


        public void handleIncomingUpdate(Request msg) throws DialogStateException {
            assert !done.get() : STACK_IS_ALREADY_SHUTDOWN_MESSAGE;
            assert msg != null && MessageType.SIP_UPDATE == MessageType.parse(msg.getMethod());

            if (!done.get()) {
                inviteService.get().handleIncomingUpdate(msg);
            }
        }
    };

    private final ProducerListener<IMSMessage> producerListener = new ProducerListener<IMSMessage>() {
        public void onPop(final IMSMessage msg) {
            getProducerListenerHolder().getNotifier().onPop(msg);
        }
    };

    private final TransactionBuildUpListener authSubscriber = new TransactionBuildUpListener() {
        public void onTransactionCreate(final TransactionBuildUpEvent event) {
            assert !done.get() : STACK_IS_ALREADY_SHUTDOWN_MESSAGE;

            if (IMSEntityType.SIP == event.getEntity().getEntityType()) {
                Transaction<Boolean, BaseSipMessage> sipTransaction = (Transaction<Boolean, BaseSipMessage>) event.getTransaction();

                sipTransaction.addListener(
                        new AuthRealmResolver(
                                sipTransaction,
                                SIPIMSStack.this.getAuthorizationRegistry(),
                                (Dialog) event.getEntity(),
                                context.getConfig()
                        )
                );
            }
        }
    };

    public SIPIMSStack(final StackContextExt context) throws IMSStackException {
        assert context != null;
        //Logger.log(TAG, "Start creating entry point");

        this.context = context;

        authorizationRegistry.compareAndSet(null, new AuthorizationRegistry(this.context));

        dispatcherRegistry.compareAndSet(null, createAndPrepareMessageDispatcherRegistry(producerListener, incomingSipRequestHandler, incomingMsrpRequestHandler));

        transactionManager.compareAndSet(
                null,
                createAndPrepareTransactionManager(this.context, dispatcherRegistry.get())
        );
        transactionManager.get().addListener(authSubscriber);

        registerServiceImpl.compareAndSet(null, createAndPrepareRegisterService(context, transactionManager.get()));
        inviteService.compareAndSet(null, new InviteServiceImpl(context, transactionManager.get()));
        referService.compareAndSet(null, new ReferServiceImpl(context, transactionManager.get(), inviteService.get()));
        subscribeService.compareAndSet(null, new SubscribeServiceImpl(context, transactionManager.get()));
        pageMessageService.compareAndSet(null, new PageMessageServiceImpl(context, transactionManager.get()));
        xdmService.compareAndSet(null, new XDMServiceImpl(context, transactionManager.get()));
        presenceService.compareAndSet(null, new PresenceServiceImpl(context, transactionManager.get()));
        msrpService.compareAndSet(null, new MSRPServiceImpl(context, transactionManager.get(), getInviteService()));
        optionsService.compareAndSet(null, new DefaultOptionsService(context, transactionManager.get()));
        //TODO is it ok to take the same time as for registration?
        publishService.compareAndSet(null, new PublishServiceImpl(context, transactionManager.get(), context.getConfig().getPublicationExpirationSeconds()));

        addStackListener(registerServiceImpl.get());
        
        ConnectionDataProvider connDataProvider = context.getConnectionDataProvider();
        connDataProvider.addListener(dnsConnectionDatalistener);

        registerConnectionManagerListeners(context.getEnvironment().getConnectionManager());

    }

    private ConnectionDataListener dnsConnectionDatalistener = new ConnectionDataListener() {
        public void onConnectiondataChanged(ConnectionData connectionData) {
            if(connectionData != null) {
                context.updateProtocol(connectionData.getProtocol());
            }
        };
    };    

    private Shutdownable[] getShutdownableList() {
        return new Shutdownable[]{
                dispatcherRegistry.get(),
                registerServiceImpl.get(),
                inviteService.get(),
                referService.get(),
                subscribeService.get(),
                (Shutdownable) xdmService.get(),
                presenceService.get(),
                msrpService.get(),
                transactionManager.get(),
                //TODO: make AuthorizationRegistry shutdownable
                //,authorizationRegistry.get()
        };
    }


    public void push(final IMSMessage msg) {
        assert !done.get() : STACK_IS_ALREADY_SHUTDOWN_MESSAGE;
        if (!done.get()) {
            dispatcherRegistry.get().getOuterConsumer().push(msg);
        }
    }

    private IMSStackMessageDispatcherRegistry createAndPrepareMessageDispatcherRegistry(
            ProducerListener<IMSMessage> producerListener,
            IncomingRequestHandler<Request> incomingSipRequestHandler,
            IncomingMsrpMessageHandler<MsrpMessage> incomingMsrpRequestHandler
    ) {

        IMSStackMessageDispatcherRegistry dispatcherRegistry = new IMSStackMessageDispatcherRegistry();

        MessageDispatcher<BaseSipMessage> sipDispatcher = new MessageDispatcherSIP(
                context,
                producerListener,
                new UnknownSipMessageResolverImpl(context, incomingSipRequestHandler)
        );

        MessageDispatcher<XDMMessage> xdmDispatcher = new MessageDispatcherXDM(
                context,
                producerListener
        );

        MessageDispatcher<MsrpMessage> msrpDispatcher = new MessageDispatcherMsrp(
                context,
                producerListener,
                new UnknownMsrpMessageResolverImpl(context, incomingMsrpRequestHandler)
        );


        dispatcherRegistry.addDispatcher(IMSEntityType.SIP, sipDispatcher);
        dispatcherRegistry.addDispatcher(IMSEntityType.XDM, xdmDispatcher);
        dispatcherRegistry.addDispatcher(IMSEntityType.MSRP, msrpDispatcher);

        return dispatcherRegistry;
    }

    private TransactionManagerImpl createAndPrepareTransactionManager(
            final StackContext stackContext,
            final MessageDispatcherRegistry messageDispatcherRegistry) {

        final TransactionManagerImpl retValue = new TransactionManagerImpl(
                stackContext,
                messageDispatcherRegistry
        );

        final DialogStorageListener dialogStorageListener = new DialogStorageListenerAdapter() {

            {
                final DialogStorageListener dialogStorageSuperVisor = this;

                addStackListener(new StackListenerAdapter(){

                    public void onShutdown() {
                        super.onShutdown();
                        removeStackListener(this);
                        stackContext.getDialogStorage().removeDialogStorageListener(dialogStorageSuperVisor);
                    }
                });
            }


            public void onShutdownDialog(final DialogStorageEvent event) {
                super.onShutdownDialog(event);

                Logger.log(Logger.Tag.COMMON, "Dialog is ready to be shutdown. Dialog = "+event.getDialog());
                Logger.log(Logger.Tag.COMMON, "Dialog related transactions will be interrupted.");

                final TransactionManagerImpl trnsMngr = transactionManager.get();
                if (trnsMngr != null) {
                    trnsMngr.cleanUpEntity(event.getDialog());
                }

            }
        };

        stackContext.getDialogStorage().addDialogStorageListener(dialogStorageListener);

        return retValue;
    }

    private RegisterServiceImpl createAndPrepareRegisterService(
            final StackContext context,
            final TransactionManager transactionManager) {
        final RegisterServiceImpl retValue = new RegisterServiceImpl(
                context,
                transactionManager,
                context.getConfig().getRegistrationExpirationSeconds()
        );

        retValue.addRegistrationListener(
                new RegistrationListenerAdapter() {

                    public void onRegistrationAttempt(RegisterEvent event) {
                        //TODO put here some reasonable code to change password for given realm
                    }
                }
        );
        return retValue;
    }


    public StackContextExt getContext() {
        return context;
    }


    public RegisterServiceImpl getRegisterService() {
        return registerServiceImpl.get();
    }


    public InviteService getInviteService(/*String remoteUser*/) {
        return inviteService.get().getTransactionSafeView();
    }


    public ReferService getReferService() {
        return referService.get().getTransactionSafeView();
    }


    public SubscribeService getSubscribeService() {
        return subscribeService.get().getTransactionSafeView();
    }


    public PageMessageService getPageMessageService() {
        return pageMessageService.get().getTransactionSafeView();
    }


    public OptionsService getOptionsService() {
        return optionsService.get().getTransactionSafeView();
    }


    public PublishService getPublishService() {
        return publishService.get().getTransactionSafeView();
    }


    public XDMService getXDMService() {
        return xdmService.get();
    }


    public PresenceService getPresenceService() {
        return presenceService.get().getTransactionSafeView();
    }

    public MSRPService getMSRPService() {
        return msrpService.get().getTransactionSafeView();
    }

    public AuthorizationRegistry getAuthorizationRegistry() {
        return authorizationRegistry.get();
    }

    public TransactionManagerImpl getTransactionManager() {
        return transactionManager.get();
    }


    public void shutdown() {

        super.shutdown();

        if (done.compareAndSet(false, true)) {
            doShutdown();
        }
    }


    public void handleError(final StackOuterError stackOuterError) {

        final Throwable throwable = stackOuterError.getThrowableCause();

        if (throwable instanceof Error) {
            throw (Error) throwable;
        }

        getStackListenerHolder().getNotifier().onError(stackOuterError);

        if (StackOuterErrorType.NO_NETWORK == stackOuterError.getType()) {

            String errMsg = "Stack network failure detected durining sending message " +
                    stackOuterError.getUnprocessedMessage().shortDescription();

            Logger.log(Logger.Tag.WARNING, errMsg);
            Logger.log(Logger.Tag.WARNING, "Current network type: "+context.getEnvironment().getConnectionManager().getNetworkType());
            Logger.log(Logger.Tag.WARNING, "Current connection status: "+context.getEnvironment().getConnectionManager().getCurrentState());
            Logger.log(Logger.Tag.WARNING, "All ongoing transactions will be shutdown.");

            resetOnGoingCommunications();
        }
        else {
            String errMsg = "Stack outer error detected: " + stackOuterError;
            Logger.log(Logger.Tag.WARNING, errMsg);
            Logger.log(Logger.Tag.WARNING, "All ongoing transactions will be shutdown.");

            new RuntimeException(errMsg).printStackTrace();

            if (throwable != null) {
                throwable.printStackTrace();
            }
        }
    }

    private void resetOnGoingCommunications() {
        assert !done.get() : STACK_IS_ALREADY_SHUTDOWN_MESSAGE;

        final TransactionManagerImpl transactionMngr = transactionManager.get();
        if (transactionMngr != null) {
            transactionMngr.terminateAllTransactions();
        }
    }

    private void doShutdown() {
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "start");

        unregisterConnectionManagerListeners(context.getEnvironment().getConnectionManager());
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "transport listeners disconnected");

        Shutdownable[] list = getShutdownableList();
        for (Shutdownable item : list) {
            item.shutdown();
        }

        context.getConnectionDataProvider().removeListener(dnsConnectionDatalistener);
        removeStackListener(registerServiceImpl.get());

        dispatcherRegistry.set(null);
        registerServiceImpl.set(null);
        inviteService.set(null);
        referService.set(null);
        subscribeService.set(null);

        transactionManager.set(null);
        authorizationRegistry.set(null);
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "end");
    }

    private void registerConnectionManagerListeners(final ConnectionManager connectionManager) {
        connectionManager.addIpChangeListener(registerServiceImpl.get());
        connectionManager.addNetTypeChangeListener(registerServiceImpl.get());
        connectionManager.addConnStateListener(registerServiceImpl.get());
    }

    private void unregisterConnectionManagerListeners(final ConnectionManager connectionManager) {
        connectionManager.removeIpChangeListener(registerServiceImpl.get());
        connectionManager.removeNetTypeChangeListener(registerServiceImpl.get());
        connectionManager.removeConnStateListener(registerServiceImpl.get());
    }

    public static void main(String[] args) {
        boolean b = true;

        System.out.println("");
    }
}
