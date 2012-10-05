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

package javax.microedition.ims.android.msrp;

import android.os.RemoteException;
import android.util.Log;

import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.util.RemoteListenerHolder;
import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.util.SIPUtil;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.IMSStack;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.DialogCallIDImpl;
import javax.microedition.ims.core.msrp.MSRPService;
import javax.microedition.ims.core.msrp.MSRPSession;
import javax.microedition.ims.core.msrp.MSRPSessionType;
import javax.microedition.ims.core.msrp.listener.IncomingMSRPChatInviteEvent;
import javax.microedition.ims.core.msrp.listener.IncomingMSRPChatInviteListener;
import javax.microedition.ims.core.msrp.listener.MSRPSessionStartListener;
import javax.microedition.ims.core.msrp.listener.MSRPSessionStopListener;
import javax.microedition.ims.core.sipservice.Acceptable;
import java.util.HashMap;
import java.util.Map;

public class ConferenceManagerImpl extends IConferenceManager.Stub {
    private static final String TAG = "Service - ConferenceManagerImpl";

    private final RemoteListenerHolder<IConferenceManagerListener> listenerHolder =
            new RemoteListenerHolder<IConferenceManagerListener>(IConferenceManagerListener.class);

    private final Map<String, ChatInvitationImpl> chatInvitationsMap = new HashMap<String, ChatInvitationImpl>();
    private final Map<String, ConferenceInvitationImpl> conferenceInvitationsMap = new HashMap<String, ConferenceInvitationImpl>();

    private final Map<String, ChatImpl> chatMap = new HashMap<String, ChatImpl>();

    private class DefaultMSRPSessionStartListener implements MSRPSessionStartListener {

        private final MSRPSession msrpSession;

        public DefaultMSRPSessionStartListener(MSRPSession msrpSession) {
            Logger.log(TAG, "DefaultMSRPSessionStartListener()");
            this.msrpSession = msrpSession;
        }

        public void onMSRPSessionStarted() {
            Logger.log(TAG, "msrpSessionStartListener.onMSRPSessionStarted#started");
            notifyChatStarted(msrpSession);
            cleanUp();
            Logger.log(TAG, "msrpSessionStartListener.onMSRPSessionStarted#finished");
        }

        
        public void onMSRPSessionStartFailed(final int reasonType, final String reasonPhrase, final int statusCode) {
            Logger.log(TAG, "msrpSessionStartListener.onMSRPSessionStartFailed#started");
            notifyChatStartFailed(msrpSession.getIMSEntityId().stringValue(), reasonType, reasonPhrase, statusCode);
            cleanUp();
            Logger.log(TAG, "msrpSessionStartListener.onMSRPSessionStartFailed#finished");
        }

        private void cleanUp() {
            Logger.log(TAG, "DefaultMSRPSessionStartListener.cleanUp()");
            msrpSession.removeMSRPSessionStartListener(this);
        }
    }

    private class DefaultMSRPSessionStopListener implements MSRPSessionStopListener {

        private final MSRPSession msrpSession;

        private DefaultMSRPSessionStopListener(MSRPSession msrpSession) {
            this.msrpSession = msrpSession;
            Logger.log(TAG, "DefaultMSRPSessionStopListener()");
        }

        
        public void onMSRPSessionFinished() {
            Logger.log(TAG, "msrpSessionStopListener.onMSRPSessionFinished#started");

            String sessionId = msrpSession.getIMSEntityId().stringValue();

            if (chatMap.containsKey(sessionId)) {
                ChatImpl chatImpl = chatMap.get(sessionId);
                IMSessionImpl imSessionImpl = (IMSessionImpl) chatImpl.getIMSession();

                imSessionImpl.notifySessionClosed(new IReasonInfo("", -1, 0));
                chatMap.remove(sessionId);
            }
            cleanUp();

            Logger.log(TAG, "msrpSessionStopListener.onMSRPSessionFinished#finished");
        }

        private void cleanUp() {
            Logger.log(TAG, "DefaultMSRPSessionStopListener.cleanUp()");
            msrpSession.removeMSRPSessionStopListener(this);
        }
    }


