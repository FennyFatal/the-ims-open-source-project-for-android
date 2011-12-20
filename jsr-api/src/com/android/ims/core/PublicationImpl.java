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
import com.android.ims.rpc.RemotePublicationListener;

import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.android.IError;
import javax.microedition.ims.android.core.IPublication;
import javax.microedition.ims.android.core.IServiceMethod;
import javax.microedition.ims.core.Publication;
import javax.microedition.ims.core.PublicationListener;

public class PublicationImpl extends ServiceMethodImpl implements Publication {
    private static final String TAG = "JSR - PublicationImpl";

    private IPublication mPublication;

    private PublicationListener mCurrentPublicationListener;
    private final RemotePublicationListener remotePublicationListener;

    
    public PublicationImpl(final IServiceMethod serviceMethod, final IPublication mPublication) {
        super(serviceMethod);
        
        this.mPublication = mPublication;
        
        this.remotePublicationListener = new RemotePublicationListener(this);
        try {
            mPublication.addListener(remotePublicationListener);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    
    public void publish(byte[] state, String contentType) throws ServiceClosedException {
        Log.i(TAG, "publish#started");
        
        if (!isServiceOpen()) {
            throw new ServiceClosedException("The Service is closed");
        }
        
        if (getState() == STATE_PENDING) {
            throw new IllegalStateException("The Publication is in STATE_PENDING");
        }
        
        flushRequestBodies();
        
        try {
            IExceptionHolder exceptionHolder = new IExceptionHolder();
            mPublication.publish(state, contentType, exceptionHolder);
            
            if (exceptionHolder.getParcelableException() != null) {
                javax.microedition.ims.android.IError error = (IError)exceptionHolder.getParcelableException();
                Log.e(TAG, "error = " + error.toString());
                if (error.getErrorCode() == IError.ERROR_WRONG_PARAMETERS) {
                    throw new IllegalArgumentException(error.getMessage());
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "publish#finished");
    }

    
    public void unpublish() throws ServiceClosedException {
        Log.i(TAG, "unpublish#started");
        
        if (!isServiceOpen()) {
            throw new ServiceClosedException("The Service is closed");
        }
        
        int state = getState();
        if (state != STATE_ACTIVE) {
            throw new IllegalStateException("The Subscription is not in STATE_ACTIVE");
        }
        
        flushRequestBodies();
        
        try {
            mPublication.unpublish();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "unpublish#finished");
    }

    
    public void setListener(PublicationListener listener) {
        if (mCurrentPublicationListener != null) {
            remotePublicationListener.removeListener(mCurrentPublicationListener);
        }

        if (listener != null) {
            remotePublicationListener.addListener(listener);
        }
        mCurrentPublicationListener = listener;
    }

    
    public String getEvent() {
        Log.i(TAG, "getEvent#started");
        String event = null;
        try {
            event = mPublication.getEvent();
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
            state = mPublication.getState();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "getState#finished");
        return state;
    }

}
