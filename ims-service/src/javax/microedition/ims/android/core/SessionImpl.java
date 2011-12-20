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

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import javax.microedition.ims.android.core.CoreServiceImpl.SessionOnCloseListener;
import javax.microedition.ims.android.core.media.IMedia;
import javax.microedition.ims.android.core.media.SdpBuilder;
import javax.microedition.ims.android.core.media.SdpConvertor;
import javax.microedition.ims.common.DefaultTimeoutUnit;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.DialogStateEvent;
import javax.microedition.ims.core.dialog.DialogStateListener;
import javax.microedition.ims.core.dialog.DialogStateListenerAdapter;
import javax.microedition.ims.core.sipservice.Acceptable;
import javax.microedition.ims.core.sipservice.SessionState;
import javax.microedition.ims.core.sipservice.SteppedAcceptable;
import javax.microedition.ims.core.sipservice.invite.DialogStateException;
import javax.microedition.ims.core.sipservice.invite.InviteService;
import javax.microedition.ims.core.sipservice.options.OptionsService;
import javax.microedition.ims.core.sipservice.refer.Refer;
import javax.microedition.ims.core.sipservice.refer.ReferImpl;
import javax.microedition.ims.core.sipservice.refer.ReferService;
import javax.microedition.ims.messages.utils.StatusCode;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Request;
import javax.microedition.ims.messages.wrappers.sip.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Session implementation.
 *
 * @author ext-akhomush
 * @see ISession.aidl
 */
class SessionImpl extends ISession.Stub {
    private static final String TAG = "SessionImpl-Service";

    enum StateCode {
        /**
         * This state specifies that the Session is established.
         */
        STATE_ESTABLISHED(4),

        /**
         * This state specifies that the Session is ACCEPTED by the mobile terminated endpoint.
         */
        STATE_ESTABLISHING(3),

        /**
         * This state specifies that the Session is created but not started.
         */
        STATE_INITIATED(1),

        /**
         * This state specifies that the Session establishment has been started.
         */
        STATE_NEGOTIATING(2),

        /**
         * This state specifies that the Session update is ACCEPTED by the mobile terminated endpoint.
         */
        STATE_REESTABLISHING(6),

        /**
         * This state specifies that the Session is negotiating updates to the SESSION.
         */
        STATE_RENEGOTIATING(5),

        /**
         * This state specifies that the Session has been terminated.
         */
        STATE_TERMINATED(8),

        /**
         * This state specifies that an established Session is being terminated by the local endpoint.
         */
        STATE_TERMINATING(7);

        private int code;

        private StateCode(int code) {
            this.code = code;
        }
    }

    private static final int CODE_MOVED_TEMPORARILY = 302;

    private final AtomicReference<StateCode> state = new AtomicReference<StateCode>(StateCode.STATE_INITIATED);
    private final RemoteCallbackList<ISessionListener> listeners = new RemoteCallbackList<ISessionListener>();
    private final Dialog dialog;
    private SteppedAcceptable<Dialog> acceptable;
    private final InviteService inviteService;
    private final OptionsService optionsService;
    private final ReferService referService;
    private final ISessionDescriptor sessionDescriptor;
    private final IServiceMethod serviceMethod;
    private final boolean useResourceReservation;

    private final SessionOnCloseListener sessionOnCloseListener;

