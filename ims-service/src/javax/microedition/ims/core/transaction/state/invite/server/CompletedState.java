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

import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.common.RepetitiousTaskManager;
import javax.microedition.ims.common.Shutdownable;
import javax.microedition.ims.core.sipservice.State;
import javax.microedition.ims.core.sipservice.StateChangeReason;
import javax.microedition.ims.core.sipservice.TransactionState;
import javax.microedition.ims.core.sipservice.TransactionStateChangeEvent;
import javax.microedition.ims.core.sipservice.timer.TimeoutTimer;
import javax.microedition.ims.core.sipservice.timer.TimeoutTimer.TimeoutListener;
import javax.microedition.ims.core.transaction.CommonTransaction.MessageStateListener;
import javax.microedition.ims.core.transaction.server.ServerCommonInviteTransaction;
import javax.microedition.ims.core.transaction.state.TerminatedState;
import javax.microedition.ims.messages.utils.StatusCode;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import java.util.concurrent.atomic.AtomicReference;

public class CompletedState extends TransactionState<ServerCommonInviteTransaction, BaseSipMessage> {
    private final RequestState requestState;
    private final AtomicReference<TimeoutListener> requestResendListener = new AtomicReference<TimeoutListener>(null);

    private StateChangeReason stateChangeReason;

    public CompletedState(
            final ServerCommonInviteTransaction transaction,
            final RequestState requestState) {

        super(transaction);
        this.requestState = requestState;

    }

    protected State getTransactionStateName() {
        return State.COMPLETED;
    }

    protected void onFirstInit(final TransactionStateChangeEvent<BaseSipMessage> triggeringEvent) {
        stateChangeReason = triggeringEvent.getStateChangeReason();
        final TimeoutListener listener = new TimeoutListener() {
            public void onTimeout() {
                //RepetitiousTaskManager.getInstance().cancelTask(transaction.getInitialMessage());

                transaction.sendBye();

                final TransactionStateChangeEvent<BaseSipMessage> event =
                        createStateChangeEvent(
                                StateChangeReason.TIMER_TIMEOUT,
                                null
                        );

                transaction.transitToState(new TerminatedState<BaseSipMessage>(transaction, true, false), event);
            }
        };

        if (requestResendListener.compareAndSet(null, listener)) {
            //TIMEOUT for receiving SIP_ACK from remote party, TIMER H = 64*t1
            TimeoutTimer.getInstance().startTimerTransactionSafe(
                    requestResendListener.get(),
                    RepetitiousTaskManager.REQUEST_TIMEOUT_INTERVAL
            );
        }

        //resend 300-699 responses for unreliable transport, timer for UDP
        if (requestState == RequestState.REJECTED
                || requestState == RequestState.TIMEOUT || requestState == RequestState.CANCELED) {

            if (isUnReliableProtocol()) {

                transaction.getStackContext().getRepetitiousTaskManager().startRepetitiousTask(
                        transaction.getInitialMessage(),
                        new RepetitiousTaskManager.Repeater<BaseSipMessage>() {
                            @Override
                            public void onRepeat(BaseSipMessage msg, final Shutdownable task) {
                                transaction.sendMessage(transaction.getLastOutMessage(), new MessageStateListener() {
                                    @Override
                                    public void onMessageSent() {
                                        task.shutdown();
                                    }
                                });
                            }
                            
                        },
                        new RepetitiousTaskManager.ExponentialRepetitiousTimeStrategy(RepetitiousTaskManager.T1, RepetitiousTaskManager.T2)
                );
            }
        }
    }

    public void onMessageReceived(final BaseSipMessage msg) {
        log(msg.getMethod() + " " + requestState, "onMessageReceived");
        MessageType responseType = MessageType.parse(msg.getMethod());

        switch (responseType) {
            case SIP_INVITE: {
                //SIP_INVITE retransmission
                log("SIP_INVITE retransmission", "onMessageReceived");
                transaction.sendResponse(msg, StatusCode.OK, true);
                break;
            }
            case SIP_ACK: {
                final TransactionStateChangeEvent<BaseSipMessage> event =
                        createStateChangeEvent(
                                StateChangeReason.CLIENT_ACCEPT_DELIVERED,
                                null
                        );

                /* final TransactionStateChangeEvent<BaseSipMessage> event =
                createStateChangeEvent(
                        //StateChangeReason.INCOMING_MESSAGE,
                        stateChangeReason,
                        msg
                );*/

                transaction.transitToState(new ConfirmedState(transaction), event);
                break;
            }
            case SIP_PRACK: {
                //SIP_PRACK retransmission
                transaction.sendResponse(msg, StatusCode.OK, true);
                break;
            }
            case SIP_CANCEL: {
                log("CompletedState", "handle late CANCEL");
                transaction.sendResponse(msg, StatusCode.OK, true);
                transaction.sendResponse(msg, StatusCode.REQUEST_TERMINATED, false);//respond to invite

                final TransactionStateChangeEvent<BaseSipMessage> event =
                    createStateChangeEvent(
                            StateChangeReason.INCOMING_MESSAGE,
                            msg
                            );

                transaction.transitToState(new CompletedState(transaction, requestState), event);
                break;
            }
            default: {
                log("unexpected message", "CompletedState#onMessageReceived");
                break;
            }
        }
    }

    public void onStateCompleted() {
        super.onStateCompleted();
        transaction.getStackContext().getRepetitiousTaskManager().cancelTask(transaction.getInitialMessage());
        TimeoutTimer.getInstance().stopTimeoutTimer(requestResendListener.get());
    }
}
