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

package javax.microedition.ims.android.xdm;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class IPresenceAuthorizationRulesHolder implements Parcelable {
    private String etag;
    private IPresenceAuthorizationRule[] iPresenceAuthorizationRules;

    public static final Parcelable.Creator<IPresenceAuthorizationRulesHolder> CREATOR = new Parcelable.Creator<IPresenceAuthorizationRulesHolder>() {
        public IPresenceAuthorizationRulesHolder createFromParcel(Parcel in) {
            return new IPresenceAuthorizationRulesHolder(in);
        }

        public IPresenceAuthorizationRulesHolder[] newArray(int size) {
            return new IPresenceAuthorizationRulesHolder[size];
        }
    };

    public IPresenceAuthorizationRulesHolder() {
    }

    private IPresenceAuthorizationRulesHolder(Parcel in) {
        readFromParcel(in);
    }

    public static Parcelable.Creator<IPresenceAuthorizationRulesHolder> getCreator() {
        return CREATOR;
    }

    public IPresenceAuthorizationRule[] getiPresenceAuthorizationRules() {
        return iPresenceAuthorizationRules;
    }

    public void setiPresenceAuthorizationRules(IPresenceAuthorizationRule[] iPresenceAuthorizationRules) {
        this.iPresenceAuthorizationRules = iPresenceAuthorizationRules;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    
    public int describeContents() {
        return 0;
    }

    
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(etag);
        dest.writeInt(iPresenceAuthorizationRules == null ? -1 : iPresenceAuthorizationRules.length);
        if (iPresenceAuthorizationRules != null) {
            for (IPresenceAuthorizationRule iPresenceAuthorizationRule : iPresenceAuthorizationRules) {
                iPresenceAuthorizationRule.writeToParcel(dest, flags);
            }
        }
    }

    protected void readFromParcel(Parcel src) {
        etag = src.readString();
        int size = src.readInt();
        if (size > -1) {
            iPresenceAuthorizationRules = new IPresenceAuthorizationRule[size];
            for (int i = 0; i < size; i++) {
                iPresenceAuthorizationRules[i] = IPresenceAuthorizationRule.CREATOR.createFromParcel(src);
            }
        }
    }

    
    public String toString() {
        return "IPresenceAuthorizationRulesHolder [etag=" + etag + ", iPresenceAuthorizationRules="
                + Arrays.toString(iPresenceAuthorizationRules) + "]";
    }

}
