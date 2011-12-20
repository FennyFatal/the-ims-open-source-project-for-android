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

import javax.microedition.ims.core.media.MediaDescriptor;

/**
 * This class responsible for presentation media descriptor functionality.
 * Wrap local and remote media descriptors.
 *
 * @author ext-akhomush
 */
public class MediaDescriptorWrapperImpl implements MediaDescriptor {
    private static final String TAG = "MediaDescriptorWrapperImpl";
    private final MediaDescriptorImpl localDescriptor;
    private final MediaDescriptorImpl remoteDescriptor;

    public MediaDescriptorWrapperImpl(final MediaDescriptorImpl localDescriptor,
                                      final MediaDescriptorImpl remoteDescriptor) {
        assert localDescriptor != null || remoteDescriptor != null;
        this.localDescriptor = localDescriptor;
        this.remoteDescriptor = remoteDescriptor;
    }

    /**
     * @see MediaDescriptor#addAttribute(String)
     */
    
    public void addAttribute(final String attribute) {
        if (localDescriptor != null) {
            localDescriptor.addAttribute(attribute);
        }
    }

    /**
     * @see MediaDescriptor#getAttributes()
     */
    
    public String[] getAttributes() {
        return remoteDescriptor != null ? remoteDescriptor.getAttributes() : new String[0];
    }

    /**
     * @see MediaDescriptor#getBandwidthInfo()
     */
    
    public String[] getBandwidthInfo() {
        return remoteDescriptor != null ? remoteDescriptor.getBandwidthInfo() : new String[0];
    }

    /**
     * @see MediaDescriptor#getMediaDescription()
     */
    
    public String getMediaDescription() throws IllegalStateException {
        Log.d(TAG, "getMediaDescription#");

        if (remoteDescriptor == null) {
            throw new IllegalStateException("remote media description is not setted");
        }

        return remoteDescriptor.getMediaDescription();
    }

    /**
     * @see MediaDescriptor#getMediaTitle()
     */
    
    public String getMediaTitle() {
        return remoteDescriptor != null ? remoteDescriptor.getMediaTitle() : null;
    }

    /**
     * @see MediaDescriptor#removeAttribute(String)
     */
    
    public void removeAttribute(final String attribute) {
        if (localDescriptor != null) {
            localDescriptor.removeAttribute(attribute);
        }
    }

    /**
     * @see MediaDescriptor#setBandwidthInfo(String[])
     */
    
    public void setBandwidthInfo(final String[] info) {
        if (localDescriptor != null) {
            localDescriptor.setBandwidthInfo(info);
        }
    }

    /**
     * @see MediaDescriptor#setMediaTitle(String)
     */
    
    public void setMediaTitle(final String title) {
        if (localDescriptor != null) {
            localDescriptor.setMediaTitle(title);
        }
    }
    
    public MediaDescriptorImpl getLocalDescriptor() {
        return localDescriptor;
    }

    public MediaDescriptorImpl getRemoteDescriptor() {
        return remoteDescriptor;
    }

    
    public String toString() {
        return "MediaDescriptorWrapperImpl [localDescriptor=" + localDescriptor
                + ", remoteDescriptor=" + remoteDescriptor + "]";
    }
}
