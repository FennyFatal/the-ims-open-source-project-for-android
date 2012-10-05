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

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.ServiceImpl;
import com.android.ims.configuration.AppConfiguration;
import com.android.ims.configuration.Codec;
import com.android.ims.core.media.*;
import com.android.ims.core.media.util.DirectionUtils;
import com.android.ims.core.media.util.MediaUtils;
import com.android.ims.rpc.RemoteSessionListener;
import com.android.ims.util.CollectionsUtils;
import com.android.ims.util.ValidatorUtil;
import com.android.ims.util.CollectionsUtils.Predicate;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.android.core.*;
import javax.microedition.ims.android.core.media.IMedia;
import javax.microedition.ims.core.*;
import javax.microedition.ims.core.media.Media;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default Session implementation. For more details @see Session
 * 
 * @author ext-akhomush
 */
public class SessionImpl extends ServiceMethodImpl implements Session, SessionListener {

    public static final int REJECT_CODE_BUSY_HERE = 486;

    public static final int REJECT_CODE_NOT_ACCEPTABLE = 488;

    public static final int CODE_SESSION_PROGRESS = 183;

    public static final int CODE_SESSION_RINGING = 180;

    private static final int SESSION_STATE_NEGOTIATING = 2;

    private SessionListener mCurrentSessionListener;

    private final List<MediaExt> medias = new ArrayList<MediaExt>();

    private final RemoteSessionListener remoteSessionListener;

    private final MediaOfferHelper mediaOfferHelper = new MediaOfferHelper();

    // private final MediaAnswerHelper mediaAnswerHelper = new
    // MediaAnswerHelper();

    private final SessionDescriptorImpl sessionDescriptor;

    private boolean locallyInitiated;

    private final ISession sessionPeer;

    private ReferenceImpl reference;

    private final AtomicBoolean mediasEarlyAccepted = new AtomicBoolean(false);

    private boolean useResourceReservation;

    private final ServiceImpl mService;

    private final String srtpCryptoAlgorithm;

    private final DtmfPayload dtmfPayload;

    private WakeLock mWakeLock;

    // in case the lock is never released
    private static final int WAKELOCK_TIMEOUT = 60*1000;

    public interface SessionPrepareListener {
        void sessionPrepared();
    }

