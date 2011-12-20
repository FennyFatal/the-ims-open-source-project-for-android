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

package com.android.ims.core.media;

import android.util.Log;
import com.android.ims.core.media.util.DirectionUtils;
import com.android.ims.core.media.util.StreamTypeUtils;

import javax.microedition.ims.core.media.Media;
import javax.microedition.ims.core.media.MediaDescriptor;
import javax.microedition.ims.core.media.MediaListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation Media interface.
 * For more details @see {@link Media}
 *
 * @author ext-akhomush
 */
public abstract class MediaImpl implements Media {
    private final static String TAG = "MediaImpl";

    private int state = STATE_INACTIVE;
    private int updateState = UPDATE_UNCHANGED;

    private final List<MediaDescriptorImpl> mediaDescriptors = new ArrayList<MediaDescriptorImpl>();
    private final List<MediaDescriptorImpl> remoteMediaDescriptors = new ArrayList<MediaDescriptorImpl>();
    private MediaProposalImpl proposal;

    private MediaListener mediaListener;
    private final boolean isSecured;

    /**
     * Mode properties
     */
    private boolean canRead, canWrite, exists;
    private boolean mediaInitiated;

    protected MediaImpl(boolean isSecured) {
        this.isSecured = isSecured;
    }

    public void setMediaDescriptors(MediaDescriptorImpl[] mediaDescriptors) {
        this.mediaDescriptors.clear();
        for (MediaDescriptorImpl descriptor : mediaDescriptors) {
            descriptor.setMedia(this);
            this.mediaDescriptors.add(descriptor);
        }
    }

    public void setRemoteMediaDescriptor(MediaDescriptorImpl descriptor) {
        remoteMediaDescriptors.clear();
        descriptor.setMedia(this);
        remoteMediaDescriptors.add(descriptor);
    }

    /**
     * @see Media#canRead()
     */

    public boolean canRead() {
        return getCanRead();
    }

    protected boolean getCanRead() {
        return canRead;
    }

    protected boolean setCanRead(boolean canRead) {
        boolean propertyChanged = (this.canRead != canRead);
        if (propertyChanged) {
            this.canRead = canRead;
            Log.i(TAG, "setCanRead#canRead = " + canRead);
        }

        return propertyChanged;
    }

    /**
     * Invoke recalculating media related properties.
     */
    public void updateModeProperties() {
        updateModeProperties(false);
    }

    private void updateModeProperties(boolean force) {
        Log.i(TAG, "updateModeProperties#");
        int direction = getDirection();
        int state = getState();

        //For 'canRead' the following factors apply for any type of media:
        //  1. The 'exists' mode is true.
        //  2. The media direction is DIRECTION_RECEIVE or DIRECTION_SEND_RECEIVE
        //  3. The flow contains, or is capable to contain, data available for read.
        boolean canRead = getExists() &&
                (direction == DIRECTION_RECEIVE || direction == DIRECTION_SEND_RECEIVE) &&
                ((state == STATE_PENDING && !isSecured) || state == STATE_ACTIVE);

        //For 'canWrite' the following factors apply for any type of media:
        //  1. The 'exists' mode is true.
        //  2. The media direction is DIRECTION_SEND or DIRECTION_SEND_RECEIVE
        //  3. The media is in STATE_ACTIVE


        boolean canWrite = getExists() && state == STATE_ACTIVE && (direction == DIRECTION_SEND || direction == DIRECTION_SEND_RECEIVE);

        //only for voice-call session
        //code below is special workaround made by customer request to stop both sending and receiving players when device is set onHold.
        //The idea to set flag 'canRead' to false then client code will check this flag and stop sending player.
        //technically this is not correct because SDP contains direction 'sendonly' and this means sending player shouldn't be stopped.
        /*MediaDescriptorImpl localMediaDescriptor = getLocalMediaDescriptor(0);
        Log.i(TAG, "updateModeProperties#localMediaDescriptor=" + localMediaDescriptor);
        int mediaType = StreamTypeUtils.convertToType(localMediaDescriptor.getMediaType());
        Log.i(TAG, "updateModeProperties#mediaType == StreamMediaImpl.STREAM_TYPE_AUDIO:" + (mediaType == StreamMediaImpl.STREAM_TYPE_AUDIO));
        if (mediaType == StreamMediaImpl.STREAM_TYPE_AUDIO) {
            Log.i(TAG, "updateModeProperties#:canWrite=" + (getExists() && state == STATE_ACTIVE && direction == DIRECTION_SEND_RECEIVE));
            canWrite = getExists() && state == STATE_ACTIVE && direction == DIRECTION_SEND_RECEIVE;
        }*/

        //then there was request from Pete
        //'can we fix this (to master) so that if the session is voice-call session, in this case we interpret this so that we just stop both'


        Log.i(TAG, "updateModeProperties#canRead = " + canRead + " canWrite = " + canWrite);
        //if(proposal != null)
        //    setMediaDescriptors(proposal.getMediaDescriptors());

        boolean canReadChanged = setCanRead(canRead);
        boolean canWriteChanged = setCanWrite(canWrite);

        if (force || canReadChanged || canWriteChanged) {
            notifyPropertiesChanged();
            notifyMediaListener();
        }
    }

