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

package javax.microedition.ims.core.transaction.server.msrp;

import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.common.RepetitiousTaskManager.RepetitiousTimeStrategy;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.msrp.MSRPTransactionDescriptor;
import javax.microedition.ims.core.sipservice.State;
import javax.microedition.ims.core.transaction.CommonMsrpTransaction;
import javax.microedition.ims.core.transaction.TransactionUtils;
import javax.microedition.ims.messages.builder.msrp.IMsrpResponseAndReportMessageBuilder;
import javax.microedition.ims.messages.utils.StatusCode;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;
import java.util.concurrent.atomic.AtomicReference;

public abstract class MsrpServerTransaction extends CommonMsrpTransaction implements MsrpSrvTransaction<MsrpMessage> {

    private final AtomicReference<MsrpMessage> firstMessage = new AtomicReference<MsrpMessage>(null);

    MsrpServerTransaction(final StackContext stackContext, final MSRPTransactionDescriptor descriptor) {
        super(stackContext, descriptor);
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
    protected Boolean onMessage(MsrpMessage initialMessage, MsrpMessage message) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        currentState.onMessageReceived(message);
        return null;
    }
    
    protected RepetitiousTimeStrategy getResendRequestInterval() {
        return null;
    }

    public void setFirstMessage(MsrpMessage firstMessage) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        this.firstMessage.compareAndSet(null, firstMessage);
    }
    
    public MsrpMessage getFirstIncomingMessage() {
        return this.firstMessage.get();
    }

    public void sendResponse(MsrpMessage triggeringMessage, int code, boolean toLastRequest) {

        MsrpMessage lastMessage = triggeringMessage == null ? lastInMessage.get() : triggeringMessage;

        //TODO remove fireOnSendResponse
        getListenerHolder().getNotifier().onSendResponse(
                createTransactionEvent(lastMessage, lastOutMessage.get()),
                code
        );

        MsrpMessage message = toLastRequest ? lastMessage : getInitialMessage();

        doSendResponse(code, message);
    }

    public void sendReport(MsrpMessage triggeringMessage, int code, boolean toLastRequest) {

        MsrpMessage lastMessage = triggeringMessage == null ? lastInMessage.get() : triggeringMessage;

        //TODO remove fireOnSendResponse
        getListenerHolder().getNotifier().onSendResponse(
                createTransactionEvent(lastMessage, lastOutMessage.get()),
                code
        );

        MsrpMessage message = toLastRequest ? lastMessage : getInitialMessage();

        doSendReport(code, message);
    }

    public void sendResponseNonInvite(State state) {

        MsrpMessage initialRequest = getInitialMessage();

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

    private void doSendResponse(int code, MsrpMessage message) {
        IMsrpResponseAndReportMessageBuilder builder = builderFactory.getMsrpResponseBuilder(MessageType.MSRP_STATUS);
        MsrpMessage response = builder.buildMessage(message, code, null);
        sendMessage(response, null);
    }

    private void doSendReport(int code, MsrpMessage message) {
        IMsrpResponseAndReportMessageBuilder builder = builderFactory.getMsrpResponseBuilder(MessageType.MSRP_REPORT);
        MsrpMessage response = builder.buildMessage(message, code, null);
        sendMessage(response, null);
    }

    protected boolean isAcceptableHere(MsrpMessage msg) {
        return true;
    }

    protected int getAcceptedCode() {
        return StatusCode.OK;
    }
}
