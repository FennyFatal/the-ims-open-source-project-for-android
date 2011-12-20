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

package javax.microedition.ims.core.msrp;

import javax.microedition.ims.common.*;
import javax.microedition.ims.common.util.FileUtils;
import javax.microedition.ims.core.IMSStackException;
import javax.microedition.ims.core.InitiateParty;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.DialogStateEvent;
import javax.microedition.ims.core.dialog.DialogStateListener;
import javax.microedition.ims.core.dialog.DialogStateListenerAdapter;
import javax.microedition.ims.core.messagerouter.RouterKeyDeafultImpl;
import javax.microedition.ims.core.msrp.filetransfer.FileDescriptor;
import javax.microedition.ims.core.msrp.filetransfer.FileReceiverImpl;
import javax.microedition.ims.core.msrp.filetransfer.FileSelectorParser;
import javax.microedition.ims.core.msrp.filetransfer.FileSenderImpl;
import javax.microedition.ims.core.msrp.listener.*;
import javax.microedition.ims.core.sipservice.AbstractService;
import javax.microedition.ims.core.sipservice.TransactionStateChangeEvent;
import javax.microedition.ims.core.sipservice.invite.DialogStateException;
import javax.microedition.ims.core.sipservice.invite.InviteService;
import javax.microedition.ims.core.transaction.*;
import javax.microedition.ims.core.transaction.TransactionResult.Reason;
import javax.microedition.ims.messages.parser.sdp.SdpParser;
import javax.microedition.ims.messages.utils.MsrpUtils;
import javax.microedition.ims.messages.wrappers.msrp.ChunkTerminator;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessageType;
import javax.microedition.ims.messages.wrappers.msrp.MsrpUri;
import javax.microedition.ims.messages.wrappers.sdp.Attribute;
import javax.microedition.ims.messages.wrappers.sdp.SdpMessage;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.transport.MsrpRouteImpl;
import javax.microedition.ims.transport.messagerouter.RouteDescriptorDefaultImpl;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 6.5.2010
 * Time: 17.40.56
 */
class CommonMSRPSession implements MSRPSession {

    private static final String TAG = "CommonMSRPSession";

    public static enum State {
        NEW, OPENING, OPENED, CLOSED, CANCELED
    }

    private static final DefaultTimeoutUnit TRANSACTION_TIMEOUT = new DefaultTimeoutUnit(
            RepetitiousTaskManager.TRANSACTION_TIMEOUT_INTERVAL,
            TimeUnit.MILLISECONDS
    );

    private final IMSID sessionId;
    private final Dialog msrpDialog;
    private final int sessionLocalPort;

    private final StackContext stackContext;
    private final TransactionManager transactionManager;
    private final InviteService inviteService;

    //private final MSRPService msrpService;
    private final MSRPSessionType sessionType;
    private final FileSenderImpl fileSender;
    private final FileReceiverImpl fileReceiver;
    private MsrpRouteImpl route;

    private final AtomicReference<State> sessionState = new AtomicReference<State>(State.NEW);
    private final AtomicReference<MsrpUri> remotePartyURI = new AtomicReference<MsrpUri>();

    private final ListenerHolder<MSRPSessionStartListener> msrpSessionStartListenerHolder
            = new ListenerHolder<MSRPSessionStartListener>(MSRPSessionStartListener.class);

    private final ListenerHolder<MSRPSessionStopListener> msrpSessionStopListenerHolder
            = new ListenerHolder<MSRPSessionStopListener>(MSRPSessionStopListener.class);

    private final ListenerHolder<MSRPMessageStatusListener> msrpMessageStatusListenerHolder
            = new ListenerHolder<MSRPMessageStatusListener>(MSRPMessageStatusListener.class);