    protected abstract void notifyPropertiesChanged();

    /**
     * @see Media#canWrite()
     */

    public boolean canWrite() {
        return getCanWrite();
    }

    protected boolean getCanWrite() {
        return canWrite;
    }

    private boolean setCanWrite(boolean canWrite) {
        boolean propertyChanged = (this.canWrite != canWrite);
        if (propertyChanged) {
            this.canWrite = canWrite;
            Log.i(TAG, "setCanWrite#canWrite = " + canWrite);
        }
        return propertyChanged;
    }

    /**
     * @see Media#exists()
     */

    public boolean exists() {
        return getExists();
    }

    protected boolean getExists() {
        return exists;
    }

    protected boolean setExists(boolean exists) {
        boolean propertyChanged = (this.exists != exists);
        if (propertyChanged) {
            this.exists = exists;
            Log.i(TAG, "setExists#exists = " + exists);
        }

        return propertyChanged;
    }

    /**
     * @see Media#getDirection()
     */

    public int getDirection() {
        if (mediaDescriptors.size() == 0) {
            throw new IndexOutOfBoundsException(
                    "Can't get media direction, media descriptor has been set");
        }

        return mediaDescriptors.get(0).getDirection();
    }

    /**
     * @see Media#getMediaDescriptors()
     */

    public MediaDescriptor[] getMediaDescriptors() {
        int maxSize = (mediaDescriptors.size() > remoteMediaDescriptors.size() ? mediaDescriptors.size() : remoteMediaDescriptors.size());
        MediaDescriptorWrapperImpl[] result = new MediaDescriptorWrapperImpl[maxSize];

        for (int i = 0; i < maxSize; i++) {
            MediaDescriptorImpl localMediaDescriptor = null;
            MediaDescriptorImpl remoteMediaDescriptor = null;

            if (i < mediaDescriptors.size()) {
                localMediaDescriptor = mediaDescriptors.get(i);
            }

            if (i < remoteMediaDescriptors.size()) {
                remoteMediaDescriptor = remoteMediaDescriptors.get(i);
            }

            result[i] = new MediaDescriptorWrapperImpl(localMediaDescriptor, remoteMediaDescriptor);
        }

        return result;
    }

    /**
     * Return local media descriptors
     *
     * @return local media descriptors
     */
    public MediaDescriptorImpl[] getLocalMediaDescriptors() {
        return mediaDescriptors.toArray(new MediaDescriptorImpl[0]);
    }

    public MediaDescriptorImpl getLocalMediaDescriptor(int index) {
        MediaDescriptorImpl descriptor = null;
        if (index < mediaDescriptors.size()) {
            descriptor = mediaDescriptors.get(index);
        }
        return descriptor;
    }

    public MediaDescriptorImpl getRemoteMediaDescriptor(int index) {
        MediaDescriptorImpl descriptor = null;
        if (index < remoteMediaDescriptors.size()) {
            descriptor = remoteMediaDescriptors.get(index);
        }
        return descriptor;
    }

    /**
     * @see Media#getProposal()
     */

    public Media getProposal() {
        if (updateState != Media.UPDATE_MODIFIED) {
            throw new IllegalStateException("update state must be UPDATE_MODIFIED, state: " + updateState);
        }

        if (state != Media.STATE_ACTIVE) {
            throw new IllegalStateException("state must be STATE_ACTIVE, state: " + state);
        }

        return proposal;
    }

    private Media getProposalInternally() {
        return proposal;
    }


    /**
     * Sets the media proposal.
     *
     * @param proposal the proposal
     */
    public void setProposal(MediaProposalImpl proposal) {
        this.proposal = proposal;
    }

    /**
     * @see Media#getState()
     */

    public int getState() {
        return state;
    }

    /**
     * @see Media#getUpdateState()
     */

    public int getUpdateState() {
        if (state != Media.STATE_ACTIVE) {
            throw new IllegalStateException("state must be STATE_ACTIVE, state: " + state);
        }

        return updateState;
    }

