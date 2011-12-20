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

package javax.microedition.ims.core.transaction.client;

import javax.microedition.ims.common.ChallengeType;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.common.RepetitiousTaskManager;
import javax.microedition.ims.common.RepetitiousTaskManager.RepetitiousTimeStrategy;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.transaction.CommonSIPTransaction;
import javax.microedition.ims.core.transaction.TransactionDescription;
import javax.microedition.ims.core.transaction.TransactionEvent;
import javax.microedition.ims.core.transaction.TransactionUtils;
import javax.microedition.ims.messages.utils.StatusCode;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Request;
import javax.microedition.ims.messages.wrappers.sip.Response;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ClientTransaction extends CommonSIPTransaction {

    private final AtomicInteger authAttempts = new AtomicInteger(0);
    private static final int AUTH_ATTEMPTS_ALLOWED = 3;

    ClientTransaction(
            final StackContext stackContext,
            final Dialog dlg,
            final TransactionDescription description) {

        super(stackContext, dlg, description);
    }

    
    protected RepetitiousTimeStrategy getResendRequestInterval() {
        return new RepetitiousTaskManager.ExponentialRepetitiousTimeStrategy(RepetitiousTaskManager.T1, RepetitiousTaskManager.T2);
    }

    protected Boolean onMessage(final BaseSipMessage initialMessage, final BaseSipMessage lastMessage) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        Response response = (Response) lastMessage;
        final int statusCode = response.getStatusCode();
        final TransactionEvent<BaseSipMessage> transactionEvent = createTransactionEvent(lastMessage, lastOutMessage.get());

        Boolean retValue = Boolean.FALSE;

        if (authChallengeDetected(statusCode)) {
            retValue = handleAuthChallenge(lastMessage, statusCode, transactionEvent);
            //retValue = null;
        }
        else if (redirectDetected(statusCode)) {
            handleRedirect();
        }
        else {
            //some transactions doesn't have state-machine(currentState)
            if (currentState != null) {
                currentState.onMessageReceived(lastMessage);
            }
        }

        return retValue;
    }

    private void handleRedirect() {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        Request ackMessage = getDialog().getMessageBuilderFactory().getRequestBuilder(MessageType.SIP_ACK).buildMessage();
        sendMessage(ackMessage, null);
    }

    private Boolean handleAuthChallenge(
            final BaseSipMessage lastMessage,
            final int statusCode,
            final TransactionEvent<BaseSipMessage> transactionEvent) {

        assert TransactionUtils.isTransactionExecutionThread() :
                "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        final ChallengeType challengeType =
                statusCode == StatusCode.UNATHORIZED ?
                        ChallengeType.UAS :
                        ChallengeType.PROXY;

        getDialog().putCustomParameter(Dialog.ParamKey.CHALLENGE_TYPE, Arrays.asList(challengeType));

        getListenerHolder().getNotifier().onAuthChallenge(transactionEvent, challengeType);

        MessageType messageType = MessageType.parse(lastMessage.getMethod());
        BaseSipMessage msg = getLastOutMessage();

        //if(getDialog().getAuthorizationData() != null) {
            msg = getDialog().getMessageBuilderFactory().getRequestBuilder(messageType).
            updateRequestWithAuthorizationHeader(
                    (Request) msg,
                    messageType,
                    Arrays.asList(challengeType),
                    true
            ).build();

            //BaseSipMessage msg = dialog.getMessageBuilderFactory().getRequestBuilder(messageType).buildMessage();

            initialMessage.set(msg);
            //listenerHolder.fireOnRequestResendAfterAuthError(msg);

            if (authAttempts.get() < AUTH_ATTEMPTS_ALLOWED) {
                authAttempts.incrementAndGet();
                sendMessage(msg, null);
                return null;
            }
            else {
                getListenerHolder().getNotifier().onAuthFailed(transactionEvent, statusCode == StatusCode.UNATHORIZED ? ChallengeType.UAS : ChallengeType.PROXY);
                return Boolean.FALSE;
            }
            
        /*} else {
            Logger.log("ClientTransaction", "handleAuthChallenge#authorization data is null");
            return Boolean.FALSE;
        }

        //getStackContext().get
        }*/
    }

    private boolean authChallengeDetected(final int statusCode) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        return StatusCode.UNATHORIZED == statusCode || StatusCode.PROXY_AUTH_REQUIRED == statusCode;
    }

    private boolean redirectDetected(final int statusCode) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        return StatusCode.MOVED_PERMANENTLY == statusCode || StatusCode.MOVED_TEMPORARILY == statusCode;
    }
}