    private final TransactionBuildUpListener<IMSMessage> clientMSRPSendListener =
            new TransactionBuildUpListener<IMSMessage>() {
                public void onTransactionCreate(final TransactionBuildUpEvent<IMSMessage> event) {
                    assert TransactionType.MSRP_SEND_CLIENT == event.getTransaction().getTransactionType();

                    final MSRPTransactionDescriptor descriptor = (MSRPTransactionDescriptor) event.getTransactionDescription();
                    final Transaction<Boolean, IMSMessage> transaction = event.getTransaction();

                    //listener will un-subscribe automatically on transaction complete
                    transaction.addListener(new UnSubscribeOnLogicCompleteAdapter<IMSMessage>(transaction) {

                        private boolean notified = false;


                        public void onTransactionComplete(TransactionEvent<IMSMessage> event, Reason reason) {
                            Logger.log(TAG, "clientMSRPSendListener#onTransactionComplete");
                            if (notified == false) {
                                notifyListeners(false, null);
                            }
                            super.onTransactionComplete(event, reason);
                        }


                        public void onStateChanged(TransactionStateChangeEvent<IMSMessage> event) {
                            Logger.log(TAG, "clientMSRPSendListener#onStateChanged    state=" + event.getState()
                                    + "    transactionInitialMessage=" + ((IMSMessage) event.getTransaction().getInitialMessage()).shortDescription());

                            MsrpMessage triggeringMessage = (MsrpMessage) event.getTriggeringMessage();

                            switch (event.getState()) {
                                case COMPLETED:
                                case CONFIRMED:
                                    notifyListeners(true, triggeringMessage);
                                    doUnSubscribe(event.getTransaction());
                                    break;
                                case CANCELED:
                                    notifyListeners(false, triggeringMessage);
                                    doUnSubscribe(event.getTransaction());
                                    break;
                                case TERMINATED:
                                    notifyListeners(false, triggeringMessage);
                                    doUnSubscribe(event.getTransaction());
                                    break;
                            }
                        }

                        private void notifyListeners(boolean successFactor, MsrpMessage triggeringMessage) {
                            notified = true;

                            final MSRPMessageStatusEvent statusEvent = new DefaultMSRPMessageStatusEvent(
                                    descriptor.getMsrpSession(),
                                    descriptor.getMsrpMessage(),
                                    triggeringMessage,
                                    getSessionType()
                            );

                            if (successFactor) {
                                msrpMessageStatusListenerHolder.getNotifier().messageDeliveredSuccessfully(statusEvent);
                            } else {
                                msrpMessageStatusListenerHolder.getNotifier().messageDeliveryFailed(statusEvent);
                            }
                        }
                    });

                }
            };


    static MSRPSession createIncomingMsrpSession(
            final StackContext stackContext,
            final TransactionManager transactionManager,
            final InviteService inviteService,
            final MSRPService msrpService,
            final Dialog msrpDialog,
            final MSRPSessionType sessionType) {

        if (!msrpDialog.getIncomingSdpMessage().typeSupported(SDPType.MSRP)) {
            throw new IllegalArgumentException("Not 'MSRP' msrpDialog");
        }

        final CommonMSRPSession retValue = new CommonMSRPSession(
                stackContext,
                transactionManager,
                inviteService,
                msrpService,
                msrpDialog,
                sessionType
        );

        retValue.transitToState(State.NEW, State.OPENING, null);

        //Add MSRP SDP to DIALOG

        final MSRPSDPUpdateData updateData = retValue.obtainMSRPSDPUpdateData(sessionType, true);
        MSRPHelper.updateSDPWithMSRPData(msrpDialog.getOutgoingSdpMessage(), updateData);

        return retValue;
    }


    static MSRPSession createOutgoingMsrpSession(
            final StackContext stackContext,
            final TransactionManager transactionManager,
            final InviteService inviteService,
            final MSRPService msrpService,
            final Dialog dialog,
            final MSRPSessionType sessionType) {

        return new CommonMSRPSession(
                stackContext,
                transactionManager,
                inviteService,
                msrpService,
                dialog,
                sessionType
        );
    }