    private final DialogStateListener<BaseSipMessage> dialogStateListener = new DialogStateListenerAdapter<BaseSipMessage>() {

        /*

        public void onSessionAlerting(DialogStateEvent event) {
            Object triggeringMessage = event.getTriggeringMessage();
            
            if (triggeringMessage instanceof Response) {
                final Response response = (Response) triggeringMessage;

                if (ResponseClass.Informational == response.getResponseClass()) {
                    //if (StatusCode.CALL_SESSION_PROGRESS == response.getStatusCode())
                    
                    SdpMessage incomingSdpMessage = dialog.getIncomingSdpMessage();
                    if (incomingSdpMessage != null) {

                        //choose common medias
                        List<Media> localMedias = dialog.getOutgoingSdpMessage().getMedias();
                        List<Media> remoteMedias = incomingSdpMessage.getMedias();
                        List<Media> commonMedias = new ArrayList<Media>();
                        
                        for (Media localMedia : localMedias) {
                            for (Media remoteMedia : remoteMedias) {
                                if (localMedia.compareTo(remoteMedia)) {
                                    commonMedias.add(localMedia);
                                    break;
                                }
                            }
                        }
                        
                        dialog.getOutgoingSdpMessage().clearMedias();
                        dialog.getOutgoingSdpMessage().addMedias(commonMedias);
                        
                        try {
                            //send UPDATE
                            inviteService.update(dialog);
                            
                        } catch (DialogStateException e) {
                            e.printStackTrace();
                            assert false : "exception during update " + dialog + " " + e.toString();
                        }
                    }
                }
            }
        }
        */

        @Override
        public void onSessionEventBefore(final DialogStateEvent<BaseSipMessage> event) {
            Log.i(TAG, "onSessionEventBefore#started event = " + event + ", state = " + state);
            switch (state.get()) {
                case STATE_NEGOTIATING: {
                    switch (event.getSessionState()) {
                        case SESSION_NEGOTIATING:
                            //nothing
                            break;
                        case SESSION_ALERTING:
                            //nothing
                            break;
                        case SESSION_STARTED:
                            setState(StateCode.STATE_ESTABLISHED);
                            break;
                        case SESSION_START_FAILED:
                            setState(StateCode.STATE_TERMINATED);
                            break;
                        default:
                            printUnhandledState(state.get(), event.getSessionState());
                            break;
                    }
                    break;
                }
                case STATE_ESTABLISHED: {
                    switch (event.getSessionState()) {
                        case SESSION_TERMINATED:
                            setState(StateCode.STATE_TERMINATED);
                            break;
                        case SESSION_UPDATE_RECEIVED:
                            setState(StateCode.STATE_RENEGOTIATING);
                            break;
                        default:
                            printUnhandledState(state.get(), event.getSessionState());
                            break;
                    }
                    break;
                }
                case STATE_RENEGOTIATING: {
                    switch (event.getSessionState()) {
                        case SESSION_UPDATED:
                        case SESSION_UPDATE_FAILED:
                            setState(StateCode.STATE_ESTABLISHED);
                            break;
                        case SESSION_TERMINATED:
                            setState(StateCode.STATE_TERMINATED);
                            break;
                        case SESSION_ALERTING:
                            //nothing
                            break;
                        default:
                            printUnhandledState(state.get(), event.getSessionState());
                            break;
                    }
                    break;
                }
                case STATE_TERMINATING: {
                    switch (event.getSessionState()) {
                        case SESSION_TERMINATED:
                            setState(StateCode.STATE_TERMINATED);
                            break;
                        default:
                            printUnhandledState(state.get(), event.getSessionState());
                            break;
                    }
                    break;
                }
                case STATE_ESTABLISHING: {
                    switch (event.getSessionState()) {
                        case SESSION_STARTED:
                            setState(StateCode.STATE_ESTABLISHED);
                            break;
                        case SESSION_START_FAILED:
                            setState(StateCode.STATE_TERMINATED);
                            break;
                        default:
                            printUnhandledState(state.get(), event.getSessionState());
                            break;
                    }
                    break;
                }
                case STATE_REESTABLISHING: {
                    switch (event.getSessionState()) {
                        case SESSION_UPDATED:
                            setState(StateCode.STATE_ESTABLISHED);
                            break;
                        case SESSION_TERMINATED:
                            setState(StateCode.STATE_TERMINATED);
                            break;
                        default:
                            printUnhandledState(state.get(), event.getSessionState());
                            break;
                    }
                    break;
                }
                default:
                    break;
            }

            notifyListeners(event.getSessionState());

            Log.i(TAG, "onSessionEventBefore#finished");
        }
    };

