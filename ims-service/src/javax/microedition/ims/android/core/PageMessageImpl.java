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
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.DialogStateEvent;
import javax.microedition.ims.core.dialog.DialogStateListener;
import javax.microedition.ims.core.dialog.DialogStateListenerAdapter;
import javax.microedition.ims.core.sipservice.invite.DialogStateException;
import javax.microedition.ims.core.sipservice.pagemessage.PageMessageService;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;

public class PageMessageImpl extends IPageMessage.Stub {
    private static final String TAG = "PageMessageImpl";

    private final Dialog dialog;

    private final ServiceMethodImpl serviceMethod;
    private final PageMessageService messageService;

    private final RemoteListenerHolder<IPageMessageListener> listenerHolder = new RemoteListenerHolder<IPageMessageListener>(IPageMessageListener.class);

    public PageMessageImpl(final Dialog dialog, final PageMessageService messageService) {
        assert dialog != null;
        assert dialog.getLocalParty().getAppID() != null : "LocalParty couldn't be null";
        assert dialog.getRemoteParty() != null : "RemoteParty couldn't be null";

        this.dialog = dialog;

        assert messageService != null : "MessageService couldn't be null";
        this.messageService = messageService;

        this.serviceMethod = new PageMessageServiceMethodImpl(dialog.getRemoteParty(), dialog.getMessageHistory());

        subscribeToMessageService();
        Logger.log(TAG, "init#" + this);
    }

    private void subscribeToMessageService() {
        Logger.log(TAG, "subscribeToMessageService#" + this);
        messageService.addDialogStateListener(dialog, dialogStateListener);
    }

    private void unsubscribeFromMessageService() {
        Logger.log(TAG, "unsubscribeFromMessageService#" + this);
        messageService.removeDialogStateListener(dialogStateListener);
    }

    private final DialogStateListener<BaseSipMessage> dialogStateListener = new DialogStateListenerAdapter<BaseSipMessage>() {
        public void onSessionEventBefore(final DialogStateEvent<BaseSipMessage> event) {
            Logger.log(TAG, "onSessionEvent#event = " + event + ", sessionState = state");
            switch (event.getSessionState()) {
                case MESSAGE_DELIVERED:
                    pageMessageDelivered();
                    break;
                case MESSAGE_DELIVERY_FAILED:
                    pageMessageDeliverFailed();
                    break;
                default:
                    Logger.log(TAG, "Unknown state = " + event.getSessionState());
                    //return;
                    break;
            }
            unsubscribeFromMessageService();
        }
    };

    private void pageMessageDelivered() {
        Logger.log(TAG, "pageMessageDelivered#");
        try {
            listenerHolder.getNotifier().pageMessageDelivered(this);
        }
        catch (RemoteException e) {
            e.printStackTrace();
            Logger.log(TAG, e.getMessage());
        }
    }

    private void pageMessageDeliverFailed() {
        Logger.log(TAG, "pageMessageDeliverFailed#");
        try {
            listenerHolder.getNotifier().pageMessageDeliveryFailed(this);
        }
        catch (RemoteException e) {
            e.printStackTrace();
            Logger.log(TAG, e.getMessage());
        }
    }


    public void send(final byte[] content, final String contentType) throws RemoteException {
        Logger.log(TAG, String.format("send#content length = %s, contentType = %s", content.length, contentType));
        MessageImpl message = getServiceMethodInternally().getNextRequestInternally();
        MessageBodyPartImpl bodyPart = message.createBodyPartInternally();
        bodyPart.setContent(content);
        bodyPart.setContentType(contentType);

        try {
            messageService.sendPageMessage(dialog);
        }
        catch (DialogStateException e) {
            assert false : "EXCEPTION during send " + dialog + " " + e.toString();
        }
    }


    public void addListener(final IPageMessageListener listener)
            throws RemoteException {
        listenerHolder.addListener(listener);
    }


    public void removeListener(final IPageMessageListener listener)
            throws RemoteException {
        listenerHolder.removeListener(listener);
    }


    public IServiceMethod getServiceMethod() throws RemoteException {
        return getServiceMethodInternally();
    }

    protected ServiceMethodImpl getServiceMethodInternally() {
        return serviceMethod;
    }
    
    @Override
    protected void finalize() throws Throwable {
        Logger.log(TAG, "finalize#" + this);
        super.finalize();
    }
}