    private CommonMSRPSession(
            final StackContext stackContext,
            final TransactionManager transactionManager,
            final InviteService inviteService,
            final MSRPService msrpService,
            final Dialog dialog,
            final MSRPSessionType sessionType) {

        this.stackContext = stackContext;
        this.transactionManager = transactionManager;
        this.inviteService = inviteService;
        //this.msrpService = msrpService;
        this.msrpDialog = dialog;
        this.sessionLocalPort = MSRPHelper.generateLocalPort(stackContext.getConfig().getMsrpLocalPort());

        this.sessionId = new IMSStringID(MsrpUtils.generateSessionId());
        this.sessionType = sessionType;
        this.fileSender = new FileSenderImpl(
                new FileSenderImpl.MessageSender() {
                    public void sendMessage(final MsrpMessage msrpMessage, final boolean needProgress) {
                        doSendMessage(msrpMessage, needProgress);
                    }
                },
                msrpMessageStatusListenerHolder,
                stackContext.getConfig()
        );

        final List<FileDescriptor> fileDescriptors;
        if (sessionType == MSRPSessionType.FILE_IN) {
            final SdpMessage sdpMessage = dialog.getIncomingSdpMessage();
            Attribute fileDescriptor = MSRPHelper.obtainFileSelectorAttribute(sdpMessage);

            //final List<FileDescriptor> fileDescriptors = Arrays.asList(FileSendHelper.createDummyFileDescriptor());
            fileDescriptors = FileSelectorParser.parse(fileDescriptor.getValue());

            Logger.log("CommonMSRPSession", "fileDescriptor.getValue() === " + fileDescriptor.getValue());

            if (fileDescriptors == null || fileDescriptors.size() == 0) {
                throw new IllegalArgumentException("Dialog doesn't contain valid SDP.");
            }

        } else {
            fileDescriptors = Collections.emptyList();
        }

        this.fileReceiver = new FileReceiverImpl(fileDescriptors, stackContext.getEnvironment());
        msrpService.addIncomingMSRPMessageListener(CommonMSRPSession.this, fileReceiver);

        subscribeToTransactionManager();
        inviteService.addDialogStateListener(msrpDialog, sipSessionNegotiationListener);
    }

    public Dialog getMsrpDialog() {
        return msrpDialog;
    }

    public IMSEntityType getEntityType() {
        return IMSEntityType.MSRP;
    }

    public IMSID getIMSEntityId() {
        return sessionId;
    }

    public void addMSRPSessionStartListener(MSRPSessionStartListener listener) {
        msrpSessionStartListenerHolder.addListener(listener);
    }

    public void removeMSRPSessionStartListener(MSRPSessionStartListener listener) {
        msrpSessionStartListenerHolder.removeListener(listener);
    }

    public void addMSRPSessionStopListener(MSRPSessionStopListener listener) {
        msrpSessionStopListenerHolder.addListener(listener);
    }

    public void removeMSRPSessionStopListener(MSRPSessionStopListener listener) {
        msrpSessionStopListenerHolder.removeListener(listener);
    }

    public void addMSRPFileSendingProgressListener(MSRPFileSendingProgressListener listener) {
        //msrpFileTransferProgressListenerHolder.addRegistrationListener(listener);
        fileSender.addMSRPFileSendingProgressListener(listener);
    }

    public void removeMSRPFileSendingProgressListener(MSRPFileSendingProgressListener listener) {
        //msrpFileTransferProgressListenerHolder.removeRegistrationListener(listener);
        fileSender.removeMSRPFileSendingProgressListener(listener);
    }

    public void addMSRPFileSendingListener(MSRPFileSendingListener listener) {
        //msrpFileSendingListenerHolder.addRegistrationListener(listener);
        fileSender.addMSRPFileSendingListener(listener);
    }

    public void removeMSRPFileSendingListener(MSRPFileSendingListener listener) {
        //msrpFileSendingListenerHolder.removeRegistrationListener(listener);
        fileSender.removeMSRPFileSendingListener(listener);
    }

    public void addMSRPFileReceivingListener(MSRPFileReceivingListener listener) {
        fileReceiver.addMSRPFileReceivingListener(listener);
    }

    public void removeMSRPFileReceivingListener(MSRPFileReceivingListener listener) {
        fileReceiver.removeMSRPFileReceivingListener(listener);
    }

    public void addMSRPFileReceivingProgressListener(MSRPFileReceivingProgressListener listener) {
        fileReceiver.addMSRPFileReceivingProgressListener(listener);
    }

    public void removeMSRPFileReceivingProgressListener(MSRPFileReceivingProgressListener listener) {
        fileReceiver.removeMSRPFileReceivingProgressListener(listener);
    }

