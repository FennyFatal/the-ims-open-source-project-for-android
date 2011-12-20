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
import javax.microedition.ims.messages.wrappers.sdp.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SdpConvertor {

    public static List<IMedia> buildMedias(SdpMessage currentSdpMessage) {
        Logger.log("SDPConvertor.buildMedias", "");
        List<IMedia> ret = new ArrayList<IMedia>();
        // Logger.log("medias", String.valueOf(currentSdpMessage.getMedias() !=
        // null && currentSdpMessage.getMedias().size() > 0));

        for (Media media : currentSdpMessage.getMedias()) {
            Logger.log("media", media.getContent());
            IMedia imedia = createIMedia(media, currentSdpMessage);
            Logger.log("imedia", imedia.toString());
            ret.add(imedia);
        }
        return ret;
    }

    private static IMedia createIMedia(final Media m, SdpMessage currentSdpMessage) {
        Logger.log("createIMedia", "start");
        IMedia ret = new IMedia.IMediaBuilder()
                .type(m.getType())
                .address(
                        m.getConnectionInfo() != null ? m.getConnectionInfo().getAddress()
                                : currentSdpMessage.getConnectionInfo().getAddress())
                .isOwnAddress(m.getConnectionInfo() != null)               
                .port(m.getPort())
                .numberOfPorts(m.getNumberOfPorts())
                .protocol(m.getProtocol())
                .format(convertToFormat(m.getFormats()))
                .information(m.getInformation())
                .bandwidthes(convertBandwidth(m.getBandwidth()))
                .encryptionKey(
                        m.getEncryptionKeys() != null ? m.getEncryptionKeys().getValue() : null)
                .attributes(convertAttributes(m.getAttributes()))
                .direction(m.getDirection().getValue())
                .cryptoParams(convertCryptoParams(m.getCryptoParams())).build();
        Logger.log("createIMedia", "end");
        return ret;
    }

    private static ICryptoParam[] convertCryptoParams(List<CryptoParam> cryptoParams) {

        ICryptoParam[] retValue = new ICryptoParam[cryptoParams.size()];

        for (int i = 0; i < cryptoParams.size(); i++) {
            CryptoParam cryptoParam = cryptoParams.get(i);
            retValue[i] = new ICryptoParam(cryptoParam.getTag(), cryptoParam.getCryptoSuit()
                    .toString(), cryptoParam.getKeyParam());
        }
        // TODO Auto-generated method stub
        return retValue;
    }

    private static String convertToFormat(List<String> formats) {
        String format = null;
        if (formats != null) {
            StringBuffer sb = new StringBuffer();
            for (String f : formats) {
                sb.append(f).append(" ");
            }
            format = sb.toString().trim();
        }
        return format;
    }

    private static String[] convertBandwidth(Bandwidth bandwidth) {
        Logger.log("convertBandwidth", "test");
        String[] retValue = null;
        if (bandwidth != null) {
            retValue = new String[1];
            retValue[0] = bandwidth.getContent();
        }

        return retValue;
    }

    private static String[] convertAttributes(Iterator<Attribute> attributes) {
        Logger.log("convertAttributes", "test");
        if (attributes == null) {
            return null;
        }

        List<String> retValue = new ArrayList<String>();
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            String attrView = attribute.getContent();
            Logger.log("attr: " + attrView);
            if ("sendrecv".equalsIgnoreCase(attrView) || "sendonly".equalsIgnoreCase(attrView)
                    || "recvonly".equalsIgnoreCase(attrView)
                    || "inactive".equalsIgnoreCase(attrView)) {
                continue;
            }
            retValue.add(attrView);
        }

        return retValue.toArray(new String[0]);
    }

}
