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

package javax.microedition.ims.messages.wrappers.sip;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 04-Jan-2010
 * Time: 16:42:36
 */
public enum Header {
    Msg("") {},
    Authentication_Info("Authentication-Info"),
    Via("Via"),
    Max_Forwards("Max-Forwards"),
    To("To"),
    SIP_ETag("SIP-Etag"),
    From("From"),
    Call_ID("Call-ID"),
    CSeq("CSeq"),
    Subject("Subject"),
    Server("Server"),
    Min_Expires("Min-Expires"),
    Route("Route"),
    RefresherParam("refresher"),
    Referred_By("Referred-By"),
    Session_Expires("Session-Expires"),
    Min_SE("Min-SE"),
    Allow("Allow"),
    Event("Event"),
    SubscriptionState("Subscription-State"),
    Require("Require"),
    Supported("Supported"),
    Privacy("Privacy"),
    Contact("Contact"),
    Expires("Expires"),
    Allow_Events("Allow-Events"),
    AcceptContact("Accept-Contact"),
    RejectContact("Reject-Contact"),
    Authorization("Authorization"),
    Proxy_Authorization("Proxy-Authorization"),
    UserAgent("User-Agent"),
    SIP_If_Match("SIP-If-Match"),
    Content_Length("Content-Length"),
    WWW_Authenticate("WWW-Authenticate"),
    Proxy_Authenticate("Proxy-Authenticate"),
    Content_Type("Content-Type"),
    PAssociatedUris("P-Associated-Uri"),
    PAssociatedSipUris("P-Associated-SipUri"),
    PAssertedIdentities("P-Asserted-Identity"),
    RecordRoute("Record-Route"),
    Security_Client("Security-Client"),
    Security_Server("Security-Server"),
    Security_Verify("Security-Verify"),
    HistoryInfo("History-Info"),
    ServiceRoutes("Service-Route"),
    Unknown("Unknown"),
    RAck("RAck"),
    RSeq("RSeq"),
    PLastAccessNetwork("P-Last-Access-Network-Info"),
    PAccessNetwork("P-Access-Network-Info"),
    PPreferredIdentity("P-Preferred-Identity"),
    PEmergencyCall("P-Emergency-Call"),
    PEmergencyCallModePreference("P-Emergency-Call-Mode-Preference"),
    ReferTo("Refer-To"),
    Replaces("Replaces"),
    Accept("Accept"),
    Accept_Encoding("Accept-Encoding"),
    Accept_Language("Accept-Language"),
    Content_ID("Content-ID"),
    P_Early_Media("P-Early-Media"),
    Reason("Reason");

    private final String stringValue;

    private Header(String stringValue) {
        this.stringValue = stringValue;
    }

    public String stringValue() {
        return stringValue;
    }

    public boolean testAgainst(String candidate) {
        return candidate.toUpperCase().startsWith(stringValue.toUpperCase());
    }

}