    public void setUpdateState(int updateState) {
        this.updateState = updateState;
    }

    /**
     * @see Media#setDirection(int)
     */

    public void setDirection(int direction) {
        if (state != STATE_ACTIVE && state != STATE_INACTIVE) {
            throw new IllegalStateException(
                    "if the Media is not in STATE_INACTIVE or STATE_ACTIVE, state: " + state);
        }

        if (!DirectionUtils.isDirectionValid(direction)) {
            throw new IllegalArgumentException(
                    "direction argument is invalid, direction: " + direction);
        }

        if (state == STATE_ACTIVE) {
            if (updateState == UPDATE_REMOVED) {
                Log.d(TAG, "Ignoring attempt to change direction for removed media");
                return;
            }

            if (updateState == UPDATE_UNCHANGED) {
                updateState = UPDATE_MODIFIED;
                proposal = createMediaProposal();
                proposal.setDirectionInternal(direction);
            } else {
                proposal.setDirectionInternal(direction);
            }
        } else {
            for (MediaDescriptorImpl mediaDescriptor : mediaDescriptors) {
                mediaDescriptor.setDirection(direction);
            }
        }
    }

    /**
     * @see Media#setMediaListener(MediaListener)
     */

    public void setMediaListener(MediaListener listener) {
        this.mediaListener = listener;
        if (mediaListener != null) {
            updateModeProperties(true);
        }
    }

    private void notifyMediaListener() {
        Log.i(TAG, "notifyMediaListener#");
        if (mediaListener != null) {
            mediaListener.modeChanged(this);
        }
    }

    public void setState(int state) {

        Log.d(TAG, "*** MediaImpl.setState() state = " + state);

        this.state = state;
//        if(state == STATE_ACTIVE) {
//            prepareProposal();
//        }
    }

    private void prepareProposal() {

        Log.d(TAG, "*** MediaImpl.prepareProposal#started");

        String[] attributes = getMediaDescriptors()[0].getAttributes();
        MediaDescriptor proposalDescriptor = getProposalInternally().getMediaDescriptors()[0];
        for (String attribute : attributes) {
            Log.d(TAG, "*** MediaImpl.prepareProposal attribute = " + attribute);

            proposalDescriptor.addAttribute(attribute);
        }
    }

    /**
     * Creates a media proposal based on the media object.
     *
     * @return the media proposal
     */
    protected abstract MediaProposalImpl createMediaProposal();

    public boolean isMediaInitiated() {
        return mediaInitiated;
    }

    public void setMediaInitiated(boolean mediaInitiated) {
        this.mediaInitiated = mediaInitiated;
    }

    /**
     * Removes an attribute to one of the media descriptors of an active media.
     * <p/>
     * This operation does not modify the actual media but instead the media
     * proposal. If no media proposal exists, one will be created.
     *
     * @param descr     - the media descriptor the attribute should be removed from
     * @param attribute - the attribute to remove
     * @throws IllegalArgumentException - if attribute not set
     */
    public void removeAttributeFromActiveMedia(final MediaDescriptorImpl descr,
                                               final String attribute) {

        int idx = mediaDescriptors.indexOf(descr);

        if (updateState == Media.UPDATE_UNCHANGED) {
            proposal = createMediaProposal();
            MediaDescriptorImpl proposalDescr = proposal.getMediaDescriptors()[idx];
            removeAttributeFromProposal(proposalDescr, attribute);
            updateState = Media.UPDATE_MODIFIED;
        } else if (updateState == Media.UPDATE_MODIFIED) {
            MediaDescriptorImpl proposalDescr = proposal.getMediaDescriptors()[idx];
            removeAttributeFromProposal(proposalDescr, attribute);
        } else if (updateState == Media.UPDATE_REMOVED) {
            //nothing
        }
    }

    /**
     * Remove attribute from proposal descriptor
     *
     * @param proposalDescr - proposal descriptor
     * @param attribute     - attribute
     * @throws IllegalArgumentException - Attribute not set
     */
    private void removeAttributeFromProposal(final MediaDescriptorImpl proposalDescr, final String attribute) {
        if (!proposalDescr.containsAttribute(attribute)) {
            throw new IllegalArgumentException("Attribute not set: " + attribute);
        }
        proposalDescr.removeAttributeInternal(attribute);
    }