    public SessionImpl(final ISession mSession, final IServiceMethod serviceMethod,
            boolean locallyInitiated, final ServiceImpl service, DtmfPayload dtmfPayload) {
        super(serviceMethod);
        Log.d(TAG, "session created " + (locallyInitiated ? "locally" : "remotely"));
        Log.d(TAG, "dtmfPayload = " + dtmfPayload);

        this.srtpCryptoAlgorithm = service.getConfiguration().getSrtpCryptoAlgorithm();

        assert mSession != null;
        this.sessionPeer = mSession;

        this.mService = service;

        this.locallyInitiated = locallyInitiated;
        this.remoteSessionListener = new RemoteSessionListener(this, mService);
        // this.dtfmPayload = service.getConfiguration().getDtfmPayload();
        remoteSessionListener.addListener(this);

        this.dtmfPayload = dtmfPayload;

        try {
            this.useResourceReservation = mSession.useResourceReservation();
            mSession.addListener(remoteSessionListener);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        this.sessionDescriptor = createSessionDescriptor(mSession);

        PowerManager powerManager = (PowerManager)getService().getContext().getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    }

    public ISession getSession() {
        return sessionPeer;
    }

    private SessionDescriptorImpl createSessionDescriptor(final ISession session) {
        SessionDescriptorImpl descriptor = null;
        try {
            ISessionDescriptor iSessionDescriptor = session.getSessionDescriptor();
            assert iSessionDescriptor != null;
            descriptor = new SessionDescriptorImpl(iSessionDescriptor);
        } catch (RemoteException e) {
            Log.e(TAG, "Cann't get session descriptor", e);
        }

        return descriptor;
    }

    /**
     * Handling server invite. Examines the SDP offer in a SIP INVITE request
     * and creates the offered medias locally. These can originate from both
     * Session.start() and Session.Update().
     * 
     * @return true if invite can be handled
     */
    public boolean handlingIncommingInvite() {
        Log.i(TAG, "handlingIncommingInvite#started");
        boolean inviteHandled = false;

        // restoreInternal();
        List<IMedia> iMedias = getRemoteMedias();
        Log.i(TAG, "handlingIncommingInvite#offered medias, size = " + iMedias.size());
        Log.d(TAG, "handlingIncommingInvite#offered medias = " + iMedias);

        if (iMedias.size() != 0) {
            for (IMedia iMedia : iMedias) {
                // New media, create and add to session
                MediaExt offeredMedia = createNewMediaByRemoteOffer(iMedia);

                // TODO AK test
                // Log.i(TAG, "Add fake remote offer crypto");

                medias.add(offeredMedia);
            }
            Log.i(TAG, "handlingIncommingInvite#added medias count = " + medias.size());
            Log.d(TAG, "handlingIncommingInvite#added medias = " + medias);

            inviteHandled = processSupportedMedia(medias);

        } else {
            inviteHandled = true;
        }

        Log.i(TAG, "handlingIncommingInvite#finished: inviteHandled = " + inviteHandled);
        return inviteHandled;
    }

    /**
     * Check is at least one media supported. If media doesn't supported: 1)set
     * port to 0 2)set state to deleted
     * 
     * @param medias - medias to check
     * @return at least one media supported
     */
    private boolean processSupportedMedia(List<MediaExt> medias) {
        boolean atLeastOneMediaSupported = false;
        for (MediaExt media : medias) {
            Log.d(TAG, "processSupportedMedia# media = " + media);
            boolean isCodecsCanBeHandled = media.handleSupportedCodec();
            Log.d(TAG, "processSupportedMedia#has supported codecs = " + isCodecsCanBeHandled);

            boolean isCryptoCanBeHandled = handleSupportedCrypto(media.getLocalMediaDescriptor(0));
            Log.d(TAG, "processSupportedMedia#isCryptoCanBeHandled = " + isCryptoCanBeHandled);

            boolean isMediaSupported = isCodecsCanBeHandled && isCryptoCanBeHandled;

            if (isMediaSupported) {
                // media.setUpdateState(Media.UPDATE_MODIFIED);
                // media.setState(Media.STATE_ACTIVE);
                atLeastOneMediaSupported = true;
            } else {
                media.getLocalMediaDescriptor(0).setPort(0);
                media.setState(Media.STATE_DELETED);
            }

        }
        Log.i(TAG, "processSupportedMedia#at least one media supported");
        return atLeastOneMediaSupported;
    }

    /**
     * Check if at least one crypto supported by stack
     * 
     * @param media
     * @return
     */

    private boolean handleSupportedCrypto(final MediaDescriptorImpl mediaDescriptor) {
        // if offer doesn't contains crypto keys
        CryptoParam[] cryptoParams = mediaDescriptor.getCryptoParams();
        boolean isCryptoCanBeHandled = cryptoParams.length == 0;

        for (CryptoParam cryptoParam : cryptoParams) {
            if (isCryptoCanBeHandled) {
                mediaDescriptor.removeCryptoParam(cryptoParam);
                continue;
            }

            if (isCryptoSupported(cryptoParam)) {
                String localAuthKey = createAuthKey(getService().getConfiguration());
                cryptoParam.setKey(localAuthKey);
                isCryptoCanBeHandled = true;
            } else {
                mediaDescriptor.removeCryptoParam(cryptoParam);
            }
        }
        return isCryptoCanBeHandled;
    }

    private String createAuthKey(AppConfiguration configuration) {
        String localEncriptionKey = null;
        String cryptoAlgorithm = configuration.getSrtpCryptoAlgorithm();
        try {
            localEncriptionKey = EncriptionKeyGenerator.generateEncriptionKey(cryptoAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage());
        }
        return localEncriptionKey;
    }

    public boolean isCryptoSupported(CryptoParam cryptoParam) {
        return srtpCryptoAlgorithm.equalsIgnoreCase(cryptoParam.getAlgorithm());
    }

    /**
     * Create new media based on remote imedia.
     * 
     * @param iMedia
     * @return
     */
    private MediaExt createNewMediaByRemoteOffer(final IMedia iMedia) {
        Log.d(TAG, "createNewMediaByRemoteOffer#iMedia = " + iMedia);

        MediaExt offeredMedia = null;

        MediaDescriptorImpl mediaDescriptor = mediaOfferHelper.parseMediaDescriptor(iMedia);

        offeredMedia = MediaFactory.INSTANCE.createMediaBasedOnIncomingOffer(mediaDescriptor,
                getService().getLocalAddress(), getService().getConfiguration(), getService()
                        .getContext(), dtmfPayload);
        offeredMedia.setState(Media.STATE_PENDING);
        offeredMedia.setMediaInitiated(true);

        Log.d(TAG, "createNewMediaByRemoteOffer#offeredMedia = " + offeredMedia);
        return offeredMedia;
    }

    private class AcceptOnUpdateRunner extends SessionAdapter {
        private static final String TAG = "AcceptOnUpdateRunner";

        public void sessionUpdated(Session session) {
            Log.i(TAG, "sessionUpdated#start");
            try {
                doAccept(false);
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
                throw new RuntimeException("Cann't accept session, message: " + e.getMessage());
                // throw new ImsException("Cann't accept session, message: " +
                // e.getMessage());
            } finally {
                cleanUp();
                Log.i(TAG, "sessionUpdated#finsh");
            }
        }

        public void sessionUpdateFailed(Session session) {
            cleanUp();
        }

        private void cleanUp() {
            remoteSessionListener.removeListener(this);
        }
    };

    /**
     * @see Session#accept()
     */
    public void accept() throws ImsException {
        Log.i(TAG, "accept#started");

        int state = getState();
        if (state != STATE_NEGOTIATING && state != STATE_RENEGOTIATING
                && state != STATE_REESTABLISHING) {
            throw new IllegalStateException(
                    "The Session is not in STATE_NEGOTIATING or STATE_RENEGOTIATING, state: "
                            + state);
        }

        try {
            // TODO AK isn't clear what the case is
            /*
             * if (hasPendingUpdate() && useResourceReservation) {
             * remoteSessionListener.addListener(new AcceptOnUpdateRunner());
             * doUpdate(); } else {
             */doAccept(false);
            /*
             * }
             */
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException(e.getMessage(), e);
        } catch (RuntimeException e) {
            Log.e(TAG, e.getMessage(), e);
            throw e;
        }

        Log.i(TAG, "accept#finished");
    }

    private void doAccept(boolean isAutoAccept) throws RemoteException {
        // setting added body to service side
        flushResponceBodies();

        List<IMedia> acceptedMedias = new ArrayList<IMedia>();

        boolean updated = mediaUpdated();

        if (!useResourceReservation) {
            acceptOfferedMediaDescriptors();

            //acceptedMedias.addAll(createIMediasForAnswer(medias));
            acceptedMedias.addAll(createIMedias(medias));
        } else if (updated) {
            acceptOfferedMediaDescriptors();
            //acceptedMedias.addAll(createIMediasForAnswer(medias));
            acceptedMedias.addAll(createIMedias(medias));
        }

        Log.i(TAG, "accept#send accept to service, iMedias size = " + acceptedMedias.size());
        sessionPeer.accept(acceptedMedias, isAutoAccept);
        Log.i(TAG, "accept#accept sent");
    }

    private void doPreaccept() throws RemoteException {
        // setting added body to service side
        flushResponceBodies();

        acceptOfferedMediaDescriptors();

        // List<IMedia> iMedias = createIMediasForAnswer(medias);
        List<IMedia> iMedias = createIMedias(medias);

        Log.i(TAG, "preaccept#send accept to service, iMedias size = " + iMedias.size());
        sessionPeer.preaccept(iMedias);
        Log.i(TAG, "preaccept#accept sent");
    }

    private boolean mediaUpdated() {
        for (MediaExt media : medias) {
            if (media.getState() == Media.STATE_ACTIVE
                    && media.getUpdateState() == Media.UPDATE_MODIFIED)
                return true;
        }
        return false;
    }

    /**
     * Retrieve list media descriptors for SDP.
     **/
    private List<IMedia> createIMedias(final List<MediaExt> medias) {
        List<MediaDescriptorImpl> descriptors = retrieveMediaDescriptorsForSDP(medias);
        return mediaOfferHelper.convertToIMedias(descriptors);
    }

    /*
     * private List<IMedia> createIMediasForOffer(final List<MediaImpl> medias)
     * { List<MediaDescriptorImpl> descriptors =
     * retrieveMediaDescriptorsForSDP(medias, true); return
     * mediaOfferHelper.convertToIMedias(descriptors); } private List<IMedia>
     * createIMediasForAnswer(final List<MediaImpl> medias) {
     * List<MediaDescriptorImpl> descriptors =
     * retrieveMediaDescriptorsForSDP(medias, false); return
     * mediaOfferHelper.convertToIMedias(descriptors); }
     */
    /**
     * @see Session#createMedia(String, int)
     */

    public Media createMedia(Media.MediaType type, int direction) {
        Log.d(TAG,
                String.format("createMedia#type = %s, direction = %s", type,
                        DirectionUtils.convertToString(direction)));

        if (!DirectionUtils.isDirectionValid(direction)) {
            throw new IllegalArgumentException(String.format("Direction argument is invalid: %s",
                    direction));
        }

        int state = getState();
        if (state != STATE_ESTABLISHED && state != STATE_INITIATED && state != STATE_NEGOTIATING) {
            throw new IllegalStateException(
                    "Session is not in STATE_ESTABLISHED, STATE_INITIATED, state: " + state);
        }

        MediaExt media = MediaFactory.INSTANCE.createMedia(type, direction, Media.STATE_INACTIVE,
                getService().getLocalAddress(), getService().getConfiguration(), getService()
                        .getContext(), dtmfPayload);
        if (media == null) {
            throw new IllegalArgumentException("Could not be created for type: " + type);
        }

        addMedia(media);

        return media;
    }

    /**
     * Adds a created Media to the session, used both when Media is created with
     * session.createMedia() and when it's created based on an invite.
     * 
     * @param mediaImpl - media to add
     */
    private void addMedia(MediaExt media) {
        medias.add(media);
    }

    /**
     * @see Session#getMedia()
     */
    public Media[] getMedia() {
        return medias.toArray(new Media[0]);
    }

    /**
     * @see Session#getSessionDescriptor()
     */
    public SessionDescriptor getSessionDescriptor() {
        return sessionDescriptor;
    }

    /**
     * @see Session#getState()
     */
    public int getState() {
        int state = -1;
        try {
            state = sessionPeer.getState();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return state;
    }

    /**
     * @see Session#hasPendingUpdate()
     */
    public boolean hasPendingUpdate() {
        final boolean isPendingUpdates;
        // int state = getState();
        // if (state == STATE_ESTABLISHED) {
        boolean isInactiveMedias = hasInactiveMedias();
        boolean isChangedMedias = hasChangedMedias();
        // }

        isPendingUpdates = isInactiveMedias || isChangedMedias;
        Log.d(TAG,
                String.format(
                        "hasPendingUpdate#isPendingUpdates = %s, (isInactiveMedias = %s, isChangedMedias = %s)",
                        isPendingUpdates, isInactiveMedias, isChangedMedias));

        return isPendingUpdates;
    }

    private boolean hasInactiveMedias() {
        boolean retValue = false;

        for (Media media : medias) {
            if (media.getState() == Media.STATE_INACTIVE) {
                retValue = true;
                break;
            }
        }

        return retValue;
    }

    private boolean hasChangedMedias() {
        boolean retValue = false;

        for (Media media : medias) {
            if (media.getState() == Media.STATE_ACTIVE
                    && media.getUpdateState() != Media.UPDATE_UNCHANGED) {
                retValue = true;
                break;
            }
        }

        return retValue;
    }

    /**
     * @see Session#reject()
     */
    public void reject() {
        int sessionState = getState();
        if (sessionState == Session.STATE_NEGOTIATING) {
            reject(REJECT_CODE_BUSY_HERE);
        } else {
            reject(REJECT_CODE_NOT_ACCEPTABLE);
        }
    }

    /**
     * @see Session#reject(int)
     */
    public void reject(int statusCode) {
        Log.i(TAG, "reject#started, statusCode = " + statusCode);

        int state = getState();
        if (state != STATE_NEGOTIATING && state != STATE_RENEGOTIATING) {
            throw new IllegalStateException(
                    "Session not in STATE_NEGOTIATING or STATE_RENEGOTIATING, state = " + state);
        }

        if (!isRejectCodeValid(statusCode)) {
            throw new IllegalArgumentException("StatusCode argument is not valid identifier");
        }

        // setting added body to service side
        flushResponceBodies();

        rejectPendingMedias();

        try {
            Log.i(TAG, "reject#send reject to service");
            sessionPeer.reject(statusCode);
            Log.i(TAG, "reject#reject sent to service");
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "reject#finished");
    }

    private boolean isRejectCodeValid(int rejectCode) {
        return rejectCode == 433 || rejectCode == 480 || rejectCode == 488 || rejectCode == 600
                || rejectCode == 603 || rejectCode == 486;
    }

    private void rejectPendingMedias() {
        for (MediaExt media : medias) {
            if (media.getState() == Media.STATE_PENDING) {
                media.setState(Media.STATE_DELETED);
                try {
                    media.cleanMedia();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

    public void rejectWithDiversion(String alternativeUserAddress) {
        // setting added body to service side
        flushResponceBodies();
        try {
            sessionPeer.rejectWithDiversion(alternativeUserAddress);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * @see Session#removeMedia(Media)
     */
    public void removeMedia(Media media) {
        if (media == null) {
            throw new IllegalArgumentException("Media parameter cann't be null");
        }

        int state = getState();
        if (state != STATE_ESTABLISHED && state != STATE_INITIATED) {
            throw new IllegalStateException("Session is not in STATE_ESTABLISHED, STATE_INITIATED");
        }

        if (!medias.contains(media)) {
            throw new IllegalArgumentException("Media does not exist in the Session");
        }

        if (media.getState() == Media.STATE_ACTIVE) {
            ((MediaExt)media).setUpdateState(Media.UPDATE_REMOVED);
        } else if (media.getState() == Media.STATE_INACTIVE) {
            ((MediaExt)media).setState(Media.STATE_DELETED);
            medias.remove(media);
        }

        if (state == STATE_ESTABLISHED) {
            try {
                update();
            } catch (ImsException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public void restore() {
        // TODO Auto-generated method stub
    }

    /**
     * @see Session#setListener(SessionListener)
     */
    public void setListener(SessionListener listener) {
        /*
         * if (mCurrentSessionListener != null) {
         * //mSession.removeListener(mCurrentSessionListener);
         * remoteSessionListener.removeListener(mCurrentSessionListener); } if
         * (listener != null) { remoteSessionListener.addListener(listener); }
         */mCurrentSessionListener = listener;
    }

    /**
     * @see Session#start()
     */
    public void start() throws ImsException {
        Log.i(TAG, "start#started");
        if (!isMediasValid(medias)) {
            throw new ImsException(
                    "Media in the Session is not initialized correctly or if there are no Media in the Session");
        }

        int state = getState();
        if (state != STATE_INITIATED) {
            throw new IllegalStateException("The Session is not in STATE_INITIATED, state: "
                    + state);
        }

        // setting added body to service side
        flushRequestBodies();

        processInitialOffer();

        // List<IMedia> iMedias = createIMediasForOffer(medias);
        List<IMedia> iMedias = createIMedias(medias);
        try {
            Log.i(TAG, "start#send invite to service, offered media size = " + iMedias.size());
            sessionPeer.start(iMedias);
            Log.i(TAG, "start#invite sent");
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException(e.getMessage(), e);
        }

        // mediaOfferHelper.setInactiveMediaPending(medias);

        // preparePendingMedia();

        Log.i(TAG, "start#finished");
    }

    private void processInitialOffer() {
        for (MediaExt media : medias) {
            media.setState(Media.STATE_PENDING);

            try {
                media.prepareMedia();
                media.updateModeProperties();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    private static boolean isMediasValid(List<MediaExt> medias) {
        if (medias.size() == 0) {
            return false;
        }

        boolean isValid = true;

        for (MediaExt media : medias) {
            if (!media.isMediaInitiated()) {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    private List<IMedia> getRemoteMedias() {
        final List<IMedia> iMedias = new ArrayList<IMedia>();
        try {
            iMedias.addAll(sessionPeer.getMedias());
            Log.i(TAG, "getRemoteMedias#iMedias = " + iMedias);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return iMedias;
    }

    /**
     * @see Session#terminate()
     */
    public void terminate() {
        // setting added body to service side
        flushRequestBodies();
        try {
            cleanMedia();
            if (getState() == STATE_INITIATED) {
                sessionPeer.terminate();
                // will transit directly to STATE_TERMINATED, the
                // sessionTerminated callback will not be invoked
                close();
            } else {
                sessionPeer.terminate();
            }

        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Called when session terminated
     */
    private void close() {
        try {
            remoteSessionListener.removeListener(this);
            sessionPeer.removeListener(remoteSessionListener);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void update() throws ImsException {
        Log.i(TAG, "update#started");
        int state = getState();
        /*
         * if (state != STATE_ESTABLISHED) { throw new
         * IllegalStateException("The Session is not in STATE_ESTABLISHED"); }
         */

        if (!hasPendingUpdate()) {
            throw new IllegalStateException("There are no updates to be made to the session");
        }

        if (!isMediasValid(medias)) {
            throw new ImsException(
                    "Media in the Session is not initiated correctly or if there are no Media in the Session");
        }

        try {
            doUpdate();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException(e.getMessage(), e);
        }

        mediaOfferHelper.setInactiveMediaPending(medias);
        Log.i(TAG, "update#finished");
    }

    private void doUpdate() throws RemoteException {
        Log.i(TAG, "doUpdate#started");
        // setting added body to service side
        flushRequestBodies();

        // processUpdateOffer
        for (MediaExt media : medias) {
            if ((media.getState() == Media.STATE_ACTIVE && media.getUpdateState() != Media.UPDATE_UNCHANGED)) {
                // direction changed
                media.updateModeProperties();
            }
        }

        // List<IMedia> iMedias = createIMediasForOffer(medias);
        List<IMedia> iMedias = createIMedias(medias);

        sessionPeer.update(iMedias);
        Log.i(TAG, "doUpdate#finished");
    }

    /**
     * Retrieve list media descriptors for client SDP offer for an RE-INVITE
     * request. If a media in STATE_ACTIVE has been changed, the media
     * descriptor from the media proposal is used in the SDP.
     * 
     * @return list media descriptors for client SDP offer
     */
    private List<MediaDescriptorImpl> retrieveMediaDescriptorsForSDP(List<MediaExt> medias/*
                                                                                           * ,
                                                                                           * boolean
                                                                                           * isOffer
                                                                                           */) {
        List<MediaDescriptorImpl> descriptors = new ArrayList<MediaDescriptorImpl>();

        for (MediaExt media : medias) {
            if (media.getState() == Media.STATE_ACTIVE
                    && media.getUpdateState() == Media.UPDATE_MODIFIED) {
                descriptors.addAll(Arrays.asList((MediaDescriptorImpl[])media.getProposal()
                        .getMediaDescriptors()));
            } else if (media.getState() == Media.STATE_ACTIVE
                    && media.getUpdateState() == Media.UPDATE_REMOVED) {
                MediaDescriptorImpl[] mediaDescriptors = media.getLocalMediaDescriptors();
                for (MediaDescriptorImpl mediaDescriptor : mediaDescriptors) {
                    MediaDescriptorImpl descriptorCopy = mediaDescriptor.createCopy();
                    descriptorCopy.setPort(0);
                    descriptors.add(descriptorCopy);
                }
            } else {
                descriptors.addAll(Arrays.asList(media.getLocalMediaDescriptors()));
                /*
                 * if(isOffer) { //offer shouldn't contain already deleted
                 * medias if(media.getState() != Media.STATE_DELETED) {
                 * descriptors
                 * .addAll(Arrays.asList(media.getLocalMediaDescriptors())); } }
                 * else {
                 * descriptors.addAll(Arrays.asList(media.getLocalMediaDescriptors
                 * ())); }
                 */}
        }

        return descriptors;
    }

    /**
     * Accept offered media descriptors
     */
    private void acceptOfferedMediaDescriptors() {
        for (MediaExt media : medias) {
            if (media.getState() == Media.STATE_PENDING) {
                try {
                    media.prepareMedia();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                media.setState(Media.STATE_ACTIVE);
            } else if (media.getState() == Media.STATE_ACTIVE) {
                if (media.getUpdateState() == Media.UPDATE_MODIFIED) {
                    // Copy media descriptor from accepted proposed media to
                    // active media.

                    MediaDescriptorImpl[] localDescriptors = createDescriptorsByProposal(media);

                    media.setMediaDescriptors(localDescriptors);
                    media.setUpdateState(Media.UPDATE_UNCHANGED);
                } else if (media.getUpdateState() == Media.UPDATE_REMOVED) {
                    MediaDescriptorImpl[] mediaDescriptors = media.getLocalMediaDescriptors();
                    for (MediaDescriptorImpl mediaDescriptor : mediaDescriptors) {
                        mediaDescriptor.setPort(0);
                    }
                    media.setState(Media.STATE_DELETED);
                }
            }
            media.updateModeProperties();
        }
    }

    private MediaDescriptorImpl[] createDescriptorsByProposal(MediaExt media) {
        MediaProposalImpl proposal = (MediaProposalImpl)media.getProposal();
        MediaDescriptorImpl[] proposalDescriptors = (MediaDescriptorImpl[])proposal
                .getMediaDescriptors();

        // TODO need to create new media descriptor based on proposal and
        // originated
        for (int i = 0, count = media.getLocalMediaDescriptors().length; i < count; i++) {
            MediaDescriptorImpl localMediaDescriptor = media.getLocalMediaDescriptor(i);
            CryptoParam[] cryptoParams = localMediaDescriptor.getCryptoParams();

            MediaDescriptorImpl proposalDescriptor = proposalDescriptors[i];
            proposalDescriptor.setCryptoParam(cryptoParams);
        }

        return proposalDescriptors;
    }

    public void sessionAlerting(Session session) {
        Log.d(TAG, "*** sessionAlerting#started");

        final Message lastResponse = findLastResponse();

        if (isEarlyResponse(lastResponse)) {
            // process 183 response with medias
            if (!mediasEarlyAccepted.get()) {
                List<IMedia> iMedias = getRemoteMedias();
                handleMediaAnswer(iMedias);
            }

            mediasEarlyAccepted.compareAndSet(false, true);
        }

        if (mCurrentSessionListener != null) {
            mCurrentSessionListener.sessionAlerting(session);
        }

        Log.d(TAG, "*** sessionAlerting#finished");
    }

    private boolean isEarlyResponse(Message response) {
        boolean isEarlyResponse = false;

        if (response != null) {
            switch (response.getStatusCode()) {
                case CODE_SESSION_PROGRESS:
                    isEarlyResponse = true;
                case CODE_SESSION_RINGING: {
                    String sdpContentType = getContentLengthHeaderValue(response);
                    isEarlyResponse = sdpContentType != null;
                    break;
                }
            }
        }

        return isEarlyResponse;
    }

    private String getContentLengthHeaderValue(final Message response) {
        return getHeaderByValuePredicate(response, "Content-Length",
                new CollectionsUtils.Predicate<String>() {
                    @Override
                    public boolean evaluate(String contentLengthValue) {
                        int contentLength = -1;
                        if (!TextUtils.isEmpty(contentLengthValue)) {
                            try {
                                contentLength = Integer.parseInt(contentLengthValue);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Can't parse Content-Length value: " + contentLengthValue);
                            }
                        }

                        return contentLength > 0;
                    }
                });
    }

    private Message findLastResponse() {
        final Message[] responses = getPreviousResponses(Message.SESSION_START);
        return responses.length > 0 ? responses[responses.length - 1] : null;
    }

    public void sessionStartFailed(Session session) {
        Log.d(TAG, "sessionStartFailed#started");

        if (getSessionStatus() == REJECT_CODE_NOT_ACCEPTABLE) {
            rejectPendingMedias();
           /*
            *for (MediaExt media : medias) {
            *    media.setState(Media.STATE_DELETED);
            *}
            */} else {
            cleanMedia();
        }

        if (mCurrentSessionListener != null) {
            mCurrentSessionListener.sessionStartFailed(session);
        }

        Log.d(TAG, "sessionStartFailed#finished");
    }

    public void sessionStarted(Session session) {
        Log.d(TAG, "sessionStarted#starting");

        if (locallyInitiated) {
            // handle 200 response for invite
            if (!mediasEarlyAccepted.get()) {
                List<IMedia> iMedias = getRemoteMedias();
                handleMediaAnswer(iMedias);
            }
        } else {
            answerToOffererDelivered();
            processMedia();
            updateMediaModeProperties();
        }

        if (mCurrentSessionListener != null) {
            mCurrentSessionListener.sessionStarted(session);
        }
    }

    private void answerToOffererDelivered() {

    }

    private void processMedia() {
        Log.i(TAG, "sessionStarted#processMedia");
        for (MediaExt media : medias) {
            Log.i(TAG, "processMedia#mediaState: " + media.getState());
            if (media.getState() == Media.STATE_PENDING) {
                if (media.getLocalMediaDescriptor(0).getPort() == 0) {
                    media.setState(Media.STATE_DELETED);
                    try {
                        media.cleanMedia();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                } else {
                    media.setState(Media.STATE_ACTIVE);
                    try {
                        Log.i(TAG, "processMedia#mediaState: STATE_ACTIVE");
                        media.processMedia();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * @see SessionListener#sessionReferenceReceived(Session, Reference)
     */
    public void sessionReferenceReceived(Session session, Reference reference) {
        if (mCurrentSessionListener != null) {
            mCurrentSessionListener.sessionReferenceReceived(session, reference);
        }
    }

    /**
     * Inspects 200 OK response to a SIP INVITE or RE-INVITE.
     * 
     * @throws IllegalStateException - exception is thrown if medias in response
     *             doesn't satisfy specification
     * @param iMedias - asewer medias
     * @return true - if at least one media supported
     */
    private void handleMediaAnswer(final List<IMedia> iMedias) throws IllegalStateException {
        Log.d(TAG, "iMedias.size() = " + iMedias.size());

        if (iMedias.size() != medias.size()) {
            Log.e(TAG,
                    "Answer media descriptors has different length than originated. Answer can'nt be handled.");
            // throw new
            // IllegalStateException("Answer media descriptors has different length than originated. Answer can'nt be handled.");
            return;
        }

        for (int i = 0; i < iMedias.size(); i++) {
            MediaDescriptorImpl offerMediaDescriptor = medias.get(i).getLocalMediaDescriptor(0);
            IMedia answerMedia = iMedias.get(i);
            if (!offerMediaDescriptor.getTransport().equals(answerMedia.getProtocol())
                    || !offerMediaDescriptor.getMediaType().equals(answerMedia.getType())) {
                Log.e(TAG, "HandleInviteMediaAnswer#Offer and answer do not match.");
                throw new IllegalStateException(
                        "Answer media descriptors has different length than originated. Answer can'nt be handled.");
            }
        }

        for (int i = 0; i < iMedias.size(); i++) {
            MediaExt offerMedia = medias.get(i);
            IMedia answerMedia = iMedias.get(i);

            MediaDescriptorImpl offerMediaDescriptor = offerMedia.getLocalMediaDescriptor(0);
            MediaDescriptorImpl answerDescriptor = mediaOfferHelper
                    .parseMediaDescriptor(answerMedia);

            // TODO AK test
            // Log.i(TAG, "Add fake remote answer crypto");

            if (answerDescriptor.getTransport() == StreamMediaImpl.PROTOCOL_RTP_SAVP) {
                assert answerDescriptor.getCryptoParams().length > 0;
            }

            offerMedia.setRemoteMediaDescriptor(answerDescriptor);

            if (offerMedia.getState() == Media.STATE_PENDING) {

                Log.d(TAG,
                        "*** SessionImpl.handleMediaAnswer offerMedia.getState() = Media.STATE_PENDING");

                removeRejectedMediaFormats(offerMediaDescriptor, answerDescriptor);
                // TODO remote party can delete some attributes(for example some
                // rtpmap attributes)
                if (answerDescriptor.getPort() == 0) {
                    offerMedia.setState(Media.STATE_DELETED);
                    try {
                        offerMedia.cleanMedia();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                } else {
                    offerMedia.setState(Media.STATE_ACTIVE);
                    try {
                        Log.i(TAG, "processMedia#mediaState: STATE_ACTIVE");
                        offerMedia.processMedia();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }

                }
            } else if (offerMedia.getState() == Media.STATE_ACTIVE) {

                Log.d(TAG,
                        "*** SessionImpl.handleMediaAnswer offerMedia.getState() = Media.STATE_ACTIVE");

                if (offerMedia.getUpdateState() == Media.UPDATE_MODIFIED) {
                    // Copy media descriptor from accepted proposed media to
                    // active media.
                    MediaProposalImpl proposal = (MediaProposalImpl)offerMedia.getProposal();
                    offerMedia.setMediaDescriptors((MediaDescriptorImpl[])proposal
                            .getMediaDescriptors());
                } else if (offerMedia.getUpdateState() == Media.UPDATE_REMOVED) {
                    MediaDescriptorImpl[] mediaDescriptors = offerMedia.getLocalMediaDescriptors();
                    for (MediaDescriptorImpl mediaDescriptor : mediaDescriptors) {
                        mediaDescriptor.setPort(0);
                    }
                    offerMedia.setState(Media.STATE_DELETED);
                }
                offerMedia.setUpdateState(Media.UPDATE_UNCHANGED);
            } else {
                Log.e(TAG, "Not yet implemented for state: " + offerMedia.getState());
            }
            offerMedia.updateModeProperties();

            Log.d(TAG, "*** Finally SessionImpl.handleMediaAnswer offerMedia.getState() = "
                    + offerMedia.getState());
        }
    }

    private void removeRejectedMediaFormats(final MediaDescriptorImpl offerDescriptor,
            final MediaDescriptorImpl answerDescriptor) {
        Set<Integer> offeredMediaFormats = MediaUtils.parseMediaFormats(offerDescriptor
                .getMediaFormat());
        Set<Integer> acceptedMediaFormats = MediaUtils.parseMediaFormats(answerDescriptor
                .getMediaFormat());

        if (offeredMediaFormats.size() == acceptedMediaFormats.size()) {
            if (offeredMediaFormats.equals(acceptedMediaFormats)) {
                Log.d(TAG, "removeRejectedMediaFormats#remote party accepted all media formats");
                return;
            } else {
                assert false : "offeredMediaFormats.size() == acceptedMediaFormats.size() but not equals";
            }
        } else {
            Log.d(TAG, "removeRejectedMediaFormats#remote party accepted only some media formats");
        }

        offerDescriptor.setMediaFormat(answerDescriptor.getMediaFormat());

        for (String attribute : offerDescriptor.getAttributes()) {
            if (attribute.startsWith("rtpmap:")) {
                String codecValue = attribute.substring("rtpmap:".length());
                Codec codec = null;
                try {
                    codec = Codec.valueOf(codecValue);
                    if (!acceptedMediaFormats.contains(codec.getType())) {
                        offerDescriptor.removeAttributeInternal(attribute);
                        Log.d(TAG,
                                "removeRejectedMediaFormats#remote party rejected media format: "
                                        + attribute);
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

    /**
     * @see SessionListener#sessionTerminated(Session)
     */
    public void sessionTerminated(Session session) {
        Log.i(TAG, "SESSION_TERMINATED at jsr side");
        // updateMediaModeProperties();
        // mediaAnswerHelper.handleOfferTerminate(medias);
        cleanMedia();
        close();

        if (mCurrentSessionListener != null) {
            mCurrentSessionListener.sessionTerminated(session);
        }
    }

    /**
     * @see SessionListener#sessionUpdateFailed(Session)
     */
    public void sessionUpdateFailed(Session session) {
        Log.d(TAG, "sessionUpdateFailed#started");
        // mediaAnswerHelper.handleOfferReject(medias);
        // restoreInternal();
        Log.d(TAG, "sessionUpdateFailed#finished");

        if (mCurrentSessionListener != null) {
            Log.e(TAG, "sessionUpdateFailed: mCurrentSessionListener is null");
            mCurrentSessionListener.sessionUpdateFailed(session);
        }
    }

    /**
     * @see SessionListener#sessionUpdateReceived(Session)
     */
    public void sessionUpdateReceived(Session session) {
        Log.d(TAG, "sessionUpdateReceived#started");

        // Here we check whether the session is still in the negotiating state
        // although the UPDATE message has been received -
        // ...Here's the UPDATE during the negotiation.. The use case here is
        // that
        // user-a calls user-b and during that negotiation phase the server
        // (after 183 resopnse with SDP) actually diverts the call to voice-mail
        // by sending back new SDP in UPDATE...
        // In such case for medias we use the 183 response scenario.
        if (session.getState() == SESSION_STATE_NEGOTIATING) {
            if (!mediasEarlyAccepted.get()) {
                List<IMedia> iMedias = getRemoteMedias();
                handleMediaAnswer(iMedias);
            }

            mediasEarlyAccepted.compareAndSet(false, true);
        } else {
            List<IMedia> iMedias = getRemoteMedias();
            Log.d(TAG, "sessionUpdateReceived#offer medias size: " + iMedias.size()
                    + ", local medias size: " + medias.size());

            if (iMedias.size() != 0) {
                // Existing media
                for (int i = 0, existCount = medias.size(); i < existCount; i++) {
                    // check if the characteristics has been changed
                    IMedia offeredIMedia = iMedias.get(i);
                    MediaExt localMedia = medias.get(i);
                    MediaDescriptorImpl offerdMediaDescriptor = mediaOfferHelper
                            .parseMediaDescriptor(offeredIMedia);
                    MediaDescriptorImpl localMediaDescriptor = localMedia
                            .getLocalMediaDescriptor(0);

                    boolean updated = checkIsMediaUpdated(offerdMediaDescriptor,
                            localMediaDescriptor);

                    Log.d(TAG, "*** sessionUpdateReceived# is media updated = " + updated);

                    if (updated) {
                        updateExistingMedia(offerdMediaDescriptor, localMedia);
                    }
                }
                // New media created
                for (int startNew = medias.size(), endNew = iMedias.size(); startNew < endNew; startNew++) {
                    IMedia addedIMedia = iMedias.get(startNew);
                    MediaExt newOfferMedia = createNewMediaByRemoteOffer(addedIMedia);
                    medias.add(newOfferMedia);
                }

            } else {
                Log.d(TAG, "Offer contains no mediaDescriptors");
            }
        }

        if (mCurrentSessionListener != null) {
            mCurrentSessionListener.sessionUpdateReceived(session);
        }
        Log.d(TAG, "sessionUpdateReceived#finished");
    }

    /**
     * This method is used to update local medias based on an offer that
     * originated from a Session.update().
     * 
     * @param offeredMediaDescriptor the offered media descriptors.
     * @param existingMedia - existing media
     */
    private void updateExistingMedia(MediaDescriptorImpl offeredMediaDescriptor,
            MediaExt existingMedia) {
        Log.d(TAG, "updateExistingMedia#starting");

        // if port = 0 the media is proposed to be deleted
        boolean removeMedia = offeredMediaDescriptor.getPort() == 0;

        Log.d(TAG, "updateExistingMedia# removeMedia = " + removeMedia);

        if (removeMedia) {
            existingMedia.setUpdateState(Media.UPDATE_REMOVED);
        } else {
            MediaProposalImpl mediaProposal = existingMedia
                    .createMediaProposalBasedOnIncomingOffer(offeredMediaDescriptor);
            existingMedia.setProposal(mediaProposal);
            existingMedia.setUpdateState(Media.UPDATE_MODIFIED);
            Log.d(TAG, "MediaProposalImpl: "+mediaProposal.toString());
        }

        Log.d(TAG, "updateExistingMedia#finished");
    }

    private boolean checkIsMediaUpdated(final MediaDescriptorImpl offeredMediaDescriptor,
            final MediaDescriptorImpl acceptedMediaDescriptor) {
        return !acceptedMediaDescriptor.equals(offeredMediaDescriptor);
    }

    /**
     * @see SessionListener#sessionUpdated(Session)
     */
    public void sessionUpdated(Session session) {
        Log.d(TAG, "sessionUpdated#started");

        List<IMedia> iMedias = getRemoteMedias();

        try {
            handleMediaAnswer(iMedias);
            if (mCurrentSessionListener != null) {
                mCurrentSessionListener.sessionUpdated(session);
            }
        } catch (IllegalStateException e) {
            if (mCurrentSessionListener != null) {
                Log.e(TAG, "sessionUpdated: " + e.toString());
                mCurrentSessionListener.sessionUpdateFailed(session);
            }
        }

        Log.d(TAG, "sessionUpdated#finished");
    }

    private void updateMediaModeProperties() {
        for (MediaExt media : medias) {
            media.updateModeProperties();
        }
    }

    /**
     * This method is used to connect the Session to a Reference, so that
     * notifications on the Session establishment can be sent.
     * 
     * @param reference - the reference that this session should be connected
     *            to.
     */
    public void initReferral(ReferenceImpl reference) {
        Log.d(TAG, "initReferral#" + reference);
        this.reference = reference;
    }

    /**
     * @see Session#createReference(String, String)
     */
    public Reference createReference(String referTo, String referMethod) throws ImsException {
        if (referTo == null) {
            throw new IllegalArgumentException("referTo argument is null");
        }

        if (!ValidatorUtil.isValidReferenceMethod(referMethod)) {
            throw new IllegalArgumentException("referMethod argument is not valid");
        }

        int state = getState();
        if (state != STATE_ESTABLISHED) {
            throw new IllegalStateException("Session is not in state STATE_ESTABLISHED");
        }

        ReferenceImpl reference = null;
        try {
            IReference iReference = sessionPeer.createReference(referTo, referMethod);
            reference = new ReferenceImpl(sessionPeer.getServiceMethod(), iReference);
            getService().addServiceCloseListener(reference);
        } catch (RemoteException e) {
            throw new ImsException("Reference could not be created");
        }

        return reference;
    }

    public Capabilities createCapabilities() throws ImsException {
        int state = getState();
        if (state != STATE_ESTABLISHED) {
            throw new IllegalArgumentException("The Session is not in STATE_ESTABLISHED");
        }

        final CapabilitiesImpl capabilities;

        try {
            ICapabilities iCapabilities = sessionPeer.createCapabilities();

            if (iCapabilities != null) {
                capabilities = new CapabilitiesImpl(iCapabilities.getServiceMethod(), iCapabilities);
                getService().addServiceCloseListener(capabilities);
            } else {
                throw new ImsException("Capabilities cann't be created.");
            }

        } catch (RemoteException e) {
            throw new ImsException(e.getMessage(), e);
        }

        return capabilities;
    }

    private void cleanMedia() {
        for (MediaExt media : medias) {
            media.setState(Media.STATE_DELETED);
            try {
                media.cleanMedia();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    public void handleIncomingCall(final SessionPrepareListener prepareListener) {
        if (useResourceReservation) {
            try {
                Log.i(TAG, "handleIncomingCall acquire wakelock: " + mWakeLock);
                mWakeLock.acquire(WAKELOCK_TIMEOUT);
                remoteSessionListener.addListener(new RRUpdatesAutoAccepter());
                remoteSessionListener.addListener(new RRCompleteListener(prepareListener));
                doPreaccept();
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else {
            prepareListener.sessionPrepared();
        }
    }

    /**
     * For resource reservation case jsr shouldn't pass incoming call to clients
     * until resource reservation flow completes.
     */
    private class RRCompleteListener extends SessionAdapter {
        private final SessionPrepareListener prepareListener;

        public RRCompleteListener(SessionPrepareListener prepareListener) {
            this.prepareListener = prepareListener;
        }

        @Override
        public void sessionAlerting(Session session) {
            Log.i(TAG, "Resource reservation was completed successfully, pass incoming call to UE");

            remoteSessionListener.removeListener(this);
            prepareListener.sessionPrepared();

            if (mWakeLock.isHeld()) {
                Log.i(TAG, "release wakelock: " + mWakeLock);
                mWakeLock.release();
            }
        }
    }

    /**
     * Any update which was received while session is in reservation state
     * should be accepted automatically.
     */
    private class RRUpdatesAutoAccepter extends SessionAdapter {
        @Override
        public void sessionUpdateReceived(Session session) {
            Log.i(TAG,
                    "Update is received while session is in reservation state, so update should be accepted automatically");

            remoteSessionListener.removeListener(this);

            try {
                doAccept(true);
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage() + "", e);
            }
        }

        @Override
        public void sessionAlerting(Session session) {
            Log.i(TAG, "Resource reservation was completed successfully, remove RRUpdatesAutoAccepter");

            remoteSessionListener.removeListener(this);
        }

    }

    public ServiceImpl getService() {
        return mService;
    }
}
