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
import com.android.ims.rpc.RemoteReferenceListener;

import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.android.core.IReference;
import javax.microedition.ims.android.core.IReferenceListener;
import javax.microedition.ims.android.core.IServiceMethod;
import javax.microedition.ims.core.Reference;
import javax.microedition.ims.core.ReferenceListener;
import javax.microedition.ims.core.ServiceMethod;
import javax.microedition.ims.core.Session;

/**
 * Reference implementation of the {@link Reference} interface .
 *
 * @author ext-akhomush
 */
public class ReferenceImpl extends ServiceMethodImpl implements Reference {
    private static final String TAG = "JSR - ReferenceImpl";

    private final IReference iReference;

    private boolean implicitSubscription;

    public ReferenceImpl(final IServiceMethod serviceMethod,
                         final IReference iReference) {
        super(serviceMethod);

        assert iReference != null;
        this.iReference = iReference;
    }

    /**
     * @see Reference#accept()
     */
    
    public void accept() throws ServiceClosedException {
        if (!isServiceOpen()) {
            throw new ServiceClosedException("Service must be open");
        }

        int state = getState();
        if (state != STATE_PROCEEDING) {
            throw new IllegalStateException(
                    "accept# only allowed when in state " +
                            "STATE_PROCEEDING (state = " +
                            stateToSring(state) + ")");
        }
        
        //setting added body to service side
        flushResponceBodies();

        try {
            iReference.accept();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * @see Reference#reject()
     */
    
    public void reject() throws ServiceClosedException {
        if (!isServiceOpen()) {
            throw new ServiceClosedException("Service must be Open");
        }

        int state = getState();
        if (state != STATE_PROCEEDING) {
            throw new IllegalStateException(
                    "reject() only allowed when in state " +
                            "STATE_PROCEEDING (state = " +
                            stateToSring(state) + ")");
        }

        //setting added body to service side
        flushResponceBodies();
        try {
            iReference.reject();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * @see Reference#getState()
     */
    
    public int getState() {
        int state = 0;
        try {
            state = iReference.getState();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return state;
    }

    /**
     * @see Reference#connectReferMethod(ServiceMethod)
     */
    
    public void connectReferMethod(final ServiceMethod serviceMethod) {
        if (serviceMethod == null) {
            throw new IllegalArgumentException("ServiceMethod argument is null");
        }

        int state = getState();
        if (state != STATE_REFERRING) {
            throw new IllegalStateException("reference is not in state STATE_REFERRING");
        }

        if (serviceMethod instanceof Session) {
            SessionImpl session = (SessionImpl) serviceMethod;
            try {
                iReference.connectReferSession(session.getSession());
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else {
            assert false : "Not supported service method. The only supported method is Session. Now is " + serviceMethod;
        }

        ((SessionImpl) serviceMethod).initReferral(this);
    }

    /**
     * @see Reference#getReferMethod()
     */
    
    public String getReferMethod() {
        String method = null;
        try {
            method = iReference.getReferMethod();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return method;
    }

    /**
     * @see Reference#getReferToUserId()
     */
    
    public String getReferToUserId() {
        String referTo = null;
        try {
            referTo = iReference.getReferToUserId();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return referTo;
    }

    /**
     * @see Reference#getReplaces()
     */
    
    public String getReplaces() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see Reference#setReplaces(String)
     */
    
    public void setReplaces(String sessionId) {
        if (sessionId == null || "".equals(sessionId)) {
            throw new IllegalArgumentException("Illegal sessionId argument");
        }

        int state = getState();
        if (state != STATE_INITIATED) {
            throw new IllegalStateException("reference is not in STATE_INITIATED");
        }

        setReplacesInternal(sessionId);
    }

    private void setReplacesInternal(String sessionId) {
        // TODO Auto-generated method stub
    }

    /**
     * @see Reference#refer(boolean)
     */
    
    public void refer(boolean implicitSubscription)
            throws ServiceClosedException {
        if (!isServiceOpen()) {
            throw new ServiceClosedException("Service must be Open");
        }

        if (getState() != STATE_INITIATED) {
            throw new IllegalStateException(
                    "only allowed when in state STATE_INITIATED");
        }
        
      //setting added body to service side
        flushRequestBodies();

        this.implicitSubscription = implicitSubscription;

        try {
            iReference.refer();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * @see Reference#setListener(ReferenceListener)
     */
    
    public void setListener(ReferenceListener listener) {
        IReferenceListener referenceListener = (listener != null ? new RemoteReferenceListener(listener, this) : null);
        try {
            iReference.setListener(referenceListener);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Returns a string representation of a reference state.
     *
     * @param state the state
     * @return a string representation of the state
     */
    public static String stateToSring(int state) {
        switch (state) {
            case STATE_INITIATED:
                return "STATE_INITIATED";
            case STATE_PROCEEDING:
                return "STATE_PROCEEDING";
            case STATE_REFERRING:
                return "STATE_REFERRING";
            case STATE_TERMINATED:
                return "STATE_TERMINATED";
            default:
                return "UNKNOWN";
        }
    }
}
