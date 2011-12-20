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

import android.content.Context;
import android.util.Log;
import com.android.ims.configuration.AppConfiguration;
import com.android.ims.core.DtmfPayload;

import javax.microedition.ims.core.media.Media;

public final class MediaFactory {
    private static final String TAG = "MediaFactory";

    public static final MediaFactory INSTANCE = new MediaFactory();

    private MediaFactory() {
    }
    
    public MediaImpl createMedia(Media.MediaType mediaType, int direction,
            int state, final String localAddress, AppConfiguration configuration, 
            Context context, DtmfPayload dtmfPayload) {
        MediaImpl media = null;

        switch (mediaType) {
            case StreamMedia: {
                media = new StreamMediaImpl(direction, localAddress, configuration, context, dtmfPayload);
                media.setState(state);
                break;
            }
            default:
                Log.e(TAG, "Unsupported media type: " + mediaType);
                break;
        }

        return media;
    }

    /**
     * Create media based on server offer.
     *
     * @param mediaDescriptor
     * @return media
     */
    public MediaImpl createMediaBasedOnIncomingOffer(final MediaDescriptorImpl mediaDescriptor, 
            final String localAddress, AppConfiguration configuration, Context context,
            DtmfPayload dtmfPayload) {
        assert mediaDescriptor != null;

        MediaImpl media = null;

        final MediaDescriptorImpl remoteMediaDescriptor = mediaDescriptor.createCopy();
        final MediaDescriptorImpl localMediaDescriptor = mediaDescriptor;

        String transport = mediaDescriptor.getTransport();
        Log.d(TAG, "createMediaBasedOnIncomingOffer#transport = " + transport);
        if (StreamMediaImpl.PROTOCOL_RTP_AVP.equalsIgnoreCase(transport)
                ||StreamMediaImpl.PROTOCOL_RTP_SAVP.equalsIgnoreCase(transport)) {
            media =  new StreamMediaImpl(localMediaDescriptor, localAddress, configuration, context, dtmfPayload);
        } /*else if ("UDP".equalsIgnoreCase(transportType)) {
            media = new BasicUnreliableMediaImpl(direction);
        } else if ("TCP/MSRP".equalsIgnoreCase(transportType)) {
            media =  new FramedMediaImpl(direction);
        } */
        else {
            Log.e(TAG, String.format("Cannot create media for transportType = %s", transport));
        }

        if (media != null) {
            media.setRemoteMediaDescriptor(remoteMediaDescriptor);
        }

        Log.d(TAG, "createMediaBasedOnIncomingOffer#media = " + media);
        return media;
    }
}
