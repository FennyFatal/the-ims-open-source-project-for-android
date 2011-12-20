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
