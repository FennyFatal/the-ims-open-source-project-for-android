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

package com.android.ims;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.android.ims.configuration.AppConfiguration;
import com.android.ims.util.Utils;

import javax.microedition.ims.Service;

/**
 * Reference implementation of {@link Service}.
 */
public abstract class ServiceImpl extends ConnectionImpl implements Service {
    private final AppConfiguration configuration;
    private final String localAddress;
    private final Context context;

    public ServiceImpl(final Context mContext, final AppConfiguration configuration) {
        super(mContext);

        this.localAddress = Utils.retrieveLocalAddress();
        assert localAddress != null;
        
        assert configuration != null;
        this.configuration = configuration;

        this.context = mContext;
    }

    /**
     * @see Service#getAppId()
     */
    
    public String getAppId() {
        String appId = null;

        if (isOpen()) {
            try {
                appId = getAppIdInternally();
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else {
            logInvalidState();
        }
        return appId;
    }
    
    protected abstract String getAppIdInternally() throws RemoteException;

    /**
     * @see Service#getScheme()
     */
    
    public String getScheme() {
        String schema = null;

        if (isOpen()) {
            try {
                schema = getSchemeInternally();
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else {
            logInvalidState();
        }
        return schema;
    }
    
    protected abstract String getSchemeInternally() throws RemoteException;;

    protected void logInvalidState() {
        assert false : "Invalid state. Core service already closed. Method call ignored.";
        Log.i(TAG, "Invalid state. Core service already closed. Method call ignored.");
    }

    public String getLocalAddress() {
        return localAddress;
    }
    
    public AppConfiguration getConfiguration() {
        return configuration;
    }

    public Context getContext() {
        return context;
    }
    
    public void serviceClosed(){
    	
    }
}
