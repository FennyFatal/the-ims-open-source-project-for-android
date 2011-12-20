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

package javax.microedition.ims.im;

import com.android.ims.core.media.util.UtilsMIME;
import com.android.ims.core.media.util.UtilsMSRP;

import java.util.Date;

/**
 * The FileInfo class is a container for metadata about a file to be sent in 
 * a push request. The basic information is the file path which is formatted 
 * as a File URL according to [JSR75], and the content MIME type of the file. 
 * The file can then be further described with description, hash, size, file 
 * disposition, creation date, modification date, read date, and a file icon.
 * <p/>
 * To send a part of a file, a range may also be specified. The start offset 
 * value refers to the byte position of the file where the file transfer 
 * should start. The first byte of a file is denoted by the ordinal number 1. 
 * The stop offset value refers to the byte position of the file where the 
 * file transfer should stop, inclusive of this byte. If no stop offset 
 * value is specified, the transfer will continue until the end of the file 
 * is reached. If no range at all is specified, the entire file will be sent.
 * <p/>
 * To describe the file with a hash, the 160-bit string resulting from the 
 * computation of Secure Hash Algorithm 1 (SHA-1) should be used. The hash 
 * should be set using the following syntax: hash-algorithm : hash-value, 
 * where hash-value is a byte string with each byte in upper-case hex, 
 * separated by colons.
 * <p/>
 * Example
 * <p/>
 * <pre>
 *  FileInfo fileInfo = new FileInfo("file:///CFCard/img/bob.png", "image/png");
 *  fileInfo
 *      .setHash("sha-1:72:24:5F:E8:65:3D:DA:F3:71:36:2F:86:D4:71:91:3E:E4:A2:CE:2E");
 *  fileInfo.setDescription("My avatar");
 *  fileInfo.setFileDisposition("render");
 *  fileInfo.setSize(32349);
 *  fileInfo.setRange(new int[] {
 *      1, 32349
 *  });
 *  </pre>
 */
public class FileInfo {
    
    private String filePath;
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
    
    /**
     * A constructor for setting all required fields of a FileInfo.
     * 
     * @param filePath - the full path of the physical file including the file name
     * @param contentType - the content MIME type
     * 
     * @throws IllegalArgumentException - if the filePath argument is null
     * @throws IllegalArgumentException - if the contentType argument is not a valid MIME type according to [RFC2045]
     */
    public FileInfo(String filePath, String contentType) {
        if (filePath == null) {
            throw new IllegalArgumentException("The filePath argument must not be null");
        }
        if(!UtilsMIME.isValidContentType(contentType)) {
            throw new IllegalArgumentException("The contentType argument is not a valid MIME type according to [RFC2045]");
        }

        this.filePath = filePath;
        this.contentType = contentType;
        
        this.fileId = UtilsMSRP.generateUniqueFileId();
    }
    
    /**
     * Returns the identifier of the file.
     * 
     * @return the identifier
     */
    public String getFileId() {
        return fileId;
    }
    
    /**
     * Returns the file name.
     * 
     * @return the file name
     */
    public String getFileName() {
        if (filePath == null || filePath.length() == 0) {
            return filePath;
        }
        if (filePath.endsWith("/") || filePath.endsWith("\\")) {
            return "";
        }
        
        int slInd = filePath.lastIndexOf('/');
        int bslInd = filePath.lastIndexOf('\\');
        int posBeforeFileName = slInd > bslInd ? slInd : bslInd;
        
        if(posBeforeFileName < 0) {
            return filePath;
        }
        
        return filePath.substring(posBeforeFileName+1);
    }
    
    /**
     * Returns the file path.
     * 
     * @return the full path of the physical file including the file name
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Returns the content MIME type of the file.
     * 
     * @return the content MIME type
     */
    public String getContentType() {
        return contentType;
    }
    
    /**
     * Sets the hash computation of the file as a string with the following 
     * syntax: hash-algorithm : hash-value, where hash-value is a byte 
     * string with each byte in upper-case hex, separated by colons.
     * <p/>
     * A null value removes any existing value. 
     * 
     * @param hash - the file hash computation
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * Returns the hash computation of the file as a string with the 
     * following syntax: hash-algorithm : hash-value, where hash-value is a 
     * byte string with each byte in upper-case hex, separated by colons.
     * 
     * @return the file hash or null if not set
     */
    public String getHash() {
        return hash;
    }

