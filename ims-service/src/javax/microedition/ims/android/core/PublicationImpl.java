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

package javax.microedition.ims.android.core;

import android.os.RemoteException;
import android.util.Log;

import javax.microedition.ims.android.IError;
import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.android.util.RemoteListenerHolder;
import javax.microedition.ims.common.EventPackage;
import javax.microedition.ims.common.MimeType;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.sipservice.publish.*;
import java.util.concurrent.atomic.AtomicReference;

public class PublicationImpl extends IPublication.Stub {
    private static final String TAG = "Service - PublicationImpl";

    private final IServiceMethod serviceMethod;
    private final Dialog dialog;
    private final PublishService publishService;

    private final EventPackage event;

    private String lastETag;


    private final RemoteListenerHolder<IPublicationListener> listenerHolder = new RemoteListenerHolder<IPublicationListener>(IPublicationListener.class);

    private final AtomicReference<PublicationState> state = new AtomicReference<PublicationState>(PublicationState.STATE_INACTIVE);


    enum PublicationState {
        /**
         * The Publication is not active, event state is not published.
         */
        STATE_INACTIVE(1),

        /**
         * A Publication request is sent and the platform is waiting for a response.
         */
        STATE_PENDING(2),

        /**
         * The Publication is active and event state is available for subscription.
         */
        STATE_ACTIVE(3);

        private int code;

        private PublicationState(int code) {
            this.code = code;
        }
    }


    private class PublishStateListenerImpl implements PublishStateListener {
        
        public void publicationDelivered(PublishStateEvent event) {
            Log.i(TAG, "PublishStateListenerImpl.publicationDelivered#started");

            if (event.getEtag() != null) {
                lastETag = event.getEtag();
            }

            setState(PublicationState.STATE_ACTIVE);

            notifyPublicationDelivered();

            Log.i(TAG, "PublishStateListenerImpl.publicationDelivered#finished    lastETag=" + lastETag);
        }

        
        public void publicationDeliveryFailed(PublishStateEvent event) {
            Log.i(TAG, "PublishStateListenerImpl.publicationDeliveryFailed#started");

            setState(PublicationState.STATE_INACTIVE);

            notifyPublicationDeliveryFailed();

            unSubscribe();

            Log.i(TAG, "PublishStateListenerImpl.publicationDeliveryFailed#finished");
        }

        
        public void publicationTerminated(PublishStateEvent event) {
            Log.i(TAG, "PublishStateListenerImpl.publicationTerminated#started");

            if (event.getEtag() != null) {
                lastETag = event.getEtag();
            }

            setState(PublicationState.STATE_INACTIVE);

            notifyPublicationTerminated();

            unSubscribe();

            Log.i(TAG, "PublishStateListenerImpl.publicationTerminated#finished");
        }

        private void unSubscribe() {
            publishService.removePublishStateListener(this);
        }
    }


    public PublicationImpl(final Dialog dialog, final EventPackage event, final PublishService publishService) {
        if (event == null) {
            throw new IllegalArgumentException(String.format("Wrong event = '%s' parameter", event));
        }

        this.dialog = dialog;
        this.event = event;
        this.publishService = publishService;

        this.serviceMethod = new ReferenceServiceMethodImpl(dialog.getRemoteParty(), dialog.getMessageHistory());
    }

    
    public void publish(byte[] body, String contentType,
                        IExceptionHolder exceptionHolder) throws RemoteException {
        Log.i(TAG, "publish#started");

        try {
            if (contentType == null) {
                throw new IllegalArgumentException(String.format("Wrong contentType = '%s' parameter", contentType));
            }

            MimeType type = MimeType.parse(contentType);

            if (type == null) {
                throw new IllegalArgumentException(String.format("Wrong contentType = '%s' parameter", contentType));
            }

            PublishInfo publishInfo = new PublishInfo(event, type, PublishType.INITIAL, lastETag, body);

            if (publishInfo.getPublishType() == PublishType.INITIAL) {
                publishService.addPublishStateListener(dialog, new PublishStateListenerImpl());
            }

            setState(PublicationState.STATE_PENDING);


            //PublishInfo(EventPackage eventType, ContentType contentType, PublishType publishType, String eTag, byte[] body);

            publishService.sendPublishMessage(dialog, publishInfo);

        }
        catch (IllegalArgumentException e) {
            exceptionHolder.setParcelableException(new IError(IError.ERROR_WRONG_PARAMETERS, e.getMessage()));
            Log.e(TAG, e.getMessage(), e);
        }

        Log.i(TAG, "publish#finished");
    }

    
    public void unpublish() throws RemoteException {
        Log.i(TAG, "unpublish#started    lastETag=" + lastETag);

        setState(PublicationState.STATE_PENDING);

        PublishInfo publishInfo = new PublishInfo(event, null, PublishType.REMOVE, lastETag, null);

        publishService.sendUnpublishMessage(dialog, publishInfo);

        Log.i(TAG, "unpublish#finished");
    }

    
    public String getEvent() throws RemoteException {
        return event.stringValue();
    }

    
    public int getState() throws RemoteException {
        return state.get().code;
    }

    public void addListener(IPublicationListener listener) throws RemoteException {
        if (listener != null) {
            listenerHolder.addListener(listener);
        }
    }

    
    public void removeListener(IPublicationListener listener) throws RemoteException {
        if (listener != null) {
            listenerHolder.removeListener(listener);
        }
    }

    
    public IServiceMethod getServiceMethod() throws RemoteException {
        return serviceMethod;
    }

    private void setState(PublicationState publicationState) {
        state.set(publicationState);
    }

    private void notifyPublicationDelivered() {
        Log.i(TAG, "notifyPublicationDelivered#started");
        try {
            listenerHolder.getNotifier().publicationDelivered();
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "notifyPublicationDelivered#finished");
    }

    private void notifyPublicationDeliveryFailed() {
        Log.i(TAG, "notifyPublicationDeliveryFailed#started");
        try {
            listenerHolder.getNotifier().publicationDeliveryFailed();
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "notifyPublicationDeliveryFailed#finished");
    }

    private void notifyPublicationTerminated() {
        Log.i(TAG, "notifyPublicationTerminated#started");
        try {
            listenerHolder.getNotifier().publicationTerminated();
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "notifyPublicationTerminated#finished");
    }


}
