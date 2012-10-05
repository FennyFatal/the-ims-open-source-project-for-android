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

package javax.microedition.ims.core.transaction.state.invite.server;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.common.RepetitiousTaskManager;
import javax.microedition.ims.core.sipservice.*;
import javax.microedition.ims.core.sipservice.invite.TUResponseEvent;
import javax.microedition.ims.core.sipservice.invite.TUResponseEvent.OperationType;
import javax.microedition.ims.core.sipservice.timer.TimeoutTimer;
import javax.microedition.ims.core.sipservice.timer.TimeoutTimer.TimeoutListener;
import javax.microedition.ims.core.transaction.server.ServerCommonInviteTransaction;
import javax.microedition.ims.messages.utils.StatusCode;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ProceedingState extends TransactionState<ServerCommonInviteTransaction, BaseSipMessage> {
    private final AtomicBoolean tuAnswerReceived = new AtomicBoolean(false);
    private final AtomicReference<TimeoutListener> tuTimeoutListener = new AtomicReference<TimeoutListener>(null);

    public ProceedingState(ServerCommonInviteTransaction transaction) {
        super(transaction);
    }

    protected State getTransactionStateName() {
        return State.PROCEEDING;
    }

    protected void onFirstInit(final TransactionStateChangeEvent<BaseSipMessage> triggeringEvent) {
        //TIMEOUT for TU answer, if TU answer doesn't received that StatusCode.REQUEST_TIMEOUT should be sent to remote party
        TimeoutListener listener = new TimeoutTimer.TimeoutListener() {
            public void onTimeout() {
                tuAnswerReceived.set(true);
                Logger.log("tuTimeoutListeneronTimeout");

                //TODO: commented here. We should allow user to see ringing state for any time interval. Not only 32s
                //sendResponse(RequestState.TIMEOUT, null);
            }
        };

        if (tuTimeoutListener.compareAndSet(null, listener)) {
            //TODO: commented here. We should allow user to see ringing state for any time interval. Not only 32s
            /*
           TimeoutTimer.getInstance().startTimerTransactionSafe(
                    tuTimeoutListener.get(),
                    RepetitiousTaskManager.REQUEST_TIMEOUT_INTERVAL
            );
            */
        }
    }

    public void onStateInitiated(final TransactionStateChangeEvent<BaseSipMessage> triggeringEvent) {
        super.onStateInitiated(triggeringEvent);

        boolean reInviteInProgress = transaction.getDialog().isReInviteInProgress();

        if (!reInviteInProgress) {
            transaction.sendResponse(triggeringEvent.getTriggeringMessage(), StatusCode.TRYING, false);

            if (!tuAnswerReceived.get()) {
                transaction.sendResponse(triggeringEvent.getTriggeringMessage(), StatusCode.RINGING, false);
            }
        }
    }

    public void onMessageReceived(final BaseSipMessage msg) {
        log(msg.getMethod(), "onMessageReceived");
        if (MessageType.SIP_INVITE == MessageType.parse(msg.getMethod())) {
            //SIP_INVITE retransmission
            log("SIP_INVITE retransmission", "onMessageReceived");
            //transaction.getLastOutMessage().setcSeq(msg.getcSeq()); //TODO move CSeq management out from here
            transaction.sendResponse(msg, StatusCode.OK, true);
        } else if (MessageType.SIP_CANCEL == MessageType.parse(msg.getMethod())) {
            //SIP_CANCEL, remote party want terminate SESSION
            sendResponse(RequestState.CANCELED, msg);
        } else if (MessageType.SIP_PRACK == MessageType.parse(msg.getMethod())) {
            sendResponse(RequestState.PRACKED, msg);
        } else {
            assert false : "unexpected message, method" + msg.getMethod();
        }
    }

    public void onTUReceived(TUEvent event) {

        if (!tuAnswerReceived.get()) {
            tuAnswerReceived.set(true);

            BaseSipMessage triggeringMessage = null;
            if (event instanceof TUResponseEvent) {
                triggeringMessage = ((TUResponseEvent) event).getTriggeringMessage();
            }
            
            TUResponseEvent inviteEvent = (TUResponseEvent) event;
            if (inviteEvent.getOpType() == OperationType.ACCEPT_INVITE) {
                sendResponse(RequestState.ACCEPTED,  triggeringMessage);
            } else if (inviteEvent.getOpType() == OperationType.REJECT_INVITE) {
                sendResponse(RequestState.REJECTED, inviteEvent.getStatusCode(), triggeringMessage);
            } else if (inviteEvent.getOpType() == OperationType.ACCEPT_UPDATE) {
                sendResponse(RequestState.ACCEPTED_UPDATE,  triggeringMessage);
            } else if (inviteEvent.getOpType() == OperationType.REJECT_UPDATE) {
                sendResponse(RequestState.REJECTED_UPDATE, inviteEvent.getStatusCode(), triggeringMessage);
            } else if (inviteEvent.getOpType() == OperationType.CANCEL) {
                sendResponse(RequestState.CANCELED, inviteEvent.getStatusCode(), triggeringMessage);
            }
        }
    }

    private void sendResponse(final RequestState requestState, final BaseSipMessage triggeringMessage) {
        sendResponse(requestState, -1, triggeringMessage);
    }

    private void sendResponse(final RequestState requestState, final int customCode, final BaseSipMessage triggeringMessage) {
        switch (requestState) {
            case TIMEOUT: {
                transaction.sendResponse(triggeringMessage, StatusCode.REQUEST_TIMEOUT, false);

                final TransactionStateChangeEvent<BaseSipMessage> event =
                        createStateChangeEvent(
                                StateChangeReason.TIMER_TIMEOUT,
                                null
                        );

                transaction.transitToState(new CompletedState(transaction, requestState), event);
            }
            break;
            case CANCELED: {
                transaction.sendResponse(triggeringMessage, StatusCode.OK, true);
                transaction.sendResponse(triggeringMessage, StatusCode.REQUEST_TERMINATED, false);//respond to invite

                final TransactionStateChangeEvent<BaseSipMessage> event =
                        createStateChangeEvent(
                                StateChangeReason.INCOMING_MESSAGE,
                                triggeringMessage
                        );

                transaction.transitToState(new CompletedState(transaction, requestState), event);
            }
            break;
            case PRACKED: {
                transaction.sendResponse(triggeringMessage, StatusCode.OK, true);
            }
            break;
            case ACCEPTED: {
                transaction.sendResponse(triggeringMessage, StatusCode.OK, false);

                final TransactionStateChangeEvent<BaseSipMessage> event =
                        createStateChangeEvent(
                                StateChangeReason.CLIENT_ACCEPT,
                                null
                        );


                transaction.transitToState(new CompletedState(transaction, requestState), event);
            }
            break;
            case REJECTED: {
                transaction.sendResponse(triggeringMessage, customCode, false);

                final TransactionStateChangeEvent<BaseSipMessage> event =
                        createStateChangeEvent(
                                StateChangeReason.CLIENT_REJECT,
                                null
                        );

                transaction.transitToState(new CompletedState(transaction, requestState), event);
            }
            case ACCEPTED_UPDATE: {
                transaction.sendResponse(triggeringMessage, StatusCode.OK, true);

                final TransactionStateChangeEvent<BaseSipMessage> event =
                        createStateChangeEvent(
                                StateChangeReason.CLIENT_UPDATE_SUCCESS,
                                null
                        );

                transaction.transitToState(new CompletedState(transaction, requestState), event);
                //transaction.notifyTU(event);
            }
            break;
            case REJECTED_UPDATE: {
                transaction.sendResponse(triggeringMessage, customCode, true);

                final TransactionStateChangeEvent<BaseSipMessage> event =
                        createStateChangeEvent(
                                StateChangeReason.CLIENT_UPDATE_FAILED,
                                null
                        );

                //transaction.notifyTU(event);
                transaction.transitToState(new CompletedState(transaction, requestState), event);
            }
            break;
            default:
                break;
        }
    }

    public void onStateCompleted() {
        super.onStateCompleted();
        //stop waiting TU answer
        TimeoutTimer.getInstance().stopTimeoutTimer(tuTimeoutListener.get());

        //stop resend RINGING
        transaction.getStackContext().getRepetitiousTaskManager().cancelTask(transaction.getInitialMessage());
    }
}
