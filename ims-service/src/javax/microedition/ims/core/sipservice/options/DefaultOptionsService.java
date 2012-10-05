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

package javax.microedition.ims.core.sipservice.options;

import javax.microedition.ims.common.*;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.FirstMessageResolver;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.DialogStateEvent;
import javax.microedition.ims.core.dialog.IncomingOptionsListener;
import javax.microedition.ims.core.registry.CommonRegistry;
import javax.microedition.ims.core.registry.property.CapabilityProperty;
import javax.microedition.ims.core.sipservice.AbstractService;
import javax.microedition.ims.core.sipservice.invite.DialogCleanUpListener;
import javax.microedition.ims.core.sipservice.invite.DialogStateException;
import javax.microedition.ims.core.sipservice.invite.listener.AuthChallengeListener;
import javax.microedition.ims.core.sipservice.options.listener.OptionsClientListener;
import javax.microedition.ims.core.sipservice.options.listener.OptionsServerListener;
import javax.microedition.ims.core.sipservice.options.listener.OptionsStateListener;
import javax.microedition.ims.core.transaction.*;
import javax.microedition.ims.core.transaction.server.OptionsServerTransaction;
import javax.microedition.ims.core.transaction.server.ServerTransaction;
import javax.microedition.ims.messages.wrappers.sdp.SdpMessage;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Header;
import javax.microedition.ims.messages.wrappers.sip.Request;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static javax.microedition.ims.core.dialog.Dialog.DialogState.STATED;

/**
 * Default options service.
 *
 * @author Andrei Khomushko
 */
public class DefaultOptionsService extends AbstractService implements OptionsService, IncomingRequestHandler4OptionsService<Request> {
    private final static String TAG = "DefaultOptionsService";

    private final AtomicBoolean done = new AtomicBoolean(false);
    private final OptionsService transactionSafeView;

    //private final ListenerHolder<DialogStateListener> dialogStateListenerHolder = new ListenerHolder<DialogStateListener>(DialogStateListener.class);
    private final ListenerHolder<IncomingOptionsListener> incomingOptionsListenerHolder = new ListenerHolder<IncomingOptionsListener>(IncomingOptionsListener.class);
    private final ListenerHolder<OptionsStateListener> optionsStateListenerHolder = new ListenerHolder<OptionsStateListener>(OptionsStateListener.class);


    public DefaultOptionsService(StackContext stackContext,
                                 TransactionManager transactionManager) {
        super(stackContext, transactionManager);

        transactionSafeView = TransactionUtils.wrap(this, OptionsService.class);

        subscribeToTransactionManager();
        addIncomingOptionsListener(incomingOptionsListener);
    }

    public OptionsService getTransactionSafeView() {
        return transactionSafeView;
    }

    private final IncomingOptionsListener<Request> incomingOptionsListener = new IncomingOptionsListener<Request>() {
        
        public void optionsRequestRecieved(DialogStateEvent<Request> event) {
            Logger.log(TAG, "optionsRequestRecieved#");
            Request optionsRequest = event.getTriggeringMessage();
            assert MessageType.parse(optionsRequest.getMethod()) == MessageType.SIP_OPTIONS;

            if (isSdpNeedInResponse(optionsRequest)) {
                Logger.log(TAG, "optionsRequestRecieved#add sdp to response for options");
                CommonRegistry commonRegistry = getStackContext().getStackRegistry().getCommonRegistry();
                if (commonRegistry == null || commonRegistry.getCapabilityProperties() == null) {
                    Logger.log(TAG, "optionsRequestRecieved#add sdp to response for options NULL");
                    return;
                }

                CapabilityProperty[] capabilityProperties = commonRegistry.getCapabilityProperties();

                final SdpMessage sdpMessage = event.getDialog().getOutgoingSdpMessage();
                for (CapabilityProperty capabilityProperty : capabilityProperties) {
                    if (capabilityProperty.getMessageType() == CapabilityProperty.MessageType.Response ||
                            capabilityProperty.getMessageType() == CapabilityProperty.MessageType.Request_response) {
                        sdpMessage.getAttributes().addAll(Arrays.asList(capabilityProperty.getSdpFields()));
                    }
                }
            }
        }
    };

    private boolean isSdpNeedInResponse(Request request) {
        List<String> acceptValue = request.getCustomHeader(Header.Accept);
        return !acceptValue.isEmpty() && MimeType.APP_SDP.stringValue().equalsIgnoreCase(acceptValue.get(0));
    }

