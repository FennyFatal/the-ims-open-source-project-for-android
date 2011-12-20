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

package com.android.ims.rpc;

import android.os.RemoteException;
import android.util.Log;
import com.android.ims.core.MessageImpl;

import javax.microedition.ims.android.core.IMessage;
import javax.microedition.ims.android.core.ISubscriptionListener;
import javax.microedition.ims.core.Subscription;
import javax.microedition.ims.core.SubscriptionListener;
import java.util.ArrayList;
import java.util.List;


public class RemoteSubscriptionListener extends ISubscriptionListener.Stub {
    private static final String TAG = "RemoteSubscriptionListener";

    private final List<SubscriptionListener> listeners = new ArrayList<SubscriptionListener>();

    private final Subscription subscription;

    public RemoteSubscriptionListener(final Subscription subscription) {
        assert subscription != null;
        this.subscription = subscription;
    }

    public void addListener(SubscriptionListener listener) {
        Log.i(TAG, "addListener#");
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(SubscriptionListener listener) {
        Log.i(TAG, "removeListener#");
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    
    public void subscriptionStarted() throws RemoteException {
        Log.i(TAG, "subscriptionStarted#started");
        for (SubscriptionListener listener : listeners) {
            listener.subscriptionStarted(subscription);
        }
        Log.i(TAG, "subscriptionStarted#finished");
    }

    
    public void subscriptionStartFailed() throws RemoteException {
        Log.i(TAG, "subscriptionStartFailed#started");
        for (SubscriptionListener listener : listeners) {
            listener.subscriptionStartFailed(subscription);
        }
        Log.i(TAG, "subscriptionStartFailed#finished");
    }

    
    public void subscriptionTerminated() throws RemoteException {
        Log.i(TAG, "subscriptionTerminated#started");
        for (SubscriptionListener listener : listeners) {
            listener.subscriptionTerminated(subscription);
        }
        Log.i(TAG, "subscriptionTerminated#finished");
    }

    
    public void subscriptionNotify(IMessage notify) throws RemoteException {
        Log.i(TAG, "subscriptionNotify#started");
        
        MessageImpl messageImpl = new MessageImpl(notify);
        
        for (SubscriptionListener listener : listeners) {
            listener.subscriptionNotify(subscription, messageImpl);
        }
        Log.i(TAG, "subscriptionNotify#finished");
    }
}
