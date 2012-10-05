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
import com.android.ims.rpc.RemoteSubscriptionListener;

import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.android.core.IServiceMethod;
import javax.microedition.ims.android.core.ISubscription;
import javax.microedition.ims.core.Subscription;
import javax.microedition.ims.core.SubscriptionListener;

public class SubscriptionImpl extends ServiceMethodImpl implements Subscription {
    private static final String TAG = "JSR - SubscriptionImpl";

    private ISubscription mSubscription;

    private SubscriptionListener mCurrentSubscriptionListener = null;
    private RemoteSubscriptionListener remoteSubscriptionListener = null;

    
    public SubscriptionImpl(final IServiceMethod serviceMethod, ISubscription mSubscription) {
        super(serviceMethod);

        this.mSubscription = mSubscription;
    }

    
    public void subscribe() throws ServiceClosedException {
        Log.i(TAG, "subscribe#started");
        
        if (!isServiceOpen()) {
            throw new ServiceClosedException("The Service is closed");
        }
        
        int state = getState();
        if (state != STATE_INACTIVE && state != STATE_ACTIVE) {
            throw new IllegalStateException("The Subscription is not in STATE_INACTIVE or STATE_ACTIVE");
        }
        
        flushRequestBodies();
        
        try {
            mSubscription.subscribe();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "subscribe#finished");
    }

    
    public void poll() throws ServiceClosedException {
        Log.i(TAG, "poll#started");
        
        if (!isServiceOpen()) {
            throw new ServiceClosedException("The Service is closed");
        }
        
        int state = getState();
        if (state != STATE_INACTIVE) {
            throw new IllegalStateException("The Subscription is not in STATE_INACTIVE");
        }
        
        flushRequestBodies();
        
        try {
            mSubscription.poll();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "poll#finished");
    }
    
    
    public void unsubscribe() throws ServiceClosedException {
        Log.i(TAG, "unsubscribe#started");
        
        if (!isServiceOpen()) {
            throw new ServiceClosedException("The Service is closed");
        }
        
        int state = getState();
        if (state != STATE_ACTIVE) {
            throw new IllegalStateException("The Subscription is not in STATE_ACTIVE");
        }
        
        flushRequestBodies();
        
        try {
            mSubscription.unsubscribe();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "unsubscribe#finished");
    }

    
    public void setListener(SubscriptionListener listener) {
        if (remoteSubscriptionListener == null) {
            remoteSubscriptionListener = new RemoteSubscriptionListener(this);

            try {
                mSubscription.addListener(remoteSubscriptionListener);
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
                remoteSubscriptionListener = null;

                return;
            }
        }

        if (mCurrentSubscriptionListener != null) {
            remoteSubscriptionListener.removeListener(mCurrentSubscriptionListener);
        }

        if (listener != null) {
            remoteSubscriptionListener.addListener(listener);
        } else {
            try {
                mSubscription.removeListener(remoteSubscriptionListener);
                remoteSubscriptionListener = null;
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
                remoteSubscriptionListener = null;
                mCurrentSubscriptionListener = listener;

                return;
            }
        }

        mCurrentSubscriptionListener = listener;
    }

    
    public String getEvent() {
        Log.i(TAG, "getEvent#started");
        String event = null;
        try {
            event = mSubscription.getEvent();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "getEvent#finished");
        return event;
    }

    
    public int getState() {
        Log.i(TAG, "getState#started");
        int state = 0;
        try {
            state = mSubscription.getState();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "getState#finished");
        return state;
    }
}
