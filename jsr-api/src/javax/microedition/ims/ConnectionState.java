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

package javax.microedition.ims;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.ims.ServiceConnectionException;
import com.android.ims.rpc.RemoteConnectionStateListener;
import com.android.ims.util.ServiceInterfaceHolder;
import com.android.ims.util.ServiceInterfaceHolder.BinderResolver;

import javax.microedition.ims.android.IConnectionState;

/**
 * The <code>ConnectionState</code> class is used to monitoring the IMS
 * connection and accessing user identities.
 * </p><p>
 * The IMS core runs autonomously on the device and therefore the application
 * cannot assume that the device is connected or disconnected to the IMS
 * network. If the IMS core is not connected when the application creates a
 * service with <code>Connector.open</code> the IMS core will connect to the
 * IMS network.
 * </p><p>
 * While being connected to the IMS network, an application can retrieve the
 * available network-provisioned user identities.
 */
public class ConnectionState {
    private static final String TAG = "ConnectionState";
    private static volatile Object mutex = new Object();

    private static volatile ConnectionState INSTANCE;

    private volatile Context context;
    private final ServiceInterfaceHolder<IConnectionState> interfaceHolder;
    private final IConnectionState connectionState;

    private RemoteConnectionStateListener connectionStateListener;

    private ConnectionState(final Context context) throws ImsException, ServiceConnectionException {
        this.context = context;
        this.interfaceHolder = new ServiceInterfaceHolder<IConnectionState>(new BinderResolver<IConnectionState>() {
            public IConnectionState asInterface(IBinder service) {
                return IConnectionState.Stub.asInterface(service);
            }
        });

        connectionState = interfaceHolder.bindToService(IConnectionState.class.getName(), context, false);
    }

    /**
     * Returns a ConnectionState that monitors the connection to the IMS network.
     * <B>This differs from JSR-281/325 API in which this function is static</B>.
     *
     * @return a ConnectionState that monitors the connection to the IMS network
     */
    public static ConnectionState getConnectionState(Context context) throws ImsException{
        if (INSTANCE == null) {
            synchronized (mutex) {
                if (INSTANCE == null) {
                    try {
                        INSTANCE = new ConnectionState(context);
                    } catch (ServiceConnectionException e) {
                        Log.e(TAG, "Cannot connect to connection state service");
                        throw new ImsException(e.getMessage(), e);
                    }
                }
            }
        }

        return INSTANCE;
    }

    /**
     * Returns network provisioned user identities.
     *
     * @return network provisioned user identities.
     */
    public String[] getUserIdentities() {
        String[] userIdentities;
        try {
            userIdentities = connectionState.getUserIdentities();
        } catch (RemoteException e) {
            Log.e(TAG, "Error in getUserIdetities#", e);
            userIdentities = new String[0];
        }
        return userIdentities;
    }

    /**
     * Determine whether there is a NAT/Firewall placed on the access network connecting the local endpoint to the IMS network.
     *
     * @return true is connection is behind NAT
     * @throws IllegalStateException - if the device is not connected to the IMS network.
     */
    public boolean isBehindNat() {
        boolean isBehindNat = false;
        try {
            isBehindNat = connectionState.isBehindNat();
        } catch (RemoteException e) {
            Log.e(TAG, "Error in isBehindNat#", e);
        }
        return isBehindNat;
    }

    /**
     * This method can be used to determine if the device is connected to the IMS network.
     *
     * @return true if connection is established.
     */
    public boolean isConnected() {
        boolean isConnected = false;
        try {
            isConnected = connectionState.isConnected();
        } catch (RemoteException e) {
            Log.e(TAG, "Error in isConnected#", e);
        }
        return isConnected;
    }

    /**
     * Returns whether the connection is in a suspended state.
     *
     * @return true is connection is in suspended state.
     * @throws IllegalStateException - if the device is not connected to the IMS network.
     */
    public boolean isSuspended() {
        boolean isSuspended = false;

        try {
            isSuspended = connectionState.isSuspended();
        } catch (RemoteException e) {
            Log.e(TAG, "Error in isSuspended#", e);
        }
        return isSuspended;
    }

    /**
     * Returns array of registration URIs
     *
     * @return array of registration URIs or null in case a user is unregistered
     */
    public String[] getRegisterURIs() {
        String[] registerURIs = null;

        try {
            registerURIs = connectionState.getRegisterURIs();
        } catch (RemoteException e) {
            Log.e(TAG, "Error in getRegisterURIs#", e);
        }

        return registerURIs;
    }

    /**
     * Sets a listener for this ConnectionState, replacing any previous ConnectionStateListener.
     *
     * @param listener
     */
    public void setListener(ConnectionStateListener listener) {
        try {
            if (connectionStateListener != null) {
                connectionState.removeConnectionStateListener(connectionStateListener);
            }

            if (listener != null) {
                connectionStateListener = new RemoteConnectionStateListener(listener);
                connectionState.addConnectionStateListener(connectionStateListener);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error when set listener", e);
        }
    }

    //TODO review

    public void close() {
        try {
            if (connectionStateListener != null) {
                setListener(null);
                connectionStateListener = null;
            }
            connectionState.close();
            interfaceHolder.unbindFromService(context, false);
        } catch (ServiceConnectionException e) {
            Log.e(TAG, "Cannot unbind from connection state service");
        } catch (RemoteException e) {
            Log.e(TAG, "connection state service close FAILED", e);
        }
        context = null;
        INSTANCE = null;
    }
}
