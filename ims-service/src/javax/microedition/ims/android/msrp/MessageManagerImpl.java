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
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.util.SIPUtil;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.IMSStack;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.DialogCallIDImpl;
import javax.microedition.ims.core.msrp.MSRPService;
import javax.microedition.ims.core.msrp.MSRPSession;
import javax.microedition.ims.core.msrp.MSRPSessionType;
import javax.microedition.ims.core.msrp.listener.MSRPMessageSendingListener;
import javax.microedition.ims.core.msrp.listener.MSRPSessionStartListener;
import javax.microedition.ims.core.sipservice.invite.DialogStateException;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;

public class MessageManagerImpl extends IMessageManager.Stub {
    private static final String TAG = "Service - MessageManagerImpl";

    private final RemoteListenerHolder<IMessageManagerListener> listenerHolder = new RemoteListenerHolder<IMessageManagerListener>(IMessageManagerListener.class);

    private final MSRPService msrpService;
    private final IMSStack imsStack;

    public MessageManagerImpl(final IMSStack imsStack) {
        this.imsStack = imsStack;
        this.msrpService = imsStack.getMSRPService();
    }


    /*
    * Listener for starting sending of a large message
    */
    private class LargeMessageSendingRunner implements MSRPSessionStartListener {
        private MSRPSession msrpSession;
        private MsrpMessage msrpMessage;

        public LargeMessageSendingRunner(MSRPSession msrpSession, MsrpMessage msrpMessage) {
            this.msrpSession = msrpSession;
            this.msrpMessage = msrpMessage;
        }

        public void onMSRPSessionStarted() {
            Logger.log(TAG, "LargeMessageSendingRunner.onMSRPSessionStarted#started");

            msrpSession.sendMessage(msrpMessage);

            unSubscribe();
            Logger.log(TAG, "LargeMessageSendingRunner.onMSRPSessionStarted#finished");
        }

        public void onMSRPSessionStartFailed(int reasonType, String reasonPhrase, int statusCode) {
            Logger.log(TAG, "LargeMessageSendingRunner.onMSRPSessionStartFailed#started");
            IReasonInfo reasonInfoImpl = new IReasonInfo(reasonPhrase, reasonType, statusCode);
            notifyMessageSendFailed(msrpMessage.getMessageId(), reasonInfoImpl);

            unSubscribe();
            Logger.log(TAG, "LargeMessageSendingRunner.onMSRPSessionStartFailed#finished");
        }

        private void unSubscribe() {
            msrpSession.removeMSRPSessionStartListener(this);
        }

        public void subscribe() {
            msrpSession.addMSRPSessionStartListener(this);
        }
    }


    private class MSRPMessageSendingListenerImpl implements MSRPMessageSendingListener {
        private MSRPSession msrpSession;

        public MSRPMessageSendingListenerImpl(MSRPSession msrpSession) {
            this.msrpSession = msrpSession;
        }

        public void onMessageSent(String messageId) {
            Logger.log(TAG, "MSRPMessageSendingListenerImpl.onMessageSent#started");
            notifyMessageSent(messageId);
            msrpSession.close();
            unSubscribe();
            Logger.log(TAG, "MSRPMessageSendingListenerImpl.onMessageSent#finished");
        }

        public void onMessageSendFailed(String messageId, String reasonPhrase, int reasonType, int statusCode) {
            Logger.log(TAG, "MSRPMessageSendingListenerImpl.onMessageSendFailed#started");
            IReasonInfo reasonInfoImpl = new IReasonInfo(reasonPhrase, reasonType, statusCode);
            notifyMessageSendFailed(messageId, reasonInfoImpl);
            msrpSession.close();
            unSubscribe();
            Logger.log(TAG, "MSRPMessageSendingListenerImpl.onMessageSendFailed#finished");
        }

        private void unSubscribe() {
            msrpService.removeMSRPMessageSendingListener(this);
        }

        public void subscribe() {
            msrpService.addMSRPMessageSendingListener(this);
        }
    }

    ;


