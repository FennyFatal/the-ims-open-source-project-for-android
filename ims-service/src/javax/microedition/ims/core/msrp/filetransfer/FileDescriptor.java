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

package javax.microedition.ims.core.msrp.filetransfer;

import javax.microedition.ims.messages.utils.MsrpUtils;
import java.util.Arrays;
import java.util.Date;


public class FileDescriptor {
    private final String filePath;
    private final String fileName;
    private final String contentType;
    private final Date creationDate;
    private final String description;
    private final String fileDisposition;
    private final byte[] fileIcon;
    private final String fileIconType;
    private final long fileSize;
    private final Date fileReadDate;
    private final int[] fileRange;
    private final Date modificationDate;
    private final String hash;
    private final String fileId;

    private final int fileDescriptorHashCode;

    private FileDescriptor(final FileDescriptorBuilder builder) {

        if (builder.hash == null) {
            throw new IllegalArgumentException("'Hash' is mandatory parameter. Now its value is " + builder.hash);
        }

        this.filePath = builder.filePath;
        this.fileName = builder.fileName;
        this.contentType = builder.contentType;
        this.creationDate = builder.creationDate;
        this.description = builder.description;
        this.fileDisposition = builder.fileDisposition;
        this.fileIcon = builder.fileIcon;
        this.fileIconType = builder.fileIconType;

        this.fileSize = builder.fileSize;
        if (this.fileSize < 0) {
            throw new IllegalArgumentException("File size can not be less then 0. Now it has value '" +
                    this.fileSize + "'. File descriptor: " + builder);
        }

        this.fileReadDate = builder.fileReadDate;
        this.fileRange = builder.fileRange;
        this.modificationDate = builder.modificationDate;
        this.hash = builder.hash;

        if (builder.fileId == null) {
            this.fileId = MsrpUtils.generateFileId();
        }
        else {
            this.fileId = builder.fileId;
        }

        fileDescriptorHashCode = calculateHashCode();
    }


    public String buildContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("name:\"").append(fileName).append("\" type:").append(contentType);
        sb.append(" size:").append(fileSize);
        if (hash != null) {
            sb.append(" hash:sha-1:").append(hash);
        }
        return sb.toString();
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

    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FileDescriptor that = (FileDescriptor) o;

        return fileDescriptorHashCode == that.fileDescriptorHashCode;

    }

    
    public int hashCode() {
        return fileDescriptorHashCode;
    }

    private int calculateHashCode() {
        int result = filePath != null ? filePath.hashCode() : 0;
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (fileDisposition != null ? fileDisposition.hashCode() : 0);
        result = 31 * result + (fileIcon != null ? Arrays.hashCode(fileIcon) : 0);
        result = 31 * result + (fileIconType != null ? fileIconType.hashCode() : 0);
        result = 31 * result + (int) fileSize;
        result = 31 * result + (fileReadDate != null ? fileReadDate.hashCode() : 0);
        result = 31 * result + (fileRange != null ? Arrays.hashCode(fileRange) : 0);
        result = 31 * result + (modificationDate != null ? modificationDate.hashCode() : 0);
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        result = 31 * result + (fileId != null ? fileId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FileDescriptor{" +
                "filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", creationDate=" + creationDate +
                ", description='" + description + '\'' +
                ", fileDisposition='" + fileDisposition + '\'' +
                ", fileIcon=" + Arrays.toString(fileIcon) +
                ", fileIconType='" + fileIconType + '\'' +
                ", fileSize=" + fileSize +
                ", fileReadDate=" + fileReadDate +
                ", fileRange=" + Arrays.toString(fileRange) +
                ", modificationDate=" + modificationDate +
                ", hash='" + hash + '\'' +
                ", fileId='" + fileId + '\'' +
                ", fileDescriptorHashCode=" + fileDescriptorHashCode +
                '}';
    }

    public static class FileDescriptorBuilder {
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

        public FileDescriptorBuilder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public FileDescriptorBuilder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public FileDescriptorBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public FileDescriptorBuilder creationDate(Date creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public FileDescriptorBuilder description(String description) {
            this.description = description;
            return this;
        }

        public FileDescriptorBuilder fileDisposition(String fileDisposition) {
            this.fileDisposition = fileDisposition;
            return this;
        }

        public FileDescriptorBuilder fileIcon(byte[] fileIcon) {
            this.fileIcon = fileIcon;
            return this;
        }

        public FileDescriptorBuilder fileIconType(String fileIconType) {
            this.fileIconType = fileIconType;
            return this;
        }

        public FileDescriptorBuilder fileSize(long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public FileDescriptorBuilder fileReadDate(Date fileReadDate) {
            this.fileReadDate = fileReadDate;
            return this;
        }

        public FileDescriptorBuilder fileRange(int[] fileRange) {
            this.fileRange = fileRange;
            return this;
        }

        public FileDescriptorBuilder modificationDate(Date modificationDate) {
            this.modificationDate = modificationDate;
            return this;
        }

        public FileDescriptorBuilder hash(String hash) {
            this.hash = hash;
            return this;
        }

        public FileDescriptorBuilder fileId(String fileId) {
            this.fileId = fileId;
            return this;
        }

        public FileDescriptor build() {
            return new FileDescriptor(this);
        }

        @Override
        public String toString() {
            return "FileDescriptorBuilder{" +
                    "filePath='" + filePath + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", contentType='" + contentType + '\'' +
                    ", creationDate=" + creationDate +
                    ", description='" + description + '\'' +
                    ", fileDisposition='" + fileDisposition + '\'' +
                    ", fileIcon=" + Arrays.toString(fileIcon) +
                    ", fileIconType='" + fileIconType + '\'' +
                    ", fileSize=" + fileSize +
                    ", fileReadDate=" + fileReadDate +
                    ", fileRange=" + Arrays.toString(fileRange) +
                    ", modificationDate=" + modificationDate +
                    ", hash='" + hash + '\'' +
                    ", fileId='" + fileId + '\'' +
                    '}';
        }
    }

}