    public SessionImpl(
            final Dialog dialog,
            final InviteService inviteService,
            final OptionsService optionsService,
            final SteppedAcceptable<Dialog> acceptable,
            final ReferService referService,
            final SessionOnCloseListener sessionOnCloseListener,
            final boolean useResourceReservation) {

        this.dialog = dialog;
        this.acceptable = acceptable;
        assert dialog != null;

        this.inviteService = inviteService;
        assert inviteService != null;

        assert optionsService != null;
        this.optionsService = optionsService;

        this.referService = referService;
        assert referService != null;

        this.sessionOnCloseListener = sessionOnCloseListener;

        this.sessionDescriptor = new SessionDescriptorImpl(dialog.getOutgoingSdpMessage(), dialog.getIncomingSdpMessage());
        this.serviceMethod = new SessionServiceMethodImpl(dialog.getRemoteParty(), dialog.getRemotePartyDisplayName(), dialog.getMessageHistory(), state);
        this.useResourceReservation = useResourceReservation;
    }

    public void preaccept(List<IMedia> medias) throws RemoteException {
        Log.i(TAG, "preaccept#started state:" + state);
        switch (state.get()) {
            case STATE_NEGOTIATING: {
                dialog.getOutgoingSdpMessage().addMedias(SdpBuilder.buildMedias(medias));
                doPreaccept(dialog);
                break;
            }
            default:
                Log.i(TAG, "accept#Not handled for state: " + state);
                break;
        }
        Log.i(TAG, "preaccept#finished");
    }

    private void doPreaccept(Dialog dialog) {
        acceptable.preAccept();
    }

    public void accept(List<IMedia> medias) throws RemoteException {
        Log.i(TAG, "accept#started state:" + state);
        switch (state.get()) {
            case STATE_NEGOTIATING: {
                setState(StateCode.STATE_ESTABLISHING);
                dialog.getOutgoingSdpMessage().getMedias().clear();
                dialog.getOutgoingSdpMessage().addMedias(SdpBuilder.buildMedias(medias));
                doAccept(dialog);
                break;
            }
            case STATE_RENEGOTIATING: {
                setState(StateCode.STATE_REESTABLISHING);
                dialog.getOutgoingSdpMessage().getMedias().clear();
                dialog.getOutgoingSdpMessage().addMedias(SdpBuilder.buildMedias(medias));
                doAccept(dialog);
                break;
            }
            default:
                Log.i(TAG, "accept#Not handled for state: " + state);
                break;
        }
        Log.i(TAG, "accept#finished");
    }

    public void addListener(ISessionListener listener)
            throws RemoteException {
        if (listener != null) {
            listeners.register(listener);
        }
    }

    public List<IMedia> getMedias() throws RemoteException {
        Log.i(TAG, "getMedias#started");
        final List<IMedia> medias = new ArrayList<IMedia>();
        if (dialog.getIncomingSdpMessage() != null) {
            medias.addAll(SdpConvertor.buildMedias(dialog.getIncomingSdpMessage()));
        }
        Log.i(TAG, "getMedias#finished medias:" + medias);
        return medias;
    }

    public IServiceMethod getServiceMethod() throws RemoteException {
        return serviceMethod;
    }

    public int getState() throws RemoteException {
        return state.get().code;
    }

    public void reject(int statusCode) throws RemoteException {
        Log.i(TAG, "reject#started");
        switch (state.get()) {
            case STATE_NEGOTIATING: {
                doReject(dialog, statusCode, null);
                setState(StateCode.STATE_TERMINATED);
                break;
            }
            case STATE_RENEGOTIATING: {
                //TODO: we use different status code than passed to the method. API is not consistent. Refactoring needed.  
                statusCode = StatusCode.NOT_ACCEPTABLE_HERE;
                doReject(dialog, statusCode, null);
                setState(StateCode.STATE_ESTABLISHED);
                break;
            }
            default:
                Log.i(TAG, "reject#Not handled for state: " + state);
                break;
        }
        Log.i(TAG, "reject#finished");
    }

    public void rejectWithDiversion(String alternativeUserAddress)
            throws RemoteException {
        doReject(dialog, CODE_MOVED_TEMPORARILY, alternativeUserAddress);

    }

