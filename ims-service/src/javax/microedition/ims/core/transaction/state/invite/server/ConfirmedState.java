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
import javax.microedition.ims.core.sipservice.State;
import javax.microedition.ims.core.sipservice.StateChangeReason;
import javax.microedition.ims.core.sipservice.TransactionState;
import javax.microedition.ims.core.sipservice.TransactionStateChangeEvent;
import javax.microedition.ims.core.sipservice.timer.TimeoutTimer;
import javax.microedition.ims.core.sipservice.timer.TimeoutTimer.TimeoutListener;
import javax.microedition.ims.core.transaction.server.ServerCommonInviteTransaction;
import javax.microedition.ims.core.transaction.state.TerminatedState;
import javax.microedition.ims.messages.utils.StatusCode;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The purpose of the "Confirmed" state is to absorb any additional SIP_ACK messages that arrive,
 * triggered from retransmissions of the final response. When this state is entered,
 * timer I is set to fire in T4 seconds for unreliable transports, and zero seconds for reliable transports.
 * Once timer I fires, the server MUST transition to the "Terminated" state.
 *
 * @author ext-akhomush
 */
public class ConfirmedState extends TransactionState<ServerCommonInviteTransaction, BaseSipMessage> {

    private final AtomicReference<TimeoutListener> addAckListener = new AtomicReference<TimeoutListener>(null);

    public ConfirmedState(ServerCommonInviteTransaction mngr) {
        super(mngr);
    }

    protected State getTransactionStateName() {
        return State.CONFIRMED;
    }

    protected void onFirstInit(final TransactionStateChangeEvent<BaseSipMessage> triggeringEvent) {
        if (isUnReliableProtocol()) {

            final TimeoutListener listener = new TimeoutListener() {
                public void onTimeout() {

                    final TransactionStateChangeEvent<BaseSipMessage> event =
                            createStateChangeEvent(
                                    StateChangeReason.TIMER_TIMEOUT,
                                    null
                            );

                    transaction.transitToState(new TerminatedState<BaseSipMessage>(transaction, false, true), event);
                }
            };
            if (addAckListener.compareAndSet(null, listener)) {
                TimeoutTimer.getInstance().startTimerTransactionSafe(
                        addAckListener.get(),
                        RepetitiousTaskManager.T4
                );
            }
        }
    }

    public void onStateInitiated(final TransactionStateChangeEvent<BaseSipMessage> triggeringEvent) {

        super.onStateInitiated(triggeringEvent);

        if (isReliableProtocol()) {

            /* final TransactionStateChangeEvent<BaseSipMessage> event =
                createStateChangeEvent(
                        triggeringEvent.getStateChangeReason(),
                        triggeringEvent.getTriggeringMessage()
                );

        transaction.transitToState(new TerminatedState<BaseSipMessage>(transaction, false, true), event);*/
        }
    }

    public void onMessageReceived(BaseSipMessage msg) {
        MessageType messageType = MessageType.parse(msg.getMethod());
        if (MessageType.SIP_ACK == messageType) {
            //absorb any additional SIP_ACK messages triggered from retransmissions of the final response
        }
        else if (MessageType.SIP_PRACK == messageType) {
            //absorb any additional SIP_PRACK messages triggered from retransmissions of the final response
            log("SIP_PRACK retransmission", "onMessageReceived");
            transaction.sendResponse(msg, StatusCode.OK, true);
        }
        else if (MessageType.SIP_INVITE == messageType) {
            //SIP_INVITE retransmission
            log("SIP_INVITE retransmission", "onMessageReceived");
            transaction.sendResponse(msg, StatusCode.OK, true);
        }
        else {
            log("unexpected message: " + msg.buildContent(), "onMessageReceived");
        }
    }

    public void onStateCompleted() {
        super.onStateCompleted();
        TimeoutTimer.getInstance().stopTimeoutTimer(addAckListener.get());
    }

}