    /**
     * Sets a short description of the file.  A null value removes any existing value.
     * 
     * @param description - the description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Returns a short description of the file.
     * 
     * @return the description or null if not set
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the file size. A negative number removes any existing value.
     * 
     * @param size - the size of the file in bytes
     */
    public void setSize(long size) {
        this.fileSize = size;
    }

    /**
     * Returns the size of the file in bytes.
     * 
     * @return the file size or -1 if not set
     */
    public long getSize() {
        return fileSize;
    }
    
    /**
     * Sets the creation date of the file.  A null value removes any existing value.
     * 
     * @param creationDate - the creation date
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    
    /**
     * Returns the creation date of the file.
     * 
     * @return the creation date or null if not set
     */
    public Date getCreationDate() {
        return creationDate;
    }
    
    /**
     * Sets the modification date of the file.  A null value removes any existing value.
     * 
     * @param modificationDate - the modification date
     */
    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    /**
     * Returns the modification date of the file.
     * 
     * @return the modification date  or null if not set
     */
    public Date getModificationDate() {
        return modificationDate;
    }
    
    /**
     * Sets the last read date of the file. A null value removes any existing value.
     * 
     * @param readDate - the read date
     */
    public void setReadDate(Date readDate) {
        this.fileReadDate = readDate;
    }
    
    /**
     * Returns the last read date of the file.
     * 
     * @return the read date or null if not set
     */
    public Date getReadDate() {
        return fileReadDate;
    }
    
    /**
     * Sets the file disposition, for example render or attachment. A null 
     * value removes any existing value.
     * 
     * @param disposition - the file disposition
     */
    public void setFileDisposition(String disposition) {
        this.fileDisposition = disposition;
    }
    
    /**
     * Returns the file disposition,  for example render or attachment.
     * 
     * @return the file disposition or null if not set
     */
    public String getFileDisposition() {
        return fileDisposition;
    }
    
    /**
     * Sets the file range as an array containing the start offset and stop 
     * offset of the file.
     * <p/>
     * The start offset refers to the byte position of the file where the 
     * file transfer should start. The first byte of a file is denoted by the 
     * ordinal number 1. The stop offset refers to the byte position of the 
     * file where the file transfer should stop, inclusive of this byte.
     * <p/>
     * The stop offset can be unspecified by setting it to a negative number, 
     * meaning that the transfer will continue until the end of the file 
     * is reached.
     * <p/>
     * A null value of the range parameter removes any existing file range value. 
     * 
     * @param range - the file range
     * 
     * @throws IllegalArgumentException - if the range argument is null
     * @throws IllegalArgumentException - if the length of the range  argument is not 2
     * @throws IllegalArgumentException - if the start offset value in the 
     * range argument is less than 1
     * @throws IllegalArgumentException - if the stop offset value in the 
     * range argument is larger than -1 and less than the start offset value
     */
    public void setRange(int[] range) {
        if (range == null) {
            throw new IllegalArgumentException("The range argument must not be null");
        }
        if (range.length != 2) {
            throw new IllegalArgumentException("The length of the range argument is not 2");
        }
        if (range[0] < 1) {
            throw new IllegalArgumentException("The start offset value in the range argument is less than 1");
        }
        if (range[1] > -1 && range[1] < range[0]) {
            throw new IllegalArgumentException("The stop offset value in the range argument is larger than -1 and less than the start offset value");
        }
        
        this.fileRange = range;
    }
    
    /**
     * Returns the file range as an array containing the start offset and 
     * stop offset of the file. A negative stop offset value indicates an 
     * unspecified stop offset.
     * 
     * @return the file range or null  if not set
     */
    public int[] getRange() {
        return fileRange;
    }
    
    /**
     * Sets a file icon, a small preview of the file.  A null value of 
     * the icon parameter removes any existing file icon.
     * 
     * @param icon - the file icon as a byte array
     * @param iconType - the content MIME type of the file icon
     * 
     * @throws IllegalArgumentException - if the iconType  argument is not a 
     * valid MIME type according to [RFC2045]  when the icon argument is not null 
     */
    public void setFileIcon(byte[] icon, String iconType) {
        this.fileIcon = icon;
        this.fileIconType = iconType;
    }

    /**
     * Returns the file icon, a small preview of the file.
     * 
     * @return the file icon or null if not set
     */
    public byte[] getFileIcon() {
        return fileIcon;
    }
    
    /**
     * Returns the file icon content MIME type.
     * 
     * @return the file icon content type  or null if not set
     */
    public String getFileIconType() {
        return fileIconType;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

}
