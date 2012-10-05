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

package javax.microedition.ims.core.sipservice.invite;

import javax.microedition.ims.common.*;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.FirstMessageResolver;
import javax.microedition.ims.core.InitiateParty;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.*;
import javax.microedition.ims.core.sipservice.AbstractService;
import javax.microedition.ims.core.sipservice.Acceptable;
import javax.microedition.ims.core.sipservice.SessionState;
import javax.microedition.ims.core.sipservice.SteppedAcceptable;
import javax.microedition.ims.core.sipservice.invite.listener.*;
import javax.microedition.ims.core.sipservice.timer.TimeoutTimer;
import javax.microedition.ims.core.sipservice.timer.TimeoutTimer.TimeoutListener;
import javax.microedition.ims.core.transaction.*;
import javax.microedition.ims.core.transaction.client.InviteClntTransaction;
import javax.microedition.ims.core.transaction.server.InviteSrvTransaction;
import javax.microedition.ims.core.transaction.server.ServerCommonInviteTransaction;
import javax.microedition.ims.core.transaction.server.UpdateServerTransaction;
import javax.microedition.ims.core.transaction.server.UpdateSrvTransaction;
import javax.microedition.ims.messages.utils.SipMessageUtils;
import javax.microedition.ims.messages.wrappers.sdp.SdpMessage;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Refresher;
import javax.microedition.ims.messages.wrappers.sip.Request;
import java.util.concurrent.atomic.AtomicBoolean;

import static javax.microedition.ims.core.dialog.Dialog.DialogState.EARLY;
import static javax.microedition.ims.core.dialog.Dialog.DialogState.STATED;
import static javax.microedition.ims.core.sipservice.invite.DialogStateException.Error.*;
import static javax.microedition.ims.core.transaction.TransactionType.*;

/**
 * This service is responsible for Invite SESSION initiating, updating and destroying
 *
 * @author ext-achirko
 * @author Pavel Laboda (pavel.laboda@gmail.com)
 */
public class InviteServiceImpl extends AbstractService implements InviteService, IncomingRequestHandler4InviteService<Request> {

    private final String TAG = "InviteServiceImpl";

    private final ListenerHolder<DialogStateListener> dialogStateListenerHolder = new ListenerHolder<DialogStateListener>(DialogStateListener.class);
    private final ListenerHolder<IncomingCallListener> incomingCallListenerHolder = new ListenerHolder<IncomingCallListener>(IncomingCallListener.class);

    private final InviteService transactionSafeView;
    //private final Acceptable transactionSafeAcceptable;

    private final AtomicBoolean done = new AtomicBoolean(false);

