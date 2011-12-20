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

package javax.microedition.ims.android.msrp;

import android.os.Parcel;
import android.os.Parcelable;

public class IFileSelector implements Parcelable {

    private String fileName;
    private String contentType;
    private String hash;
    private int size;
    private int[] range;
    private String fileId;

    public static final Parcelable.Creator<IFileSelector> CREATOR = new Parcelable.Creator<IFileSelector>() {
        public IFileSelector createFromParcel(Parcel in) {
            return new IFileSelector(in);
        }

        public IFileSelector[] newArray(int size) {
            return new IFileSelector[size];
        }
    };

    private IFileSelector() {
    }

    private IFileSelector(Parcel in) {
        readFromParcel(in);
    }

    private IFileSelector(final IFileSelectorBuilder fileSelectorBuilder) {
        this.fileName = fileSelectorBuilder.fileName;
        this.contentType = fileSelectorBuilder.contentType;
        this.hash = fileSelectorBuilder.hash;
        this.size = fileSelectorBuilder.size;
        this.range = fileSelectorBuilder.range;
        this.fileId = fileSelectorBuilder.fileId;
    }

    
    public int describeContents() {
        return 0;
    }

    
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileName);
        dest.writeString(contentType);
        dest.writeString(hash);
        dest.writeInt(size);

        int rangeInt = (range != null) ? range.length : -1;
        dest.writeInt(rangeInt);
        if (rangeInt > 0) {
            dest.writeIntArray(range);
        }

        dest.writeString(fileId);
    }

    protected void readFromParcel(Parcel src) {
        fileName = src.readString();
        contentType = src.readString();
        hash = src.readString();
        size = src.readInt();

        int rangeInt = src.readInt();
        if (rangeInt > 0) {
            range = new int[rangeInt];
            src.readIntArray(range);
        }

        fileId = src.readString();
    }


    public static class IFileSelectorBuilder {
        private String fileName;
        private String contentType;
        private String hash;
        private int size;
        private int[] range;
        private String fileId;

        public IFileSelectorBuilder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public IFileSelectorBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public IFileSelectorBuilder hash(String hash) {
            this.hash = hash;
            return this;
        }

        public IFileSelectorBuilder size(int size) {
            this.size = size;
            return this;
        }

        public IFileSelectorBuilder range(int[] range) {
            this.range = range;
            return this;
        }

        public IFileSelectorBuilder fileId(String fileId) {
            this.fileId = fileId;
            return this;
        }

        public IFileSelector build() {
            return new IFileSelector(this);
        }
    }


    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getHash() {
        return hash;
    }

    public int getSize() {
        return size;
    }

    public int[] getRange() {
        return range;
    }

    public String getFileId() {
        return fileId;
    }
}
