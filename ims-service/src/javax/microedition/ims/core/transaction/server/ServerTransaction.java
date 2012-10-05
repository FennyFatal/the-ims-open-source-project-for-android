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

package javax.microedition.ims.core.transaction.server;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.common.RepetitiousTaskManager.RepetitiousTimeStrategy;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.sipservice.State;
import javax.microedition.ims.core.transaction.CommonSIPTransaction;
import javax.microedition.ims.core.transaction.TransactionDescription;
import javax.microedition.ims.core.transaction.TransactionUtils;
import javax.microedition.ims.messages.builder.IResponseMessageBuilder;
import javax.microedition.ims.messages.utils.StatusCode;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Request;
import javax.microedition.ims.messages.wrappers.sip.Response;
import java.util.concurrent.atomic.AtomicReference;

public abstract class ServerTransaction extends CommonSIPTransaction implements SrvTransaction<BaseSipMessage> {

    private final AtomicReference<BaseSipMessage> firstMessage = new AtomicReference<BaseSipMessage>(null);

    ServerTransaction(
            final StackContext stackContext,
            final Dialog dlg,
            final TransactionDescription description) {

        super(stackContext, dlg, description);
    }

    /**
     * Server transaction does not have initial message.
     */

    protected MessageType getInitialBuilderType() {
        return null;
    }

    /**
     * @param initialMessage
     * @return null if transaction is not complete otherwise result of transaction
     */
    protected Boolean onMessage(BaseSipMessage initialMessage, BaseSipMessage message) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        //TODO move SIP_CANCEL related code to SIP_INVITE transaction
        if (MessageType.SIP_CANCEL == MessageType.parse(message.getMethod())) {
            getListenerHolder().getNotifier().onIncomingCancel(
                    createTransactionEvent(message, lastOutMessage.get())
            );
        }
        currentState.onMessageReceived(message);

        return null;
    }

    protected RepetitiousTimeStrategy getResendRequestInterval() {
        return null;
    }

/*    protected void sendMessage(BaseSipMessage msg) {
        lastMessage.set(msg);
        getConsumer().push(msg);
        //currentState.onMessageReceived(msg);
    }
*/
    @Override
    public void setFirstMessage(BaseSipMessage firstMessage) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        this.firstMessage.compareAndSet(null, firstMessage);
    }

    @Override
    public BaseSipMessage getFirstIncomingMessage() {
        return this.firstMessage.get();
    }

    @Override
    public void sendResponse(BaseSipMessage triggeringMessage, int code, boolean toLastRequest) {

        Logger.log("ServerTransaction", String.format("triggeringMessage = %s, toLastRequest = %s", triggeringMessage, toLastRequest));
        //Logger.log("ServerTransaction", String.format("lastInRequest.get() = %s", lastInRequest.get()));
        
        BaseSipMessage lastMessage = triggeringMessage == null ? lastInRequest.get() : triggeringMessage;
        //Logger.log("ServerTransaction", "lastMessage = " + lastMessage);
        

        //TODO remove fireOnSendResponse
        getListenerHolder().getNotifier().onSendResponse(
                createTransactionEvent(lastMessage, lastOutMessage.get()),
                code
        );

        BaseSipMessage message = toLastRequest ? lastMessage : getInitialMessage();

        doSendResponse(code, message);
    }

    @Override
    public void sendResponseNonInvite(State state) {

        BaseSipMessage initialMessage = getInitialMessage();
        assert initialMessage instanceof Request;

        Request initialRequest = (Request) initialMessage;

        //subclasses can return not only 200ok but any from 2xx class. For example 202accepted.
        int code = getAcceptedCode();

        if (State.TRYING == state) {
            if (!isAcceptableHere(initialRequest)) {
                code = StatusCode.NOT_ACCEPTABLE_HERE;
            }
        }
        else {
            assert false : "not implemented branch";
        }

        doSendResponse(code, getInitialMessage());
    }

    private void doSendResponse(int code, BaseSipMessage message) {
        IResponseMessageBuilder builder = getDialog().getMessageBuilderFactory().getResponseBuilder();
        Response response = builder.buildMessage(message, code, null);
        sendMessage(response, null);
    }

    protected boolean isAcceptableHere(Request msg) {
        return true;
    }

    protected int getAcceptedCode() {
        return StatusCode.OK;
    }

    @Override
    public boolean isAutoAcceptable() {
        return true;
    }
}
