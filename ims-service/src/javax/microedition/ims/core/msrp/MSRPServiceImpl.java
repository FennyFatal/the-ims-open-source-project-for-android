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
import javax.microedition.ims.core.FirstMessageResolver;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.*;
import javax.microedition.ims.core.msrp.filetransfer.FileDescriptor;
import javax.microedition.ims.core.msrp.filetransfer.FileSelectorParser;
import javax.microedition.ims.core.msrp.listener.*;
import javax.microedition.ims.core.sipservice.AbstractService;
import javax.microedition.ims.core.sipservice.invite.InviteService;
import javax.microedition.ims.core.transaction.*;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessageType;
import javax.microedition.ims.messages.wrappers.sdp.Attribute;
import javax.microedition.ims.messages.wrappers.sdp.DirectionsType;
import javax.microedition.ims.messages.wrappers.sdp.Media;
import javax.microedition.ims.messages.wrappers.sdp.SdpMessage;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class MSRPServiceImpl extends AbstractService implements MSRPService, IncomingCallListener {

    private static final String TAG = "MSRPServiceImpl";

    public final static MimeType COMPOSING_INDICATOR_CONTENT_TYPE = MimeType.APP_IM_ISCOMPOSING_XML;

    private final ListenerHolder<IncomingMSRPChatInviteListener> incomingMSRPChatInviteListenerHolder
            = new ListenerHolder<IncomingMSRPChatInviteListener>(IncomingMSRPChatInviteListener.class);

    private final ListenerHolder<IncomingMSRPFilePushInviteListener> incomingMSRPFilePushInviteListenerHolder
            = new ListenerHolder<IncomingMSRPFilePushInviteListener>(IncomingMSRPFilePushInviteListener.class);

    private final ListenerHolder<IncomingMSRPMessageListener> incomingMSRPMessageListenerHolder
            = new ListenerHolder<IncomingMSRPMessageListener>(IncomingMSRPMessageListener.class);

    private final ListenerHolder<ChatExtensionListener> chatExtensionListenerHolder
            = new ListenerHolder<ChatExtensionListener>(ChatExtensionListener.class);

    private final ListenerHolder<MSRPMessageSendingListener> msrpMessageSendingListenerHolder
            = new ListenerHolder<MSRPMessageSendingListener>(MSRPMessageSendingListener.class);


    private final InviteService inviteService;
    private final AtomicBoolean done = new AtomicBoolean(false);
    //private final MSRPService transactionSafeView;
    private final MSRPSessionStorage sessionStorage;

    private final TransactionBuildUpListener<MsrpMessage> serverMSRPSendListener =
            new TransactionBuildUpListener<MsrpMessage>() {
                public void onTransactionCreate(final TransactionBuildUpEvent<MsrpMessage> trnsBuildUpEvent) {
                    assert TransactionType.MSRP_SEND_SERVER == trnsBuildUpEvent.getTransaction().getTransactionType();

                    final MSRPTransactionDescriptor trnsDescriptor = (MSRPTransactionDescriptor) trnsBuildUpEvent.getTransactionDescription();
                    final Transaction<Boolean, MsrpMessage> transaction = trnsBuildUpEvent.getTransaction();

                    //listener will un-subscribe automatically on transaction complete
                    transaction.addListener(new UnSubscribeOnCompleteAdapter<MsrpMessage>(transaction) {


                        public void onTransactionComplete(
                                final TransactionEvent<MsrpMessage> trnsCompleteEvent,
                                final TransactionResult.Reason reason) {

                            assert TransactionType.MSRP_SEND_SERVER == transaction.getTransactionType();
                            TransactionResult<Boolean> transactionValue = transaction.getTransactionValue();

                            if (transactionValue.getValue()) {
                                handleIncomingMSRPMessage(trnsCompleteEvent.getInitialMessage());
                            }

                            super.onTransactionComplete(trnsCompleteEvent, reason);
                        }

                        private void handleIncomingMSRPMessage(MsrpMessage msg) {
                            IncomingMSRPMessageEvent event = new IncomingMSRPMessageEvent(
                                    msg,
                                    trnsDescriptor.getMsrpSession().getIMSEntityId()
                            );


                            String contentType = msg.getContentType();
                            MimeType mimeType = MimeType.parse(contentType);
                            MimeTypeClass mimeTypeClass = mimeType == null ? null : mimeType.getMimeTypeClass();

                            if (COMPOSING_INDICATOR_CONTENT_TYPE == mimeType) {
                                incomingMSRPMessageListenerHolder.getNotifier().onComposingIndicatorReceived(event);
                            } else if (
                                    //this only fire for chat session and image content type
                                    MimeTypeClass.IMAGE == mimeTypeClass &&
                                            MSRPSessionType.CHAT == trnsDescriptor.getMsrpSession().getSessionType()) {

                                incomingMSRPMessageListenerHolder.getNotifier().onFileMessageReceived(event);
                            } else {
                                incomingMSRPMessageListenerHolder.getNotifier().onMessageReceived(event);
                            }
                        }
                    });
                }
            };


    public MSRPServiceImpl(
            final StackContext stackContext,
            final TransactionManager transactionManager,
            final InviteService inviteService) {

        super(stackContext, transactionManager);

        this.inviteService = inviteService;
        inviteService.addIncomingCallListener(this);

        sessionStorage = new MSRPSessionStorageImpl();
        //transactionSafeView = TransactionUtils.wrap(MSRPService.class, this);
        subscribeToTransactionManager();
    }

    private void subscribeToTransactionManager() {
        TransactionManager transactionManager = getTransactionManager();
        transactionManager.addListener(serverMSRPSendListener, TransactionType.Name.MSRP_SEND_SERVER);
    }


    public void addIncomingMSRPChatInviteListener(IncomingMSRPChatInviteListener listener) {
        incomingMSRPChatInviteListenerHolder.addListener(listener);
    }


    public void removeIncomingMSRPChatInviteListener(IncomingMSRPChatInviteListener listener) {
        incomingMSRPChatInviteListenerHolder.removeListener(listener);
    }


    public void addIncomingMSRPFilePushInviteListener(IncomingMSRPFilePushInviteListener listener) {
        incomingMSRPFilePushInviteListenerHolder.addListener(listener);
    }


    public void removeIncomingMSRPFilePushInviteListener(IncomingMSRPFilePushInviteListener listener) {
        incomingMSRPFilePushInviteListenerHolder.removeListener(listener);
    }


    public void extendToConference(String[] additionalParticipants) {
        // TODO Auto-generated method stub

    }


    public void addChatExtensionListener(ChatExtensionListener listener) {
        chatExtensionListenerHolder.addListener(listener);
    }


    public void addChatExtensionListener(Dialog dialog,
                                         ChatExtensionListener listener) {
        // TODO Auto-generated method stub

    }


    public void removeChatExtensionListener(ChatExtensionListener listener) {
        chatExtensionListenerHolder.removeListener(listener);
    }


    public void addIncomingMSRPMessageListener(
            IncomingMSRPMessageListener listener) {
        incomingMSRPMessageListenerHolder.addListener(listener);
    }


    public void addIncomingMSRPMessageListener(MSRPSession msrpSession,
                                               IncomingMSRPMessageListener listener) {
        incomingMSRPMessageListenerHolder.addListener(listener, msrpSession.getIMSEntityId());
    }


    public void removeIncomingMSRPMessageListener(
            IncomingMSRPMessageListener listener) {
        incomingMSRPMessageListenerHolder.removeListener(listener);
    }


    public void addMSRPMessageSendingListener(Dialog dialog,
                                              MSRPMessageSendingListener listener) {
        // TODO Auto-generated method stub

    }


    public void addMSRPMessageSendingListener(
            MSRPMessageSendingListener listener) {
        msrpMessageSendingListenerHolder.addListener(listener);
    }


    public void removeMSRPMessageSendingListener(
            MSRPMessageSendingListener listener) {
        msrpMessageSendingListenerHolder.removeListener(listener);
    }

    public MSRPService getTransactionSafeView() {
        return this;
    }


    public MSRPSession findMSRPSession(String sessionId) {
        return sessionStorage.findSession(sessionId);
    }


    public MSRPSession obtainMSRPSession(final Dialog msrpDialog, MSRPSessionType sessionType) {

        MSRPSession retValue = TransactionUtils.wrap(
                CommonMSRPSession.createOutgoingMsrpSession(
                        getStackContext(),
                        getTransactionManager(),
                        inviteService,
                        MSRPServiceImpl.this,
                        msrpDialog,
                        sessionType
                ), MSRPSession.class
        );

        sessionStorage.addSession(retValue);

        return retValue;
    }


    public void onIncomingCall(IncomingOperationEvent event) {
        Dialog dialog = event.getDialog();

        SdpMessage sdpMessage = dialog.getIncomingSdpMessage();

        if (sdpMessage.typeSupported(SDPType.MSRP)) {
            MSRPSessionType type = MSRPSessionType.CHAT;


            for (Media m : sdpMessage.getMedias()) {
                if (DirectionsType.DirectionSendOnly.equals(m.getDirection())) {
                    type = MSRPSessionType.FILE_IN;
                } else if (DirectionsType.DirectionReceiveOnly.equals(m.getDirection())) {
                    type = MSRPSessionType.FILE_OUT;
                }
            }

            MSRPSession msrpSession = TransactionUtils.wrap(CommonMSRPSession.createIncomingMsrpSession(
                    getStackContext(),
                    getTransactionManager(),
                    inviteService,
                    MSRPServiceImpl.this,
                    dialog,
                    type

            ), MSRPSession.class);
            sessionStorage.addSession(msrpSession);

            inviteService.addDialogStateListener(dialog, new SessionEndListener(msrpSession));

            if (type == MSRPSessionType.CHAT) {
                incomingMSRPChatInviteListenerHolder.getNotifier().onIncomingInvite(
                        new IncomingMSRPChatInviteEvent(event.getAcceptable(), msrpSession)
                );
            } else if (type == MSRPSessionType.FILE_IN) {
                Attribute fileDescriptor = MSRPHelper.obtainFileSelectorAttribute(sdpMessage);
                final List<FileDescriptor> fileDescriptors = FileSelectorParser.parse(fileDescriptor.getValue());

                incomingMSRPFilePushInviteListenerHolder.getNotifier().onIncomingInvite(
                        new IncomingMSRPFilePushInviteEvent(
                                event.getAcceptable(),
                                msrpSession,
                                fileDescriptors)
                );
            }

            //TODO notify incomingMSRPFilePushInviteListenerHolder.getNotifier().onIncomingInvite ...
        }
    }


    class SessionEndListener extends DialogStateListenerAdapter<BaseSipMessage> {

        private final MSRPSession msrpSession;

        SessionEndListener(MSRPSession msrpSession) {
            this.msrpSession = msrpSession;
        }


        public void onSessionTerminated(DialogStateEvent<BaseSipMessage> event) {
            sessionStorage.removeSession(msrpSession);
            //msrpSession.shutdown();
        }


        public void onSessionEnded(DialogStateEvent<BaseSipMessage> event) {
            sessionStorage.removeSession(msrpSession);
            //msrpSession.shutdown();
        }
    }


    public void onIncomingMediaUpdate(IncomingOperationEvent event) {

    }


    public void handleIncomingReportMessage(MsrpMessage msg) {
        Logger.log("handleIncomingReportMessage msrp message" + msg.shortDescription());
    }


    public void handleIncomingSendMessage(final MsrpMessage msg) {
        Logger.log(TAG, "_____________________handleIncomingSendMessage#started  Message=" + msg.shortDescription());
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();
        assert msg.getType() == MsrpMessageType.SEND : "Wrong message type";

        if (!done.get()) {
            MSRPSession msrpSession = sessionStorage.findSession(msg);

            assert msrpSession != null : "MSRP Session for incoming MSRP message not found. MSRP Message: " + msg;
            assert Dialog.DialogState.STATED == msrpSession.getMsrpDialog().getState();


            final TransactionManager transactionManager = getTransactionManager();
            //transactionManager.addRegistrationListener(new FirstMessageResolver(TransactionType.Name.MSRP_SEND_SERVER, DIALOG, msg, transactionManager));
            MSRPTransactionDescriptor descriptor = new DefaultMSRPTransactionDescriptor(msrpSession, msg);
            transactionManager.addListener(
                    new FirstMessageResolver(TransactionType.Name.MSRP_SEND_SERVER, descriptor, msg, transactionManager)
            );

            final Transaction transaction = transactionManager.newTransaction(
                    msrpSession,
                    descriptor,
                    TransactionType.MSRP_SEND_SERVER
            );
            runAsynchronously(transaction, TRANSACTION_TIMEOUT);
        }
    }


    public void shutdown() {
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Shutdowning MSRPServiceImpl");

        if (done.compareAndSet(false, true)) {
            sessionStorage.shutdown();
            inviteService.removeIncomingCallListener(this);
            incomingMSRPChatInviteListenerHolder.shutdown();
            incomingMSRPFilePushInviteListenerHolder.shutdown();
            incomingMSRPMessageListenerHolder.shutdown();
        }
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "MSRPServiceImpl shutdown successfully");
    }
}
