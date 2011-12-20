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
import com.android.ims.ServiceImpl;
import com.android.ims.core.ReferenceImpl;
import com.android.ims.core.SessionImpl;

import javax.microedition.ims.android.core.IReference;
import javax.microedition.ims.android.core.ISession;
import javax.microedition.ims.android.core.ISessionListener;
import javax.microedition.ims.core.SessionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * This class responsible for wrapping and forwarding events to specified @SessionListener object.
 *
 * @author ext-akhomush
 */
public class RemoteSessionListener extends ISessionListener.Stub {
    private static final String TAG = "RemoteSessionListener";

    private final List<SessionListener> listeners = new ArrayList<SessionListener>();
    private final SessionImpl mSession;
    private final ServiceImpl mServiceImpl;

    public RemoteSessionListener(final SessionImpl session, final ServiceImpl serviceImpl) {
        assert session != null;
        this.mSession = session;
        this.mServiceImpl = serviceImpl;
    }

    public void addListener(SessionListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(SessionListener listener) {
        Log.i(TAG, "removeListener#");
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    
    public void sessionAlerting(ISession session) throws RemoteException {
        Log.i(TAG, "sessionAlerting#");
        for (SessionListener listener : listeners) {
            listener.sessionAlerting(mSession);
        }
    }

    
    public void sessionStartFailed(ISession session) throws RemoteException {
        Log.i(TAG, "sessionStartFailed#");
        for (SessionListener listener : listeners) {
            listener.sessionStartFailed(mSession);
        }
    }

    
    public void sessionStarted(ISession session) throws RemoteException {
        Log.i(TAG, "sessionStarted#");
        for (SessionListener listener : listeners) {
            listener.sessionStarted(mSession);
        }
    }

    
    public void sessionTerminated(ISession session) throws RemoteException {
        Log.i(TAG, "sessionTerminated#");
        for (SessionListener listener : listeners) {
            listener.sessionTerminated(mSession);
        }
    }

    
    public void sessionUpdateFailed(ISession session) throws RemoteException {
        Log.i(TAG, "sessionUpdateFailed#");
        for (SessionListener listener : listeners) {
            listener.sessionUpdateFailed(mSession);
        }
    }

    
    public void sessionUpdateReceived(ISession session) throws RemoteException {
        Log.i(TAG, "sessionUpdateReceived#");
        for (SessionListener listener : listeners) {
            listener.sessionUpdateReceived(mSession);
        }
    }

    
    public void sessionUpdated(ISession session) throws RemoteException {
        Log.d(TAG, "sessionUpdated#start");
        for (SessionListener listener : listeners) {
            listener.sessionUpdated(mSession);
        }
        Log.d(TAG, "sessionUpdated#end");
    }

    
    public void sessionReferenceReceived(ISession iSession, IReference iReference)
            throws RemoteException {
        Log.i(TAG, "sessionReferenceReceived#");
        for (SessionListener listener : listeners) {
            ReferenceImpl reference = new ReferenceImpl(iReference.getServiceMethod(), iReference);
            mServiceImpl.addServiceCloseListener(reference);
            listener.sessionReferenceReceived(mSession, reference);
        }
    }
}
