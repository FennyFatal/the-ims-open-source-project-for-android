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

package javax.microedition.ims.core.connection;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 8.6.2010
 * Time: 17.36.09
 */

/*
3GPP TS 24.229 V9.1.0 (2009-09)
3rd Generation Partnership Project;
Technical Specification Group Core Network and Terminals;
IP multimedia call control protocol based on
Session Initiation Protocol (SIP)
and Session Description Protocol (SDP);
Stage 3
(Release 9)

The P-Access-Network-Info header field is populated with the following contents:
1) the access-type field set to one of "3GPP-GERAN","3GPP-UTRAN-FDD", "3GPP-UTRAN-TDD",
"3GPP2-1X", "3GPP2-1X-HRPD", "3GPP2-UMB", "IEEE-802.11", "IEEE-802.11a", "IEEE-802.11b",
"IEEE-802.11g", "IEEE-802.11n", "ADSL", "ADSL2", "ADSL2+", "RADSL", "SDSL", "HDSL", "HDSL2",
"G.SHDSL", "VDSL", "IDSL", or "DOCSIS", "IEEE-802.3", "IEEE-802.3a", "IEEE-802.3e", "IEEE-802.3i",
"IEEE-802.3j", "IEEE-802.3u", or "IEEE-802.3ab", "IEEE-802.3ae", IEEE-802.3ak", IEEE-802.3aq",
IEEE-802.3an", "IEEE-802.3y" or "IEEE-802.3z" as appropriate to the access technology in use.
 */
public enum NetworkSubType {
    IEEE_802_11("IEEE-802.11", NetworkType.WIFI),
    IEEE_802_11a("IEEE-802.11a", NetworkType.WIFI),
    IEEE_802_11b("IEEE-802.11b", NetworkType.WIFI),
    IEEE_802_11g("IEEE-802.11g", NetworkType.WIFI),
    IEEE_802_11n("IEEE-802.11n", NetworkType.WIFI),
    IEEE_802_3("IEEE-802.3", NetworkType.WIFI),
    IEEE_802_3a("IEEE-802.3a", NetworkType.WIFI),
    IEEE_802_3e("IEEE-802.3e", NetworkType.WIFI),
    IEEE_802_3i("IEEE-802.3i", NetworkType.WIFI),
    IEEE_802_3j("IEEE-802.3j", NetworkType.WIFI),
    IEEE_802_3u("IEEE-802.3u", NetworkType.WIFI),
    IEEE_802_3ab("IEEE-802.3ab", NetworkType.WIFI),
    IEEE_802_3ae("IEEE-802.3ae", NetworkType.WIFI),
    IEEE_802_3ak("IEEE-802.3ak", NetworkType.WIFI),
    IEEE_802_3aq("IEEE-802.3aq", NetworkType.WIFI),
    IEEE_802_3an("IEEE-802.3an", NetworkType.WIFI),
    IEEE_802_3y("IEEE-802.3y", NetworkType.WIFI),
    IEEE_802_3z("IEEE-802.3z", NetworkType.WIFI),

    G3_TDD("3GPP-UTRAN-TDD", NetworkType.MOBILE),
    G3_FDD("3GPP-UTRAN-FDD", NetworkType.MOBILE),
    G3_2_1X("3GPP2-1X", NetworkType.MOBILE),
    G3_2_1X_HRPD("3GPP2-1X-HRPD", NetworkType.MOBILE),
    G3_2_UMB("3GPP2-UMB", NetworkType.MOBILE),

    ADSL("ADSL", NetworkType.ETHERNET),
    ADSL2("ADSL2", NetworkType.ETHERNET),
    ADSL2_PLUS("ADSL2+", NetworkType.ETHERNET),
    RADSL("RADSL", NetworkType.ETHERNET),
    SDSL("SDSL", NetworkType.ETHERNET),
    HDSL("HDSL", NetworkType.ETHERNET),
    HDSL2("HDSL2", NetworkType.ETHERNET),
    G_SHDSL("G.SHDSL", NetworkType.ETHERNET),
    VDSL("VDSL", NetworkType.ETHERNET),
    IDSL("IDSL", NetworkType.ETHERNET),

    GSM("3GPP-GERAN", NetworkType.MOBILE),
    GPRS("3GPP-GERAN", NetworkType.MOBILE),
    UMTS("3GPP-GERAN", NetworkType.MOBILE),
    HSDPA("3GPP-GERAN", NetworkType.MOBILE),
    HSPA("3GPP-GERAN", NetworkType.MOBILE),
    HSUPA("3GPP-GERAN", NetworkType.MOBILE),
    EDGE("3GPP-GERAN", NetworkType.MOBILE),
    CDMA("3GPP-CDMA2000", NetworkType.MOBILE),
    EVDO_0("3GPP-CDMA2000", NetworkType.MOBILE),
    EVDO_A("3GPP-CDMA2000", NetworkType.MOBILE),
    EVDO_B("3GPP-CDMA2000", NetworkType.MOBILE),

    DOCSIS("DOCSIS", NetworkType.ETHERNET),
    IDEN("IDEN", NetworkType.MOBILE),
    RTT("RTT", NetworkType.UNKNOWN);

    private final String stringValue;
    private final NetworkType networkType;

    private NetworkSubType(final String stringValue, final NetworkType networkType) {
        this.stringValue = stringValue;
        this.networkType = networkType;
    }

    public String stringValue() {
        return stringValue;
    }

    public NetworkType getNetworkType() {
        return networkType;
    }
}
