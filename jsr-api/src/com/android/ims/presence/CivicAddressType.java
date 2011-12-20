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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum CivicAddressType {
    ADDITIONAL_LOCATION_INFO(13, "LOC"),
    CITY(3, "A3"),
    CITY_DIVISION(4, "A4"),
    COUNTRY_CODE(0, "country"),
    COUNTRY(2, "A2"),
    FLOOR(14, "FLR"),
    HOUSE_NUMBER(10, "HNO"),
    HOUSE_NUMBER_SUFFIX(11, "HNS"),
    LANDMARK(12, "LMK"),
    LEADING_STREET_DIRECTION(7, "PRD"),
    NATIONAL_SUBDIVISION(1, "A1"),
    NEIGHBORHOOD(5, "A5"),
    POSTAL_CODE(16, "PC"),
    RESIDENCE(15, "NAM"),
    STREET(6, "A6"),
    STREET_SUFFIX(9, "STS"),
    TRAILING_STREET_SUFFIX(8, "POD");
    
    private final static Set<Integer> identifiers = new HashSet<Integer>();
    private final static Map<String, CivicAddressType> mapping = new HashMap<String, CivicAddressType>();
    
    static {
        for(CivicAddressType addressType: values()) {
            identifiers.add(addressType.code);
            mapping.put(addressType.tag.toLowerCase(), addressType);
        }
    }
    
    private final int code;
    private final String tag;
    
    private CivicAddressType(int code, String tag) {
        this.code = code;
        this.tag = tag;
    }

    public int getCode() {
        return code;
    }

    public String getTag() {
        return tag;
    }
    
    public static boolean isCodeValid(int identifier) {
        return identifiers.contains(identifier);
    }
    
    public static CivicAddressType createByTagName(String tag) {
        return mapping.get(tag.toLowerCase());
    }
}
