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

package javax.microedition.ims.core.sipservice.pagemessage;

import javax.microedition.ims.common.ListenerHolder;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.common.TimeoutUnit;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.FirstMessageResolver;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.DialogStateListener;
import javax.microedition.ims.core.dialog.IncomingOperationListener;
import javax.microedition.ims.core.sipservice.AbstractService;
import javax.microedition.ims.core.sipservice.invite.DialogCleanUpListener;
import javax.microedition.ims.core.sipservice.invite.DialogStateException;
import javax.microedition.ims.core.sipservice.invite.listener.AuthChallengeListener;
import javax.microedition.ims.core.sipservice.invite.listener.DialogStateMiddleMan;
import javax.microedition.ims.core.transaction.*;
import javax.microedition.ims.core.transaction.server.PageMessageServerTransaction;
import javax.microedition.ims.core.transaction.server.ServerTransaction;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Request;
import java.util.concurrent.atomic.AtomicBoolean;

import static javax.microedition.ims.core.dialog.Dialog.DialogState.STATED;

public class PageMessageServiceImpl extends AbstractService implements PageMessageService,
        IncomingRequestHandler4PageMessageService<Request> {

    private final AtomicBoolean done = new AtomicBoolean(false);

    private final ListenerHolder<DialogStateListener> dialogStateListenerHolder = new ListenerHolder<DialogStateListener>(
            DialogStateListener.class);

    private final ListenerHolder<IncomingOperationListener> incomingPageMessageListenerHolder = new ListenerHolder<IncomingOperationListener>(
            IncomingOperationListener.class);

    private final PageMessageService transactionSafeView;

    public PageMessageServiceImpl(StackContext stackContext, TransactionManager transactionManager) {
        super(stackContext, transactionManager);

        transactionSafeView = TransactionUtils.wrap(this, PageMessageService.class);

        subscribeToTransactionManager();
    }

    public PageMessageService getTransactionSafeView() {
        return transactionSafeView;
    }

    private final TransactionBuildUpListener<BaseSipMessage> clientMessageListener = new TransactionBuildUpListener<BaseSipMessage>() {

        public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
            assert TransactionType.SIP_MESSAGE_CLIENT == event.getTransaction()
                    .getTransactionType();

            Dialog dialog = (Dialog)event.getEntity();
            final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

            transaction.addListener(new AuthChallengeListener<BaseSipMessage>(transaction, dialog));
            transaction.addListener(new DialogStateMiddleMan<BaseSipMessage>(transaction, dialog,
                    dialogStateListenerHolder));
            transaction.addListener(new DialogCleanUpListener<BaseSipMessage>(transaction, dialog,
                    false) {

                protected void onDialogCleanUp(final Dialog dialog) {
                    Logger.log("PageMessageServiceImpl$clientMessageListener", "onDialogCleanUp");
                    getStackContext().getDialogStorage().cleanUpDialog(dialog);
                }
            });
        }
    };

    private final TransactionBuildUpListener<BaseSipMessage> serverMessageListener = new TransactionBuildUpListener<BaseSipMessage>() {

        public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
            assert TransactionType.SIP_MESSAGE_SERVER == event.getTransaction()
                    .getTransactionType();

            Dialog dialog = (Dialog)event.getEntity();
            final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

            // listener will un-subscribe automatically on transaction complete
            transaction.addListener(new IncomingPageMessageListener<BaseSipMessage>(dialog,
                    transaction, incomingPageMessageListenerHolder));

            transaction.addListener(new DialogStateMiddleMan<BaseSipMessage>(transaction, dialog,
                    dialogStateListenerHolder));

            PageMessageTransactionDescription transactionDescription = (PageMessageTransactionDescription)transaction.getDescription();
            
            if(transactionDescription.isOwnDialog()) {
                transaction.addListener(new DialogCleanUpListener<BaseSipMessage>(transaction, dialog,
                        false) {

                    protected void onDialogCleanUp(final Dialog dialog) {
                        Logger.log("PageMessageServiceImpl$serverMessageListener", "onDialogCleanUp");
                        getStackContext().getDialogStorage().cleanUpDialog(dialog);
                    }
                });
            }
        }
    };

    public void handleIncomingPageMessage(Request msg) throws DialogStateException {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in "
                + Thread.currentThread();
        assert !done.get();
        assert msg != null && MessageType.SIP_MESSAGE == MessageType.parse(msg.getMethod());
        if (!done.get()) {
            Logger.log("Remote party has sent message");
            // ClientIdentity localParty =
            // getStackContext().getStackClientRegistry().findAddressee(msg.getTo().getUriBuilder().getShortURI());
            ClientIdentity localParty = getStackContext().getClientRouter().findAddressee(msg);

            if (localParty != null) {
                // assert
                // getStackContext().getDialogStorage().findDialogForMessage(msg)
                // == null;
                // AK: there cases when message is sent within dialog, for
                // example ussd calling
                boolean isDialogAlreadyExists = getStackContext().getDialogStorage()
                        .findDialogForMessage(msg) != null;
                final Dialog dialog = getStackContext().getDialogStorage()
                        .getDialogForIncomingMessage(localParty, msg);
                TransactionType<ServerTransaction, ? extends PageMessageServerTransaction> transactionType = TransactionType.SIP_MESSAGE_SERVER;

                doHandleIncomingPageMessage(msg, dialog, transactionType, !isDialogAlreadyExists);
            } else {
                Logger.log("Can't find client for page message");
                throw new DialogStateException(null,
                        DialogStateException.Error.REQUEST_CANNOT_BE_HANDLED, msg);
            }
        }
    }

    private void doHandleIncomingPageMessage(
            final Request msg,
            final Dialog dialog,
            final TransactionType<ServerTransaction, ? extends PageMessageServerTransaction> transactionType,
            boolean isOwnDialog) {
        dialog.getMessageHistory().addMessage(msg, true);

        Logger.log("doHandleIncomingPageMessage", "");
        /*
         * if
         * (ContentType.SDP.stringValue().equals(msg.getContentType().getValue
         * ())) { DIALOG.setIncomingSdpMessage(SdpParser.parse(new
         * String(msg.getBody()))); }
         */

        final TransactionManager transactionManager = getTransactionManager();
        transactionManager.addListener(new FirstMessageResolver(transactionType.getName(), dialog,
                msg, transactionManager));

        final PageMessageServerTransaction transaction = (PageMessageServerTransaction)transactionManager
                .lookUpTransaction(dialog, new DegaultPageMessageTransactionDescription(isOwnDialog), transactionType);
        runAsynchronously(transaction, TRANSACTION_TIMEOUT);
    }

    
    public void sendPageMessage(final Dialog dialog) throws DialogStateException {
        assert !done.get();

        if (!done.get()) {
            if (STATED == dialog.getState()) {
                throw new DialogStateException(dialog,
                        DialogStateException.Error.SEND_MESSAGE_FOR_STATED_DIALOG, null,
                        "Can not send message. Dialog is already stated.");
            }

            doSendPageMessage(dialog, TRANSACTION_TIMEOUT);
        }
    }

    private void doSendPageMessage(final Dialog dialog, final TimeoutUnit timeoutUnit) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in "
                + Thread.currentThread();
        assert !done.get();

        final Transaction transaction = getTransactionManager().lookUpTransaction(dialog, null,
                TransactionType.SIP_MESSAGE_CLIENT);

        runAsynchronously((Transaction<Boolean, BaseSipMessage>)transaction, timeoutUnit);
    }


    
    public void shutdown() {
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Shutdowning InviteService");
        if (done.compareAndSet(false, true)) {
            unSubscribeFromTransactionManager();
        }
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "InviteService shutdown successfully");
    }

    public void addIncomingOperationListener(ClientIdentity identity,
            IncomingOperationListener listener) {
        incomingPageMessageListenerHolder.addListener(listener, identity);
    }

    
    public void removeIncomingOperationListener(IncomingOperationListener listener) {
        incomingPageMessageListenerHolder.removeListener(listener);
    }

    public void addDialogStateListener(Dialog dialog, DialogStateListener listener) {
        dialogStateListenerHolder.addListener(listener, dialog);
    }

    
    public void removeDialogStateListener(DialogStateListener listener) {
        dialogStateListenerHolder.removeListener(listener);
    }

    private void subscribeToTransactionManager() {
        getTransactionManager().addListener(clientMessageListener,
                TransactionType.Name.SIP_MESSAGE_CLIENT);
        getTransactionManager().addListener(serverMessageListener,
                TransactionType.Name.SIP_MESSAGE_SERVER);
    }

    private void unSubscribeFromTransactionManager() {
        getTransactionManager().removeListener(clientMessageListener);
        getTransactionManager().removeListener(serverMessageListener);
    }
}
