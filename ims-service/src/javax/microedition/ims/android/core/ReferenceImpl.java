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

package javax.microedition.ims.android.core;

import android.os.RemoteException;
import android.util.Log;

import javax.microedition.ims.android.util.RemoteListenerHolder;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.core.IMSStackException;
import javax.microedition.ims.core.dialog.*;
import javax.microedition.ims.core.sipservice.Acceptable;
import javax.microedition.ims.core.sipservice.refer.Refer;
import javax.microedition.ims.core.sipservice.refer.ReferService;
import javax.microedition.ims.core.sipservice.refer.listener.ThirdPartySessionInitializationListener;
import javax.microedition.ims.core.sipservice.subscribe.listener.NotifyEvent;
import javax.microedition.ims.messages.history.MessageData;
import javax.microedition.ims.messages.history.MessageDataBuilder;
import javax.microedition.ims.messages.utils.StatusCode;
import javax.microedition.ims.messages.wrappers.sip.Request;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service implementation reference.
 *
 * @author ext-akhomush
 * @see IReference.aidl
 */
public class ReferenceImpl extends IReference.Stub {
    private static final String TAG = "Service - ReferenceImpl";

    //private final ReferenceImpl.Side side;

    private enum Side {
        CLIENT, SERVER
    }

    private final IServiceMethod serviceMethod;
    //private final Dialog dialog;

    //private final String fromUserId;
    //private final String toUserId;
    private final Refer refer;

    private final AtomicReference<ReferenceState> state = new AtomicReference<ReferenceState>(ReferenceState.STATE_INITIATED);

    private final RemoteListenerHolder<IReferenceListener> listenerHolder = new RemoteListenerHolder<IReferenceListener>(IReferenceListener.class);

    private final ReferService referService;
    private IReferenceListener referenceListener;
    private final Acceptable<Refer> acceptable;

    enum ReferenceState {
        /**
         * This state specifies that the <code>Reference</code> is created but not
         * started.
         */
        STATE_INITIATED(1),

        /**
         * This state specifies that the <code>Reference</code> has been started.
         */
        STATE_PROCEEDING(2),

        /**
         * This state specifies that the <code>Reference</code> has been accepted
         * and that the remote endpoint is referring to the third party.
         */
        STATE_REFERRING(3),

        /**
         * This state specifies that the <code>Reference</code> has been REJECTED
         * or terminated.
         */
        STATE_TERMINATED(4);

        private int code;

        private ReferenceState(int code) {
            this.code = code;
        }

    }


    private IncomingNotifyListener incomingNotifyListener = new IncomingNotifyListener() {
        
        public void notificationReceived(final NotifyEvent event) {
            Request lastRequest = refer.getDialog().getMessageHistory().findLastRequestByMethod(MessageType.SIP_NOTIFY);

            MessageData messageData = MessageDataBuilder.buildMessageData(lastRequest, true);

            MessageImpl notify = new MessageImpl(messageData);

            notifyReferenceNotified(notify);

            String subscrState = event.getNotifyInfo().getNotifySubscriptionState().stringValue();
            String[] bodyMsgs = event.getNotifyInfo().getNotifyBodyMessages();
            String bodyMsg = (bodyMsgs != null && bodyMsgs.length > 0 ? bodyMsgs[0] : null);
            if (subscrState != null && subscrState.toLowerCase().equals("terminated")) {
                toTerminatedState();
            }
            else if (bodyMsg != null && bodyMsg.toLowerCase().equals("connected")) {
                toTerminatedState();
            }

        }
    };

/*    private IncomingReferListener incomingReferListener = new IncomingReferListener() {
        
        public void referenceReceived(final IncomingReferEvent event) {
        }
    };
*/
    private ReferStateAdapter dialogStateListenerAdapter = new ReferStateAdapter() {
        
        public void onReferenceDelivered(ReferStateEvent event) {
            notifyReferenceDelivered();
        }

        
        public void onReferenceDeliveryFailed(ReferStateEvent event) {
            notifyReferenceDeliveryFailed();
            notifyReferenceTerminated();
        }
    };


    public static ReferenceImpl createOnClientSide(
            /*final Dialog dialog,*/
            final ReferService referService,
            final Refer refer) {

        ReferenceImpl referenceImpl = new ReferenceImpl(Side.CLIENT, /*dialog,*/ referService, null, refer);

        //TODO where listeners should remove?
        referService.addReferStateListener(refer.getDialog(), referenceImpl.dialogStateListenerAdapter);
        referService.addIncomingNotifyListener(refer.getDialog(), referenceImpl.incomingNotifyListener);
        //referService.addIncomingReferListener(DIALOG, referenceImpl.incomingReferListener);

        return referenceImpl;
    }

