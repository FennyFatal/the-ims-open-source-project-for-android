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

package javax.microedition.ims.android.core.media;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.util.NumberUtils;
import javax.microedition.ims.messages.wrappers.sdp.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SdpBuilder {

    private static final String TAG = "SdpBuilder";

    public static List<Media> buildMedias(List<IMedia> iMedias) {
        Logger.log(TAG, "buildMedias#started");

        final List<Media> medias = new ArrayList<Media>();
        for (IMedia iMedia : iMedias) {

            Media media = new Media.MediaBuilder()
                .type(iMedia.getType())
                .port(iMedia.getPort())
                .address(iMedia.getAddress(), iMedia.isOwnAddress())
                .numberOfPorts(iMedia.getNumberOfPorts())
                .protocol(iMedia.getProtocol())
                .direction(iMedia.getDirection())
                .formats(convertToFormats(iMedia.getFormat()))
                .information(iMedia.getInformation())
                .encryptionKeys(iMedia.getEncryptionKey())
                .attributes(convertAttributes(iMedia.getAttributes()))
                .bandwidths(convertBandwidth(iMedia.getBandwidth()))
                .cryptoParams(convertCryptoParams(iMedia.getCryptoParams()))
                .build();
            medias.add(media);
        }
        Logger.log(TAG, "buildMedias#finished" + medias);
        return medias;
    }
    
    private static CryptoParam[] convertCryptoParams(ICryptoParam[] cryptoParams) {
    	List<CryptoParam> params = new ArrayList<CryptoParam>();
		if(cryptoParams != null) {
			for(ICryptoParam iCryptoParam: cryptoParams) {
				CryptoSuit cryptoSuit = CryptoSuit.valueOf(iCryptoParam.getCryptoSuit());
				if(cryptoSuit != null) {
					params.add(new CryptoParam(iCryptoParam.getTag(), cryptoSuit, iCryptoParam.getCryptoParam()));	
				} else {
					Logger.log("Stack doesn't support crypto suit = " + iCryptoParam.getCryptoSuit());
				}
				
			}
		}
		return params.toArray(new CryptoParam[0]);
	}

	private static List<String> convertToFormats(String formats) {
        List<String> retFormats = null;
        if (formats != null) {
            retFormats = Arrays.asList(new String[]{formats});
        }
        return retFormats;
    }

    public static List<Attribute> convertAttributes(String[] iAttributes) {
        Logger.log(TAG, "convertAttributes#started iAttributes = " + Arrays.toString(iAttributes));
        if (iAttributes == null) {
            Logger.log(TAG, "convertAttributes#finished  iAttributes == null");
            return null;
        }

        final List<Attribute> attributes = new ArrayList<Attribute>();
        for (final String iAttribute : iAttributes) {
            if (iAttribute == null) {
                continue;
            }
            String[] exprs = iAttribute.split(":", 2);

            Attribute attribute = null;
            if (exprs.length == 1) {
                attribute = new Attribute(iAttribute, null);
            }
            else if (exprs.length > 1) {
                attribute = new Attribute(exprs[0], exprs[1]);
            }
            else {
                assert false : "attribute: " + iAttribute;
            }

            attributes.add(attribute);
        }
        Logger.log(TAG, "convertAttributes#finished");
        return attributes;
    }

    private static List<Bandwidth> convertBandwidth(String[] iBandwidths) {
        Logger.log(TAG, "convertBandwidth#started");
        if (iBandwidths == null) {
            Logger.log(TAG, "convertBandwidth#finished iBandwidths == null");
            return null;
        }
        Logger.log(TAG, "iBandwidths=" + iBandwidths);
        Logger.log(TAG, "length=" + iBandwidths.length);

        List<Bandwidth> bandwidths = new ArrayList<Bandwidth>();
        for (String iBandwidth : iBandwidths) {
            if (iBandwidth == null) {
                continue;
            }
            String[] exprs = iBandwidth.split(":");

            Bandwidth bandwidth = new Bandwidth();
            if (exprs.length == 2) {
                bandwidth.setType(exprs[0].trim());
                bandwidth.setBandwidth(NumberUtils.parseLong(exprs[1].trim()));
            }
            else {
                assert false;
            }

            bandwidths.add(bandwidth);
        }

        Logger.log(TAG, "convertBandwidth#finished");
        return bandwidths;
    }
}