    /**
     * Adds an attribute to one of the media descriptors of an active media.
     * <p/>
     * This operation does not modify the actual media but instead the media
     * proposal. If no media proposal exists, one will be created.
     *
     * @param descr     the media descriptor the attribute should be added to
     * @param attribute the attribute to add
     * @throws IllegalArgumentException - Attribute already set
     */
    public void addAttributeToActiveMedia(final MediaDescriptorImpl descr,
                                          final String attribute) {

        int idx = mediaDescriptors.indexOf(descr);

        if (updateState == Media.UPDATE_UNCHANGED) {
            proposal = createMediaProposal();
            MediaDescriptorImpl proposalDescr = proposal.getMediaDescriptors()[idx];
            addAttributeToProposal(proposalDescr, attribute);
            updateState = Media.UPDATE_MODIFIED;
        } else if (updateState == Media.UPDATE_MODIFIED) {
            MediaDescriptorImpl proposalDescr = proposal.getMediaDescriptors()[idx];
            addAttributeToProposal(proposalDescr, attribute);
        } else if (updateState == Media.UPDATE_REMOVED) {
            //nothing
        }
    }

    /**
     * Add attribute to proposal description
     *
     * @param proposalDescr - proposal description
     * @param attribute     - attribute
     * @throws IllegalArgumentException - Attribute already set
     */
    private void addAttributeToProposal(final MediaDescriptorImpl proposalDescr, final String attribute) {
        if (proposalDescr.containsAttribute(attribute)) {
            throw new IllegalArgumentException("Attribute already set, atttribute: " + attribute);
        }
        proposalDescr.addAttributeInternal(attribute);
    }


    /**
     * Creates a media proposal based on the media object and a received media
     * offer.
     */
    public MediaProposalImpl createMediaProposalBasedOnIncomingOffer(
            MediaDescriptorImpl offeredDescriptor) {
        MediaProposalImpl proposal = createMediaProposal();
        proposal.setMediaDescriptors(new MediaDescriptorImpl[]{offeredDescriptor});

        int direction = DirectionUtils.reverseDirection(proposal.getDirection());
        proposal.setDirectionInternal(direction);

        for (int i = 0; i < proposal.getMediaDescriptors().length; i++) {
            MediaDescriptorImpl originalDescriptor = getLocalMediaDescriptor(i);
            MediaDescriptorImpl offerDescriptor = proposal.getMediaDescriptors()[i];
            offerDescriptor.setPort(originalDescriptor.getPort());
        }

        return proposal;
    }


    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((mediaDescriptors == null) ? 0 : mediaDescriptors.hashCode());
        result = prime * result
                + ((proposal == null) ? 0 : proposal.hashCode());
        result = prime
                * result
                + ((remoteMediaDescriptors == null) ? 0
                : remoteMediaDescriptors.hashCode());
        return result;
    }


    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MediaImpl other = (MediaImpl) obj;
        if (mediaDescriptors == null) {
            if (other.mediaDescriptors != null)
                return false;
        } else if (!mediaDescriptors.equals(other.mediaDescriptors))
            return false;
        if (proposal == null) {
            if (other.proposal != null)
                return false;
        } else if (!proposal.equals(other.proposal))
            return false;
        if (remoteMediaDescriptors == null) {
            if (other.remoteMediaDescriptors != null)
                return false;
        } else if (!remoteMediaDescriptors.equals(other.remoteMediaDescriptors))
            return false;
        return true;
    }


    public String toString() {
        return "MediaImpl [canRead=" + canRead + ", canWrite=" + canWrite
                + ", exists=" + exists + ", mediaDescriptors="
                + mediaDescriptors + ", mediaInitiated=" + mediaInitiated
                + ", mediaListener=" + mediaListener + ", proposal=" + proposal
                + ", remoteMediaDescriptors=" + remoteMediaDescriptors
                + ", state=" + state + ", updateState=" + updateState + "]";
    }

    /**
     * Check if media contains at least one supported codec.
     * Handle rtpmap attributes. If codec doesn't supported then attribute should be deleted.
     *
     * @return true if media contains at least one supported codec.
     */
    public abstract boolean handleSupportedCodec();

    /**
     * This method is responsible for early media preparation.
     * This method should be invoked immediately after session#start
     *
     * @throws IOException
     */
    public abstract void prepareMedia() throws IOException;

    /**
     * Invoked before session#sessionStarted.
     * This method is responsible for sockets, buffers preparation and etc.
     *
     * @throws IOException If an IO error occurs.
     */
    public abstract void processMedia() throws IOException;

    /**
     * Invoked after session#sessionTerminated.
     * This method is responsible for closing sockets, free buffers etc.
     */
    public abstract void cleanMedia() throws IOException;

    protected boolean isSecured() {
        return isSecured;
    }
}
