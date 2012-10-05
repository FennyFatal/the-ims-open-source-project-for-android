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

package javax.microedition.ims.core.transaction.state.invite.client;

import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.common.OptionFeature;
import javax.microedition.ims.common.RepetitiousTaskManager;
import javax.microedition.ims.common.util.CollectionsUtils;
import javax.microedition.ims.core.sipservice.*;
import javax.microedition.ims.core.sipservice.invite.SessionRefreshData;
import javax.microedition.ims.core.sipservice.invite.TUCancelUpdateEvent;
import javax.microedition.ims.core.sipservice.timer.TimeoutTimer;
import javax.microedition.ims.core.sipservice.timer.TimeoutTimer.TimeoutListener;
import javax.microedition.ims.core.transaction.client.ClientCommonInviteTransaction;
import javax.microedition.ims.core.transaction.state.TerminatedState;
import javax.microedition.ims.messages.utils.StatusCode;
import javax.microedition.ims.messages.wrappers.common.ResponseClass;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Header;
import javax.microedition.ims.messages.wrappers.sip.Response;
import javax.microedition.ims.messages.wrappers.sip.Request;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CallingState extends
        TransactionState<ClientCommonInviteTransaction, BaseSipMessage> {

    private final AtomicReference<TimeoutListener> tListener = new AtomicReference<TimeoutListener>(
            null);

    private final AtomicReference<TimeoutListener> requestPendingTimer = new AtomicReference<TimeoutListener>(
            null);

    public CallingState(
            final ClientCommonInviteTransaction clientInviteTransaction) {
        super(clientInviteTransaction);
    }


    protected State getTransactionStateName() {
        return State.CALLING;
    }


    protected void onFirstInit(
            final TransactionStateChangeEvent<BaseSipMessage> triggeringEvent) {

        TimeoutListener timer = new TimeoutListener() {
            public void onTimeout() {
                // send cancel to remote party?!
                // RepetitiousTaskManager.getInstance().cancelTask(transaction.getInitialMessage());

                final TransactionStateChangeEvent<BaseSipMessage> event = createStateChangeEvent(
                        StateChangeReason.TIMER_TIMEOUT, null);

                transaction.transitToState(new TerminatedState<BaseSipMessage>(
                        transaction, true, false), event);
            }
        };

        if (tListener.compareAndSet(null, timer)) {
            // startBTimer anywhere t2 = 64t1
            TimeoutTimer.getInstance().startTimerTransactionSafe(
                    tListener.get(),
                    RepetitiousTaskManager.REQUEST_TIMEOUT_INTERVAL);
        }

    }


    public void onMessageReceived(final BaseSipMessage msg) {
        if (msg instanceof Response) {
            Response response = (Response) msg;
            if (ResponseClass.Informational == response.getResponseClass()) {
                // send prack if response code 101..199 and response contains
                // require HEADER with value 100rel
                if (response.getStatusCode() != 100) {
                    if (CollectionsUtils.contains(msg.getRequire(),
                            OptionFeature._100REL.getName())) {
                        // start re-send prack timer
                        transaction.sendPrack();
                    }
                }

                final TransactionStateChangeEvent<BaseSipMessage> event = createStateChangeEvent(
                        StateChangeReason.INCOMING_MESSAGE, msg);

                transaction.transitToState(new ProceedingState(transaction),
                        event);
            }
            else if (ResponseClass.Success == response.getResponseClass()) {
                if (MessageType.SIP_INVITE == MessageType.parse(response
                        .getMethod())) {
                    transaction.confirmResponse();

                    final TransactionStateChangeEvent<BaseSipMessage> event = createStateChangeEvent(
                            StateChangeReason.INCOMING_MESSAGE, msg);

                    transaction.transitToState(
                            new TerminatedState<BaseSipMessage>(transaction,
                                    false, true), event);
                }
                else if (MessageType.SIP_PRACK == MessageType.parse(response
                        .getMethod())) {
                    // stop re-send prack timer

                }
            }
            else /*if (ResponseClass.Client == response.getResponseClass()) {*/
                if (StatusCode.SESSION_INTERVAL_TOO_SMALL == response
                        .getStatusCode()
                        || StatusCode.REQUEST_PENDING == response
                        .getStatusCode()) {
                    Request ackMessage = transaction.getDialog().getMessageBuilderFactory().getRequestBuilder(MessageType.SIP_ACK).buildMessage();
                    transaction.sendMessage(ackMessage, null);

                    List<String> minSeHeader = msg
                            .getCustomHeader(Header.Min_SE);
                    if (minSeHeader != null && minSeHeader.size() > 0) {
                        Long value = Long.parseLong(minSeHeader.get(0));
                        transaction.getDialog().setSessionRefreshData(
                                new SessionRefreshData(null, value, value));
                    }

                    transaction.getStackContext().getRepetitiousTaskManager().cancelTask(
                            transaction.getInitialMessage());

                    requestPendingTimer.compareAndSet(null,
                            new TimeoutTimer.TimeoutListener() {
                                public void onTimeout() {
                                    TimeoutTimer.getInstance()
                                            .stopTimeoutTimer(
                                                    requestPendingTimer.get());
                                    transaction.retryInvite();
                                }
                            });

                    // TODO: currently we don't have access to Dialog. So there
                    // is no way to determine who is the owner of CallId.
                    // Proposal: introduce Dialog to transaction.
                    // Workaround: use 2 seconds as middle value between cases 1
                    // and 2 from RFC3261 (see comments above)
                    //final long requestTimeoutInterval = 2000;
                    long requestTimeoutInterval = ((int)(Math.random()*400))*10;
                    TimeoutTimer.getInstance().startTimerTransactionSafe(
                            requestPendingTimer.get(), requestTimeoutInterval);
                }
                /*}*/
                else if (response.getResponseClass().isErrorResponse()) {

                    if (MessageType.SIP_INVITE == MessageType.parse(response
                            .getMethod())) {
                        transaction.confirmResponse();

                        final TransactionStateChangeEvent<BaseSipMessage> event = createStateChangeEvent(
                                StateChangeReason.INCOMING_MESSAGE, msg);

                        transaction.transitToState(new CompletedState(transaction),
                                event);
                    }
                    else {
                        assert false : "Wrong response message";
                    }

                }
        }
    }

    /**
     */
    public void onStateCompleted() {
        super.onStateCompleted();
        // Stopping this timer to stop SIP_INVITE retransmissions - because
        // we've received response
        transaction.getStackContext().getRepetitiousTaskManager().cancelTask(
                transaction.getInitialMessage());
        TimeoutTimer.getInstance().stopTimeoutTimer(tListener.get());

        if (requestPendingTimer.get() != null) {
            TimeoutTimer.getInstance().stopTimeoutTimer(
                    requestPendingTimer.get());
        }
    }


    public void onTUReceived(TUEvent event) {
        TUCancelUpdateEvent inviteEvent = (TUCancelUpdateEvent) event;
        if (inviteEvent.getOpType() == TUCancelUpdateEvent.OPERATION_TYPE_CANCEL) {
            BaseSipMessage mes = transaction.sendCancel();

            final TransactionStateChangeEvent<BaseSipMessage> changeEvent = createStateChangeEvent(
                    StateChangeReason.CLIENT_CANCEL, mes);

            transaction.transitToState(new CanceledState(transaction),
                    changeEvent);
        }
    }
}
