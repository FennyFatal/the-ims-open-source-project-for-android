/*
 * This software code is � 2010 T-Mobile USA, Inc. All Rights Reserved.
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
 * THIS SOFTWARE IS PROVIDED ON AN �AS IS� AND �WITH ALL FAULTS� BASIS
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

package javax.microedition.ims.core.transaction.server;

import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.sipservice.DefaultTransactionStateChangeEvent;
import javax.microedition.ims.core.sipservice.TUEvent;
import javax.microedition.ims.core.sipservice.TransactionState;
import javax.microedition.ims.core.sipservice.TransactionStateChangeEvent;
import javax.microedition.ims.core.sipservice.invite.TUResponseEvent;
import javax.microedition.ims.core.sipservice.invite.TUResponseEvent.OperationType;
import javax.microedition.ims.core.transaction.TransactionDescription;
import javax.microedition.ims.core.transaction.TransactionUtils;
import javax.microedition.ims.core.transaction.state.invite.server.ProceedingState;
import javax.microedition.ims.messages.builder.IRequestMessageBuilder;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;

public abstract class ServerCommonInviteTransaction extends ServerTransaction implements InviteSrvTransaction {

    ServerCommonInviteTransaction(
            final StackContext stackContext,
            final Dialog dlg,
            final TransactionDescription description) {

        super(stackContext, dlg, description);
    }


    protected void startResendTimer() {
        //run resender task to resend messages if there is no answer for a long time
        /*if(Context.INSTANCE.getConfiguration().getConnectionType().router(messageRouter).equals(Protocol.UDP)){
            RepetitiousTaskManager.getInstance().startRepetitiousTask(
                   getInitialMessage(),
                    new RepetitiousTaskManager.Repeater<BaseSipMessage>() {

                        public void onResend(final BaseSipMessage msg) {
                            ///handle message to upper level
                            if (!transactionComplete.get()) {
                                Logger.log("Resending initail invite message");
                                sendMessage(msg);
                            }
                        }
                    },
                    RepetitiousTaskManager.RESEND_INTERVAL
            );
        }*/
    }

    /*
        protected Object onMessage(final BaseSipMessage initialMessage, final BaseSipMessage lastMessage) {
            assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in "+Thread.currentThread();
            super.onMessage(initialMessage, lastMessage);
            return null;
        }
    */
    public void sendResponse(final BaseSipMessage triggeringMessage, int code, boolean toLastRequest) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        super.sendResponse(triggeringMessage, code, toLastRequest);
    }

/*    public void sendResponse(BaseSipMessage baseSipMessage){
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in "+Thread.currentThread();
        sendMessage(baseSipMessage);
    }
*/



    protected void onTransactionInited() {
        assert TransactionUtils.isTransactionExecutionThread() :
                "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        BaseSipMessage initialMessage = getInitialMessage();
        assert initialMessage != null;
        final TransactionStateChangeEvent<BaseSipMessage> event =
                DefaultTransactionStateChangeEvent.createInitEvent(this, initialMessage);

        transitToState(getInitialState(), event);
    }

    TransactionState getInitialState() {
        return new ProceedingState(this);
    }

    public void accept() {
        assert TransactionUtils.isTransactionExecutionThread() :
                "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        currentState.onTUReceived(createTUEvent(TUResponseEvent.OperationType.ACCEPT_INVITE, 200, null));
    }

    public void reject(int statusCode, String alternativeUserAddress) {
        assert TransactionUtils.isTransactionExecutionThread() :
                "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        currentState.onTUReceived(createTUEvent(TUResponseEvent.OperationType.REJECT_INVITE, statusCode, alternativeUserAddress));
    }

    public void cancel() {
        BaseSipMessage cancelMessage = getDialog().getMessageHistory().findLastRequestByMethod(MessageType.SIP_CANCEL);
        currentState.onTUReceived(new TUResponseEvent(OperationType.CANCEL, 200, null, cancelMessage));
    }

    public void update() {
        assert TransactionUtils.isTransactionExecutionThread() :
                "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        IRequestMessageBuilder mb = getDialog().getMessageBuilderFactory().getRequestBuilder(MessageType.SIP_UPDATE);
        sendMessage(mb.buildMessage(), null);
    }


    public void acceptUpdate() {
        assert TransactionUtils.isTransactionExecutionThread() :
                "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        currentState.onTUReceived(createTUEvent(TUResponseEvent.OperationType.ACCEPT_UPDATE, 200, null));
    }

    public void rejectUpdate(int statusCode, String alternativeUserAddress) {
        assert TransactionUtils.isTransactionExecutionThread() :
                "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        currentState.onTUReceived(createTUEvent(TUResponseEvent.OperationType.REJECT_UPDATE, statusCode, alternativeUserAddress));

    }

    TUEvent createTUEvent(OperationType opType, int statusCode, String alternativeUserAddress) {
        return new TUResponseEvent(opType, statusCode, alternativeUserAddress, null);
    }

    public void sendBye() {
        assert TransactionUtils.isTransactionExecutionThread() :
                "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        //TODO send by as separate transaction
    }

    public void preAccept() {
        assert TransactionUtils.isTransactionExecutionThread() :
                "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        currentState.onTUReceived(createTUEvent(TUResponseEvent.OperationType.PREACCEPT_INVITE, 200, null));
    }
}