    private final TransactionBuildUpListener<BaseSipMessage> clientMessageListener =
            new TransactionBuildUpListener<BaseSipMessage>() {
                public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
                    assert TransactionType.SIP_OPTIONS_CLIENT == event.getTransaction().getTransactionType();

                    Dialog dialog = (Dialog) event.getEntity();
                    final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

                    transaction.addListener(new AuthChallengeListener<BaseSipMessage>(transaction, dialog));
                    //listener will un-subscribe automatically on transaction complete
                    transaction.addListener(
                            new OptionsClientListener<BaseSipMessage>(
                                    dialog,
                                    transaction,
                                    optionsStateListenerHolder
                            )
                    );

                    //transaction.addRegistrationListener(
                    //    new DialogStateMiddleMan<BaseSipMessage>(transaction, dialog, dialogStateListenerHolder)
                    //);

                    transaction.addListener(
                            new DialogCleanUpListener<BaseSipMessage>(transaction, dialog, false) {

                                protected void onDialogCleanUp(final Dialog dialog) {
                                    getStackContext().getDialogStorage().cleanUpDialog(dialog);
                                }
                            }
                    );
                }
            };

    private final TransactionBuildUpListener<BaseSipMessage> serverMessageListener =
            new TransactionBuildUpListener<BaseSipMessage>() {
                public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
                    assert TransactionType.SIP_OPTIONS_SERVER == event.getTransaction().getTransactionType();

                    Dialog dialog = (Dialog) event.getEntity();
                    final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

                    //listener will un-subscribe automatically on transaction complete
                    transaction.addListener(
                            new OptionsServerListener<BaseSipMessage>(
                                    dialog,
                                    transaction,
                                    incomingOptionsListenerHolder
                            )
                    );

                    //transaction.addRegistrationListener(
                    //        new DialogStateMiddleMan<BaseSipMessage>(transaction, dialog, dialogStateListenerHolder)
                    //);

                    transaction.addListener(
                            new DialogCleanUpListener<BaseSipMessage>(transaction, dialog, false) {

                                protected void onDialogCleanUp(final Dialog dialog) {
                                    getStackContext().getDialogStorage().cleanUpDialog(dialog);
                                }
                            }
                    );
                }
            };

/*
    public void addDialogStateListener(Dialog dialog,
            DialogStateListener listener) {
        dialogStateListenerHolder.addRegistrationListener(dialog, listener);
    }

    
    public void removeDialogStateListener(DialogStateListener listener) {
        dialogStateListenerHolder.removeRegistrationListener(listener);
    }
*/
    public void addIncomingOptionsListener(IncomingOptionsListener listener) {
        incomingOptionsListenerHolder.addListener(listener);
    }

    
    public void removeIncomingOptionsListener(
            IncomingOptionsListener listener) {
        incomingOptionsListenerHolder.removeListener(listener);
    }

    public void addOptionsStateListener(Dialog dialog,
                                        OptionsStateListener listener) {
        optionsStateListenerHolder.addListener(listener, dialog);
    }

    public void removeOptionsStateListener(OptionsStateListener listener) {
        optionsStateListenerHolder.removeListener(listener);
    }

    
    public void sendOptionsMessage(Dialog dialog, boolean sdpInRequest) throws DialogStateException {
        assert !done.get();

        if (!done.get()) {
            if (STATED == dialog.getState()) {
                throw new DialogStateException(dialog, DialogStateException.Error.SEND_MESSAGE_FOR_STATED_DIALOG, null, "Can not send options. Dialog is already stated.");
            }
            dialog.putCustomParameter(Dialog.ParamKey.OPTIONS_USE_BODY, sdpInRequest);
            doSendOptionsMessage(dialog, TRANSACTION_TIMEOUT);
        }
    }

    private void doSendOptionsMessage(final Dialog dialog, final TimeoutUnit timeoutUnit) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();

        final Transaction transaction =
                getTransactionManager().lookUpTransaction(
                        dialog,
                        null,
                        TransactionType.SIP_OPTIONS_CLIENT
                );

        runAsynchronously((Transaction<Boolean, BaseSipMessage>) transaction, timeoutUnit);
    }

    
    public void handleIncomingOptionsMessage(Request msg) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();
        assert msg != null && MessageType.SIP_OPTIONS == MessageType.parse(msg.getMethod());
        if (!done.get()) {
            Logger.log(TAG, "handleIncomingOptionsMessage#Remote party has sent message");

            //ClientIdentity localParty = getStackContext().getStackClientRegistry().findAddressee(msg.getTo().getUriBuilder().getShortURI());
            //ClientIdentity localParty = getStackContext().getClientRouter().findAddressee(msg);
            ClientIdentity localParty = getStackContext().getRegistrationIdentity();

            final Dialog dialog = getStackContext().getDialogStorage().getDialogForIncomingMessage(localParty, msg);

            TransactionType<ServerTransaction, ? extends OptionsServerTransaction> transactionType = TransactionType.SIP_OPTIONS_SERVER;
            doHandleIncomingOptionsMessage(msg, dialog, transactionType);
        }
    }

    private void doHandleIncomingOptionsMessage(final Request msg, final Dialog dialog, final TransactionType<ServerTransaction, ? extends OptionsServerTransaction> transactionType) {
        dialog.getMessageHistory().addMessage(msg, true);

        Logger.log(TAG, "doHandleIncomingOptionsMessage#");

        final TransactionManager transactionManager = getTransactionManager();
        transactionManager.addListener(new FirstMessageResolver(transactionType.getName(), dialog, msg, transactionManager));

        final OptionsServerTransaction transaction = (OptionsServerTransaction) transactionManager.lookUpTransaction(
                dialog,
                null,
                transactionType
        );

        runAsynchronously(transaction, TRANSACTION_TIMEOUT);
    }

    
    public void shutdown() {
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Shutdowning OptionsService");
        if (done.compareAndSet(false, true)) {
            unSubscribeFromTransactionManager();
            removeIncomingOptionsListener(incomingOptionsListener);
            optionsStateListenerHolder.shutdown();
            incomingOptionsListenerHolder.shutdown();
        }
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "OptionsService shutdown successfully");
    }

    private void subscribeToTransactionManager() {
        getTransactionManager().addListener(clientMessageListener, TransactionType.Name.SIP_OPTIONS_CLIENT);
        getTransactionManager().addListener(serverMessageListener, TransactionType.Name.SIP_OPTIONS_SERVER);
    }

    private void unSubscribeFromTransactionManager() {
        getTransactionManager().removeListener(clientMessageListener);
        getTransactionManager().removeListener(serverMessageListener);
    }
}