    private final TransactionBuildUpListener<BaseSipMessage> clientInviteListener = new TransactionBuildUpListener<BaseSipMessage>() {
        public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
            assert SIP_INVITE_CLIENT == event.getTransaction().getTransactionType();

            Dialog dialog = (Dialog) event.getEntity();
            final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(new AuthChallengeListener<BaseSipMessage>(transaction, dialog));
            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(
                    new MiddleManForClientMessageBuildingSupport<BaseSipMessage>(transaction, dialog)
            );
            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(
                    new DialogStateMiddleMan<BaseSipMessage>(transaction, dialog, dialogStateListenerHolder)
            );
            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(new DialogBecomeStatedListener(transaction, dialog));
            transaction.addListener(new RemotePartyAnnouncedListener(transaction, dialog));
            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(
                    new DialogCleanUpListener<BaseSipMessage>(transaction, dialog, false) {

                        protected void onDialogCleanUp(final Dialog dialog) {
                            getStackContext().getDialogStorage().cleanUpDialog(dialog);
                        }
                    }
            );
        }
    };

    private final TransactionBuildUpListener<BaseSipMessage> clientReInviteListener = new TransactionBuildUpListener<BaseSipMessage>() {
        public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
            assert SIP_REINVITE_CLIENT == event.getTransaction().getTransactionType();

            Dialog dialog = (Dialog) event.getEntity();
            final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

            //DIALOG.putCustomParameter(Dialog.ParamKey.REINVITE_IN_PROGRESS, Boolean.TRUE);
            dialog.markReInviteInProgress(InitiateParty.LOCAL);

            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(new AuthChallengeListener<BaseSipMessage>(transaction, dialog));
            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(new MiddleManForClientMessageBuildingSupport<BaseSipMessage>(transaction, dialog));
            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(
                    new ReInviteStateMiddleMan<BaseSipMessage>(transaction, dialog, dialogStateListenerHolder) {

                        protected void onDialogCleanUp(final Dialog dialog) {
                            getStackContext().getDialogStorage().cleanUpDialog(dialog);
                        }
                    }
            );
            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(
                    new ReinviteInProgressListener<BaseSipMessage>(transaction, dialog, InitiateParty.LOCAL)
            );
        }
    };

    private final TransactionBuildUpListener<BaseSipMessage> serverInviteListener = new TransactionBuildUpListener<BaseSipMessage>() {
        public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
            assert SIP_INVITE_SERVER == event.getTransaction().getTransactionType();

            Dialog dialog = (Dialog) event.getEntity();
            final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(
                    new IncomingInviteListener<BaseSipMessage>(
                            dialog,
                            createSafeInviteAcceptable(dialog),
                            transaction,
                            incomingCallListenerHolder
                    )
            );
            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(new MiddleManForServerMessageBuildingSupport(transaction, dialog));
            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(
                    new DialogStateMiddleMan<BaseSipMessage>(transaction, dialog, dialogStateListenerHolder)
            );

            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(new DialogBecomeStatedListener(transaction, dialog));
            transaction.addListener(new RemotePartyAnnouncedListener(transaction, dialog));

            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(
                    new DialogCleanUpListener<BaseSipMessage>(transaction, dialog, false) {

                        protected void onDialogCleanUp(final Dialog dialog) {
                            getStackContext().getDialogStorage().cleanUpDialog(dialog);
                        }
                    }
            );
        }
    };

    private final TransactionBuildUpListener<BaseSipMessage> serverReInviteListener = new TransactionBuildUpListener<BaseSipMessage>() {
        public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
            assert SIP_REINVITE_SERVER == event.getTransaction().getTransactionType();

            Dialog dialog = (Dialog) event.getEntity();
            final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

            //DIALOG.putCustomParameter(Dialog.ParamKey.REINVITE_IN_PROGRESS, Boolean.TRUE);
            //TODo reinvire is used for update too
            //dialog.markReInviteInProgress(InitiateParty.REMOTE);

            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(
                    new IncomingReInviteListener<BaseSipMessage>(
                            dialog,
                            createSafeInviteAcceptable(dialog),
                            transaction,
                            incomingCallListenerHolder
                    )
            );

            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(new MiddleManForServerMessageBuildingSupport(transaction, dialog));
            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(
                    new ReInviteStateMiddleMan<BaseSipMessage>(transaction, dialog, dialogStateListenerHolder) {

                        protected void onDialogCleanUp(final Dialog dialog) {
                            getStackContext().getDialogStorage().cleanUpDialog(dialog);
                        }
                    }
            );
            //listener will un-subscribe automatically on transaction complete
            //TODO change
            if(dialog.isUpdateInProgress()) {
                transaction.addListener(
                        new UpdateInProgressListener<BaseSipMessage>(transaction, dialog, InitiateParty.REMOTE)
                );
            } else {
                transaction.addListener(
                        new ReinviteInProgressListener<BaseSipMessage>(transaction, dialog, InitiateParty.REMOTE)
                );
            }
            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(
                    new DialogCleanUpListener<BaseSipMessage>(transaction, dialog, false) {

                        protected void onDialogCleanUp(final Dialog dialog) {
                            getStackContext().getDialogStorage().cleanUpDialog(dialog);
                        }
                    }
            );
        }
    };
    
    private final TransactionBuildUpListener<BaseSipMessage> serverUpdateListener = new TransactionBuildUpListener<BaseSipMessage>() {
        public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
            assert SIP_UPDATE_SERVER == event.getTransaction().getTransactionType();

            Dialog dialog = (Dialog) event.getEntity();
            final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

            //DIALOG.putCustomParameter(Dialog.ParamKey.REINVITE_IN_PROGRESS, Boolean.TRUE);
            //TODo reinvire is used for update too
            //dialog.markReInviteInProgress(InitiateParty.REMOTE);

            //listener will un-subscribe automatically on transaction complete

            transaction.addListener(
                    new IncomingReInviteListener<BaseSipMessage>(
                            dialog,
                            createSafeInviteAcceptable(dialog),
                            transaction,
                            incomingCallListenerHolder
                    )
            );
            
            transaction.addListener(
                    new DialogStateMiddleMan<BaseSipMessage>(transaction, dialog, dialogStateListenerHolder)
            );
            
            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(new MiddleManForServerMessageBuildingSupport(transaction, dialog));
            
            transaction.addListener(
                new UpdateInProgressListener<BaseSipMessage>(transaction, dialog, InitiateParty.REMOTE)
            );
        }
    };

    private final TransactionBuildUpListener<BaseSipMessage> clientByeListener = new TransactionBuildUpListener<BaseSipMessage>() {
        public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
            assert SIP_BYE_CLIENT == event.getTransaction().getTransactionType();

            Dialog dialog = (Dialog) event.getEntity();
            final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(new DialogStateMiddleMan<BaseSipMessage>(transaction, dialog, dialogStateListenerHolder));
            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(
                    new DialogCleanUpListener<BaseSipMessage>(transaction, dialog, null) {

                        protected void onDialogCleanUp(final Dialog dialog) {
                            getStackContext().getDialogStorage().cleanUpDialog(dialog);
                        }
                    }
            );
        }
    };

    private final TransactionBuildUpListener<BaseSipMessage> serverByeListener = new TransactionBuildUpListener<BaseSipMessage>() {
        public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
            assert SIP_BYE_SERVER == event.getTransaction().getTransactionType();

            Dialog dialog = (Dialog) event.getEntity();
            final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(
                    new DialogStateMiddleMan<BaseSipMessage>(transaction, dialog, dialogStateListenerHolder)
            );

            //listener will un-subscribe automatically on transaction complete
            transaction.addListener(
                    new DialogCleanUpListener<BaseSipMessage>((ListenerSupport<TransactionListener<BaseSipMessage>>) transaction, dialog, null) {

                        protected void onDialogCleanUp(final Dialog dialog) {
                            getStackContext().getDialogStorage().cleanUpDialog(dialog);
                        }
                    }
            );
        }
    };

    private final DialogStateListener<BaseSipMessage> listenerForInviteRefresh = new DialogStateListenerAdapter<BaseSipMessage>() {

        public void onSessionStarted(DialogStateEvent<BaseSipMessage> event) {
            super.onSessionStarted(event);
            BaseSipMessage mes = event.getTriggeringMessage();
            if (mes.getSessionExpires() != null) {
                Refresher refresher = getStackContext().getConfig().getRefresher();
                long refreshTime = getStackContext().getConfig().getSessionExpiresTime() / 2;
                final Dialog dialog = event.getDialog();
                //TODO it's recommended in RFC to divide this time by 2. Check.
                if (mes.getSessionExpires().getRefresher() != null) {
                    refresher = mes.getSessionExpires().getRefresher();
                    refreshTime = mes.getSessionExpires().getExpiresValue() / 2;
                }
                if ((event.getDialog().getInitiateParty() == InitiateParty.LOCAL && refresher == Refresher.UAC) ||
                        (event.getDialog().getInitiateParty() == InitiateParty.REMOTE && refresher == Refresher.UAS)) {
                    //we are responsible for handling invite refresh
                    TimeoutTimer.getInstance().startTimer(new TimeoutListener() {

                        public void onTimeout() {
                            Logger.log(TAG, "listenerForInviteRefresh.onTimeout()");
                            try {
                                reInvite(dialog);
                            }
                            catch (DialogStateException e) {
                                e.printStackTrace();
                                assert false : "exception during invite refresh " + dialog + " " + e.toString();
                            }
                        }
                    }, 32000/*refreshTime*/);
                }
            }
        }
    };


    public InviteServiceImpl(
            final StackContext stackContext,
            final TransactionManager transactionManager) {
        super(stackContext, transactionManager);

        transactionSafeView = TransactionUtils.wrap(this, InviteService.class);


        subscribeToTransactionManager();
    }

    private SteppedAcceptable createSafeInviteAcceptable(final Dialog dialog) {
        return TransactionUtils.wrap(
                new SteppedAcceptable<Dialog>() {

                    private final Dialog dlg = dialog;

                    public void reject(Dialog parameter, int statusCode, String alternativeUserAddress) {
                        if (dialog.isUpdateInProgress()) {
                            InviteServiceImpl.this.doRejectUpdate(dlg, statusCode, alternativeUserAddress);
                            dialog.unmarkUpdateInProgress();
                        }
                        else {
                            InviteServiceImpl.this.doReject(dlg, statusCode, alternativeUserAddress);
                        }
                    }

                    public void accept(Dialog parameter) {
                        Logger.log("SteppedAcceptable", "accept#dialog.isUpdateInProgress() = " + dialog.isUpdateInProgress());
                        
                        if (dialog.isUpdateInProgress()) {
                            Logger.log("SteppedAcceptable", "doAcceptUpdate");
                            InviteServiceImpl.this.doAcceptUpdate(dialog);
                            dialog.unmarkUpdateInProgress();
                        }
                        else {
                            Logger.log("SteppedAcceptable", "doAccept");
                            InviteServiceImpl.this.doAccept(dlg);
                        }
                    }

                    public void preAccept() {
                        if (!dialog.isUpdateInProgress()) {
                            InviteServiceImpl.this.preAccept(dlg);
                        }
                        else {
                            assert false : "Method not supported";
                        }
                    }
                }, SteppedAcceptable.class

        );
    }

    public InviteService getTransactionSafeView() {
        return transactionSafeView;
    }

    public void addDialogStateListener(final Dialog dialog, final DialogStateListener<BaseSipMessage> listener) {
        dialogStateListenerHolder.addListener(listener, dialog);
    }

    public void removeDialogStateListener(final DialogStateListener<BaseSipMessage> listener) {
        dialogStateListenerHolder.removeListener(listener);
    }

    public void addIncomingCallListener(final IncomingCallListener listener) {
        incomingCallListenerHolder.addListener(listener);
    }

    public void addIncomingCallListener(SDPType sdpType, final IncomingCallListener listener) {
        incomingCallListenerHolder.addListener(listener, sdpType);
    }

    public void addIncomingCallListener(ClientIdentity clientIdentity, IncomingCallListener listener) {
        incomingCallListenerHolder.addListener(listener, clientIdentity);
    }

    public void addIncomingCallListener(ClientIdentity identity, SDPType type, IncomingCallListener listener) {
        incomingCallListenerHolder.addListener(listener, identity, type);
    }

    public void removeIncomingCallListener(final IncomingCallListener listener) {
        incomingCallListenerHolder.removeListener(listener);
    }


    /**
     * This method tries to invite remote party
     */

    public void invite(final Dialog dialog) throws DialogStateException {
        Logger.log(TAG, "invite#started");
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();

        if (!done.get()) {
            if (STATED == dialog.getState()) {
                throw new DialogStateException(dialog, INVITE_FOR_STATED_DIALOG, null, "Can not invite remote party. Dialog is already stated.");
            }

            if (getStackContext().getConfig().useInviteRefresh()) {
                addDialogStateListener(dialog, listenerForInviteRefresh);
            }
            doInvite(dialog, LONG_TRANSACTION_TIMEOUT);
        }
        Logger.log(TAG, "invite#finished");
    }

    public void reInvite(final Dialog dialog) throws DialogStateException {
        Logger.log(TAG, "reInvite#started");
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();
        assert getStackContext().getDialogStorage().findDialogByCallId(dialog.getCallId()) != null : "DIALOG being re-invited is already  terminated";

        if (!done.get()) {

            checkReInvitePreconditions(dialog, null);
            dialog.getOutgoingSdpMessage().setSessionVersion(dialog.getOutgoingSdpMessage().getSessionVersion() + 1);
            doReInvite(dialog, LONG_TRANSACTION_TIMEOUT);
        }
        Logger.log(TAG, "reInvite#finished");
    }


    public void update(Dialog dialog) throws DialogStateException {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();
        assert getStackContext().getDialogStorage().findDialogByCallId(dialog.getCallId()) != null : "DIALOG being updated is already  terminated";

        if (!done.get()) {

            checkUpdatePreconditions(dialog, null);
            dialog.getOutgoingSdpMessage().setSessionVersion(dialog.getOutgoingSdpMessage().getSessionVersion() + 1);
            doUpdate(dialog);
        }

    }


    /**
     * This method initiates SIP_BYE  for established DIALOG
     *
     * @param dialog - DIALOG to terminate
     */
    public void bye(final Dialog dialog) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();

        if (!done.get()) {
            assert STATED == dialog.getState() : "Wrong dialog state. Must be " + STATED + " now is " + dialog.getState();
            doBye(dialog, TRANSACTION_TIMEOUT);
        }
    }

    /**
     * Cancels establishing DIALOG
     *
     * @param dialog - DIALOG to cancel
     */
    public void cancel(final Dialog dialog) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();
        Logger.log("Canceling call");

        if (!done.get()) {
            doCancel(dialog);
        }
    }

    private void preAccept(final Dialog dialog) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();
        Logger.log(TAG, "preAccept");

        if (!done.get()) {
            doPreAccept(dialog);
        }
    }

    //TODO: check if DIALOG terminated on successive reject

    /**
     * Rejects server invite
     *
     * @param dialog                 - associated DIALOG
     * @param statusCode             - status code to send to remote party
     * @param alternativeUserAddress - alternative USER address for status code 302(Moved)
     */
    private void doReject(final Dialog dialog, final int statusCode, final String alternativeUserAddress) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();

        TransactionType<InviteSrvTransaction, ? extends ServerCommonInviteTransaction> transactionType;
        if (dialog.isReInviteInProgress()) {
            transactionType = SIP_REINVITE_SERVER;
        }
        else {
            transactionType = SIP_INVITE_SERVER;
        }

        assert transactionType == SIP_INVITE_SERVER || statusCode == 488;

        getTransactionManager().findTransaction(dialog, transactionType).reject(statusCode, alternativeUserAddress);
    }


    /**
     * Accept server invite
     *
     * @param dialog - DIALOG associated with this invite SESSION
     */
    private void doAccept(final Dialog dialog) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();

        TransactionType<InviteSrvTransaction, ? extends ServerCommonInviteTransaction> transactionType;

        if (dialog.isReInviteInProgress()) {
            transactionType = SIP_REINVITE_SERVER;
        }
        else {
            transactionType = SIP_INVITE_SERVER;
        }

        getTransactionManager().findTransaction(dialog, transactionType).accept();
    }

    private void doRejectUpdate(final Dialog dialog, final int statusCode, final String alternativeUserAddress) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();

        if (dialog.getInitiateParty() == InitiateParty.LOCAL) {
            getTransactionManager().findTransaction(dialog, SIP_INVITE_CLIENT).rejectUpdate(statusCode, alternativeUserAddress);
        }
        else {
            //getTransactionManager().findTransaction(dialog, SIP_INVITE_SERVER).rejectUpdate(statusCode, alternativeUserAddress);
            TransactionType<UpdateSrvTransaction, UpdateServerTransaction> transactionType = SIP_UPDATE_SERVER;
            getTransactionManager().findTransaction(dialog, transactionType).reject(statusCode, alternativeUserAddress);
        }

    }

    private void doAcceptUpdate(final Dialog dialog) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();

        
        
        if (dialog.isReInviteInProgress() || dialog.isUpdateInProgress()) {
            //transactionType = SIP_REINVITE_SERVER;
            TransactionType<UpdateSrvTransaction, UpdateServerTransaction> transactionType = SIP_UPDATE_SERVER;
            getTransactionManager().findTransaction(dialog, transactionType).accept();
        }
        else {
            TransactionType<InviteSrvTransaction, ? extends ServerCommonInviteTransaction> transactionType = SIP_INVITE_SERVER;
            getTransactionManager().findTransaction(dialog, transactionType).acceptUpdate();
        }
        
        
        
