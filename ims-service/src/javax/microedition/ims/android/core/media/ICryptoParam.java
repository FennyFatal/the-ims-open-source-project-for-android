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
package javax.microedition.ims.android.core.media;

import android.os.Parcel;
import android.os.Parcelable;

public class ICryptoParam implements Parcelable{
	//private static final String TAG = "ICryptoParam";
	
	private final int tag;
    private final String cryptoSuit;
    private final String cryptoParam;
    
    public static final Parcelable.Creator<ICryptoParam> CREATOR = new Parcelable.Creator<ICryptoParam>() {
        public ICryptoParam createFromParcel(Parcel in) {
            return readFromParcel(in);
        }

        public ICryptoParam[] newArray(int size) {
            return new ICryptoParam[size];
        }
    };

    public ICryptoParam(int tag, String cryptoSuit, String cryptoParam) {
    	if(cryptoSuit == null) {
    		throw new IllegalArgumentException("The cryptoSuit argument can't be null");
    	}

    	
    	if(cryptoParam == null) {
    		throw new IllegalArgumentException("The cryptoParam argument can't be null");
    	}

		this.tag = tag;
		this.cryptoSuit = cryptoSuit;
		this.cryptoParam = cryptoParam;
	}
    
	public int getTag() {
		return tag;
	}

	public String getCryptoSuit() {
		return cryptoSuit;
	}

	public String getCryptoParam() {
		return cryptoParam;
	}

    public int describeContents() {
    	return 0;
    }
	
    public void writeToParcel(Parcel dest, int arg1) {
        dest.writeInt(tag);
        dest.writeString(cryptoSuit);
        dest.writeString(cryptoParam);
    }
    
    private static ICryptoParam readFromParcel(Parcel src) {
        int tag = src.readInt();
        String cryptoSuit = src.readString();
        String cryptoParam = src.readString();
        return new ICryptoParam(tag, cryptoSuit, cryptoParam);
    }

	@Override
	public String toString() {
		return "ICryptoParam [tag=" + tag + ", cryptoSuit=" + cryptoSuit
				+ ", cryptoParam=" + cryptoParam + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cryptoParam == null) ? 0 : cryptoParam.hashCode());
		result = prime * result
				+ ((cryptoSuit == null) ? 0 : cryptoSuit.hashCode());
		result = prime * result + tag;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ICryptoParam other = (ICryptoParam) obj;
		if (cryptoParam == null) {
			if (other.cryptoParam != null)
				return false;
		} else if (!cryptoParam.equals(other.cryptoParam))
			return false;
		if (cryptoSuit == null) {
			if (other.cryptoSuit != null)
				return false;
		} else if (!cryptoSuit.equals(other.cryptoSuit))
			return false;
		if (tag != other.tag)
			return false;
		return true;
	}
}
