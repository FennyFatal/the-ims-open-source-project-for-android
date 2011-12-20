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
 * Data transfer bean for XCAP error.
 *
 * @author Andrei Khomushko
 */
public class IXCAPError implements Parcelable {
    private String closestAncestor;
    private String extensionContent;
    private IUniquenessError[] uniquenessErrors;
    private String xCAPErrorPhrase;
    private int xCAPErrorType;

    public static final Parcelable.Creator<IXCAPError> CREATOR = new Parcelable.Creator<IXCAPError>() {
        public IXCAPError createFromParcel(Parcel in) {
            return new IXCAPError(in);
        }

        public IXCAPError[] newArray(int size) {
            return new IXCAPError[size];
        }
    };

    public IXCAPError() {
    }

    private IXCAPError(Parcel in) {
        readFromParcel(in);
    }

    public IXCAPError(String closestAncestor, String extensionContent,
                      IUniquenessError[] uniquenessErrors, String xCAPErrorPhrase,
                      int xCAPErrorType) {
        this.closestAncestor = closestAncestor;
        this.extensionContent = extensionContent;
        this.uniquenessErrors = uniquenessErrors;
        this.xCAPErrorPhrase = xCAPErrorPhrase;
        this.xCAPErrorType = xCAPErrorType;
    }

    public static Parcelable.Creator<IXCAPError> getCreator() {
        return CREATOR;
    }

    public String getClosestAncestor() {
        return closestAncestor;
    }

    public String getExtensionContent() {
        return extensionContent;
    }

    public IUniquenessError[] getUniquenessErrors() {
        return uniquenessErrors;
    }

    public String getxCAPErrorPhrase() {
        return xCAPErrorPhrase;
    }

    public int getxCAPErrorType() {
        return xCAPErrorType;
    }

    
    public int describeContents() {
        return 0;
    }

    
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(closestAncestor);
        dest.writeString(extensionContent);

        int length = (uniquenessErrors == null ? -1 : uniquenessErrors.length);
        if (length > -1) {
            for (IUniquenessError uniquenessError : uniquenessErrors) {
                uniquenessError.writeToParcel(dest, flags);
            }
        }

        dest.writeString(xCAPErrorPhrase);
        dest.writeInt(xCAPErrorType);
    }

    protected void readFromParcel(Parcel dest) {
        closestAncestor = dest.readString();
        extensionContent = dest.readString();
        int length = dest.readInt();
        if (length > -1) {
            uniquenessErrors = IUniquenessError.CREATOR.newArray(length);
            for (int i = 0; i < length; i++) {
                uniquenessErrors[i] = IUniquenessError.CREATOR.createFromParcel(dest);
            }
        }
    }

    
    public String toString() {
        return "IXCAPError [closestAncestor=" + closestAncestor
                + ", extensionContent=" + extensionContent
                + ", uniquenessErrors=" + Arrays.toString(uniquenessErrors)
                + ", xCAPErrorPhrase=" + xCAPErrorPhrase + ", xCAPErrorType="
                + xCAPErrorType + "]";
    }
}
