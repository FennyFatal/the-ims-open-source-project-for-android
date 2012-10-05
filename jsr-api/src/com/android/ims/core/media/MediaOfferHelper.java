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
import com.android.ims.core.media.MediaDescriptorImpl.Builder;
import com.android.ims.core.media.util.DirectionUtils;
import com.android.ims.util.CollectionsUtils;

import javax.microedition.ims.android.core.media.ICryptoParam;
import javax.microedition.ims.android.core.media.IMedia;
import javax.microedition.ims.core.media.Media;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class for handling media offer.
 *
 * @author ext-akhomush
 */
public final class MediaOfferHelper {
    private static final String TAG = "MediaOfferHelper";

    /**
     * Setting media that are in STATE_INACTIVE to
     * STATE_PENDING.
     */
    public void setInactiveMediaPending(List<MediaExt> medias) {
        for (MediaExt media : medias) {
            if (media.getState() == Media.STATE_INACTIVE) {
                media.setState(Media.STATE_PENDING);
            }
        }
    }

    public List<IMedia> convertToIMedias(List<MediaDescriptorImpl> mediaDescriptors) {
        Log.d(TAG, "convertToIMedias#mediaDescriptors = " + mediaDescriptors);
        List<IMedia> iMedias = new ArrayList<IMedia>();
        for (MediaDescriptorImpl mediaDescriptor : mediaDescriptors) {
            IMedia iMedia = convertToIMedia(mediaDescriptor);
            iMedias.add(iMedia);
        }
        Log.d(TAG, "convertToIMedias#iMedias = " + iMedias);
        return iMedias;
    }

    //TODO review
/*    private MediaDescriptorImpl resolveMediaDescriptor(final MediaImpl media) {
        Media mediaForDescriptor = media.getState() == Media.STATE_ACTIVE? media.getProposal(): media;
        return ((MediaImpl)mediaForDescriptor).getLocalMediaDescriptors()[0];
    }
*/
/*    public List<MediaImpl> parseMedias(final List<IMedia> iMedias, int state) {
        List<MediaImpl> medias = new ArrayList<MediaImpl>();
        for(IMedia iMedia: iMedias) {
            MediaDescriptorImpl mediaDescriptor = parseMediaDescriptor(iMedia);
            MediaImpl media = MediaFactory.INSTANCE.createMediaBasedOnIncomingOffer(mediaDescriptor);
            media.setState(state);
            medias.add(media);
        }
        return medias;
    }
*/

    private IMedia convertToIMedia(final MediaDescriptorImpl mDescriptor) {
        IMedia iMedia = new IMedia.IMediaBuilder()
                .type(mDescriptor.getMediaType())
                .port(mDescriptor.getPort())
                .protocol(mDescriptor.getTransport())
                .format(mDescriptor.getMediaFormat())
                .address(mDescriptor.getConnectionAddress())
                .isOwnAddress(mDescriptor.isOwnAddress())
                .direction(DirectionUtils.convertToString(mDescriptor.getDirection()))
                .information(mDescriptor.getMediaTitle())
                .bandwidthes(mDescriptor.getBandwidthInfo())
                .attributes(mDescriptor.getAttributes())
                .cryptoParams(convertToICryptoParams(mDescriptor.getCryptoParams()))
                .build();

        assert iMedia != null;
        return iMedia;
    }

	private ICryptoParam[] convertToICryptoParams(CryptoParam[] cryptoParams) {
		List<ICryptoParam> iCryptoParams = new ArrayList<ICryptoParam>();
		
		for(CryptoParam cryptoParam: cryptoParams) {
			String key = cryptoParam.getKey();
			List<String> values = new ArrayList<String>();
			values.add(key);
			
			//TODO for test
			//values.add("2^20");
			//values.add("1:3");
			
			values.addAll(Arrays.asList(cryptoParam.getParams()));
			String paramValue = CollectionsUtils.concatenate("|", values.toArray(new String[0]));
			
			ICryptoParam iCryptoParam = new ICryptoParam(cryptoParam.getTag(), cryptoParam.getAlgorithm(), paramValue);
			iCryptoParams.add(iCryptoParam);
		}
			
		return iCryptoParams.toArray(new ICryptoParam[0]);
	}

    public MediaDescriptorImpl parseMediaDescriptor(final IMedia iMedia) {
        Log.d(TAG, "parseMediaDescriptor#iMedia = " + iMedia);
        final MediaDescriptorImpl mediaDescriptor;

        Builder builder = new MediaDescriptorImpl.Builder()
                .mediaType(iMedia.getType())
                .port(iMedia.getPort())
                .transport(iMedia.getProtocol())
                .mediaFormat(iMedia.getFormat())
                .connectionAddress(iMedia.getAddress())
                .isOwnAddress(iMedia.isOwnAddress())
                .direction(DirectionUtils.convertToType(iMedia.getDirection()))
                .cryptoParams(convertToCryptoParams(iMedia.getCryptoParams()))
                .title(iMedia.getInformation());

        if (iMedia.getBandwidth() != null) {
            builder.bandwidthInfo(Arrays.asList(iMedia.getBandwidth()));
        }

        if (iMedia.getAttributes() != null) {
            builder.attributes(Arrays.asList(iMedia.getAttributes()));
        }

        mediaDescriptor = builder.build();
        
        Log.d(TAG, "parseMediaDescriptor#mediaDescriptor = " + mediaDescriptor);
        return mediaDescriptor;
    }

	private CryptoParam[] convertToCryptoParams(final ICryptoParam[] iCryptoParams) {
		List<CryptoParam> cryptoParams = new ArrayList<CryptoParam>();
		for(ICryptoParam iCrypto: iCryptoParams) {
			String iCryptoParam = iCrypto.getCryptoParam();
			String[] values = iCryptoParam.split("\\|");
			
			final String key = values[0];
			
			final String[] params = new String[values.length - 1];
			System.arraycopy(values, 1, params, 0, params.length);
			
			CryptoParam cryptoParam = new CryptoParam(iCrypto.getTag(), iCrypto.getCryptoSuit(), key, params);
			cryptoParams.add(cryptoParam);
		}
		return cryptoParams.toArray(new CryptoParam[0]);
	}
}