    public void sendMessage(IMessage message, boolean deliveryReport) throws RemoteException {
        Logger.log(TAG, "sendMessage#started");

        //TODO Implement

        Logger.log(TAG, "sendMessage#finished");
    }

    
    public void sendLargeMessage(IMessage message, boolean deliveryReport) throws RemoteException {
        Logger.log(TAG, "sendLargeMessage#started");

        MsrpMessage msrpMessage = IMessageBuilderUtils.iMessageToMsrpMessage(message);

        Logger.log(TAG, "sendLargeMessage#converted");

        for (String recepient : message.getRecipients()) {

            final ClientIdentity callingParty = imsStack.getContext().getRegistrationIdentity();
            Dialog msrpDialog = imsStack.getContext().getDialogStorage().getDialog(
                    callingParty,
                    recepient,
                    new DialogCallIDImpl(SIPUtil.newCallId())
            );

            final MSRPSession msrpSession = msrpService.obtainMSRPSession(msrpDialog, MSRPSessionType.CHAT);

            try {
                //subscribe to SESSION start event for running message sending
                new LargeMessageSendingRunner(msrpSession, msrpMessage).subscribe();

                //subscribe to message sending events
                new MSRPMessageSendingListenerImpl(msrpSession).subscribe();

                msrpSession.openChatSession();

            }
            catch (DialogStateException e) {
                Logger.log(TAG, e.getMessage());
                e.printStackTrace();
            }

        }

        Logger.log(TAG, "sendLargeMessage#finished");
    }

    
    public void cancel(String messageId) throws RemoteException {
        Logger.log(TAG, "cancel#started");

        //TODO Implement

        Logger.log(TAG, "cancel#finished");
    }

    
    public void addListener(IMessageManagerListener listener) throws RemoteException {
        if (listener != null) {
            listenerHolder.addListener(listener);
        }
    }

    
    public void removeListener(IMessageManagerListener listener) throws RemoteException {
        if (listener != null) {
            listenerHolder.removeListener(listener);
        }
    }

    /*
    * TODO call this method
    */
    private void notifyIncomingLargeMessage() {
        Logger.log(TAG, "notifyIncomingLargeMessage#started");
        try {
            LargeMessageRequestImpl largeMessageRequestImpl = new LargeMessageRequestImpl();

            listenerHolder.getNotifier().incomingLargeMessage(largeMessageRequestImpl);
        }
        catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyIncomingLargeMessage#finished");
    }

    /*
    * TODO call this method
    */
    private void notifyMessageReceived(MsrpMessage msrpMessage) {
        Logger.log(TAG, "notifyMessageReceived#started");
        IMessage messageImpl = IMessageBuilderUtils.msrpMessageToIMessage(msrpMessage);
        try {
            listenerHolder.getNotifier().messageReceived(messageImpl);
        }
        catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyMessageReceived#finished");
    }

    /*
    * TODO call this method
    */
    private void notifyMessageReceiveFailed(String messageId, IReasonInfo reasonInfoImpl) {
        Logger.log(TAG, "notifyMessageReceiveFailed#started");
        try {
            listenerHolder.getNotifier().messageReceiveFailed(messageId, reasonInfoImpl);
        }
        catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyMessageReceiveFailed#finished");
    }

    private void notifyMessageSendFailed(String messageId, IReasonInfo reasonInfoImpl) {
        Logger.log(TAG, "notifyMessageSendFailed#started");
        try {
            listenerHolder.getNotifier().messageSendFailed(messageId, reasonInfoImpl);
        }
        catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyMessageSendFailed#finished");
    }

    private void notifyMessageSent(String messageId) {
        Logger.log(TAG, "notifyMessageSent#started");
        try {
            listenerHolder.getNotifier().messageSent(messageId);
        }
        catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyMessageSent#finished");
    }

    /*
    * TODO call this method
    */
    private void notifyTransferProgress(String messageId, int bytesTransferred, int bytesTotal) {
        Logger.log(TAG, "notifyTransferProgress#started");
        try {
            listenerHolder.getNotifier().transferProgress(messageId, bytesTransferred, bytesTotal);
        }
        catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyTransferProgress#finished");
    }

}
