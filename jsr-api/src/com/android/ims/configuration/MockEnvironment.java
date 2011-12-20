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
public class MockEnvironment implements Environment {
	private boolean forceSrtp;
	private final String srtpCryptoAlgorithm = "AES_CM_128_HMAC_SHA1_80";
	
    private final Map<MediaType, Set<Codec>> supportedMediaFormats = new HashMap<MediaType, Set<Codec>>();
    
    {
        Set<Codec> videoCodecs = new HashSet<Codec>();
        videoCodecs.add(new Codec(31, "H261", 90000, 0));
        videoCodecs.add(new Codec(32, "MPV", 90000, 0));
        
        supportedMediaFormats.put(MediaType.VIDEO, videoCodecs);
        
        Set<Codec> audioCodecs = new HashSet<Codec>();
        audioCodecs.add(new Codec(0, "PCMU", 8000, 0));
        //audioCodecs.add(new Codec(8, "PCMA", 8000, 0));
        //audioCodecs.add(new Codec(31, "H261", 90000, 1));
        
        supportedMediaFormats.put(MediaType.AUDIO, audioCodecs);
    }
    
    
    public Set<Codec> getCodecs(MediaType mediaType) {
        return supportedMediaFormats.get(mediaType);
    }
    
    public boolean isForceSrtp() {
        return forceSrtp;
    }
    
    public void setForceSrtp(boolean forceSrtp) {
    	this.forceSrtp = forceSrtp;
    }
    
    public String getSrtpCryptoAlgorithm() {
    	return srtpCryptoAlgorithm;
    }
}
