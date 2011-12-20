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

import javax.microedition.ims.core.media.Media;
import javax.microedition.ims.core.media.MediaListener;
import java.util.Arrays;

/**
 * Implementation of the {@link Media} interface for media proposals.
 * <p/>
 * Media proposals are so different from other media, they can
 * not be used to send or receive data only for temporary storing data.
 *
 * @author ext-akhomush
 */
public class MediaProposalImpl implements Media {
    private MediaDescriptorImpl[] descriptors;

    /**
     * Creates the proposal based on a set of media descriptors.
     *
     * @param descriptors the media descriptors.
     * @throws IllegalArgumentException - At least one descriptor must be set
     */
    public MediaProposalImpl(final MediaDescriptorImpl[] descriptors) {
        if (descriptors == null || descriptors.length == 0) {
            throw new IllegalArgumentException(
                    "At least one descriptor must be set");
        }

        setMediaDescriptors(descriptors);
    }

    /**
     * Cloned media descriptors.
     * For for details about clone mechanism @see {@link MediaDescriptorImpl#createCopy()}
     *
     * @param descriptors the media descriptors
     */
    public void setMediaDescriptors(final MediaDescriptorImpl[] mDescriptors) {
        this.descriptors = new MediaDescriptorImpl[mDescriptors.length];
        for (int i = 0, count = mDescriptors.length; i < count; i++) {
            MediaDescriptorImpl mediaDescriptor = mDescriptors[i].createCopy();
            mediaDescriptor.setMedia(this);
            descriptors[i] = mediaDescriptor;
        }
    }

    /**
     * Changes the direction of the media proposal without checking the media
     * state.
     *
     * @param direction the direction
     */
    public void setDirectionInternal(int direction) {
        for (MediaDescriptorImpl descriptor : descriptors) {
            descriptor.setDirection(direction);
        }
    }

    /**
     * @see Media#canRead()
     */
    
    public boolean canRead() {
        return false;
    }

    /**
     * @see Media#canWrite()
     */
    
    public boolean canWrite() {
        return false;
    }

    /**
     * @see Media#exists()
     */
    
    public boolean exists() {
        return false;
    }

    /**
     * @see Media#getDirection()
     */
    
    public int getDirection() {
        if (descriptors.length == 0) {
            throw new IndexOutOfBoundsException(
                    "Can't get media direction, media descriptor has not been set");
        }

        return descriptors[0].getDirection();
    }

    /**
     * @see Media#getMediaDescriptors()
     */
    
    public MediaDescriptorImpl[] getMediaDescriptors() {
        MediaDescriptorImpl[] result = new MediaDescriptorImpl[descriptors.length];
        System.arraycopy(descriptors, 0, result, 0, descriptors.length);
        return result;
    }

    /**
     * @see Media#getProposal()
     */
    
    public Media getProposal() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see Media#getState()
     */
    
    public int getState() {
        return Media.STATE_PROPOSAL;
    }

    /**
     * @see Media#getUpdateState()
     */
    
    public int getUpdateState() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see Media#setDirection(int)
     */
    
    public void setDirection(int direction) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see Media#setMediaListener(MediaListener)
     */
    
    public void setMediaListener(MediaListener listener) {
        throw new UnsupportedOperationException();
    }

    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(descriptors);
        return result;
    }

    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MediaProposalImpl other = (MediaProposalImpl) obj;
        if (!Arrays.equals(descriptors, other.descriptors))
            return false;
        return true;
    }

    
    public String toString() {
        return "MediaProposalImpl [descriptors=" + Arrays.toString(descriptors)
                + "]";
    }
}
