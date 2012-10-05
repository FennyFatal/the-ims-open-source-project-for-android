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

import javax.microedition.ims.android.util.ListenerHolder;
import javax.microedition.ims.android.util.RemoteListenerHolder;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.sipservice.invite.DialogStateException;
import javax.microedition.ims.core.sipservice.options.OptionsInfo;
import javax.microedition.ims.core.sipservice.options.OptionsService;
import javax.microedition.ims.core.sipservice.options.listener.OptionsStateEvent;
import javax.microedition.ims.core.sipservice.options.listener.OptionsStateListener;
import javax.microedition.ims.messages.history.MessageHistory;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default implementation ICapabilities.aidl.
 *
 * @author Andrei Khomushko
 */
public class CapabilitiesImpl extends ICapabilities.Stub implements OptionsStateListener {
    private static final String TAG = "CapabilitiesImpl-Service";

    /**
     * The capability response is received.
     */
    private static final int STATE_ACTIVE = 3;

    /**
     * The capability request has not been sent.
     */
    private static final int STATE_INACTIVE = 1;

    /**
     * The capability request is sent and the platform is waiting for a
     * response.
     */
    private static final int STATE_PENDING = 2;

    private final ListenerHolder<ICapabilitiesListener> listenerHolder = new RemoteListenerHolder<ICapabilitiesListener>(ICapabilitiesListener.class);

    private int state = STATE_INACTIVE;

    private final IServiceMethod serviceMethod;
    private final OptionsService optionsService;
    private final Dialog dialog;

    private final AtomicReference<OptionsInfo> atomicReference = new AtomicReference<OptionsInfo>(null);

    CapabilitiesImpl(final OptionsService optionsService, final Dialog dialog) {
        assert dialog != null;
        this.dialog = dialog;

        this.serviceMethod = new CapabilitiesServiceMethodImpl(dialog.getRemoteParty());

        assert optionsService != null;
        this.optionsService = optionsService;
    }

    private void subscribeToOptionsService() {
        optionsService.addOptionsStateListener(dialog, this);
    }

    private void unsubscribeFromMessageService() {
        optionsService.removeOptionsStateListener(this);
    }

    public void onOptionsDelivered(OptionsStateEvent event) {
        atomicReference.compareAndSet(null, event.getOptionsInfo());
        unsubscribeFromMessageService();
        onCapabilityQueryDelivered();
    }

    public void onOptionsDelivereFailed(OptionsStateEvent event) {
        atomicReference.compareAndSet(null, event.getOptionsInfo());
        unsubscribeFromMessageService();
        onCapabilityQueryDeliveryFailed();
    }

    
    public String[] getRemoteUserIdentities() throws RemoteException {
        //TODO AK: review
        return new String[]{dialog.getRemoteParty()};
    }

    
    public IServiceMethod getServiceMethod() throws RemoteException {
        return serviceMethod;
    }

    
    public int getState() throws RemoteException {
        return state;
    }

    
    public boolean hasCapabilities(String connection) throws RemoteException {
        boolean retValue = false;
        OptionsInfo optionsInfo = atomicReference.get();
        if (optionsInfo != null) {
            for (String str : optionsInfo.getSupportedEntities()) {
                retValue = str.contains(connection);
                if (retValue == true)
                    break;
            }
        }

        return retValue;
    }

    
    public void queryCapabilities(boolean sdpInRequest) throws RemoteException {
        subscribeToOptionsService();

        this.state = STATE_PENDING;
        try {
            optionsService.sendOptionsMessage(dialog, sdpInRequest);
        }
        catch (DialogStateException e) {
            e.printStackTrace();
            Logger.log(TAG, e.getMessage());
            onCapabilityQueryDeliveryFailed();
        }
    }

    private void onCapabilityQueryDelivered() {
        this.state = STATE_ACTIVE;
        try {
            listenerHolder.getNotifier().capabilityQueryDelivered();
        }
        catch (RemoteException e) {
            e.printStackTrace();
            Logger.log(TAG, e.getMessage());
        }
    }

    private void onCapabilityQueryDeliveryFailed() {
        this.state = STATE_INACTIVE;
        try {
            listenerHolder.getNotifier().capabilityQueryDeliveryFailed();
        }
        catch (RemoteException e) {
            e.printStackTrace();
            Logger.log(TAG, e.getMessage());
        }
    }

    public void addListener(ICapabilitiesListener listener)
            throws RemoteException {
        listenerHolder.addListener(listener);
    }

    public void removeListener(ICapabilitiesListener listener)
            throws RemoteException {
        listenerHolder.removeListener(listener);
    }

    class CapabilitiesServiceMethodImpl extends ServiceMethodImpl {
        public CapabilitiesServiceMethodImpl(final String remoteUserId) {
            super(remoteUserId, new MessageHistory());
        }

        
        protected MessageType getMethodById(int methodId) {
            return MessageType.SIP_OPTIONS;
        }
    }
}
