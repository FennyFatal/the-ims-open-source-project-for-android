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
import javax.microedition.ims.common.Shutdownable;
import javax.microedition.ims.common.util.CollectionsUtils;
import javax.microedition.ims.core.sipservice.*;
import javax.microedition.ims.core.sipservice.invite.TUCancelUpdateEvent;
import javax.microedition.ims.core.sipservice.timer.TimeoutTimer;
import javax.microedition.ims.core.transaction.CommonTransaction.MessageStateListener;
import javax.microedition.ims.core.transaction.client.ClientCommonInviteTransaction;
import javax.microedition.ims.core.transaction.state.TerminatedState;
import javax.microedition.ims.messages.utils.StatusCode;
import javax.microedition.ims.messages.wrappers.common.ResponseClass;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Response;
import java.util.concurrent.atomic.AtomicReference;

public class ProceedingState extends
        TransactionState<ClientCommonInviteTransaction, BaseSipMessage> {

    private final AtomicReference<TimeoutTimer.TimeoutListener> requestPendingTimer = new AtomicReference<TimeoutTimer.TimeoutListener>(
            null);
    private BaseSipMessage updateTimerKey;

    public ProceedingState(
            final ClientCommonInviteTransaction clientInviteTransaction) {
        super(clientInviteTransaction);
    }

    
    protected State getTransactionStateName() {
        return State.PROCEEDING;
    }

    protected void onFirstInit(
            final TransactionStateChangeEvent<BaseSipMessage> triggeringEvent) {

    }

    
    public void onMessageReceived(final BaseSipMessage msg) {
        if (msg instanceof Response) {
            Response response = (Response) msg;
            if (response.getResponseClass() == ResponseClass.Informational) {

                // send prack if response code 101..199 and response contains
                // require HEADER with value 100rel
                if (response.getStatusCode() != 100) {
                    if (CollectionsUtils.contains(msg.getRequire(),
                            OptionFeature._100REL.getName())) {
                        // start timer
                        transaction.sendPrack();
                    }
                }

                final TransactionStateChangeEvent<BaseSipMessage> event = createStateChangeEvent(
                        StateChangeReason.INCOMING_MESSAGE, msg);

                transaction.transitToState(this, event);
            }
            else if (response.getResponseClass() == ResponseClass.Success) {
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
                else if (MessageType.SIP_UPDATE == MessageType.parse(response
                        .getMethod())) {
                    final TransactionStateChangeEvent<BaseSipMessage> event = createStateChangeEvent(
                            StateChangeReason.CLIENT_UPDATE_SUCCESS, msg);

                    transaction.notifyTU(event);
                }
            }
            /*
             * If a UAC receives a 491 response to a re-SIP_INVITE, it SHOULD
             * start a timer with a value T chosen as follows:
             * 
             * 1. If the UAC is the owner of the Call-ID of the DIALOG ID
             * (meaning it generated the value), T has a randomly chosen value
             * between 2.1 and 4 seconds in units of 10 ms.
             * 
             * 2. If the UAC is not the owner of the Call-ID of the DIALOG ID, T
             * has a randomly chosen value of between 0 and 2 seconds in units
             * of 10 ms.
             * 
             * When the timer fires, the UAC SHOULD attempt the re-SIP_INVITE
             * once more, if it still desires for that SESSION modification to
             * take place. For example, if the call was already hung up with a
             * SIP_BYE, the re-SIP_INVITE would not take place.
             */
            else if (ResponseClass.Client == response.getResponseClass()
                    && StatusCode.REQUEST_PENDING == response.getStatusCode()) {
                transaction.getStackContext().getRepetitiousTaskManager().cancelTask(
                        transaction.getInitialMessage());

                requestPendingTimer.compareAndSet(null,
                        new TimeoutTimer.TimeoutListener() {
                            
                            public void onTimeout() {
                                TimeoutTimer.getInstance().stopTimeoutTimer(
                                        requestPendingTimer.get());
                                transaction.retryInvite();
                            }
                        });

                // TODO: currently we don't have access to Dialog. So there is
                // no way to determine who is the owner of CallId.
                // Proposal: introduce Dialog to transaction.
                // Workaround: use 2 seconds as middle value between cases 1 and
                // 2 from RFC3261 (see comments above)
                final long requestTimeoutInterval = 2000;
                TimeoutTimer.getInstance().startTimerTransactionSafe(
                        requestPendingTimer.get(), requestTimeoutInterval);
            }
            else if (response.getResponseClass().isErrorResponse()) {
                MessageType messageType = MessageType.parse(response
                        .getMethod());
                if (MessageType.SIP_INVITE == messageType) {
                    transaction.confirmResponse();

                    final TransactionStateChangeEvent<BaseSipMessage> event = createStateChangeEvent(
                            StateChangeReason.INCOMING_MESSAGE, msg);

                    transaction.transitToState(new CompletedState(transaction),
                            event);
                }
                else if (MessageType.SIP_UPDATE == messageType) {

                    final TransactionStateChangeEvent<BaseSipMessage> event = createStateChangeEvent(
                            StateChangeReason.CLIENT_UPDATE_FAILED, msg);

                    transaction.notifyTU(event);
                }
            }
        }
    }

    public void onTUReceived(TUEvent event) {
        TUCancelUpdateEvent inviteEvent = (TUCancelUpdateEvent) event;
        if (inviteEvent.getOpType() == TUCancelUpdateEvent.OPERATION_TYPE_CANCEL) {
            transaction.sendCancel();

            final TransactionStateChangeEvent<BaseSipMessage> changeEvent = createStateChangeEvent(
                    StateChangeReason.CLIENT_CANCEL, null);

            transaction.transitToState(new CanceledState(transaction),
                    changeEvent);
        }
        else if (inviteEvent.getOpType() == TUCancelUpdateEvent.OPERATION_TYPE_UPDATE) {
            updateTimerKey = transaction.sendUpdate();
            transaction.getStackContext().getRepetitiousTaskManager()
                    .startRepetitiousTask(
                            updateTimerKey,
                            new RepetitiousTaskManager.Repeater<BaseSipMessage>() {
                                
                                @Override
                                public void onRepeat(BaseSipMessage msg, final Shutdownable task) {
                                    transaction.sendMessage(msg, new MessageStateListener() {
                                        @Override
                                        public void onMessageSent() {
                                            task.shutdown();
                                        }
                                    });
                                }
                            },
                            new RepetitiousTaskManager.ExponentialRepetitiousTimeStrategy(
                                    RepetitiousTaskManager.T1,
                                    RepetitiousTaskManager.T2));
        }
    }

    
    public void onStateCompleted() {
        super.onStateCompleted();
        transaction.getStackContext().getRepetitiousTaskManager().cancelTask(updateTimerKey);
        if (requestPendingTimer.get() != null) {
            TimeoutTimer.getInstance().stopTimeoutTimer(
                    requestPendingTimer.get());
        }
    }
}
