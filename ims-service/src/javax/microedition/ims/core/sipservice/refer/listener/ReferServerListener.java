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

package javax.microedition.ims.core.sipservice.refer.listener;

import javax.microedition.ims.common.DefaultTimeoutUnit;
import javax.microedition.ims.common.ListenerHolder;
import javax.microedition.ims.common.ListenerSupport;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.core.dialog.DefaultIncomingReferEvent;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.IncomingReferListener;
import javax.microedition.ims.core.sipservice.Acceptable;
import javax.microedition.ims.core.sipservice.ReferState;
import javax.microedition.ims.core.sipservice.TransactionStateChangeEvent;
import javax.microedition.ims.core.sipservice.refer.Refer;
import javax.microedition.ims.core.sipservice.refer.ReferImpl;
import javax.microedition.ims.core.transaction.TransactionListener;
import javax.microedition.ims.core.transaction.UnSubscribeOnLogicCompleteAdapter;
import javax.microedition.ims.core.transaction.server.ReferServerTransaction;
import javax.microedition.ims.messages.wrappers.sip.Header;
import javax.microedition.ims.messages.wrappers.sip.Request;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReferServerListener<T> extends UnSubscribeOnLogicCompleteAdapter<T> {
    private final Dialog dialog;
    private final ListenerHolder<IncomingReferListener> listenerHolder;
    private final Acceptable<Refer> acceptable;

    public ReferServerListener(
            final Dialog dialog, final Acceptable<Refer> acceptable,
            final ListenerSupport<TransactionListener<T>> listenerSupport,
            final ListenerHolder<IncomingReferListener> listenerHolder) {
        super(listenerSupport);
        this.dialog = dialog;
        this.acceptable = acceptable;
        this.listenerHolder = listenerHolder;
    }

    
    public void onStateChanged(final TransactionStateChangeEvent<T> event) {
        //do listener un-subscribe here
        super.onStateChanged(event);

//        assert Dialog.InitiateParty.REMOTE == DIALOG.getInitiateParty();

        ReferState referState = ReferState.toReferState(event);

//        if (SessionState.SESSION_ALERTING == sessionState) {
//
//            //As far as we detected server media update we can un-subscribe form listening state changes
//            doUnSubscribe(event.getTransaction());
//            listenerHolder.getNotifier().onIncomingCall(new DefaultIncomingOperationEvent(DIALOG, acceptable));
//        }
        if (ReferState.REFERENCE_RECEIVED == referState) {

            //As far as we detected server media update we can un-subscribe form listening state changes
            doUnSubscribe(event.getTransaction());
//            listenerHolder.getNotifier().onIncomingCall(new DefaultIncomingOperationEvent(DIALOG, acceptable));

            String uri = null;
            List<String> referTo = dialog.getMessageHistory().findLastRequestByMethod(MessageType.SIP_REFER).getCustomHeader(Header.ReferTo);
            if (!referTo.isEmpty()) {
                uri = referTo.get(0);
            }

            final Request message = (Request) event.getTriggeringMessage();
            if (((ReferServerTransaction) event.getTransaction()).isAcceptableHere(message)) {
                listenerHolder.getNotifier().referenceReceived(
                        new DefaultIncomingReferEvent(
                                new ReferImpl(
                                        dialog,
                                        uri,
                                        MessageType.SIP_INVITE.stringValue(),
                                        new DefaultTimeoutUnit(60, TimeUnit.SECONDS)
                                ),
                                acceptable
                        )
                );
            }
        }
    }

    
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("ReferServerListener");
        sb.append("{DIALOG=").append(dialog);
        sb.append('}');
        return sb.toString();
    }
}
