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

package com.android.ims.presence;

import java.util.HashMap;
import java.util.Map;

public enum MoodsValues{
    AFRAID("afraid"),
    AMAZED("amazed"),
    ANGRY("angry"),
    ANNOYED("annoyed"),
    ANXIOUS("anxious"),
    ASHAMED("ashamed"),
    BORED ("bored"),
    BRAVE ("brave"),
    CALM  ("calm"),
    COLD  ("cold"),
    CONFUSED("confused"),
    CONTENTED("contented"),
    CRANKY("cranky"),
    CURIOUS("curious"),
    DEPRESSED ("depressed"),
    DISAPPOINTED("disappointed"),
    DISGUSTED("disgusted"),
    DISTRACTED("distracted"),
    EMBARRASSED("embarrassed"),
    EXCITED("excited"),
    FLIRTATIOUS("flirtatious"),
    FRUSTRATED("frustrated"),
    GRUMPY("grumpy"),
    GUILTY("guilty"),
    HAPPY("happy"),
    HOT("hot"),
    HUMBLED("humbled"),
    HUMILIATED("humiliated"),
    HUNGRY("hungry"),
    HURT("hurt"),
    IMPRESSED("impressed"),
    IN_AWE("in_awe"),
    IN_LOVE("in_love"),
    INDIGNANT ("indignant"),
    INTERESTED("interested"),
    INVINCIBLE("invincible"),
    JEALOUS("jealous"),
    LONELY("lonely"),
    MEAN("mean"),
    MOODY("moody"),
    NERVOUS("nervous"),
    NEUTRAL("neutral"),
    OFFENDED("offended"),
    PLAYFUL("playful"),
    PROUD ("proud"),
    RELIEVED  ("relieved"),
    REMORSEFUL("remorseful"),
    RESTLESS  ("restless"),
    SAD("sad"),
    SARCASTIC ("sarcastic"),
    SERIOUS("serious"),
    SHOCKED("shocked"),
    SHY("shy"),
    SICK("sick"),
    SLEEPY("sleepy"),
    STRESSED("stressed"),
    SURPRISED("surprised"),
    THIRSTY("thirsty"),
    UNKNOWN("unknown"),
    WORRIED("worried");

    private static final Map<String, MoodsValues> mapping = new HashMap<String, MoodsValues>();
    
    static {
        for(MoodsValues moodsValue: values()) {
            mapping.put(moodsValue.value, moodsValue);
        }
    }
    
    private String value;
    
    private MoodsValues(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    static boolean isPredefined(String mood) {
        return mapping.get(mood) != null;
    }
}    