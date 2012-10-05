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

package javax.microedition.ims.core.sipservice.options.listener;

import javax.microedition.ims.common.ListenerHolder;
import javax.microedition.ims.common.ListenerSupport;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.sipservice.State;
import javax.microedition.ims.core.sipservice.StateChangeReason;
import javax.microedition.ims.core.sipservice.TransactionStateChangeEvent;
import javax.microedition.ims.core.sipservice.options.DefaultOptionsInfo;
import javax.microedition.ims.core.sipservice.options.OptionsInfo;
import javax.microedition.ims.core.transaction.TransactionListener;
import javax.microedition.ims.core.transaction.TransactionType;
import javax.microedition.ims.core.transaction.UnSubscribeOnLogicCompleteAdapter;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.util.MessageUtilHolder;
import javax.microedition.ims.util.SipMessageUtil;

import java.util.ArrayList;
import java.util.Collection;
/**
 * Options listener for uac.
 *
 * @author Andrei Khomushko
 * @param <T>
 */
public class OptionsClientListener<T> extends
        UnSubscribeOnLogicCompleteAdapter<T> {
    private static final String TAG = "OptionsClientListener";

    private final Dialog dialog;
    private final ListenerHolder<OptionsStateListener> listenerHolder;

    private enum OptionsState {
        OPTIONS_DELIVERED, OPTIONS_DELIVERY_FAILED, UNKNOWN
    }

    public OptionsClientListener(final Dialog dialog,
                                 final ListenerSupport<TransactionListener<T>> listenerSupport,
                                 final ListenerHolder<OptionsStateListener> listenerHolder) {
        super(listenerSupport);
        this.dialog = dialog;
        this.listenerHolder = listenerHolder;
    }

    
    public void onStateChanged(final TransactionStateChangeEvent<T> event) {
        Logger.log(TAG, "onSessionEvent#event = " + event
                + ", sessionState = state");

        super.onStateChanged(event);

        OptionsState state = toSessionState(event);

        switch (state) {
            case OPTIONS_DELIVERED: {
                doUnSubscribe(event.getTransaction());
                listenerHolder.getNotifier().onOptionsDelivered(
                        new DefaultOptionsStateEvent(dialog,
                                createOptionsInfo(event)));
                break;
            }
            case OPTIONS_DELIVERY_FAILED: {
                doUnSubscribe(event.getTransaction());
                listenerHolder.getNotifier().onOptionsDelivereFailed(
                        new DefaultOptionsStateEvent(dialog,
                                createOptionsInfo(event)));
                break;
            }
        }
    }

    private OptionsInfo createOptionsInfo(
            final TransactionStateChangeEvent<T> event) {
        // Allow, Accept, Accept-Encoding, Accept-Language, and
        BaseSipMessage message = (BaseSipMessage) event.getTriggeringMessage();
        Collection<String> entities = new ArrayList<String>();
        if (message != null)
            entities.add(message.getContacts().toString());
        return new DefaultOptionsInfo(entities);
    }

    private static <T> OptionsState toSessionState(
            final TransactionStateChangeEvent<T> event) {
        Logger.log(TAG, "toSessionState#event state: " + event.getState());

        final OptionsState retValue;

        final TransactionType.Name transactionName = event.getTransaction()
                .getTransactionType().getName();
        final State stateName = event.getState();
        final StateChangeReason reason = event.getStateChangeReason();
        final T message = event.getTriggeringMessage();
        final SipMessageUtil<T> sipMessageUtil = MessageUtilHolder.getSIPMessageUtil();

        switch (transactionName) {
            case SIP_OPTIONS_CLIENT: {
                if (State.COMPLETED == stateName
                        && StateChangeReason.INCOMING_MESSAGE == reason) {
                    if (sipMessageUtil.isSuccessResponse(message)) {
                        retValue = OptionsState.OPTIONS_DELIVERED;
                    }
                    else {
                        retValue = OptionsState.OPTIONS_DELIVERY_FAILED;
                    }
                } else if (State.TRYING == stateName
                    && StateChangeReason.TIMER_TIMEOUT == reason) {
                    retValue = OptionsState.OPTIONS_DELIVERY_FAILED;
                } else {
                    retValue = OptionsState.UNKNOWN;
                }
            }
            break;
            default:
                retValue = OptionsState.UNKNOWN;
        }

        Logger.log(TAG, "toSessionState#retValue: " + retValue);
        return retValue;
    }

    
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("OptionsClientListener");
        sb.append("{DIALOG=").append(dialog);
        sb.append('}');
        return sb.toString();
    }
}
