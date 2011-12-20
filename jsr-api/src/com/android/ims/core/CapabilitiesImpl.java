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

package com.android.ims.core;

import android.os.RemoteException;
import android.util.Log;
import com.android.ims.rpc.RemoteCapabilitiesListener;

import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.android.core.ICapabilities;
import javax.microedition.ims.android.core.IServiceMethod;
import javax.microedition.ims.core.Capabilities;
import javax.microedition.ims.core.CapabilitiesListener;

/**
 * Default implementation {@link Capabilities}.
 * 
 * @author Andrei Khomushko
 * 
 */
public class CapabilitiesImpl extends ServiceMethodImpl implements Capabilities {
    private static final String TAG = "CapabilitiesImpl";
    private final ICapabilities capabilitiesPeer;

    private RemoteCapabilitiesListener remoteCapabilitiesListener;

    public CapabilitiesImpl(IServiceMethod serviceMethod,
            ICapabilities capabilitiesPeer) {
        super(serviceMethod);

        assert capabilitiesPeer != null;
        this.capabilitiesPeer = capabilitiesPeer;
    }

    /**
     * @see Capabilities#getRemoteUserIdentities()
     */
    
    public String[] getRemoteUserIdentities() {
        if (getState() != STATE_ACTIVE) {
            throw new IllegalStateException(
                    "The Capabilities is not in STATE_ACTIVE");
        }

        String[] remoteUserIdentities = null;
        try {
            remoteUserIdentities = capabilitiesPeer.getRemoteUserIdentities();
        } catch (RemoteException e) {
            Log.i(TAG, e.getMessage(), e);
        }
        return remoteUserIdentities;
    }

    /**
     * @see Capabilities#getState()
     */
    
    public int getState() {
        int state = -1;

        try {
            state = capabilitiesPeer.getState();
        } catch (RemoteException e) {
            Log.i(TAG, e.getMessage(), e);
        }
        return state;
    }

    /**
     * @see Capabilities#hasCapabilities(String)
     */
    
    public boolean hasCapabilities(String connection) {
        if (connection == null) {
            throw new IllegalArgumentException(
                    "The connection argument is null");
        }

        if (getState() != STATE_ACTIVE) {
            throw new IllegalStateException(
                    "The Capabilities is not in STATE_ACTIVE");
        }

        boolean hasCapabilities = false;
        try {
            hasCapabilities = capabilitiesPeer.hasCapabilities(connection);
        } catch (RemoteException e) {
            Log.i(TAG, e.getMessage(), e);
        }
        return hasCapabilities;
    }

    /**
     * @see Capabilities#queryCapabilities(boolean)
     */
    
    public void queryCapabilities(boolean sdpInRequest)
            throws ServiceClosedException {
        if (!isServiceOpen()) {
            throw new ServiceClosedException();
        }
        
        if (getState() != STATE_INACTIVE) {
            throw new IllegalStateException(
                    "The Capabilities is not in STATE_INACTIVE");
        }

        try {
            capabilitiesPeer.queryCapabilities(sdpInRequest);
        } catch (RemoteException e) {
            Log.i(TAG, e.getMessage(), e);
        }
    }

    /**
     * @see Capabilities#setListener(CapabilitiesListener)
     */
    
    public void setListener(CapabilitiesListener listener) {
        if (remoteCapabilitiesListener != null) {
            removeRemoteListener(remoteCapabilitiesListener);
        }

        if (listener != null) {
            addRemoteListener(new RemoteCapabilitiesListener(this, listener));
        }
    }

    private void addRemoteListener(RemoteCapabilitiesListener listener) {
        try {
            capabilitiesPeer.addListener(listener);
        } catch (RemoteException e) {
            Log.i(TAG, e.getMessage(), e);
        }
    }

    private void removeRemoteListener(RemoteCapabilitiesListener listener) {
        try {
            capabilitiesPeer.removeListener(listener);
        } catch (RemoteException e) {
            Log.i(TAG, e.getMessage(), e);
        }
    }
}