    public static ReferenceImpl createOnServerSide(
            /*final Dialog dialog,*/
            final ReferService referService,
            final Refer refer,
            final Acceptable<Refer> acceptable) {

        ReferenceImpl referenceImpl = new ReferenceImpl(Side.SERVER, /*dialog, */referService, acceptable, refer);
        referenceImpl.setState(ReferenceState.STATE_PROCEEDING);

        return referenceImpl;
    }


    private ReferenceImpl(
            final Side side,
            /*final Dialog dialog,*/
            final ReferService referService,
            final Acceptable<Refer> acceptable,
            final Refer refer) throws IllegalArgumentException {

        //this.side = side;
        assert Side.CLIENT == side || Side.SERVER == side && acceptable != null;

        this.acceptable = acceptable;

/*        if (dialog == null) {
            throw new IllegalArgumentException("DIALOG parameter can not be null. Now it has value " + dialog);
        }
*/        //this.dialog = dialog;

        if (referService == null) {
            throw new IllegalArgumentException("referService parameter can not be null. Now it has value " + referService);
        }
        this.referService = referService;

        if (refer == null) {
            throw new IllegalArgumentException("REFER parameter can not be null. Now it has value " + refer);
        }
        this.refer = refer;

        //this.fromUserId = dialog.getLocalParty().getAppID();
        //this.toUserId = dialog.getRemoteParty();

        serviceMethod = new ReferenceServiceMethodImpl(refer.getDialog().getRemoteParty(), refer.getDialog().getMessageHistory());
    }

    public void startByRemoteParty() {
        setState(ReferenceState.STATE_PROCEEDING);
    }

    
    public String getReferMethod() throws RemoteException {
        return refer.getReferMethod();
    }

    
    public String getReferToUserId() throws RemoteException {
        return refer.getReferTo();
    }

    
    public IServiceMethod getServiceMethod() throws RemoteException {
        return serviceMethod;
    }

    
    public int getState() throws RemoteException {
        return state.get().code;
    }

    
    public void accept() throws RemoteException {

        setState(ReferenceState.STATE_REFERRING);

        //acceptable.accept(dialog);
        acceptable.accept(refer);
    }

    
    public void reject() throws RemoteException {

        setState(ReferenceState.STATE_TERMINATED);

        //acceptable.reject(dialog, StatusCode.TEMPORARY_UNAVAILABLE, "TEMPORARY_UNAVAILABLE");
        acceptable.reject(refer, StatusCode.TEMPORARY_UNAVAILABLE, "TEMPORARY_UNAVAILABLE");

        notifyReferenceTerminated();
    }

    private void toTerminatedState() {
        setState(ReferenceState.STATE_TERMINATED);

        notifyReferenceTerminated();
    }

    
    public void connectReferSession(ISession session) throws RemoteException {

        SessionImpl sess = (SessionImpl) session;
        //sess.

        ThirdPartySessionInitializationListener listener = new ThirdPartySessionInitializationListener() {
            
            public void onSessionStarted() {
                toTerminatedState();
            }

            
            public void onSessionStartFailed() {
                toTerminatedState();
            }
        };

        try {
            referService.createSubscription(sess.getDialog(), refer, listener);
        }
        catch (IMSStackException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    
    public void refer() throws RemoteException {
        setState(ReferenceState.STATE_PROCEEDING);

        Logger.log("ReferenceImpl.REFER()", " " + refer);

        referService.refer(refer);

        Logger.log("ReferenceImpl.REFER()", "finished");
    }

    private void notifyReferenceNotified(final IMessage notify) {
        try {
            listenerHolder.getNotifier().referenceNotify(notify);
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void notifyReferenceTerminated() {
        try {
            listenerHolder.getNotifier().referenceTerminated();
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void notifyReferenceDelivered() {
        setState(ReferenceState.STATE_REFERRING);
        try {
            listenerHolder.getNotifier().referenceDelivered();
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void notifyReferenceDeliveryFailed() {
        setState(ReferenceState.STATE_TERMINATED);
        try {
            listenerHolder.getNotifier().referenceDeliveryFailed();
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    
    public void setListener(IReferenceListener newListener) throws RemoteException {
        if (referenceListener != null) {
            listenerHolder.removeListener(referenceListener);
        }

        if (newListener != null) {
            listenerHolder.addListener(referenceListener = newListener);
        }
    }

    private void setState(ReferenceState refState) {
        state.set(refState);
    }
}
