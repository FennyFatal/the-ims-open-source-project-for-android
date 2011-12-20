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

package com.android.ims.configuration;

import javax.microedition.ims.Registry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Emulate environment.
 * {@link Environment}
 * 
 * @author ext-akhomush
 *
 */
public final class DefaultEnvironment implements Environment {
	private boolean isForceSrtp;
	private final String srtpCryptoAlgorithm;
    private final Map<MediaType, Set<Codec>> supportedMediaFormats = new HashMap<MediaType, Set<Codec>>();
     
    
    public DefaultEnvironment(Registry registry, String srtpCryptoAlgorithm) {
        if(registry == null) {
            throw new IllegalArgumentException("The registry argument is null");
        }
        this.srtpCryptoAlgorithm = srtpCryptoAlgorithm;
        init(registry.getProperties());
    }
    
    private void init(String[][] properties) {
        for(String[] property: properties) {
            if(property[0].equalsIgnoreCase("Cap")) {
                String sectorId = property[1];
                
                String[] headers = new String[property.length - 3];
                System.arraycopy(property, 3, headers, 0, property.length - 3);

                if("StreamAudio".equalsIgnoreCase(sectorId)) {
                    handleProperty(MediaType.AUDIO, headers);
                } else if("StreamVideo".equalsIgnoreCase(sectorId)) {
                    handleProperty(MediaType.VIDEO, headers);
                }
            }
        }
    }
    
    private void handleProperty(MediaType mediaType, String[] codecsValues) {
        for(String header : codecsValues) {
            String attrValue = header.split("=")[1];
            if(attrValue.startsWith("rtpmap:")) {
                String codecValue = attrValue.substring("rtpmap:".length());
                Codec codec = Codec.valueOf(codecValue);
                addCodec(mediaType, codec);
            }
        }
    }
    
    private void addCodec(MediaType mediaType, Codec codec) {
        Set<Codec> codecs = supportedMediaFormats.get(mediaType);
        if(codecs == null) {
            supportedMediaFormats.put(mediaType, codecs = new HashSet<Codec>());
        }
        codecs.add(codec);
    }
    
    
    public Set<Codec> getCodecs(MediaType mediaType) {
        return supportedMediaFormats.get(mediaType);
    }
    
    public boolean isForceSrtp() {
    	return isForceSrtp;
    }
    
    public void setForceSrtp(boolean forceSrtp) {
    	this.isForceSrtp = forceSrtp;
    }
    
    public String getSrtpCryptoAlgorithm() {
    	return srtpCryptoAlgorithm;
    }
}
