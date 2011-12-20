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

package javax.microedition.ims.core.sipservice.register;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 12.8.2010
 * Time: 12.47.28
 */
final class RegResultImpl implements RegResult {
    private final boolean success;
    private final Integer statusCode;
    private final String reasonPhrase;
    private final String reasonData;
    private final boolean byTemout;
    private final RedirectData redirectData;
    private final String emergencyCallType;
    private final String[] registerURIs;

    RegResultImpl(
            final boolean success,
            final boolean byTemout,
            final Integer statusCode,
            final String reasonPhrase,
            final String reasonData,
            final RedirectData redirectData,
            final String emergencyCallType,
            final String[] registerURIs) {
        this.success = success;
        this.byTemout = byTemout;
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.reasonData = reasonData;
        this.redirectData = redirectData;
        this.emergencyCallType = emergencyCallType;
        this.registerURIs = registerURIs;
    }

    public boolean isSuccessful() {
        return success;
    }

    public boolean byTimeout() {
        return byTemout;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public String getReasonData() {
        return reasonData;
    }

    public RedirectData getRedirectData() {
        return redirectData;
    }

    public String getEmergencyCallType() {
        return emergencyCallType;
    }

    @Override
    public String[] getRegisterURIs() {
        return registerURIs;
    }

    @Override
    public String toString() {
        return "RegResultImpl [success=" + success + ", statusCode=" + statusCode
                + ", reasonPhrase=" + reasonPhrase + ", reasonData=" + reasonData + ", byTemout="
                + byTemout + ", redirectData=" + redirectData + ", emergencyCallType="
                + emergencyCallType + "]";
    }
}
