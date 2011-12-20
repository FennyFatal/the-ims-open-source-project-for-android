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

/**
 * Data transfer bean for uniqueness bean.
 *
 * @author Andrei Khomushko
 */
public class IUniquenessError implements Parcelable {
    private String field;
    private String[] alternativeValues;

    public static final Parcelable.Creator<IUniquenessError> CREATOR = new Parcelable.Creator<IUniquenessError>() {
        public IUniquenessError createFromParcel(Parcel in) {
            return new IUniquenessError(in);
        }

        public IUniquenessError[] newArray(int size) {
            return new IUniquenessError[size];
        }
    };

    public IUniquenessError() {
    }

    private IUniquenessError(Parcel in) {
        readFromParcel(in);
    }

    public IUniquenessError(String field, String[] alternativeValues) {
        this.field = field;
        this.alternativeValues = alternativeValues;
    }

    public static Parcelable.Creator<IUniquenessError> getCreator() {
        return CREATOR;
    }

    public String getField() {
        return field;
    }

    public String[] getAlternativeValues() {
        return alternativeValues;
    }

    
    public int describeContents() {
        return 0;
    }

    
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(field);

        int length = (alternativeValues == null ? -1 : alternativeValues.length);
        dest.writeInt(length);
        if (length > -1) {
            dest.writeStringArray(alternativeValues);
        }
    }

    protected void readFromParcel(Parcel dest) {
        field = dest.readString();

        int length = dest.readInt();
        if (length > -1) {
            alternativeValues = new String[length];
            dest.readStringArray(alternativeValues);
        }
    }

    
    public String toString() {
        return "IUniquenessError [alternativeValues="
                + Arrays.toString(alternativeValues) + ", field=" + field + "]";
    }
}
