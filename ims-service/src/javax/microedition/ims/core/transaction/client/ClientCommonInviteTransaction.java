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

package javax.microedition.ims.core.transaction.client;

import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.sipservice.DefaultTransactionStateChangeEvent;
import javax.microedition.ims.core.sipservice.TUEvent;
import javax.microedition.ims.core.sipservice.TransactionStateChangeEvent;
import javax.microedition.ims.core.sipservice.invite.TUCancelUpdateEvent;
import javax.microedition.ims.core.transaction.TransactionDescription;
import javax.microedition.ims.core.transaction.TransactionUtils;
import javax.microedition.ims.core.transaction.state.invite.client.CallingState;
import javax.microedition.ims.messages.builder.IRequestMessageBuilder;
import javax.microedition.ims.messages.builder.IResponseMessageBuilder;
import javax.microedition.ims.messages.utils.StatusCode;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Response;

public abstract class ClientCommonInviteTransaction extends ClientTransaction implements InviteClntTransaction {
    ClientCommonInviteTransaction(
            final StackContext stackContext,
            final Dialog dlg,
            final TransactionDescription description) {

        super(stackContext, dlg, description);
    }

    public void confirmResponse() {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        getListenerHolder().getNotifier().onConfirmResponse(createTransactionEvent(lastInMessage.get(), lastOutMessage.get()));
        IRequestMessageBuilder mb = getDialog().getMessageBuilderFactory().getRequestBuilder(MessageType.SIP_ACK);
        sendMessage(mb.buildMessage(), null);
    }


    protected Boolean onMessage(final BaseSipMessage initialMessage, final BaseSipMessage lastMessage) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        super.onMessage(initialMessage, lastMessage);
        return null;
    }


    protected void onTransactionInited() {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        final TransactionStateChangeEvent<BaseSipMessage> event =
                DefaultTransactionStateChangeEvent.createInitEvent(this, null);

        transitToState(new CallingState(this), event);
    }


    protected MessageType getInitialBuilderType() {
        return MessageType.SIP_INVITE;
    }


    public void cancel() {
        currentState.onTUReceived(createTUEvent(TUCancelUpdateEvent.OPERATION_TYPE_CANCEL));
    }


    public void update() {
        currentState.onTUReceived(createTUEvent(TUCancelUpdateEvent.OPERATION_TYPE_UPDATE));
    }


    public void acceptUpdate() {
        IResponseMessageBuilder builder = getDialog().getMessageBuilderFactory().getResponseBuilder();
        Response response = builder.buildMessage(lastInRequest.get(), StatusCode.OK, null);
        sendMessage(response, null);
    }

    public void rejectUpdate(int statusCode, String alternativeUserAddress) {
        IResponseMessageBuilder builder = getDialog().getMessageBuilderFactory().getResponseBuilder();
        Response response = builder.buildMessage(lastInRequest.get(), statusCode, null);
        sendMessage(response, null);
    }

    TUEvent createTUEvent(int opType) {
        return new TUCancelUpdateEvent(opType);
    }

    public BaseSipMessage sendCancel() {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        //getListenerHolder().fireOnConfirmResponse(createTransactionEvent(lastResponse.get()));
        IRequestMessageBuilder mb = getDialog().getMessageBuilderFactory().getRequestBuilder(MessageType.SIP_CANCEL);
        BaseSipMessage ret = mb.buildMessage();
        sendMessage(ret, null);
        return ret;
    }

    public BaseSipMessage sendUpdate() {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        IRequestMessageBuilder mb = getDialog().getMessageBuilderFactory().getRequestBuilder(MessageType.SIP_UPDATE);
        BaseSipMessage ret = mb.buildMessage();
        sendMessage(ret, null);
        return ret;
    }

    public BaseSipMessage sendPrack() {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        getListenerHolder().getNotifier().onSendPrack(createTransactionEvent(lastInMessage.get(), lastOutMessage.get()));
        IRequestMessageBuilder mb = getDialog().getMessageBuilderFactory().getRequestBuilder(MessageType.SIP_PRACK);
        BaseSipMessage ret = mb.buildMessage();
        sendMessage(ret, null);
        return ret;
    }

    public void retryInvite() {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        IRequestMessageBuilder mb = getDialog().getMessageBuilderFactory().getRequestBuilder(MessageType.SIP_INVITE);
        sendMessage(mb.buildMessage(), null);
    }
}
