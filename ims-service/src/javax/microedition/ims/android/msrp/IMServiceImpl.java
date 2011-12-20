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
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.IMSStack;
import javax.microedition.ims.core.msrp.MSRPService;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;

public class IMServiceImpl extends IIMService.Stub {
    private static final String TAG = "Service - IMServiceImpl";

    private final ClientIdentity callingParty;
    private final MSRPService msrpService;

    private final ConferenceManagerImpl conferenceManagerImpl;
    private final DeferredMessageManagerImpl deferredMessageManagerImpl;
    private final FileTransferManagerImpl fileTransferManagerImpl;
    private final HistoryManagerImpl historyManagerImpl;
    private final MessageManagerImpl messageManagerImpl;

    private final RemoteListenerHolder<IIMServiceListener> listenerHolder = new RemoteListenerHolder<IIMServiceListener>(IIMServiceListener.class);


    public IMServiceImpl(final ClientIdentity callingParty, final IMSStack<IMSMessage> imsStack) {
        this.callingParty = callingParty;
        this.msrpService = imsStack.getMSRPService();

        conferenceManagerImpl = new ConferenceManagerImpl(imsStack, callingParty);
        deferredMessageManagerImpl = new DeferredMessageManagerImpl();
        fileTransferManagerImpl = new FileTransferManagerImpl(imsStack);
        historyManagerImpl = new HistoryManagerImpl();
        messageManagerImpl = new MessageManagerImpl(imsStack);
    }

    
    public IConferenceManager getConferenceManager() throws RemoteException {
        return conferenceManagerImpl;
    }

    
    public IDeferredMessageManager getDeferredMessageManager() throws RemoteException {
        return deferredMessageManagerImpl;
    }

    
    public IFileTransferManager getFileTransferManager() throws RemoteException {
        return fileTransferManagerImpl;
    }

    
    public IHistoryManager getHistoryManager() throws RemoteException {
        return historyManagerImpl;
    }

    
    public IMessageManager getMessageManager() throws RemoteException {
        return messageManagerImpl;
    }

    
    public void close() throws RemoteException {
        // TODO implement

        notifyServiceClosed(IReasonInfo.REASONTYPE_USER_ACTION, "reasonPhrase", 0);
    }

    
    public String getLocalUserId() throws RemoteException {
        return callingParty.getUserInfo().toUri();
    }

    
    public String getAppId() throws RemoteException {
        return callingParty.getAppID();
    }

    
    public String getSheme() throws RemoteException {
        return callingParty.getSchema();
    }

    
    public void addListener(IIMServiceListener listener) throws RemoteException {
        if (listener != null) {
            listenerHolder.addListener(listener);
        }
    }

    
    public void removeListener(IIMServiceListener listener) throws RemoteException {
        if (listener != null) {
            listenerHolder.removeListener(listener);
        }
    }

    /*
    * TODO call this method
    */
    private void notifyAdvertisementMessageReceived(MsrpMessage msrpMessage) {
        IMessage messageImpl = IMessageBuilderUtils.msrpMessageToIMessage(msrpMessage);
        try {
            listenerHolder.getNotifier().advertisementMessageReceived(this, messageImpl);
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /*
     * TODO call this method
     */
    private void notifySystemMessageReceived(MsrpMessage msrpMessage) {
        IMessage messageImpl = IMessageBuilderUtils.msrpMessageToIMessage(msrpMessage);
        try {
            listenerHolder.getNotifier().systemMessageReceived(this, messageImpl);
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /*
    * TODO call this method
    */
    private void notifyDeliveryReportsReceived() {
        DeliveryReportImpl deliveryReportImpl = new DeliveryReportImpl();
        // TODO get deliveryReportImpl by conversion
        try {
            listenerHolder.getNotifier().deliveryReportsReceived(this, deliveryReportImpl);
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /*
    * TODO call this method
    */
    private void notifyServiceClosed(int reasonType, String reasonPhrase, int statusCode) {
        IReasonInfo reasonInfoImpl = new IReasonInfo(reasonPhrase, reasonType, statusCode);
        try {
            listenerHolder.getNotifier().serviceClosed(this, reasonInfoImpl);
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

}
