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

package com.android.ims.xdm;

import android.os.RemoteException;
import android.util.Log;
import com.android.ims.ManagableConnection;
import com.android.ims.ReasonInfoImpl;
import com.android.ims.ServiceCloseListener;
import org.w3c.dom.Document;

import javax.microedition.ims.ReasonInfo;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.xdm.IDocumentSubscriber;
import javax.microedition.ims.android.xdm.IDocumentSubscriberListener;
import javax.microedition.ims.xdm.DocumentSubscriber;
import javax.microedition.ims.xdm.DocumentSubscriberListener;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default implementation {@link DocumentSubscriber}
 * 
 * @author Andrei Khomushko
 * 
 */
public class DefaultDocumentSubscriber implements DocumentSubscriber,
        DocumentSubscriberListener, ServiceCloseListener {
    private static final String TAG = "DefaultDocumentSubscriber";

    private final String[] urls;
    private final IDocumentSubscriber subscriberPeer;
    private final RemoteDocumentSubscriberListener remoteListener;
    private final AtomicBoolean done = new AtomicBoolean();

    private int state = STATE_INACTIVE;
    private DocumentSubscriberListener listener;

    public DefaultDocumentSubscriber(IDocumentSubscriber documentSubscriber)
            throws InstantiationException {
        this.subscriberPeer = documentSubscriber;

        try {
            this.urls = subscriberPeer.getURLs();
        } catch (RemoteException e) {
            throw new InstantiationException();
        }

        try {
            subscriberPeer
                    .addListener(remoteListener = new RemoteDocumentSubscriberListener(
                            this));
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new InstantiationException();
        }
    }

    
    public int getState() {
        return state;
    }

    
    public String[] getURLs() {
        return urls;
    }

    
    public void setListener(DocumentSubscriberListener listener) {
        this.listener = listener;
    }

    
    public void subscribe() throws ServiceClosedException, IOException {
        if (done.get()) {
            throw new ServiceClosedException("The Service is closed");
        }

        if (state == STATE_PENDING_SUBSCRIBE
                || state == STATE_PENDING_UNSUBSCRIBE) {
            throw new IllegalStateException(
                    "the DocumentSubscriber is in STATE_PENDING_SUBSCRIBE or STATE_PENDING_UNSUBSCRIBE");
        }

        setState(STATE_PENDING_SUBSCRIBE);
        try {
            subscriberPeer.subscribe();
        } catch (RemoteException e) {
            setState(STATE_INACTIVE);
            Log.e(TAG, e.getMessage(), e);
            throw new IOException(e.getMessage());
        }
    }

    
    public void unsubscribe() throws IOException {
        if (state != STATE_ACTIVE) {
            throw new IllegalStateException(
                    "The DocumentSubscriber is not in STATE_ACTIVE");
        }

        setState(STATE_PENDING_UNSUBSCRIBE);
        try {
            subscriberPeer.unsubscribe();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new IOException(e.getMessage());
        }
        setState(STATE_INACTIVE);
    }

    
    public void documentDeleted(DocumentSubscriber subscriber,
            String documentSelector, Document xcapDiff) {
        Log.i(TAG, "documentDeleted#documentSelector = " + documentSelector);
        if (listener != null) {
            listener.documentDeleted(subscriber, documentSelector, xcapDiff);
        }
    }

    
    public void documentUpdateReceived(DocumentSubscriber subscriber,
            String documentSelector, Document xcapDiff) {
        Log.i(TAG, "documentUpdateReceived#documentSelector = "
                + documentSelector);
        if (listener != null) {
            listener.documentUpdateReceived(subscriber, documentSelector,
                    xcapDiff);
        }
    }

    
    public void subscriptionStarted(DocumentSubscriber subscriber) {
        Log.i(TAG, "subscriptionStarted#");

        setState(STATE_ACTIVE);
        if (listener != null) {
            listener.subscriptionStarted(subscriber);
        }
    }

    
    public void subscriptionFailed(DocumentSubscriber subscriber,
            ReasonInfo reason) {
        Log.i(TAG, "subscriptionFailed#reason = " + reason);

        setState(STATE_INACTIVE);
        if (listener != null) {
            listener.subscriptionFailed(subscriber, reason);
        }
    }

    
    public void subscriptionTerminated(DocumentSubscriber subscriber) {
        Log.i(TAG, "subscriptionTerminated#");

        setState(STATE_INACTIVE);
        if (listener != null) {
            listener.subscriptionTerminated(subscriber);
        }
    }

    private void setState(int state) {
        this.state = state;
    }

    private class RemoteDocumentSubscriberListener extends
            IDocumentSubscriberListener.Stub {
        private DocumentSubscriberListener listener;

        public RemoteDocumentSubscriberListener(
                DocumentSubscriberListener listener) {
            this.listener = listener;
        }

        
        public void documentDeleted(String documentSelector, String xcapDiff)
                throws RemoteException {
            // TODO convert xcapDiff to Document
            Document document = null;
            listener.documentDeleted(DefaultDocumentSubscriber.this,
                    documentSelector, document);
        }

        
        public void documentUpdateReceived(String documentSelector,
                String xcapDiff) throws RemoteException {
            // TODO convert xcapDiff to Document
            Document document = null;
            listener.documentUpdateReceived(DefaultDocumentSubscriber.this,
                    documentSelector, document);
        }

        
        public void subscriptionFailed(IReasonInfo reason)
                throws RemoteException {
            listener.subscriptionFailed(DefaultDocumentSubscriber.this,
                    new ReasonInfoImpl(reason));
        }

        
        public void subscriptionStarted() throws RemoteException {
            listener.subscriptionStarted(DefaultDocumentSubscriber.this);
        }

        
        public void subscriptionTerminated() throws RemoteException {
            listener.subscriptionTerminated(DefaultDocumentSubscriber.this);
        }
    }

    
    public void serviceClosed(ManagableConnection connection) {
        connection.removeServiceCloseListener(this);
        
        try {
            subscriberPeer.removeListener(remoteListener);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            done.set(true);
        }
    }
}