/*        if (dialog.getInitiateParty() == InitiateParty.LOCAL) {
            getTransactionManager().findTransaction(dialog, SIP_INVITE_CLIENT).acceptUpdate();
        }
        else {
            getTransactionManager().findTransaction(dialog, SIP_INVITE_SERVER).acceptUpdate();
        }
*/    }

    private void doPreAccept(final Dialog dialog) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();

        TransactionType<InviteSrvTransaction, ? extends ServerCommonInviteTransaction> transactionType;

        if (dialog.isReInviteInProgress()) {
            transactionType = SIP_REINVITE_SERVER;
        }
        else {
            transactionType = SIP_INVITE_SERVER;
        }

        getTransactionManager().findTransaction(dialog, transactionType).preAccept();
    }

    //TODO method should call callback,
    //TODO add forseCreating on absence to getTransactionManager().lookUpTransaction

    private void doCancel(final Dialog dialog) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();

        final InviteClntTransaction transaction = getTransactionManager().findTransaction(dialog, SIP_INVITE_CLIENT);

        final DialogStateEvent<BaseSipMessage> stateEvent =
                new DefaultDialogStateEvent<BaseSipMessage>(dialog, SessionState.SESSION_TERMINATED, null);

        dialogStateListenerHolder.getNotifier().onSessionTerminated(stateEvent);
        //dialogStateListenerHolder.getNotifier().onSessionEnded(stateEvent);
        //DIALOG.putCustomParameter(ParamKey.INITIAL_MESSAGE, ((Transaction) transaction).getInitialMessage());

        if (transaction != null) {
            transaction.cancel();
        }
    }


    private void doInvite(final Dialog dialog, final TimeoutUnit timeoutUnit) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();

        final InviteClntTransaction transaction = getTransactionManager().lookUpTransaction(
                dialog,
                null,
                SIP_INVITE_CLIENT
        );

        //runAsynchronously((Transaction<Boolean, BaseSipMessage>) transaction, timeoutUnit);
        runAsynchronously((Transaction<Boolean, BaseSipMessage>) transaction);
    }

    private void doReInvite(final Dialog dialog, final TimeoutUnit timeoutUnit) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();
        final InviteClntTransaction transaction = getTransactionManager().lookUpTransaction(
                dialog,
                null,
                SIP_REINVITE_CLIENT
        );

        //runAsynchronously((Transaction<Boolean, BaseSipMessage>) transaction, timeoutUnit);
        runAsynchronously((Transaction<Boolean, BaseSipMessage>) transaction);
    }


    private void doUpdate(Dialog dialog) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();

        if (dialog.getInitiateParty() == InitiateParty.LOCAL) {
            getTransactionManager().findTransaction(dialog, SIP_INVITE_CLIENT).update();
        }
        else {
            getTransactionManager().findTransaction(dialog, SIP_INVITE_SERVER).update();
        }

    }

    //TODO: check if DIALOG terminated on successive noninvite

    private void doBye(final Dialog dialog, final TimeoutUnit timeoutUnit) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();

        final Transaction transaction = getTransactionManager().lookUpTransaction(
                dialog,
                null,
                SIP_BYE_CLIENT
        );

        runAsynchronously((Transaction<Boolean, BaseSipMessage>) transaction, timeoutUnit);
    }

    /**
     * Handles server invite message
     *
     * @param msg - invite message
     */
    public void handleIncomingInvite(final Request msg) throws DialogStateException {
        assert TransactionUtils.isTransactionExecutionThread() :
                "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        assert !done.get();
        assert msg != null && MessageType.SIP_INVITE == MessageType.parse(msg.getMethod());

        if (!done.get()) {
            Logger.log("Remote party has sent invite");
            //ClientIdentity localParty = getStackContext().getStackClientRegistry().findAddressee(msg.getTo().getUriBuilder().getShortURI());
            ClientIdentity localParty = getStackContext().getClientRouter().findAddressee(msg);
            if (localParty != null) {
                assert getStackContext().getDialogStorage().findDialogForMessage(msg) == null;
                final Dialog dialog = getStackContext().getDialogStorage().getDialogForIncomingMessage(localParty, msg);
                TransactionType<InviteSrvTransaction, ? extends ServerCommonInviteTransaction> transactionType = SIP_INVITE_SERVER;

                doHandleIncomingInvite(msg, dialog, transactionType);
            }
            else {
                throw new DialogStateException(null, DialogStateException.Error.ADDRESSEE_NOT_FOUND, msg);
            }
        }
    }

    public void handleIncomingCancel(Request msg) throws DialogStateException {
        assert !done.get();
        assert msg != null && MessageType.SIP_CANCEL == MessageType.parse(msg.getMethod());

        if (!done.get()) {
            Logger.log("Remote party has sent SIP_CANCEL");

            final Dialog dialog = getStackContext().getDialogStorage().findDialogForMessage(msg);

            if (dialog != null) {
                dialog.getMessageHistory().addMessage(msg, true);

                final InviteSrvTransaction transaction = getTransactionManager().findTransaction(dialog, SIP_INVITE_SERVER);

                if (transaction != null) {
                    final DialogStateEvent<BaseSipMessage> stateEvent =
                            new DefaultDialogStateEvent<BaseSipMessage>(dialog, SessionState.SESSION_TERMINATED, msg);

                    dialogStateListenerHolder.getNotifier().onSessionTerminated(stateEvent);
                    dialogStateListenerHolder.getNotifier().onSessionEnded(stateEvent);
                    //DIALOG.putCustomParameter(ParamKey.INITIAL_MESSAGE, ((Transaction) transaction).getInitialMessage());

                    transaction.cancel();
                }
                else {
                    //assert false : "Transaction already terminated for msg: " + msg.shortDescription() + " dialog: " + dialog;
                    throw new DialogStateException(dialog, DialogStateException.Error.REQUEST_FOR_UNKNOWN_DIALOG, msg);
                }
            }
            else {
                //assert false : "Dialog is already terminated or never exist. Message :" + msg.shortDescription();
                throw new DialogStateException(dialog, DialogStateException.Error.REQUEST_FOR_UNKNOWN_DIALOG, msg);
            }
        }

    }

    public void handleIncomingReInvite(final Request msg) throws DialogStateException {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();
        assert msg != null && MessageType.SIP_INVITE == MessageType.parse(msg.getMethod());


        if (!done.get()) {
            Logger.log("Remote party has sent ReInvite");

            final Dialog dialog = getStackContext().getDialogStorage().findDialogForMessage(msg);
            assert dialog != null;

            checkReInvitePreconditions(dialog, msg);
            dialog.markReInviteInProgress(InitiateParty.REMOTE);

            TransactionType<InviteSrvTransaction, ? extends ServerCommonInviteTransaction> transactionType = SIP_REINVITE_SERVER;

            doHandleIncomingInvite(msg, dialog, transactionType);
        }
    }

    public void handleIncomingUpdate(Request msg) throws DialogStateException {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();

        if (!done.get()) {
            Logger.log("Remote party has sent update");

            final Dialog dialog = getStackContext().getDialogStorage().findDialogForMessage(msg);
            assert dialog != null;

            checkUpdatePreconditions(dialog, msg);

            Logger.log(TAG, "mark dialog as update in progress");
            dialog.markUpdateInProgress(InitiateParty.REMOTE);

            //TransactionType<InviteSrvTransaction, ? extends ServerCommonInviteTransaction> transactionType = SIP_REINVITE_SERVER;
            TransactionType<UpdateSrvTransaction, UpdateServerTransaction> transactionType = SIP_UPDATE_SERVER;

            doHandleIncomingUpdate(msg, dialog, transactionType);
        }
    }

    private void doHandleIncomingUpdate(
            Request msg,
            Dialog dialog,
            TransactionType<UpdateSrvTransaction, UpdateServerTransaction> transactionType) {

        dialog.getMessageHistory().addMessage(msg, true);
        SdpMessage sdp = SipMessageUtils.getSdpFromMessage(msg);
        if (sdp != null) {
            dialog.setIncomingSdpMessage(sdp);
        }

        final TransactionManager transactionManager = getTransactionManager();
        transactionManager.addListener(new FirstMessageResolver(transactionType.getName(), dialog, msg, transactionManager));

        final UpdateSrvTransaction transaction = transactionManager.lookUpTransaction(
                dialog,
                null,
                transactionType
        );
        runAsynchronously((Transaction<Boolean, BaseSipMessage>) transaction);

    }

    private void doHandleIncomingInvite(
            final Request msg,
            final Dialog dialog,
            final TransactionType<InviteSrvTransaction, ? extends ServerCommonInviteTransaction> transactionType) {

        dialog.getMessageHistory().addMessage(msg, true);
        //DIALOG.putCustomParameter(ParamKey.LAST_MESSAGE, msg);
        Logger.log("doHandleIncomingInvite", "");
        SdpMessage sdp = SipMessageUtils.getSdpFromMessage(msg);
        if (sdp != null) {
            dialog.setIncomingSdpMessage(sdp);
        }

        final TransactionManager transactionManager = getTransactionManager();
        transactionManager.addListener(new FirstMessageResolver(transactionType.getName(), dialog, msg, transactionManager));

        final InviteSrvTransaction transaction = transactionManager.lookUpTransaction(
                dialog,
                null,
                transactionType
        );
        runAsynchronously((Transaction<Boolean, BaseSipMessage>) transaction);
    }

    /**
     * Handles server noninvite message
     *
     * @param msg - noninvite message
     */
    public void handleIncomingBye(final Request msg) {

        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();
        assert msg != null && MessageType.SIP_BYE == MessageType.parse(msg.getMethod());

        Logger.log("Remote party has sent noninvite");
        if (!done.get()) {

            final Dialog dialog = getStackContext().getDialogStorage().findDialogForMessage(msg);
            assert dialog != null;
            assert STATED == dialog.getState();

            dialog.getMessageHistory().addMessage(msg, true);

            final TransactionManager transactionManager = getTransactionManager();
            transactionManager.addListener(new FirstMessageResolver(SIP_BYE_SERVER.getName(), dialog, msg, transactionManager));

            final Transaction transaction = transactionManager.lookUpTransaction(
                    dialog,
                    null,
                    SIP_BYE_SERVER
            );
            runAsynchronously(transaction, TRANSACTION_TIMEOUT);
        }
    }


    /**
     * Shutdowns the service
     */
    public void shutdown() {
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Shutdowning InviteService");
        if (done.compareAndSet(false, true)) {

            unSubscribeFromTransactionManager();
        }
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "InviteService shutdown successfully");
    }

    private void checkReInvitePreconditions(final Dialog dialog, final Request msg) throws DialogStateException {
        if (EARLY == dialog.getState()) {
            throw new DialogStateException(dialog, REINVITE_FOR_EARLY_DIALOG, msg, "Can not update (reinvite) DIALOG. Dialog is not stated yet. Dialog  state is " + dialog.getState());
        }
        if (dialog.isReInviteInProgress()) {
            throw new DialogStateException(dialog, REINVITE_DURING_PREVIOUS_REINVITE, msg, "Can not update (reinvite) DIALOG. Previous reinvite still in progress.");
        }
    }

    private void checkUpdatePreconditions(final Dialog dialog, final Request msg) throws DialogStateException {
        if (STATED == dialog.getState()) {
            throw new DialogStateException(dialog, UPDATE_FOR_STATED_DIALOG, msg, "Can not update DIALOG. Dialog is stated already. ");
        }
        if (dialog.isUpdateInProgress()) {
            throw new DialogStateException(dialog, UPDATE_DURING_PREVIOUS_UPDATE, msg, "Can not update DIALOG. Previous update still in progress.");
        }

        /**
         *  A UAS that receives an UPDATE before it has generated a final
         response to a previous UPDATE on the same dialog MUST return a 500
         response to the new UPDATE, and MUST include a Retry-After HEADER
         field with a randomly chosen value between 0 and 10 seconds.

         If an UPDATE is received that contains an offer, and the UAS has
         generated an offer (in an UPDATE, PRACK or INVITE) to which it has
         not yet received an answer, the UAS MUST reject the UPDATE with a 491
         response.  Similarly, if an UPDATE is received that contains an
         offer, and the UAS has received an offer (in an UPDATE, PRACK, or
         INVITE) to which it has not yet generated an answer, the UAS MUST
         reject the UPDATE with a 500 response, and MUST include a Retry-After
         HEADER field with a randomly chosen value between 0 and 10 seconds.

         If a UA receives an UPDATE for an existing dialog, it MUST check any
         version identifiers in the SESSION description or, if there are no
         version identifiers, the content of the SESSION description to see if
         it has changed.  If the SESSION description has changed, the UAS MUST
         adjust the SESSION parameters accordingly and generate an answer in
         the 2xx response.  However, unlike a re-INVITE, the UPDATE MUST be
         responded to promptly, and therefore the USER cannot generally be
         prompted to approve the SESSION changes.  If the UAS cannot change
         the SESSION parameters without prompting the USER, it SHOULD reject
         the request with a 504 response.  If the new SESSION description is
         not acceptable, the UAS can reject it by returning a 488 (Not
         Acceptable Here) response for the UPDATE.  This response SHOULD
         include a Warning HEADER field.

         If a UAC receives a 491 response to a UPDATE, it SHOULD start a timer
         with a value T chosen as follows:

         1. If the UAC is the owner of the Call-ID of the dialog ID
         (meaning it generated the value), T has a randomly chosen value
         between 2.1 and 4 seconds in units of 10 ms.

         2. If the UAC is not the owner of the Call-ID of the dialog ID, T
         has a randomly chosen value between 0 and 2 seconds in units of
         10 ms.

         When the timer fires, the UAC SHOULD attempt the UPDATE once more, if
         it still desires for that SESSION modification to take place.  For
         example, if the call was already hung up with a BYE, the UPDATE would
         not take place.
         */
    }

    private void subscribeToTransactionManager() {
        getTransactionManager().addListener(clientInviteListener, TransactionType.Name.SIP_INVITE_CLIENT);
        getTransactionManager().addListener(clientReInviteListener, TransactionType.Name.SIP_REINVITE_CLIENT);
        getTransactionManager().addListener(serverInviteListener, TransactionType.Name.SIP_INVITE_SERVER);
        getTransactionManager().addListener(serverReInviteListener, TransactionType.Name.SIP_REINVITE_SERVER);
        getTransactionManager().addListener(serverUpdateListener, TransactionType.Name.SIP_UPDATE_SERVER);
        getTransactionManager().addListener(clientByeListener, TransactionType.Name.SIP_BYE_CLIENT);
        getTransactionManager().addListener(serverByeListener, TransactionType.Name.SIP_BYE_SERVER);
    }

    private void unSubscribeFromTransactionManager() {
        getTransactionManager().removeListener(clientInviteListener);
        getTransactionManager().removeListener(clientReInviteListener);
        getTransactionManager().removeListener(serverInviteListener);
        getTransactionManager().removeListener(serverReInviteListener);
        getTransactionManager().removeListener(serverUpdateListener);
        getTransactionManager().removeListener(clientByeListener);
        getTransactionManager().removeListener(serverByeListener);
    }


}
