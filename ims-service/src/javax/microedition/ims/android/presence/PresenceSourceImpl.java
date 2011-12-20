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

package javax.microedition.ims.android.presence;

import android.os.RemoteException;
import android.util.Log;

import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.util.RemoteListenerHolder;
import javax.microedition.ims.common.EventPackage;
import javax.microedition.ims.common.MimeType;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.sipservice.ReasonInfo;
import javax.microedition.ims.core.sipservice.publish.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default implementation IPresenceSource.aidl
 *
 * @author Andrei Khomushko
 */
public class PresenceSourceImpl extends IPresenceSource.Stub implements PublishStateListener {
    private static final String TAG = "IPresenceSourceImpl";

    // private static final String AUID = "pidf-manipulation";
    // public static final String DEFAULT_DOCUMENT_PATH = "index";
    // public static final String DEF_MIME_TYPE = "application/pidf+xml";

    private final Dialog dialog;
    private final PublishService publishServicePeer;

    private final RemoteListenerHolder<IPresenceSourceListener> listenerHolder = new RemoteListenerHolder<IPresenceSourceListener>(
            IPresenceSourceListener.class);

    private final AtomicReference<StateCode> state = new AtomicReference<StateCode>(
            StateCode.STATE_INACTIVE);

    private final AtomicReference<String> eTag = new AtomicReference<String>(
            null);

    private enum StateCode {
        STATE_ACTIVE(4), STATE_INACTIVE(1), STATE_PENDING_PUBLISH(2), STATE_PENDING_UNPUBLISH(
                3);

        private int code;

        private StateCode(int code) {
            this.code = code;
        }
    }

    /**
     * Create IPresenceSourceImpl
     *
     * @param userIdentity
     * @throws IllegalArgumentException - if the userIdentity argument is null
     */
    public PresenceSourceImpl(Dialog dialog, PublishService publishService) {
        if (dialog == null) {
            throw new IllegalArgumentException("The dialog argument is null");
        }

        if (publishService == null) {
            throw new IllegalArgumentException(
                    "The publishService argument is null");
        }

        this.dialog = dialog;
        this.publishServicePeer = publishService;
    }

    public String getUserIdentity() {
        return dialog.getLocalParty().getUserInfo().toUri();
    }

    
    public int getState() throws RemoteException {
        return state.get().code;
    }

    private void setState(StateCode stateCode) {
        state.set(stateCode);
    }

    
    public void publish(String source) throws RemoteException {
        PublishType publishType = (state.get() == StateCode.STATE_INACTIVE ? PublishType.INITIAL
                : PublishType.MODIFY);

        if (publishType == PublishType.INITIAL) {
            publishServicePeer.addPublishStateListener(dialog, this);
        }


        PublishInfo publishInfo = createPublishInfo(publishType, source);
        publishServicePeer.sendPublishMessage(dialog, publishInfo);

        setState(StateCode.STATE_PENDING_PUBLISH);
    }

    
    public void unpublish() throws RemoteException {
        PublishInfo publishInfo = createPublishInfo(PublishType.REMOVE, null);
        publishServicePeer.sendUnpublishMessage(dialog, publishInfo);

        setState(StateCode.STATE_PENDING_UNPUBLISH);
    }

    private PublishInfo createPublishInfo(PublishType publishType, String source) {
        byte[] body = source != null ? source.getBytes() : null;
        PublishInfo publishInfo = new PublishInfo(
                EventPackage.PRESENCE,
                MimeType.APP_PIDF_XML,
                publishType,
                eTag.get(),
                body
        );

        return publishInfo;
    }

    
    public void addListener(IPresenceSourceListener listener)
            throws RemoteException {
        listenerHolder.addListener(listener);
    }

    
    public void removeListener(IPresenceSourceListener listener)
            throws RemoteException {
        listenerHolder.removeListener(listener);
    }

    
    public void publicationDelivered(PublishStateEvent event) {
        setState(StateCode.STATE_ACTIVE);
        try {
            eTag.set(event.getEtag());
            listenerHolder.getNotifier().publicationDelivered();
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    
    public void publicationDeliveryFailed(PublishStateEvent event) {
        setState(StateCode.STATE_INACTIVE);
        try {
            listenerHolder.getNotifier().publicationFailed(createReasonInfo(event.getReasonInfo()));
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    
    public void publicationTerminated(PublishStateEvent event) {
        setState(StateCode.STATE_INACTIVE);
        publishServicePeer.removePublishStateListener(this);

        try {
            listenerHolder.getNotifier().publicationTerminated(createReasonInfo(event.getReasonInfo()));
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private IReasonInfo createReasonInfo(ReasonInfo event) {
        return new IReasonInfo(event.getReasonPhrase(), event.getReasonType(), event.getStatusCode());
    }
}
