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

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.RepetitiousTaskManager;
import javax.microedition.ims.core.sipservice.State;
import javax.microedition.ims.core.sipservice.StateChangeReason;
import javax.microedition.ims.core.sipservice.TransactionState;
import javax.microedition.ims.core.sipservice.TransactionStateChangeEvent;
import javax.microedition.ims.core.sipservice.timer.TimeoutTimer;
import javax.microedition.ims.core.sipservice.timer.TimeoutTimer.TimeoutListener;
import javax.microedition.ims.core.transaction.client.ClientCommonInviteTransaction;
import javax.microedition.ims.core.transaction.state.TerminatedState;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Response;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class responsible for waiting retransmission error messages(for UDP).
 */
public class CompletedState extends TransactionState<ClientCommonInviteTransaction, BaseSipMessage> {

    private final AtomicReference<TimeoutListener> tListener = new AtomicReference<TimeoutListener>(null);

    public CompletedState(ClientCommonInviteTransaction mngr) {
        super(mngr);
    }


    protected State getTransactionStateName() {
        return State.COMPLETED;
    }


    protected void onFirstInit(final TransactionStateChangeEvent<BaseSipMessage> triggeringEvent) {
        if (isUnReliableProtocol()) {

            TimeoutListener listener = new TimeoutTimer.TimeoutListener() {
                public void onTimeout() {
                    Logger.log("timeout2");

                    final TransactionStateChangeEvent<BaseSipMessage> event =
                            createStateChangeEvent(
                                    StateChangeReason.TIMER_TIMEOUT,
                                    null
                            );

                    transaction.transitToState(new TerminatedState<BaseSipMessage>(transaction, true, false), event);
                }
            };

            if (tListener.compareAndSet(null, listener)) {
                TimeoutTimer.getInstance().startTimerTransactionSafe(
                        tListener.get(),
                        RepetitiousTaskManager.REQUEST_TIMEOUT_INTERVAL
                );
            }
        }
    }


    public void onStateInitiated(final TransactionStateChangeEvent<BaseSipMessage> triggeringEvent) {

        super.onStateInitiated(triggeringEvent);

        final TransactionStateChangeEvent<BaseSipMessage> event =
                createStateChangeEvent(
                        StateChangeReason.INCOMING_MESSAGE,
                        triggeringEvent.getTriggeringMessage()
                );

        transaction.transitToState(new TerminatedState<BaseSipMessage>(transaction, false, true), event);
    }



    public void onMessageReceived(BaseSipMessage msg) {
        if (msg instanceof Response) {
            if (((Response) msg).getResponseClass().isErrorResponse()) {
                transaction.confirmResponse();
            }
        }
    }

    public void onStateCompleted() {
        TimeoutTimer.getInstance().stopTimeoutTimer(tListener.get());
    }
}
