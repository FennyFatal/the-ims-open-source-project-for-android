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
 * Data transfer bean for document entry.
 *
 * @author Andrei Khomushko
 */
public class IDocumentEntry implements Parcelable {
    private String documentSelector;
    private String etag;
    private long lastModified;
    private int size;

    public static final Parcelable.Creator<IDocumentEntry> CREATOR = new Parcelable.Creator<IDocumentEntry>() {
        public IDocumentEntry createFromParcel(Parcel in) {
            return new IDocumentEntry(in);
        }

        public IDocumentEntry[] newArray(int size) {
            return new IDocumentEntry[size];
        }
    };

    public IDocumentEntry() {
    }

    private IDocumentEntry(Parcel in) {
        readFromParcel(in);
    }

    public IDocumentEntry(String documentSelector, String etag,
                          long lastModified, int size) {
        this.documentSelector = documentSelector;
        this.etag = etag;
        this.lastModified = lastModified;
        this.size = size;
    }

    public static Parcelable.Creator<IDocumentEntry> getCreator() {
        return CREATOR;
    }

    public String getDocumentSelector() {
        return documentSelector;
    }

    public String getEtag() {
        return etag;
    }

    public long getLastModified() {
        return lastModified;
    }

    public int getSize() {
        return size;
    }

    
    public int describeContents() {
        return 0;
    }

    
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(documentSelector);
        dest.writeString(etag);
        dest.writeLong(lastModified);
        dest.writeInt(size);

    }

    protected void readFromParcel(Parcel dest) {
        documentSelector = dest.readString();
        etag = dest.readString();
        lastModified = dest.readLong();
        size = dest.readInt();
    }

    
    public String toString() {
        return "IDocumentEntry [documentSelector=" + documentSelector
                + ", etag=" + etag + ", lastModified=" + lastModified
                + ", size=" + size + "]";
    }
}