    private IncomingMSRPChatInviteListener incomingMSRPInviteListener = new IncomingMSRPChatInviteListener() {
        
        public void onIncomingInvite(IncomingMSRPChatInviteEvent event) {
            Logger.log(TAG, "incomingMSRPInviteListener.onIncomingInvite#started");

            MSRPSession msrpSession = event.getMsrpSession();

            msrpSession.addMSRPSessionStartListener(new DefaultMSRPSessionStartListener(msrpSession));
            msrpSession.addMSRPSessionStopListener(new DefaultMSRPSessionStopListener(msrpSession));

            notifyChatInvitationReceived(event.getAcceptable(), msrpSession);

            Logger.log(TAG, "incomingMSRPInviteListener.onIncomingInvite#finished");
        }
    };


    private final IMSStack<IMSMessage> imsStack;

    private ClientIdentity callingParty;


    public ConferenceManagerImpl(final IMSStack<IMSMessage> imsStack,
                                 ClientIdentity callingParty) {
        this.imsStack = imsStack;
        this.callingParty = callingParty;

        final MSRPService msrpService = imsStack.getMSRPService();
        msrpService.addIncomingMSRPChatInviteListener(incomingMSRPInviteListener);
    }

    public String sendChatInvitation(String sender, String recipient, String subject) throws RemoteException {
        Logger.log(TAG, "sendChatInvitation#started");

        final MSRPService msrpService = imsStack.getMSRPService();
        final StackContext stackContext = imsStack.getContext();
        MSRPSession msrpSession;
        try {
            //Create DIALOG for negotiation MSRP medias
/*        	final ClientIdentity callingParty = ClientIdentityImpl.Creator.
        	createFromUserInfo(null, UserInfo.valueOf(sender));*/

            Dialog msrpDialog = stackContext.getDialogStorage().getDialog(
                    callingParty,
                    recipient,
                    new DialogCallIDImpl(SIPUtil.newCallId())
            );

            assert msrpDialog != null;

            //msrpDialog.setAuthorizationData(authRegisrty.getAuthorizationData());

            //Obtain MSRP SESSION for given DIALOG
            msrpSession = msrpService.obtainMSRPSession(msrpDialog, MSRPSessionType.CHAT);

            msrpSession.addMSRPSessionStartListener(new DefaultMSRPSessionStartListener(msrpSession));
            msrpSession.addMSRPSessionStopListener(new DefaultMSRPSessionStopListener(msrpSession));

            //start SESSION
            msrpSession.openChatSession();

            //sessionId = msrpService.openSession(sender, recipient, subject);
            Logger.log(TAG, "sendChatInvitation#finish - " + msrpSession.getIMSEntityId().stringValue());

        }
        catch (Throwable e) {
            msrpSession = null;
            
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }

        return msrpSession == null ? null : msrpSession.getIMSEntityId().stringValue();
    }

    
    public String sendConferenceInvitation(String sender, String[] recipients, String subject) throws RemoteException {

        // TODO Implement

        return null;
    }

    
    public String joinPredefinedConference(String sender, String conferenceURI) throws RemoteException {

        // TODO Implement

        return null;
    }

    
    public void cancelInvitation(String sessionId) throws RemoteException {
        Logger.log(TAG, "cancelInvitation#started");

        final MSRPSession msrpSession = imsStack.getMSRPService().findMSRPSession(sessionId);
        if (msrpSession != null) {
            msrpSession.cancelSessionOpening();
        }

        Logger.log(TAG, "cancelInvitation#finished");
    }

    
    public void addListener(IConferenceManagerListener listener)
            throws RemoteException {
        if (listener != null) {
            listenerHolder.addListener(listener);
        }
    }

    
    public void removeListener(IConferenceManagerListener listener)
            throws RemoteException {
        if (listener != null) {
            listenerHolder.removeListener(listener);
        }
    }

