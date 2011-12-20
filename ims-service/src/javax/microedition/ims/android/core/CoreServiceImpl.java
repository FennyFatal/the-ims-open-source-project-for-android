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
import android.text.TextUtils;
import android.util.Log;

import javax.microedition.ims.android.IError;
import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.android.util.ListenerHolder;
import javax.microedition.ims.android.util.RemoteListenerHolder;
import javax.microedition.ims.common.*;
import javax.microedition.ims.common.util.SIPUtil;
import javax.microedition.ims.config.UserInfo;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.ClientIdentityImpl;
import javax.microedition.ims.core.IMSStack;
import javax.microedition.ims.core.dialog.*;
import javax.microedition.ims.core.sipservice.refer.ReferImpl;
import javax.microedition.ims.core.sipservice.subscribe.listener.NotifyEvent;
import javax.microedition.ims.messages.history.BodyPartData;
import javax.microedition.ims.messages.parser.message.SipUriParser;
import javax.microedition.ims.messages.wrappers.sip.Header;
import javax.microedition.ims.util.MessageUtilHolder;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * CoreService implementation.
 *
 * @author ext-akhomush
 * @see ICoreService.aidl
 */
public class CoreServiceImpl extends ICoreService.Stub implements
        IncomingOperationListener, IncomingReferListener,
        IncomingNotifyListener {
    private static final String TAG = "CoreServiceImpl";

    private final ClientIdentity callingParty;
    private final IMSStack<IMSMessage> imsStack;
    private final boolean forceSrtp;
    private final DtmfPayloadType dtmfPayload;

    private Map<String, SessionImpl> sessionsMap = Collections
            .synchronizedMap(new HashMap<String, SessionImpl>());

    private final ListenerHolder<ICoreServiceListener> listenerHolder = new RemoteListenerHolder<ICoreServiceListener>(
            ICoreServiceListener.class);

    private final List<CoreServiceStateListener> coreServiceStateListeners = new ArrayList<CoreServiceStateListener>();

    public interface SessionOnCloseListener {
        void onClose(String dialogCallId);
    }

    public interface CoreServiceStateListener {
        void onCoreServiceClosed();
    }

    public CoreServiceImpl(final ClientIdentity callingParty,
                           final IMSStack<IMSMessage> imsStack,
                           final boolean forceSrtp) {
        assert callingParty != null;
        assert imsStack != null;

        this.callingParty = callingParty;
        this.imsStack = imsStack;
        this.forceSrtp = forceSrtp;

        // DialogStateMediator.INSTANCE.registerCoreServiceListener(callingParty,
        // this);
        imsStack.getInviteService().addIncomingCallListener(callingParty, SDPType.VOIP,
                voipIncomimgCallListener);
        imsStack.getReferService().addIncomingReferListener(this);
        imsStack.getReferService().addIncomingNotifyListener(this);
        imsStack.getPageMessageService().addIncomingOperationListener(callingParty, this);
        imsStack.getContext().getStackClientRegistry().addStackClient(
                callingParty);
        this.dtmfPayload = imsStack.getContext().getConfig().getDtmfPayload();
    }

    private IncomingCallListener voipIncomimgCallListener = new IncomingCallListener() {

        public void onIncomingCall(IncomingOperationEvent event) {
            Log.i(TAG, "IncomingCallListener.onIncomingCall#started");
            SessionImpl session = new SessionImpl(event.getDialog(),
                    imsStack.getInviteService(),
                    imsStack.getOptionsService(),
                    event.getAcceptable(),
                    imsStack.getReferService(),
                    getSessionOnCloseListener(),
                    imsStack.getContext().getConfig().useResourceReservation());

            sessionsMap.put(event.getDialog().getCallId(), session);
            session.startByRemoteParty();
            notifySessionInvitationReceived(session);
            Log.i(TAG, "IncomingCallListener.onIncomingCall#finished");
        }

        private void notifySessionInvitationReceived(final ISession session) {
            try {
                listenerHolder.getNotifier().sessionInvitationReceived(
                        CoreServiceImpl.this, session);
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            // notifyInvitationReceived(SESSION);
        }


        public void onIncomingMediaUpdate(IncomingOperationEvent event) {
            Log.i(TAG, "IncomingCallListener.onIncomingMediaUpdate#started");
            SessionImpl session = sessionsMap.get(event.getDialog().getCallId());
            session.setAcceptable(event.getAcceptable());
            Log.i(TAG, "IncomingCallListener.onIncomingMediaUpdate#finished");
        }
    };


    @Override
    public String getSheme() throws RemoteException {
        return callingParty.getSchema();
    }

    /**
     * Returns the display name and public USER callingParty for the
     * CoreService.
     */

    @Override
    public String getLocalUserId() throws RemoteException {
        return getLocalUser();
    }

    private String getLocalUser() {
        return callingParty.getUserInfo().toUri();
    }

    @Override
    public String getAppId() throws RemoteException {
        return callingParty.getAppID();
    }


    @Override
    public ISession createSession(final String from, final String toUser,
                                  final IExceptionHolder exceptionHolder) throws RemoteException {
        ISession session = null;

        assert callingParty != null;
        assert toUser != null;

        try {
            session = createSessionInternally(from, toUser);
        } catch (IllegalArgumentException e) {
            exceptionHolder.setParcelableException(new IError(
                    IError.ERROR_WRONG_PARAMETERS, e.getMessage() == null ? "Wring input parameters" : e.getMessage()));
            Log.e(TAG, e.getMessage(), e);
        }

        return session;
    }

    private ISession createSessionInternally(final String fromUser,
                                            final String toUser) {
        ISession iSession = null;

        if(!TextUtils.isEmpty(fromUser) && SipUriParser.parseUri(fromUser) == null) {
            throw new IllegalArgumentException(
                    String.format("Wrong fromUser = '%s' parameter", fromUser)
            );
        }

        if (!MessageUtilHolder.isValidUri(imsStack.getContext().getConfig(), toUser)) {
            throw new IllegalArgumentException(
                    String.format("Wrong toUser = '%s' parameter", toUser)
            );
        }

        ClientIdentity localParty = TextUtils.isEmpty(fromUser)? callingParty: createIdentity(fromUser);

        //Logger.log(Logger.Tag.COMMON, "createSessionInternally from: " + fromUser);
        //Logger.log(Logger.Tag.COMMON, "createSessionInternally to: " + toUser);
        //Logger.log(Logger.Tag.COMMON, "createSessionInternally callingParty: " + callingParty);
        //Logger.log(Logger.Tag.COMMON, "createSessionInternally localParty: " + localParty);

        Logger.log(Logger.Tag.COMMON, String.format("createDialog#localParty = %s, remoteParty = %s", localParty, toUser));

        final Dialog dialog = imsStack.getContext().getDialogStorage().getDialog(
                localParty,
                toUser,
                new DialogCallIDImpl(SIPUtil.newCallId())
        );

        assert dialog != null;
        iSession = new
                SessionImpl(dialog,
                imsStack.getInviteService(),
                imsStack.getOptionsService(),
                null,
                imsStack.getReferService(),
                getSessionOnCloseListener(),
                imsStack.getContext().getConfig().useResourceReservation());

        sessionsMap.put(dialog.getCallId(), (SessionImpl) iSession);

        return iSession;
    }

    private ClientIdentity createIdentity(final String sipName) {
        final ClientIdentity clientIdentity;

        final UserInfo userInfo = UserInfo.valueOf(sipName);
        clientIdentity = ClientIdentityImpl.Creator.createFromUserInfo(callingParty.getAppID(), userInfo);

        return clientIdentity;
    }

    @Override
    public void close() throws RemoteException {
        ((Shutdownable) listenerHolder).shutdown();
        // DialogStateMediator.INSTANCE.untegisterCoreServiceListener(callingParty);
        imsStack.getInviteService().removeIncomingCallListener(
                voipIncomimgCallListener);

        imsStack.getReferService().removeIncomingReferListener(this);

        sessionsMap.clear();

        imsStack.getPageMessageService().removeIncomingOperationListener(this);
        imsStack.getContext().getStackClientRegistry().removeStackClient(
                callingParty);

        notifyServiceClosed();
    }

    @Override
    public void addListener(ICoreServiceListener listener)
            throws RemoteException {
        listenerHolder.addListener(listener);
    }

    @Override
    public void removeListener(ICoreServiceListener listener)
            throws RemoteException {
        listenerHolder.removeListener(listener);
    }

    @Override
    public void referenceReceived(final IncomingReferEvent event) {
        // TODO redirect to active SESSION if need

        Logger.log("XXX",
                "CoreServiceImpl.referenceRecieved() : START : referToUserId : "
                        + event.getRefer());

        SessionImpl session = sessionsMap.get(event.getRefer().getDialog()
                .getCallId());
        if (session != null) {
            session.referenceRecieved(/*event.getRefer().getDialog(), */event
                    .getAcceptable(), event.getRefer());

        } else {
            ReferenceImpl reference = ReferenceImpl.createOnServerSide(/*event
                    .getRefer().getDialog(), */imsStack.getReferService(), event
                    .getRefer(), event.getAcceptable());

            reference.startByRemoteParty();
            referenceRecieved(reference);
        }

        Logger.log("XXX",
                "CoreServiceImpl.referenceRecieved() : STOP : referToUserId : "
                        + event.getRefer());
    }

    private void referenceRecieved(final IReference reference) {
        try {
            listenerHolder.getNotifier().referenceReceived(this, reference);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        // notifyReferenceReceived(reference);
    }

    @Override
    public void notificationReceived(NotifyEvent event) {
        // Every Reference is subscribed on this message, so nothing todo here
    }

    @Override
    public void pageMessageRecieved(final IncomingPageEvent event) {
        Log.i(TAG, "pageMessageRecieved#");

        final Dialog dialog = event.getDialog();
        PageMessageImpl pageMessage = new PageMessageImpl(dialog, imsStack
                .getPageMessageService());

        final ServiceMethodImpl serviceMethod = pageMessage
                .getServiceMethodInternally();
        MessageImpl message = serviceMethod
                .getPreviousRequestInternally(MessageImpl.MethodId.PAGEMESSAGE_SEND);
        BodyPartData bodyPart = message.getBodyPartInternally(0);

        if (bodyPart != null) {
            final byte[] content = bodyPart.getContent();
            final String contentType = bodyPart.getHeader(Header.Content_Type
                    .stringValue());

            pageMessageRecieved(pageMessage, content, contentType);

        } else {
            Log
                    .e(TAG,
                            "Page message cann't be created, message without body part");
        }
    }

    private void pageMessageRecieved(final IPageMessage pageMessage,
                                     final byte[] content, final String contentType) {
        Log.d(TAG, String.format(
                "pageMessageRecieved#contentType = %s, content = %s",
                contentType, new String(content)));
        try {
            listenerHolder.getNotifier().pageMessageReceived(this, pageMessage,
                    content, contentType);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        // notifyPageMessageReceived(pageMessage, content, contentType);
    }

    /*
    * private void notifyPageMessageReceived(final IPageMessage iPageMessage,
    * final byte[] content, final String contentType) { Log.d(TAG,
    * "notifyPageMessageReceived#starting"); final int N =
    * listeners.beginBroadcast(); for (int i = 0; i < N; i++) {
    * ICoreServiceListener coreServiceListener = listeners.getBroadcastItem(i);
    * try { Log.i(TAG,coreServiceListener.toString());
    * coreServiceListener.pageMessageReceived(this, iPageMessage, content,
    * contentType); } catch (RemoteException e) { Log.e(TAG, e.getMessage(),
    * e); } } listeners.finishBroadcast(); Log.d(TAG,
    * "notifyPageMessageReceived#finished"); }
    */

    /*
     * private void notifyInvitationReceived(ISession SESSION) { Log.d(TAG,
     * "notifyListeners#starting"); final int N = listeners.beginBroadcast();
     * for (int i = 0; i < N; i++) { ICoreServiceListener coreServiceListener =
     * listeners.getBroadcastItem(i); try {
     * Log.i(TAG,coreServiceListener.toString());
     * coreServiceListener.sessionInvitationReceived(this, SESSION); } catch
     * (RemoteException e) { Log.e(TAG, e.getMessage(), e); } }
     * listeners.finishBroadcast(); Log.d(TAG, "notifyListeners#finished"); }
     */
    /*
     * private void notifyReferenceReceived(IReference reference) { Log.d(TAG,
     * "notifyListeners#starting"); final int N = listeners.beginBroadcast();
     * for (int i = 0; i < N; i++) { ICoreServiceListener coreServiceListener =
     * listeners.getBroadcastItem(i); try {
     * Log.i(TAG,coreServiceListener.toString());
     * coreServiceListener.referenceReceived(this, reference); } catch
     * (RemoteException e) { Log.e(TAG, e.getMessage(), e); } }
     * listeners.finishBroadcast(); Log.d(TAG, "notifyListeners#finished"); }
     */

    @Override
    public IReference createReference(String fromUserId, String toUserId,
                                      String referToUserId, String method,
                                      IExceptionHolder exceptionHolder) throws RemoteException {
        IReference reference = null;

        try {
//            if (fromUserId != null) {
//                if (SipUriParser.parseUri(fromUserId) == null) {
//                    throw new IllegalArgumentException(String.format(
//                            "Wrong fromUserId = '%s' parameter", fromUserId));
//                }
//            }
            if (toUserId != null && !MessageUtilHolder.isValidUri(imsStack.getContext().getConfig(), toUserId)) {
                throw new IllegalArgumentException(String.format(
                        "Wrong toUserId = '%s' parameter", toUserId));
            }
            if (method != null && method.compareToIgnoreCase("INVITE") == 0) {
                if (referToUserId == null
                        || !MessageUtilHolder.isValidUri(imsStack.getContext().getConfig(), referToUserId)) {

                    throw new IllegalArgumentException(
                            String
                                    .format(
                                            "method parameter is INVITE. But Wrong referToUserId = '%s' parameter",
                                            referToUserId));
                }
            }

            Dialog dialog = imsStack.getContext().getDialogStorage().getDialog(
                    callingParty, toUserId,
                    new DialogCallIDImpl(SIPUtil.newCallId()));
            reference = ReferenceImpl.createOnClientSide(/*dialog, */imsStack
                    .getReferService(), new ReferImpl(dialog, referToUserId,
                    method, new DefaultTimeoutUnit(60, TimeUnit.SECONDS)));

        } catch (IllegalArgumentException e) {
            exceptionHolder.setParcelableException(new IError(
                    IError.ERROR_WRONG_PARAMETERS, e.getMessage()));
            Log.e(TAG, e.getMessage(), e);
        }

        return reference;
    }

    @Override
    public ISubscription createSubscription(String fromUser, String remoteParty,
                                            String event, IExceptionHolder exceptionHolder)
            throws RemoteException {
        ISubscription subscription = null;

        try {
            if (!TextUtils.isEmpty(fromUser) && SipUriParser.parseUri(fromUser) == null) {
                    throw new IllegalArgumentException(String.format(
                            "Wrong from = '%s' parameter", fromUser));
            }

            if (!MessageUtilHolder.isValidUri(imsStack.getContext().getConfig(), remoteParty)) {
                throw new IllegalArgumentException(String.format(
                        "Wrong to = '%s' parameter", remoteParty));
            }

            if (event == null) {
                throw new IllegalArgumentException(String.format(
                        "Wrong event = '%s' parameter", event));
            }

            EventPackage packageEvent = EventPackage.parse(event);

            if (packageEvent == null) {
                throw new IllegalArgumentException(String.format(
                        "Wrong event = '%s' parameter", event));
            }

/*            Dialog dialog = imsStack.getContext().getDialogStorage()
                    .getDialog(callingParty, remoteParty,
                            new DialogCallIDImpl(SIPUtil.newCallId()));
*/

            ClientIdentity localParty = TextUtils.isEmpty(fromUser)? callingParty: createIdentity(fromUser);

            Logger.log(Logger.Tag.COMMON, String.format("createSubscription#localParty = %s, remoteParty = %s", localParty, remoteParty));

            subscription = new SubscriptionImpl(
                    localParty,
                    remoteParty,
                    packageEvent,
                    imsStack.getSubscribeService()
            );

        } catch (IllegalArgumentException e) {
            exceptionHolder.setParcelableException(new IError(
                    IError.ERROR_WRONG_PARAMETERS, e.getMessage()));
            Log.e(TAG, e.getMessage(), e);
        }

        return subscription;
    }

    @Override
    public IPublication createPublication(String from, String to, String event,
                                          IExceptionHolder exceptionHolder) throws RemoteException {
        IPublication publication = null;

        try {
            if (from != null && !MessageUtilHolder.isValidUri(imsStack.getContext().getConfig(), from)) {
                throw new IllegalArgumentException(String.format(
                        "Wrong from = '%s' parameter", from));
            }
            if (to != null && !MessageUtilHolder.isValidUri(imsStack.getContext().getConfig(), to)) {
                throw new IllegalArgumentException(String.format(
                        "Wrong to = '%s' parameter", to));
            }
            if (event == null) {
                throw new IllegalArgumentException(String.format(
                        "Wrong event = '%s' parameter", event));
            }
            EventPackage packageEvent = EventPackage.parse(event);
            if (packageEvent == null) {
                throw new IllegalArgumentException(String.format(
                        "Wrong event = '%s' parameter", event));
            }

            Dialog dialog = imsStack.getContext().getDialogStorage()
                    .getDialog(callingParty, to,
                            new DialogCallIDImpl(SIPUtil.newCallId()));

            publication = new PublicationImpl(dialog, packageEvent, imsStack
                    .getPublishService());

        } catch (IllegalArgumentException e) {
            exceptionHolder.setParcelableException(new IError(
                    IError.ERROR_WRONG_PARAMETERS, e.getMessage()));
            Log.e(TAG, e.getMessage(), e);
        }

        return publication;
    }

    private SessionOnCloseListener getSessionOnCloseListener() {
        return new SessionOnCloseListener() {

            public void onClose(String dialogCallId) {
                sessionsMap.remove(dialogCallId);
            }
        };
    }

    @Override
    public IPageMessage createPageMessage(final String from, final String to,
                                          final IExceptionHolder exceptionHolder) throws RemoteException {
        Log.i(TAG, String.format("createPageMessage#from = '%s', to = '%s'",
                from, to));

        IPageMessage pageMessage = null;
        try {
            pageMessage = createPageMessageInternally(from, to);
        } catch (IllegalArgumentException e) {
            exceptionHolder.setParcelableException(new IError(
                    IError.ERROR_WRONG_PARAMETERS, e.getMessage()));
            Log.e(TAG, String.format(
                    "createPageMessage#from = '%s', to = '%s'", from, to));
        }

        return pageMessage;
    }

    private IPageMessage createPageMessageInternally(final String from,
                                                     final String to) {

        //if (from != null && SipUriParser.parseUri(to) == null) {
        if (from != null && !MessageUtilHolder.isValidUri(imsStack.getContext().getConfig(), from)) {
            throw new IllegalArgumentException(String.format(
                    "Wrong from = '%s' parameter", from));
        }

        IPageMessage pageMessage = null;

        // TODO from should be used
        // String fromUser = from != null ? from : getLocalUser();
        Dialog dialog = imsStack.getContext().getDialogStorage().getDialog(
                callingParty, to, new DialogCallIDImpl(SIPUtil.newCallId()));
        pageMessage = new PageMessageImpl(dialog, imsStack
                .getPageMessageService());

        return pageMessage;
    }

    @Override
    public ICapabilities createCapabilities(String from, String to,
                                            IExceptionHolder exceptionHolder) throws RemoteException {
        Log.i(TAG, String.format("createCapabilities#from = '%s', to = '%s'",
                from, to));

        ICapabilities capabilities = null;
        try {
            capabilities = createCapabilitiesInternally(from, to);
        } catch (IllegalArgumentException e) {
            exceptionHolder.setParcelableException(new IError(
                    IError.ERROR_WRONG_PARAMETERS, e.getMessage()));
            Log.e(TAG, e.getMessage(), e);
        }

        return capabilities;
    }

    private ICapabilities createCapabilitiesInternally(final String from,
                                                       final String to) {
        ICapabilities capabilities;

        //if (from != null && SipUriParser.parseUri(from) == null) {
        if (from != null && !MessageUtilHolder.isValidUri(imsStack.getContext().getConfig(), from)) {
            throw new IllegalArgumentException(String.format(
                    "Wrong from = '%s' parameter", from));
        }

        if (to != null && !MessageUtilHolder.isValidUri(imsStack.getContext().getConfig(), to)) {
            throw new IllegalArgumentException(String.format(
                    "Wrong to = '%s' parameter", to));
        }

        final ClientIdentity fromUser;
        if (from != null) {
            fromUser = ClientIdentityImpl.Creator.createFromUriAndUser(from, callingParty.getUserInfo());
        } else {
            fromUser = callingParty;
        }

        Dialog dialog = imsStack.getContext().getDialogStorage().getDialog(
                fromUser, to, new DialogCallIDImpl(SIPUtil.newCallId()));
        assert dialog != null;

        capabilities = new CapabilitiesImpl(imsStack.getOptionsService(),
                dialog);

        return capabilities;
    }

    private void notifyServiceClosed() {
        for (CoreServiceStateListener listener : coreServiceStateListeners) {
            listener.onCoreServiceClosed();
        }
    }

    public void addCoreServiceStateListener(CoreServiceStateListener listener) {
        coreServiceStateListeners.add(listener);
    }

    public void removeCoreServiceStateListener(CoreServiceStateListener listener) {
        coreServiceStateListeners.remove(listener);
    }

    public boolean isForceSrtp() {
        return forceSrtp;
    }

    public String getDtmfPayload() {
        return dtmfPayload.getValue();
    }
}