    public void removeListener(ISessionListener listener)
            throws RemoteException {
        if (listener != null) {
            boolean unregistered = listeners.unregister(listener);
            assert unregistered == true;
        }
    }

    public void start(List<IMedia> medias) throws RemoteException {
        Log.i(TAG, "start#started");
        setState(StateCode.STATE_NEGOTIATING);

        dialog.getOutgoingSdpMessage().addMedias(SdpBuilder.buildMedias(medias));
        try {
            inviteService.addDialogStateListener(dialog, dialogStateListener);
            inviteService.invite(dialog);
        }
        catch (DialogStateException e) {
            e.printStackTrace();
            assert false : "exception during invite " + dialog + " " + e.toString();
        }
        Log.i(TAG, "start#finished");
    }

    void startByRemoteParty() {
        Log.i(TAG, "startByRemoteParty#started");
        setState(StateCode.STATE_NEGOTIATING);

        inviteService.addDialogStateListener(dialog, dialogStateListener);
        Log.i(TAG, "startByRemoteParty#finished");
    }

    void referenceRecieved(/*final Dialog dialog, */final Acceptable<Refer> acceptable, final Refer refer) {
        Log.i(TAG, "referenceRecieved#started");

        ReferenceImpl reference = ReferenceImpl.createOnServerSide(/*dialog, */referService, refer, acceptable);
        notifyReferenceReceived(reference);

        Log.i(TAG, "referenceRecieved#finished");
    }

    private void printUnhandledState(StateCode state, SessionState dialogState) {
        Log.e(TAG, String.format("Invalid state: state = %s, DIALOG state = %s", state, dialogState));
    }

    private void setState(StateCode state) {
        Log.i(TAG, "setState#started =================== state: " + state);
        this.state.set(state);
        if (state == StateCode.STATE_TERMINATED) {
            close();
        }
        Log.i(TAG, "setState#finished");
    }

    private void close() {
        Log.i(TAG, "close#started");
        inviteService.removeDialogStateListener(dialogStateListener);

        sessionOnCloseListener.onClose(dialog.getCallId());
        Log.i(TAG, "close#finished");
    }

    private void notifyReferenceReceived(ReferenceImpl reference) {
        Log.i(TAG, "notifyReferenceReceived#started");
        final int N = listeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            ISessionListener listener = listeners.getBroadcastItem(i);
            try {
                Log.i(TAG, "notifyReferenceReceived# listener:" + listener.toString());
                listener.sessionReferenceReceived(this, reference);
            }
            catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        listeners.finishBroadcast();
        Log.i(TAG, "notifyReferenceReceived#finished");
    }

    private void notifyListeners(final SessionState state) {
        Log.i(TAG, "notifyListeners#started state:" + state);
        final int N = listeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            ISessionListener listener = listeners.getBroadcastItem(i);
            try {
                switch (state) {
                    case SESSION_ALERTING:
                        listener.sessionAlerting(this);
                        break;
                    case SESSION_STARTED:
                        listener.sessionStarted(this);
                        break;
                    case SESSION_START_FAILED:
                        Log.i(TAG, "SESSION_START_FAILED at service side");
                        listener.sessionStartFailed(this);
                        break;
                    case SESSION_TERMINATED:
                        Log.i(TAG, "SESSION_TERMINATED at service side");
                        listener.sessionTerminated(this);
                        break;
                    case SESSION_UPDATE_FAILED:
                        listener.sessionUpdateFailed(this);
                        break;
                    case SESSION_UPDATE_RECEIVED:
                   		listener.sessionUpdateReceived(this);	
                        break;
                    case SESSION_UPDATED:
                        listener.sessionUpdated(this);
                        break;
                    default:
                        Log.e(TAG, "Unknown state: " + state);
                        break;
                }
            }
            catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        listeners.finishBroadcast();
        Log.i(TAG, "notifyListeners#finished");
    }


