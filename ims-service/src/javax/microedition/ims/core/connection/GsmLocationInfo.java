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


/**
 * Wrapper object for GSM location info
 *
 * @author Anton Chirko
 */
public final class GsmLocationInfo {
    private final int cid;
    private final int lac;
    private final String mccMnc;
    private final NetworkSubType networkSubType;

    public GsmLocationInfo(
            final int cid,
            final int lac,
            final String mccMnc,
            final NetworkSubType networkSubType) {

        this.cid = cid;
        this.lac = lac;
        this.mccMnc = mccMnc;
        this.networkSubType = networkSubType;
    }

    public int getCid() {
        return cid;
    }

    public int getLac() {
        return lac;
    }

    //see TS 24.229 (http://www.quintillion.co.jp/3GPP/Specs/24229-890.pdf page 238)
    //"3GPP-UTRAN-TDD; utran-cell-id-3gpp=" + locationInfo.toUtranCellId3gppValue();
    public String toCellIdentity() {

        String retValue = null;

        if (networkSubType != null) {
            String mccMncAsString = mccMnc == null ? "" : mccMnc;

            if (NetworkSubType.G3_TDD == networkSubType) {
                // Both lac and cid are fixed length fields, filled with leading zeros if needed.
                // The length of the fields are 16 and 28 bits, respectively
                retValue = networkSubType.stringValue() + "; utran-cell-id-3gpp=" + mccMncAsString +
                        String.format("%04x%07x", lac, cid);
            } else if (NetworkSubType.GSM == networkSubType) {
                // In 2G case both lac and cid are 16 bit fields
                retValue = networkSubType.stringValue() + "; cgi-3gpp=" + mccMncAsString +
                        String.format("%04x%04x", lac, cid);
            } else {
                // Some other case, not sure about the field sizes here...
                retValue = networkSubType.stringValue() + "; cgi-3gpp=" + mccMncAsString +
                        String.format("%04x%04x", lac, cid);
            }
        }

        return retValue;
    }


    public String toString() {
        return "GsmLocationInfo{" +
                "cid=" + cid +
                ", lac=" + lac +
                ", mccMnc='" + mccMnc + '\'' +
                '}';
    }
}
