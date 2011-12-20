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

package javax.microedition.ims.core.sipservice.invite;

import javax.microedition.ims.common.ListenerSupport;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.sipservice.SessionState;
import javax.microedition.ims.core.sipservice.TransactionStateChangeEvent;
import javax.microedition.ims.core.transaction.*;
import javax.microedition.ims.core.transaction.TransactionResult.Reason;

import java.util.concurrent.atomic.AtomicReference;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 27-Jan-2010
 * Time: 12:18:47
 */
public abstract class DialogCleanUpListener<T> extends UnSubscribeOnCompleteAdapter<T> {
    private static final String LOG_TAG = "DialogCleanUpListener";
    private final Dialog dialog;
    private final Boolean resultToCleanUp;
    private final AtomicReference<Boolean> currentResult = new AtomicReference<Boolean>(null);

    public DialogCleanUpListener(
            final ListenerSupport<TransactionListener<T>> listenerSupport,
            final Dialog dialog,
            final Boolean resultToCleanUp) {
        super(listenerSupport);
        this.dialog = dialog;
        this.resultToCleanUp = resultToCleanUp;
    }

    public void onStateChanged(final TransactionStateChangeEvent<T> event) {
        //do listener un-subscribe here
        super.onStateChanged(event);

        SessionState sessionState = SessionState.toSessionState(event);

/*        if (SessionState.SESSION_START_FAILED == sessionState || SessionState.SESSION_TERMINATED == sessionState) {
            //doCleanUp(false);
            currentResult.compareAndSet(null, false);
        }
        else if (SessionState.SESSION_STARTED == sessionState) {
            //doCleanUp(true);
            currentResult.compareAndSet(null, true);
        } 
*/
        if(sessionState != null) {
            switch (sessionState) {
                case SESSION_START_FAILED:
                case SESSION_TERMINATED: {
                    //doCleanUp(false);
                    currentResult.compareAndSet(null, false);
                    break;
                } 
                case SESSION_STARTED: {
                    //doCleanUp(true);
                    currentResult.compareAndSet(null, true);
                    break;
                } 
                case MESSAGE_DELIVERED:
                case MESSAGE_DELIVERY_FAILED: 
                case MESSAGE_RECEIVED: {
                    currentResult.compareAndSet(null, false);
                    break;
                }
                default:
                    break;
            }            
        }
    }

    public void onTransactionComplete(final TransactionEvent<T> event, final TransactionResult.Reason reason) {
        super.onTransactionComplete(event, reason);

        final Boolean transactionResult = event.getTransaction().getTransactionValue().getValue();
        currentResult.compareAndSet(null, transactionResult);
    }

    protected void onUnSubscribe(final Transaction transaction) {
        Logger.log(LOG_TAG, "onUnSubscribe#transaction = " + transaction);
        super.onUnSubscribe(transaction);
        
        boolean isTimeout = transaction.getTransactionValue().getReason() == Reason.TIMEOUT;
        
        Logger.log(LOG_TAG, "onUnSubscribe#resultToCleanUp = " + resultToCleanUp + ", currentResult.get() = " + currentResult.get() + ", isTimeout = " + isTimeout);
        
        
        
        if (resultToCleanUp == null || currentResult.get() == resultToCleanUp || isTimeout) {
            Logger.log(LOG_TAG, "onUnSubscribe#onDialogCleanUp, dialog = " + dialog);
            onDialogCleanUp(dialog);
        }
    }

    protected abstract void onDialogCleanUp(final Dialog dialog);


    public String toString() {
        return "DialogCleanUpListener{" +
                "DIALOG=" + dialog +
                '}';
    }
}