    private void notifyChatInvitationReceived(final Acceptable acceptable, final MSRPSession msrpSession) {
        Logger.log(TAG, "notifyChatInvitationReceived#started");

        String msrtSessioId = msrpSession.getIMSEntityId().stringValue();
        Dialog msrpDialog = msrpSession.getMsrpDialog();

        try {
            ChatInvitationImpl chatInvitationImpl = new ChatInvitationImpl(msrpDialog, acceptable,
                    msrtSessioId, msrpDialog.getRemoteParty(), null/*, chatAcceptRejectListener*/);

            chatInvitationsMap.put(msrtSessioId, chatInvitationImpl);

            Logger.log(TAG, "notifyChatInvitationReceived#before notifier");
            listenerHolder.getNotifier().chatInvitationReceived(chatInvitationImpl);
        }
        catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyChatInvitationReceived#finish");
    }

    private void notifyChatStarted(MSRPSession msrpSession) {
        Logger.log(TAG, "notifyChatStarted#started");
        try {
            ChatImpl chatImpl;
            Logger.log(TAG, "notifyChatStarted#1");

            String sessionId = msrpSession.getIMSEntityId().stringValue();
            if (chatMap.containsKey(sessionId)) {
                Logger.log(TAG, "notifyChatStarted#2");
                chatImpl = chatMap.get(sessionId);
            }
            else {
                Logger.log(TAG, "notifyChatStarted#3");
                IMSessionImpl imSessionImpl = new IMSessionImpl(msrpSession, imsStack.getMSRPService());

                chatImpl = new ChatImpl(imSessionImpl);

                chatMap.put(sessionId, chatImpl);
            }

            Logger.log(TAG, "notifyChatStarted#4");

            listenerHolder.getNotifier().chatStarted(chatImpl);
            Logger.log(TAG, "notifyChatStarted#5");
        }
        catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyChatStarted#finish");
    }

    private void notifyChatStartFailed(String sessionId, int reasonType, String reasonPhrase, int statusCode) {
        Logger.log(TAG, "notifyChatStartFailed#started");
        if (chatInvitationsMap.containsKey(sessionId)) {
            chatInvitationsMap.get(sessionId).expire();
        }

        try {
            IReasonInfo reasonInfoImpl = new IReasonInfo(reasonPhrase, reasonType, statusCode);

            listenerHolder.getNotifier().chatStartFailed(sessionId, reasonInfoImpl);
        }
        catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyChatStartFailed#finished");
    }

    /*
    * TODO call this method
    */
    private void notifyConferenceInvitationReceived(String sessionId) {
        Logger.log(TAG, "notifyConferenceInvitationReceived#started");
        try {
            ConferenceInvitationImpl conferenceInvitationImpl = new ConferenceInvitationImpl();

            conferenceInvitationsMap.put(sessionId, conferenceInvitationImpl);

            listenerHolder.getNotifier().conferenceInvitationReceived(conferenceInvitationImpl);
        }
        catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyConferenceInvitationReceived#finished");
    }

    /*
     * TODO call this method
     */
    private void notifyConferenceStarted(MSRPSession msrpSession) {
        Logger.log(TAG, "notifyConferenceStarted#started");
        try {
            IMSessionImpl imSessionImpl = new IMSessionImpl(msrpSession, imsStack.getMSRPService());

            ConferenceImpl conferenceImpl = new ConferenceImpl(imSessionImpl);

            listenerHolder.getNotifier().conferenceStarted(conferenceImpl);
        }
        catch (RemoteException e) {
            e.printStackTrace();
            Logger.log(TAG, e.getMessage());
        }
        Logger.log(TAG, "notifyConferenceStarted#finished");
    }

    /*
     * TODO call this method
     */
    private void notifyConferenceStartFailed(String sessionId, int reasonType, String reasonPhrase, int statusCode) {
        Logger.log(TAG, "notifyConferenceStartFailed#started");
        if (conferenceInvitationsMap.containsKey(sessionId)) {
            conferenceInvitationsMap.get(sessionId).expire();
        }

        try {
            IReasonInfo reasonInfoImpl = new IReasonInfo(reasonPhrase, reasonType, statusCode);

            listenerHolder.getNotifier().conferenceStartFailed(sessionId, reasonInfoImpl);
        }
        catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyConferenceStartFailed#finished");
    }

}