    public void terminate() throws RemoteException {
        switch (state.get()) {
            case STATE_INITIATED:
                //will transit directly to STATE_TERMINATED, the sessionTerminated callback will not be invoked
                setState(StateCode.STATE_TERMINATED);
                break;
            case STATE_NEGOTIATING:
                setState(StateCode.STATE_TERMINATING);
                doCancel(dialog);
                break;
            case STATE_ESTABLISHED:
                setState(StateCode.STATE_TERMINATING);
                doBye(dialog);
                break;
            case STATE_RENEGOTIATING:
                setState(StateCode.STATE_TERMINATING);
                //TODO send cancel
                break;
            case STATE_ESTABLISHING:
                setState(StateCode.STATE_TERMINATING);
                doCancel(dialog);
                break;
            case STATE_REESTABLISHING:
                setState(StateCode.STATE_TERMINATING);
                //TODO send cancel
                break;
            case STATE_TERMINATING:
                //will not do anything
                break;
            case STATE_TERMINATED:
                //will not do anything
                break;
            default:
                Log.i(TAG, "terminate#Not handled for state: " + state);
                break;
        }
    }

    public void update(List<IMedia> medias) throws RemoteException {
        Log.i(TAG, "update#started state=" + state);
        switch (state.get()) {
            case STATE_ESTABLISHED:
                setState(StateCode.STATE_RENEGOTIATING);
                dialog.getOutgoingSdpMessage().getMedias().clear();
                dialog.getOutgoingSdpMessage().addMedias(SdpBuilder.buildMedias(medias));
                try {
                    inviteService.reInvite(dialog);
                }
                catch (DialogStateException e) {
                    e.printStackTrace();
                    assert false : "exception during reinvite " + dialog + " " + e.toString();
                }
                break;
            case STATE_NEGOTIATING:

                Request prackReq = dialog.getMessageHistory().findLastRequestByMethod(MessageType.SIP_PRACK);
                Response prackRes = dialog.getMessageHistory().findLastResponseByMethod(MessageType.SIP_PRACK);

                //send UPDATE only if there was SPD offer and answer and PRACK sent
                if (!dialog.getOutgoingSdpMessage().getMedias().isEmpty() && dialog.getIncomingSdpMessage() != null
                        && (prackReq != null || prackRes != null)
                        ) {

                    dialog.getOutgoingSdpMessage().getMedias().clear();
                    dialog.getOutgoingSdpMessage().addMedias(SdpBuilder.buildMedias(medias));
                    try {
                        inviteService.update(dialog);
                    }
                    catch (DialogStateException e) {
                        e.printStackTrace();
                        assert false : "exception during update " + dialog + " " + e.toString();
                    }
                }
                break;
            default:
                Log.i(TAG, "update#Not handled for state: " + state);
                break;
        }
        Log.i(TAG, "update#finished");
    }

    public ISessionDescriptor getSessionDescriptor() {
        return sessionDescriptor;
    }

    private void doCancel(Dialog dialog) {
        inviteService.cancel(dialog);
    }

    private void doBye(Dialog dialog) {
        inviteService.bye(dialog);
    }

    private void doReject(Dialog dialog,
                          int statusCode, String alternativeUserAddress) {
        acceptable.reject(dialog, statusCode, alternativeUserAddress);
    }

    private void doAccept(Dialog dialog) {
        acceptable.accept(dialog);
    }

    Dialog getDialog() {
        return dialog;
    }

    public IReference createReference(String referToUserId, String method) throws RemoteException {
        return ReferenceImpl.createOnClientSide(
                /*dialog,*/
                referService,
                new ReferImpl(
                        dialog,
                        referToUserId,
                        method,
                        new DefaultTimeoutUnit(60, TimeUnit.SECONDS)
                )
        );
    }

    public boolean useResourceReservation() {
        return useResourceReservation;
    }

    public ICapabilities createCapabilities() throws RemoteException {
        return new CapabilitiesImpl(optionsService, dialog);
    }

    public void setAcceptable(SteppedAcceptable<Dialog> acceptable) {
        this.acceptable = acceptable;
    }

}
