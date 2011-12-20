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
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.core.dialog.DefaultDialogStateEvent;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.DialogStateEvent;
import javax.microedition.ims.core.dialog.DialogStateListener;
import javax.microedition.ims.core.sipservice.SessionState;
import javax.microedition.ims.core.sipservice.TransactionStateChangeEvent;
import javax.microedition.ims.core.transaction.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 25-Feb-2010
 * Time: 15:17:53
 */
public class DialogStateMiddleMan<T> extends UnSubscribeOnCompleteAdapter<T> {
    private final Dialog dialog;
    private final ListenerHolder<DialogStateListener> listenerHolder;
    private final AtomicBoolean logicallyComplete = new AtomicBoolean(false);

    public DialogStateMiddleMan(
            final ListenerSupport<TransactionListener<T>> listenerSupport,
            final Dialog dialog,
            final ListenerHolder<DialogStateListener> listenerHolder) {
        super(listenerSupport);
        this.dialog = dialog;
        this.listenerHolder = listenerHolder;
    }

    
    public void onStateChanged(final TransactionStateChangeEvent<T> event) {
        //do listener un-subscribe here
        super.onStateChanged(event);

        if (!logicallyComplete.get()) {

            SessionState state = SessionState.toSessionState(event);

            if (state != null && state != SessionState.UNKNOWN) {
                handleStateChange(event, state);
            }

            logicallyComplete.compareAndSet(false, event.getState().isLogicallyCompleted());
        }

    }

    public void onTransactionComplete(final TransactionEvent<T> event, final TransactionResult.Reason reason) {
        if (event.getTransaction().getTransactionType() == TransactionType.SIP_INVITE_CLIENT &&
                reason == TransactionResult.Reason.TIMEOUT) {

            final DialogStateEvent<T> dialogStateEvent =
                    new DefaultDialogStateEvent<T>(dialog, SessionState.SESSION_START_FAILED, event.getLastInMessage());

            //((InviteClntTransaction)event.getTransaction()).cancel();
            listenerHolder.getNotifier().onSessionEventBefore(dialogStateEvent);

            listenerHolder.getNotifier().onSessionStartFailed(dialogStateEvent);
            listenerHolder.getNotifier().onSessionEnded(dialogStateEvent);
        }
    }

    private void handleStateChange(final TransactionStateChangeEvent<T> event, final SessionState state) {
        Logger.log("DialogStateMiddleMan", "handleStateChange#state = " + state);
        final DialogStateEvent<T> dialogStateEvent =
                new DefaultDialogStateEvent<T>(dialog, state, event.getTriggeringMessage());

        listenerHolder.getNotifier().onSessionEventBefore(dialogStateEvent);

        switch (state) {
            case SESSION_ALERTING: {
                listenerHolder.getNotifier().onSessionAlerting(dialogStateEvent);
            }
            break;
            case SESSION_STARTED: {
                listenerHolder.getNotifier().onSessionStarted(dialogStateEvent);
            }
            break;
            case SESSION_START_FAILED: {
                listenerHolder.getNotifier().onSessionStartFailed(dialogStateEvent);
                listenerHolder.getNotifier().onSessionEnded(dialogStateEvent);
            }
            break;
            case SESSION_TERMINATED: {
                listenerHolder.getNotifier().onSessionTerminated(dialogStateEvent);
                listenerHolder.getNotifier().onSessionEnded(dialogStateEvent);
            }
            break;
            case SESSION_UPDATE_RECEIVED: {
                listenerHolder.getNotifier().onSessionUpdateReceived(dialogStateEvent);
            }
            break;
            case SESSION_UPDATE_FAILED: {
                listenerHolder.getNotifier().onSessionUpdateFailed(dialogStateEvent);
                listenerHolder.getNotifier().onSessionUpdateEnded(dialogStateEvent);
            }
            break;
            case SESSION_UPDATED: {
                listenerHolder.getNotifier().onSessionUpdated(dialogStateEvent);
                listenerHolder.getNotifier().onSessionUpdateEnded(dialogStateEvent);
            }
            break;
        }

        listenerHolder.getNotifier().onSessionEventAfter(dialogStateEvent);
    }

    protected Dialog getDialog() {
        return dialog;
    }

    
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("DialogStateMiddleMan");
        sb.append("{DIALOG=").append(dialog);
        sb.append('}');
        return sb.toString();
    }
}
