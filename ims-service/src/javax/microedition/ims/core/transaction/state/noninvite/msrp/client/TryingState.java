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

package javax.microedition.ims.core.transaction.state.noninvite.msrp.client;

import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.RepetitiousTaskManager;
import javax.microedition.ims.core.sipservice.State;
import javax.microedition.ims.core.sipservice.StateChangeReason;
import javax.microedition.ims.core.sipservice.TransactionState;
import javax.microedition.ims.core.sipservice.TransactionStateChangeEvent;
import javax.microedition.ims.core.sipservice.timer.TimeoutTimer;
import javax.microedition.ims.core.transaction.CommonTransaction;
import javax.microedition.ims.core.transaction.state.noninvite.CompletedState;
import javax.microedition.ims.messages.wrappers.common.ResponseClass;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;
import java.util.concurrent.atomic.AtomicReference;

public class TryingState<M extends IMSMessage> extends TransactionState<CommonTransaction<M>, M> {

    private final AtomicReference<TimeoutTimer.TimeoutListener> timeoutTimer =
            new AtomicReference<TimeoutTimer.TimeoutListener>(null);

    public TryingState(CommonTransaction<M> transaction) {
        super(transaction);
    }

    protected State getTransactionStateName() {
        return State.TRYING;
    }

    protected void onFirstInit(final TransactionStateChangeEvent<M> triggeringEvent) {
        // startFTimer anywhere t2 = 64t1
        final TimeoutTimer.TimeoutListener timer = new TimeoutTimer.TimeoutListener() {
            public void onTimeout() {
                stopTimers();

                final TransactionStateChangeEvent<M> event =
                        createStateChangeEvent(
                                StateChangeReason.TIMER_TIMEOUT,
                                null
                        );

                transaction.transitToState(new CompletedState<M>(transaction), event);
            }
        };

        if (timeoutTimer.compareAndSet(null, timer)) {
            TimeoutTimer.getInstance().startTimerTransactionSafe(timer, 32000);
        }
    }

    public void onMessageReceived(final M msg) {
        MsrpMessage response = (MsrpMessage) msg;

        if (isMSRPResponce(response)) {
            if (response.getResponseClass() == ResponseClass.Success) {
                final TransactionStateChangeEvent<M> event =
                        createStateChangeEvent(
                                StateChangeReason.INCOMING_MESSAGE,
                                msg
                        );

                transaction.transitToState(new CompletedState<M>(transaction), event);
            }
            else if (response.getResponseClass().isErrorResponse()) {
                final TransactionStateChangeEvent<M> event =
                        createStateChangeEvent(
                                StateChangeReason.INCOMING_MESSAGE,
                                msg
                        );

                transaction.transitToState(new CompletedState<M>(transaction), event);
            }
        }
    }

    private boolean isMSRPResponce(MsrpMessage response) {
        return response.getCode() != 0;
    }

    public void onStateCompleted() {
        super.onStateCompleted();
        stopTimers();
    }

    private void stopTimers() {
        TimeoutTimer.TimeoutListener timer = timeoutTimer.getAndSet(null);
        if (timer != null) {
            TimeoutTimer.getInstance().stopTimeoutTimer(timer);
        }
        transaction.getStackContext().getRepetitiousTaskManager().cancelTask(transaction.getInitialMessage());
    }
}
