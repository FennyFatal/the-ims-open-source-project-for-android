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

package javax.microedition.ims.core.sipservice.invite.listener;

import javax.microedition.ims.common.ListenerHolder;
import javax.microedition.ims.common.ListenerSupport;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.DialogStateListener;
import javax.microedition.ims.core.sipservice.DefaultTransactionStateChangeEvent;
import javax.microedition.ims.core.sipservice.State;
import javax.microedition.ims.core.sipservice.StateChangeReason;
import javax.microedition.ims.core.sipservice.TransactionStateChangeEvent;
import javax.microedition.ims.core.transaction.Transaction;
import javax.microedition.ims.core.transaction.TransactionListener;
import javax.microedition.ims.messages.utils.StatusCode;
import javax.microedition.ims.util.MessageUtilHolder;
import javax.microedition.ims.util.SipMessageUtil;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 02-Mar-2010
 * Time: 11:01:23
 */
public abstract class ReInviteStateMiddleMan<T> extends DialogStateMiddleMan<T> {

    private final AtomicBoolean needDialogCleanUp = new AtomicBoolean(false);

    public ReInviteStateMiddleMan(
            final ListenerSupport<TransactionListener<T>> listenerSupport,
            final Dialog dialog,
            final ListenerHolder<DialogStateListener> listenerHolder) {
        super(listenerSupport, dialog, listenerHolder);
    }

    
    public void onStateChanged(final TransactionStateChangeEvent<T> event) {
        //TODO commented as listener already unsubscribed here in case of terminated state and we got this assertion
        //assert getMsrpDialog().isReInviteInProgress();
        //do listener un-subscribe here
        super.onStateChanged(event);


        if (sessionTerminationDetected(event)) {

            TransactionStateChangeEvent<T> newEvent = new DefaultTransactionStateChangeEvent<T>(
                    event.getTransaction(),
                    State.TERMINATED,
                    StateChangeReason.TRANSACTION_SHUTDOWN,
                    null
            );

            needDialogCleanUp.compareAndSet(false, true);

            super.onStateChanged(newEvent);
        }
    }

    private boolean sessionTerminationDetected(final TransactionStateChangeEvent<T> event) {
        final T msg = event.getTriggeringMessage();
        SipMessageUtil<T> messageUtil = MessageUtilHolder.getSIPMessageUtil();

        return StateChangeReason.INCOMING_MESSAGE == event.getStateChangeReason() &&
                messageUtil.isResponse(msg) &&
                messageUtil.isClientClassResponse(msg) &&
                isSessionTerminationMessage(msg);
    }

    private boolean isSessionTerminationMessage(final T msg) {
        boolean retValue = false;
        if (msg != null) {
            SipMessageUtil<T> messageUtil = MessageUtilHolder.getSIPMessageUtil();
            retValue = StatusCode.CALL_OR_TRANSACTION_DOESNOT_EXISTS == messageUtil.getStatusCode(msg) ||
                    StatusCode.REQUEST_TIMEOUT == messageUtil.getStatusCode(msg);
        }
        return retValue;
    }


    
    protected void onUnSubscribe(final Transaction transaction) {
        super.onUnSubscribe(transaction);

        if (needDialogCleanUp.get()) {
            onDialogCleanUp(getDialog());
        }
    }

    protected abstract void onDialogCleanUp(Dialog dialog);
}