    public void sendFile(final FileDescriptor fileDescriptor) throws IMSStackException {

        if (MSRPSessionType.FILE_IN == sessionType) {
            throw new IllegalStateException("Can not send file in FILE_IN session. Now session type is " + sessionType);
        }

        if (MSRPSessionType.FILE_OUT == sessionType) {

            TransactionUtils.getExecutorService().execute(new Runnable() {
                public void run() {
                    try {
                        fileSender.sendFile(fileDescriptor);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        } else if (MSRPSessionType.CHAT == sessionType) {

            final MsrpMessage msrpMessage = packFileToChatMessage(fileDescriptor);

            if (msrpMessage == null) {
                throw new IMSStackException("Can't send file " + fileDescriptor);
            }

            TransactionUtils.invokeLaterSmart(new TransactionRunnable("Send file through chat session. " + fileDescriptor + " " + this) {
                
                public void run() {
                    final State currentSessionState = sessionState.get();
                    assert State.OPENED == currentSessionState;

                    if (State.OPENED == currentSessionState) {
                        doSendMessage(msrpMessage, true);
                    }
                }
            });
        }

    }

    private MsrpMessage packFileToChatMessage(FileDescriptor fileDescriptor) {
        MsrpMessage.IMsrpMessageBuilder builder = new MsrpMessage.IMsrpMessageBuilder();

        File filePath = new File(fileDescriptor.getFilePath());
        MsrpMessage msrpChatMessage = null;

        if (filePath.exists()) {

            File fileToSend;

            if (filePath.isFile()) {
                fileToSend = filePath;
            } else {
                fileToSend = new File(fileDescriptor.getFilePath() + File.separator + fileDescriptor.getFileName());
            }

            if (fileToSend.exists() && fileToSend.isFile()) {
                builder
                        .contentType(fileDescriptor.getContentType())
                        .contentParts(FileUtils.readAll(fileToSend))
                        .messageId("" + System.currentTimeMillis());

                msrpChatMessage = builder.build();
                msrpChatMessage.setTerminator(ChunkTerminator.FINISHED);
            }
        }
        return msrpChatMessage;
    }

    public void cancelFileSending(final FileDescriptor fileDescriptor) {
        fileSender.cancelFileSending(fileDescriptor);
    }

    public void cancelFileReceiving(final FileDescriptor fileDescriptor) {
        fileReceiver.cancelFileReceiving(fileDescriptor);
    }

    private final DialogStateListener<BaseSipMessage> sipSessionNegotiationListener = new DialogStateListenerAdapter<BaseSipMessage>() {

        private void cleanUp() {
            inviteService.removeDialogStateListener(this);
        }


        public void onSessionStarted(DialogStateEvent<BaseSipMessage> event) {

            final State state = sessionState.get();
            assert state == State.OPENING : "Wrong state. now '" + state + "' expected '" + State.OPENING + "'";

            if (state == State.OPENING) {
                handleSessionStartedEvent(event);
            }

            //have wrong state. just close SESSION
            else {
                handleMSRPSessionStartFailed(new SessionStartFailedData(456788, "45678", 456788));
            }
        }


        public void onSessionStartFailed(DialogStateEvent<BaseSipMessage> baseSipMessageDialogStateEvent) {
            cleanUp();
            handleMSRPSessionStartFailed(new SessionStartFailedData(33333, "33333", 33333));
        }


        public void onSessionTerminated(DialogStateEvent<BaseSipMessage> event) {
            cleanUp();

            State state = sessionState.get();
            Logger.log(TAG, "sipDialogTerminatedListener.onSessionTerminated  sessionState=" + state);
            if (state == State.NEW || state == State.OPENING || state == State.CANCELED) {
                //handleMSRPSessionStartFailed(new SessionStartFailedData(333332, "333332", 333332));
                msrpSessionStartListenerHolder.getNotifier().onMSRPSessionStartFailed(333332, "333332", 333332);
            } else {
                //handleMSRPSessionClosed();
                msrpSessionStopListenerHolder.getNotifier().onMSRPSessionFinished();
            }

            transitToState(null, State.CLOSED, null);

            shutdown();
        }

        private void handleSessionStartedEvent(DialogStateEvent<BaseSipMessage> event) {

            final BaseSipMessage sipMessage = obtainMSRPSessionControlMessage(event);

            if (isMSRPMessage(sipMessage) &&
                    transitToState(State.OPENING, State.OPENED, new StartedAtomicUpdate(sipMessage))) {

                //TODO: make SESSION stated only after successful response on empty message
                ensureMSRPChannelOpen();
            } else {
                handleMSRPSessionStartFailed(new SessionStartFailedData(12345, "12345", 12345));
            }
        }

        private BaseSipMessage obtainMSRPSessionControlMessage(DialogStateEvent<BaseSipMessage> event) {
            BaseSipMessage sipMessage;

            InitiateParty initiateParty = msrpDialog.getInitiateParty();
            if (InitiateParty.REMOTE == initiateParty) {
                sipMessage = msrpDialog.getMessageHistory().getFirstMessage();
            } else {
                sipMessage = event.getTriggeringMessage();
            }
            return sipMessage;
        }

        class StartedAtomicUpdate implements Runnable {

            private final BaseSipMessage msg;

            public StartedAtomicUpdate(BaseSipMessage msg) {
                this.msg = msg;
            }

            public void run() {
                SdpMessage sdp = SdpParser.parse(new String(msg.getBody()));
                final MsrpUri msrpUri = MSRPHelper.obtainMSRPURI(sdp);
                remotePartyURI.compareAndSet(null, msrpUri);


                TransactionUtils.invokeLater(new TransactionRunnable("MSRP event notification " + CommonMSRPSession.this) {
                    public void run() {
                        msrpSessionStartListenerHolder.getNotifier().onMSRPSessionStarted();
                    }
                });
            }
        }
    };

    public void openChatSession() throws DialogStateException {

        final State currSessionState = sessionState.get();
        InitiateParty initiateParty = msrpDialog.getInitiateParty();
        assert InitiateParty.LOCAL == initiateParty && State.NEW == currSessionState :
                "MSRP SESSION must be in '" + State.NEW + "' state. Now it in '" + currSessionState + "' state";

        if (InitiateParty.LOCAL != initiateParty) {
            final String errMsg = "Call to open() is only possible for locally initiated SESSION. " +
                    "Current SESSION is '" + initiateParty + "'";
            throw new IllegalStateException(errMsg);
        }

        if (transitToState(State.NEW, State.OPENING, null)) {
            doOpenSession(null);
        }
    }


    public void openSendFileSession(FileDescriptor fileDescriptor)
            throws DialogStateException {
        final State currSessionState = sessionState.get();
        InitiateParty initiateParty = msrpDialog.getInitiateParty();
        assert InitiateParty.LOCAL == initiateParty && State.NEW == currSessionState :
                "MSRP SESSION must be in '" + State.NEW + "' state. Now it in '" + currSessionState + "' state";

        if (InitiateParty.LOCAL != initiateParty) {
            final String errMsg = "Call to open() is only possible for locally initiated SESSION. " +
                    "Current SESSION is '" + initiateParty + "'";
            throw new IllegalStateException(errMsg);
        }

        if (transitToState(State.NEW, State.OPENING, null)) {
            doOpenSession(fileDescriptor);
        }
    }

    public void close() {
        if (transitToState(null, State.CLOSED, null)) {
            inviteService.bye(msrpDialog);
            stackContext.getTransportIO().shutdownRoute(route);
        }
    }

    public void cancelSessionOpening() {
        if (transitToState(null, State.CANCELED, null)) {
            inviteService.cancel(msrpDialog);
        }
    }

    private void doOpenSession(FileDescriptor fileDescriptor) throws DialogStateException {

        //Add MSRP SDP to DIALOG
        MSRPSessionType type = fileDescriptor == null ? MSRPSessionType.CHAT : MSRPSessionType.FILE_OUT;
        final MSRPSDPUpdateData updateData = obtainMSRPSDPUpdateData(type, true);
        if (type == MSRPSessionType.CHAT) {
            MSRPHelper.updateSDPWithMSRPData(msrpDialog.getOutgoingSdpMessage(), updateData);
        } else {
            MSRPHelper.updateSDPWithMSRPData(msrpDialog.getOutgoingSdpMessage(), updateData, fileDescriptor);
        }

        //In case of successful MSRP negotiations try to start MSRP SESSION
        //inviteService.addDialogStateListener(msrpDialog, sipNegotiationListener);

        assert msrpDialog.getOutgoingSdpMessage() != null : "Not MSRP DIALOG";
        assert msrpDialog.getOutgoingSdpMessage().typeSupported(SDPType.MSRP) : "Not MSRP Dialog";

        //try to negotiate MSRP SESSION
        inviteService.invite(msrpDialog);
    }

    private void ensureMSRPChannelOpen() {

        String address = msrpDialog.getIncomingSdpMessage().getConnectionInfo().getAddress();
        int port = msrpDialog.getIncomingSdpMessage().getMedias().get(0).getPort();

        /*stackContext.getTransportIO().updateRouter(new DefaultRoute(
                address,
                port,
                sessionLocalPort,
                Protocol.TCP,
                IMSEntityType.MSRP
        ), sessionId.stringValue());*/
        this.route = new MsrpRouteImpl(
                address,
                port,
                sessionLocalPort,
                Protocol.TCP,
                IMSEntityType.MSRP
        );

        stackContext.getMessageRouter().addRoute(
                route,
                new RouteDescriptorDefaultImpl(new RouterKeyDeafultImpl(sessionId.stringValue()), null)
        );
        sendEmptyMsrpMessage();
    }

    private void sendEmptyMsrpMessage() {

        final State currentSessionState = sessionState.get();
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert State.OPENED == currentSessionState;

        //we need to send empty msrp message to ensure that connection is open
        MsrpMessage msrpMessage = new MsrpMessage();
        msrpMessage.setPrevProgress(1);
        msrpMessage.setCurrentProgress("0");
        msrpMessage.setTerminator(ChunkTerminator.FINISHED);

        doSendMessage(msrpMessage, true);
    }

    public void sendReport() {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        final State currSessionState = sessionState.get();
        assert State.OPENED == currSessionState;

        if (State.OPENED == currSessionState) {

            /*  final Transaction transaction = getTransactionManager().lookUpTransaction(DIALOG, BYE_CLIENT);

          runAsynchronously((Transaction<Boolean, BaseSipMessage>)transaction, timeoutUnit);*/
        }
    }

    public void sendStatus() {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        final State currSessionState = sessionState.get();
        assert State.OPENED == currSessionState;

        if (State.OPENED == currSessionState) {

            /* final Transaction transaction = getTransactionManager().lookUpTransaction(DIALOG, TransactionType.MSRP_SEND_CLIENT);

          runAsynchronously((Transaction<Boolean, BaseSipMessage>)transaction, timeoutUnit);*/
        }
    }

    public void sendMessage(final MsrpMessage msrpMessage) {

        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        final State currentSessionState = sessionState.get();
        assert State.OPENED == currentSessionState;

        if (State.OPENED == currentSessionState) {
            doSendMessage(msrpMessage, true);
        }
    }

    private void doSendMessage(final MsrpMessage msrpMessage, final boolean needProgress) {

        if (msrpMessage.getTerminator() == ChunkTerminator.NOT_SET) {
            msrpMessage.setTerminator(ChunkTerminator.FINISHED);
        }

        msrpMessage.setType(MsrpMessageType.SEND);
        msrpMessage.setFromPath(generateFromURI());
        msrpMessage.setToPath(remotePartyURI.get());

        if (needProgress) {
            if (msrpMessage.getBody() != null) {
                final int totalSize = msrpMessage.getBody().length;

                msrpMessage.setTotalSize(totalSize);
                msrpMessage.setPrevProgress(1);
                msrpMessage.setCurrentProgress("" + totalSize);
            } else {
                msrpMessage.setTotalSize(0);
                msrpMessage.setPrevProgress(1);
                msrpMessage.setCurrentProgress("0");//"0" should be here instead of "*", bug 0020769
            }
        }

        //final Transaction transaction = transactionManager.lookUpTransaction(this, TransactionType.MSRP_SEND_CLIENT);
        MSRPTransactionDescriptor descriptor = new DefaultMSRPTransactionDescriptor(this, msrpMessage);
        final Transaction transaction = transactionManager.newTransaction(
                this,
                descriptor,
                TransactionType.MSRP_SEND_CLIENT
        );

        AbstractService.runAsynchronously((Transaction<Boolean, MsrpMessage>) transaction, TRANSACTION_TIMEOUT);
    }


    private static boolean isMSRPMessage(final BaseSipMessage message) {

        boolean retValue = false;

        if (MimeType.APP_SDP.stringValue().equals(message.getContentType().getValue())) {
            SdpMessage sdpMessage = SdpParser.parse(new String(message.getBody()));

            if (sdpMessage.typeSupported(SDPType.MSRP)) {
                retValue = true;
            }
        }

        return retValue;
    }

    private MSRPSDPUpdateData obtainMSRPSDPUpdateData(MSRPSessionType type, boolean active) {
        String address = stackContext.getEnvironment().getConnectionManager().getInetAddress();

        MsrpUri fromPath = generateFromURI();

        return new MSRPSDPUpdateData(
                fromPath,
                sessionLocalPort,
                address,
                type
        );
    }

    private MsrpUri generateFromURI() {
        String address = stackContext.getEnvironment().getConnectionManager().getInetAddress();

        return MsrpUtils.generateUri(
                sessionId.stringValue(),
                address,
                sessionLocalPort,
                /*stackContext.getConfig().getRegistrationName().getName()*/
                stackContext.getRegistrationIdentity().getUserInfo().getName()
        );
    }

    //transit to closed state in from any state except closed. Listeners are being notified

    private void handleMSRPSessionStartFailed(final SessionStartFailedData reasonType) {
        Logger.log(TAG, "handleMSRPSessionStartFailed()");

        transitToState(null, State.CLOSED, new Runnable() {
            public void run() {
                TransactionUtils.invokeLater(new TransactionRunnable("MSRP event notification " + CommonMSRPSession.this) {
                    public void run() {
                        Logger.log(TAG, "msrpSessionStartListenerHolder.getNotifier().onMSRPSessionStartFailed");

                        msrpSessionStartListenerHolder.getNotifier().onMSRPSessionStartFailed(
                                reasonType.getReasonType(),
                                reasonType.getReasonPhrase(),
                                reasonType.getStatusCode()
                        );
                    }
                });
            }
        });
    }

/*    private void handleMSRPSessionClosed() {

        transitToState(null, State.CLOSED, new Runnable() {
            public void run() {
                TransactionUtils.invokeLater(new TransactionRunnable("MSRP event notification " + CommonMSRPSession.this) {
                    public void run() {
                        msrpSessionStopListenerHolder.getNotifier().onMSRPSessionFinished();
                    }
                });
            }
        });
    }
*/
    private boolean transitToState(final State expected, final State update, final Runnable atomicUpdate) {
        final boolean retValue;

        synchronized (sessionState) {
            if (expected != null) {
                retValue = sessionState.compareAndSet(expected, update);
            } else {

                if (sessionState.get() != update) {
                    sessionState.set(update);
                    retValue = true;
                } else {
                    retValue = false;
                }
            }

            if (retValue && atomicUpdate != null) {
                atomicUpdate.run();
            }
        }

        return retValue;
    }


    private void subscribeToTransactionManager() {
        transactionManager.addListener(clientMSRPSendListener, TransactionType.Name.MSRP_SEND_CLIENT);
    }

    private void unSubscribeFromTransactionManager() {
        transactionManager.removeListener(clientMSRPSendListener);
    }

    public void shutdown() {
        Logger.log("CommonMSRPSession shutdown");
        //new Exception().printStackTrace();
        close();
        msrpSessionStartListenerHolder.shutdown();
        msrpSessionStopListenerHolder.shutdown();
        fileSender.shutdown();
        if (fileReceiver != null) {
            fileReceiver.shutdown();
        }
        MSRPHelper.freeLocalPort(sessionLocalPort);
        if (stackContext.getMessageRouter() != null) {
            stackContext.getMessageRouter().removeRoute(new RouterKeyDeafultImpl(sessionId.stringValue()));
        }
        unSubscribeFromTransactionManager();
    }


    public String toString() {
        return "CommonMSRPSession{" +
                "sessionId=" + sessionId +
                ", msrpDialog=" + msrpDialog +
                '}';
    }


    public MSRPSessionType getSessionType() {
        return sessionType;
    }
}
