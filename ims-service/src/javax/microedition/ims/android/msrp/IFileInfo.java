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

import java.util.Date;

public class IFileInfo implements Parcelable {

    private String filePath;
    private String fileName;
    private String contentType;
    private Date creationDate;
    private String description;
    private String fileDisposition;
    private byte[] fileIcon;
    private String fileIconType;
    private long fileSize;
    private Date fileReadDate;
    private int[] fileRange;
    private Date modificationDate;
    private String hash;
    private String fileId;

    public static final Parcelable.Creator<IFileInfo> CREATOR = new Parcelable.Creator<IFileInfo>() {
        public IFileInfo createFromParcel(Parcel in) {
            return new IFileInfo(in);
        }

        public IFileInfo[] newArray(int size) {
            return new IFileInfo[size];
        }
    };

    private IFileInfo() {
    }

    private IFileInfo(Parcel in) {
        readFromParcel(in);
    }

    private IFileInfo(final IFileInfoBuilder fileInfoBuilder) {
        this.filePath = fileInfoBuilder.filePath;
        this.fileName = fileInfoBuilder.fileName;
        this.contentType = fileInfoBuilder.contentType;
        this.creationDate = fileInfoBuilder.creationDate;
        this.description = fileInfoBuilder.description;
        this.fileDisposition = fileInfoBuilder.fileDisposition;
        this.fileIcon = fileInfoBuilder.fileIcon;
        this.fileIconType = fileInfoBuilder.fileIconType;
        this.fileSize = fileInfoBuilder.fileSize;
        this.fileReadDate = fileInfoBuilder.fileReadDate;
        this.fileRange = fileInfoBuilder.fileRange;
        this.modificationDate = fileInfoBuilder.modificationDate;
        this.hash = fileInfoBuilder.hash;
        this.fileId = fileInfoBuilder.fileId;
    }

    
    public int describeContents() {
        return 0;
    }

    
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filePath);
        dest.writeString(fileName);
        dest.writeString(contentType);
        writeDate(dest, creationDate);
        dest.writeString(description);
        dest.writeString(fileDisposition);

        int fileIconInt = (fileIcon != null) ? fileIcon.length : -1;
        dest.writeInt(fileIconInt);
        if (fileIconInt > 0) {
            dest.writeByteArray(fileIcon);
        }

        dest.writeString(fileIconType);
        dest.writeLong(fileSize);
        writeDate(dest, fileReadDate);

        int fileRangeInt = (fileRange != null) ? fileRange.length : -1;
        dest.writeInt(fileRangeInt);
        if (fileRangeInt > 0) {
            dest.writeIntArray(fileRange);
        }

        writeDate(dest, modificationDate);
        dest.writeString(hash);
        dest.writeString(fileId);
    }

    protected void readFromParcel(Parcel src) {
        filePath = src.readString();
        fileName = src.readString();
        contentType = src.readString();
        creationDate = readDate(src);
        description = src.readString();
        fileDisposition = src.readString();

        int fileIconInt = src.readInt();
        if (fileIconInt > 0) {
            fileIcon = new byte[fileIconInt];
            src.readByteArray(fileIcon);
        }

        fileIconType = src.readString();
        fileSize = src.readLong();
        fileReadDate = readDate(src);

        int fileRangeInt = src.readInt();
        if (fileRangeInt > 0) {
            fileRange = new int[fileRangeInt];
            src.readIntArray(fileRange);
        }

        modificationDate = readDate(src);
        hash = src.readString();
        fileId = src.readString();
    }

    private void writeDate(Parcel dest, Date date) {
        long dateLong = -1;
        if (date != null) {
            dateLong = date.getTime();
        }
        dest.writeLong(dateLong);
    }

    private Date readDate(Parcel src) {
        long dateLong = src.readLong();
        if (dateLong != -1) {
            return new Date(dateLong);
        }
        return null;
    }


    public static class IFileInfoBuilder {
        private String filePath;
        private String fileName;
        private String contentType;
        private Date creationDate;
        private String description;
        private String fileDisposition;
        private byte[] fileIcon;
        private String fileIconType;
        private long fileSize;
        private Date fileReadDate;
        private int[] fileRange;
        private Date modificationDate;
        private String hash;
        private String fileId;

        public IFileInfoBuilder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public IFileInfoBuilder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public IFileInfoBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public IFileInfoBuilder creationDate(Date creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public IFileInfoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public IFileInfoBuilder fileDisposition(String fileDisposition) {
            this.fileDisposition = fileDisposition;
            return this;
        }

        public IFileInfoBuilder fileIcon(byte[] fileIcon) {
            this.fileIcon = fileIcon;
            return this;
        }

        public IFileInfoBuilder fileIconType(String fileIconType) {
            this.fileIconType = fileIconType;
            return this;
        }

        public IFileInfoBuilder fileSize(long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public IFileInfoBuilder fileReadDate(Date fileReadDate) {
            this.fileReadDate = fileReadDate;
            return this;
        }

        public IFileInfoBuilder fileRange(int[] fileRange) {
            this.fileRange = fileRange;
            return this;
        }

        public IFileInfoBuilder modificationDate(Date modificationDate) {
            this.modificationDate = modificationDate;
            return this;
        }

        public IFileInfoBuilder hash(String hash) {
            this.hash = hash;
            return this;
        }

        public IFileInfoBuilder fileId(String fileId) {
            this.fileId = fileId;
            return this;
        }

        public IFileInfo build() {
            return new IFileInfo(this);
        }
    }


    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getDescription() {
        return description;
    }

    public String getFileDisposition() {
        return fileDisposition;
    }

    public byte[] getFileIcon() {
        return fileIcon;
    }

    public String getFileIconType() {
        return fileIconType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public Date getFileReadDate() {
        return fileReadDate;
    }

    public int[] getFileRange() {
        return fileRange;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public String getHash() {
        return hash;
    }

    public String getFileId() {
        return fileId;
    }

}
