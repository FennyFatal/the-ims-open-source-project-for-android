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

/**
 * Data transfer bean for XCAP exception.
 *
 * @author Andrei Khomushko
 */
public class IXCAPException implements Parcelable {
    private int statusCode;
    private String reasonPhrase;
    private IXCAPError xcapError;

    public static final Parcelable.Creator<IXCAPException> CREATOR = new Parcelable.Creator<IXCAPException>() {
        public IXCAPException createFromParcel(Parcel in) {
            return new IXCAPException(in);
        }

        public IXCAPException[] newArray(int size) {
            return new IXCAPException[size];
        }
    };

    public IXCAPException() {
    }

    private IXCAPException(Parcel in) {
        readFromParcel(in);
    }

    public IXCAPException(int statusCode, String reasonPhrase) {
        this(statusCode, reasonPhrase, null);
    }

    public IXCAPException(int statusCode, String reasonPhrase, IXCAPError xcapError) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.xcapError = xcapError;
    }

    public static Parcelable.Creator<IXCAPException> getCreator() {
        return CREATOR;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public IXCAPError getXcapError() {
        return xcapError;
    }

    
    public int describeContents() {
        return 0;
    }

    
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(statusCode);
        dest.writeString(reasonPhrase);

        dest.writeInt(xcapError == null ? 0 : 1);
        if (xcapError != null) {
            xcapError.writeToParcel(dest, flags);
        }
    }

    protected void readFromParcel(Parcel src) {
        statusCode = src.readInt();
        reasonPhrase = src.readString();

        int xcapErrorPresent = src.readInt();
        if (xcapErrorPresent == 1) {
            xcapError = IXCAPError.CREATOR.createFromParcel(src);
        }
    }

    
    public String toString() {
        return "IXCAPExeption [reasonPhrase=" + reasonPhrase + ", statusCode="
                + statusCode + ", xcapError=" + xcapError + "]";
    }
}
