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

package javax.microedition.ims.android;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.core.connection.*;
import javax.microedition.ims.core.env.ConnectionManager;
import javax.microedition.ims.core.sipservice.register.RegResult;
import javax.microedition.ims.core.sipservice.register.RegisterEvent;
import javax.microedition.ims.core.sipservice.register.RegisterService;
import javax.microedition.ims.core.sipservice.register.RegistrationListener;
import java.util.List;

/**
 * This class responsible for monitoring connection state and notifying
 * subscribers.
 *
 * @author ext-akhomush
 */

public class ConnectionStateBinder extends IConnectionState.Stub implements ConnStateListener,
        RegistrationListener {
    private static final String TAG = "ConnectionState";

    private final RegisterService registerService;

    private final ConnectionManager connectionManager;

    private final RemoteCallbackList<IConnectionStateListener> connStateListeners = new RemoteCallbackList<IConnectionStateListener>();

    private int callbackListSize = 0;

    private enum ConnectionStateType {
        IMS_CONNECTED, IMS_DISCONNECTED, CONNECTION_RESUMED, CONNECTION_SUSPENDED;
    }

    public ConnectionStateBinder(final RegisterService registerService,
            final ConnectionManager connectionManager) {
        assert registerService != null;
        this.registerService = registerService;

        assert connectionManager != null;
        this.connectionManager = connectionManager;
    }

    public void addConnectionStateListener(IConnectionStateListener listener)
            throws RemoteException {
        if (listener != null) {
            connStateListeners.register(listener);
            callbackListSize++;
        }
    }

    public void removeConnectionStateListener(IConnectionStateListener listener)
            throws RemoteException {
        if (listener != null) {
            connStateListeners.unregister(listener);
            callbackListSize--;
        }
    }

    /*
     * private void notifyConnStateListeners(ConnectionStateType connState) {
     * this.notifyConnStateListeners(connState, null); }
     */

    private void notifyConnStateListeners(ConnectionStateType connState,
            ConnectionInfo connectionInfo) {
        Logger.log(TAG, "notifyConnStateListeners#connState = " + connState);

        final int N = connStateListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IConnectionStateListener coreServiceListener = connStateListeners.getBroadcastItem(i);
            try {
                switch (connState) {
                    case IMS_CONNECTED: {
                        String callType = connectionInfo != null ? connectionInfo
                                .getEmergencyCallsType() : null;
                        coreServiceListener.imsConnected(callType);
                        break;
                    }
                    case IMS_DISCONNECTED: {
                        coreServiceListener.imsDisconnected();
                        break;
                    }
                    case CONNECTION_RESUMED: {
                        coreServiceListener.connectionResumed();
                        break;
                    }
                    case CONNECTION_SUSPENDED: {
                        coreServiceListener.connectionSuspended();
                        break;
                    }
                    default: {
                        Logger.log("Unhandled connection state: " + connState);
                        break;
                    }
                }

            } catch (RemoteException e) {
                Logger.log(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
        connStateListeners.finishBroadcast();
    }

    public void onConnected(ConnStateEvent event) {
        notifyConnStateListeners(ConnectionStateType.CONNECTION_RESUMED, null);
    }

    public void onConnecting(ConnStateEvent event) {

    }

    public void onDisconnected(ConnStateEvent event) {
        notifyConnStateListeners(ConnectionStateType.CONNECTION_SUSPENDED, null);
    }

    public void onDisconnecting(ConnStateEvent event) {

    }

    public void onUnknown(ConnStateEvent event) {

    }

    public void onRegistered(RegisterEvent event) {
        notifyConnStateListeners(ConnectionStateType.IMS_CONNECTED, buildConnectionInfo(event));

    }

    private ConnectionInfo buildConnectionInfo(RegisterEvent event) {
        ConnectionInfo connectionInfo = new DefaultConnectionInfo(event.getRegistrationResult()
                .getEmergencyCallType());
        return connectionInfo;
    }

    public void onRegistrationAttempt(RegisterEvent event) {
    }

    public void onRegistrationRefresh(RegisterEvent event) {
        RegResult regResult = event.getRegistrationResult();
        ConnectionStateType state = regResult.isSuccessful() && !regResult.byTimeout() ? ConnectionStateType.IMS_CONNECTED
                : ConnectionStateType.IMS_DISCONNECTED;

        notifyConnStateListeners(state, buildConnectionInfo(event));
    }

    public void onUnregistered(RegisterEvent event) {
        notifyConnStateListeners(ConnectionStateType.IMS_DISCONNECTED, (event != null) ? buildConnectionInfo(event) : null);
    }

    /**
     * This method can be used to determine if the device is connected to the
     * IMS network.
     *
     * @return true if connection is established.
     */
    public boolean isConnected() {
        return registerService.isRegistered();
    }

    /**
     * Returns whether the connection is in a suspended state.
     *
     * @return true is connection is in suspended state.
     * @throws IllegalStateException - if the device is not connected to the IMS
     *             network.
     */
    public boolean isSuspended() {
        return connectionManager.getCurrentState() == ConnState.DISCONNECTED;
    }

    public String[] getUserIdentities() throws RemoteException {
        final List<String> stringList = registerService.getRegistrationInfo().getIntegralURIList();
        return stringList.toArray(new String[stringList.size()]);
    }

    // TODO Is not implemented yet
    public boolean isBehindNat() throws RemoteException {
        return false;
    }

    @Override
    public String[] getRegisterURIs() throws RemoteException {
        return registerService.getRegisterURIs();
    }

    public void close() {
        if (isConnected() && (callbackListSize != 0))
            registerService.refreshRegistration();
    }

}
