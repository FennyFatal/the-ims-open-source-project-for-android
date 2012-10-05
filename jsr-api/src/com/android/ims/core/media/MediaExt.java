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

import java.io.IOException;

import javax.microedition.ims.core.media.Media;

/**
 * @author Khomushko
 */
public interface MediaExt extends Media {
    void setUpdateState(int updateState);

    void setState(int state);

    /**
     * Invoked after session#sessionTerminated. This method is responsible for
     * closing sockets, free buffers etc.
     */
    void cleanMedia() throws IOException;

    /**
     * Invoke recalculating media related properties.
     */
    void updateModeProperties();

    /**
     * Return local media descriptors
     * 
     * @return local media descriptors
     */
    MediaDescriptorImpl[] getLocalMediaDescriptors();

    MediaDescriptorImpl getLocalMediaDescriptor(int index);

    void setRemoteMediaDescriptor(MediaDescriptorImpl descriptor);

    boolean isMediaInitiated();

    /**
     * This method is responsible for early media preparation. This method
     * should be invoked immediately after session#start
     * 
     * @throws IOException
     */
    void prepareMedia() throws IOException;

    void setMediaDescriptors(MediaDescriptorImpl[] mediaDescriptors);

    /**
     * Invoked before session#sessionStarted. This method is responsible for
     * sockets, buffers preparation and etc.
     * 
     * @throws IOException If an IO error occurs.
     */
    void processMedia() throws IOException;

    /**
     * Sets the media proposal.
     * 
     * @param proposal the proposal
     */
    void setProposal(MediaProposalImpl proposal);

    /**
     * Creates a media proposal based on the media object and a received media
     * offer.
     */
    MediaProposalImpl createMediaProposalBasedOnIncomingOffer(MediaDescriptorImpl offeredDescriptor);

    void setMediaInitiated(boolean mediaInitiated);

    /**
     * Check if media contains at least one supported codec. Handle rtpmap
     * attributes. If codec doesn't supported then attribute should be deleted.
     * 
     * @return true if media contains at least one supported codec.
     */
    boolean handleSupportedCodec();

    /**
     * Adds an attribute to one of the media descriptors of an active media.
     * <p/>
     * This operation does not modify the actual media but instead the media
     * proposal. If no media proposal exists, one will be created.
     * 
     * @param descr the media descriptor the attribute should be added to
     * @param attribute the attribute to add
     * @throws IllegalArgumentException - Attribute already set
     */
    void addAttributeToActiveMedia(final MediaDescriptorImpl descr, final String attribute);

    /**
     * Removes an attribute to one of the media descriptors of an active media.
     * <p/>
     * This operation does not modify the actual media but instead the media
     * proposal. If no media proposal exists, one will be created.
     * 
     * @param descr - the media descriptor the attribute should be removed from
     * @param attribute - the attribute to remove
     * @throws IllegalArgumentException - if attribute not set
     */
    void removeAttributeFromActiveMedia(final MediaDescriptorImpl descr, final String attribute);
}
