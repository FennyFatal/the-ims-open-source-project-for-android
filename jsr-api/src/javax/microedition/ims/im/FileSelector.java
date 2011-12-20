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

/**
 * The FileSelector class is a container for metadata about a file that is 
 * going to be pulled from a remote user in a pull request. Since the remote 
 * user must be able to identify the specific file to send, the file must be 
 * described by at least one of the following attributes: file name, content 
 * type, hash, or size.
 * <p/>
 * To retrieve a part of a file, a range may also be specified. The start 
 * offset value refers to the byte position of the file where the file 
 * transfer should start. The first byte of a file is denoted by the ordinal 
 * number 1. The stop offset value refers to the byte position of the file 
 * where the file transfer should stop, inclusive of this byte. If no stop 
 * offset value is specified, the transfer will continue until the end of 
 * the file is reached. If no range at all is specified, the entire file 
 * will be sent.
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
 *  FileSelector fileSelector = new FileSelector("bob.png", "image/png",
 *      "sha-1:72:24:5F:E8:65:3D:DA:F3:71:36:2F:86:D4:71:91:3E:E4:A2:CE:2E", 32349,
 *      new int[] {
 *          1, 32349
 *      });
 * </pre>
 */
public class FileSelector {
    
    private String fileName;
    private String contentType;
    private String hash;
    private int size;
    private int[] range;
    
    private String fileId;
    
    /**
     * A constructor for setting all relevant fields of a FileSelector. 
     * Unspecified values can be set to null. The file size should be set 
     * to -1 if unspecified. At least one of the arguments fileName, 
     * contentType, hash, or size must be set.
     * 
     * @param fileName - the file name, or null
     * @param contentType - the content MIME type, or null
     * @param hash - the file hash computation (see above for details), or null
     * @param size - the file size in bytes, or -1 if not known
     * @param range - the file range as an array containing the start offset and stop offset, or null 
     * 
     * @throws IllegalArgumentException - if fileName, contentType, and hash are null and size is -1
     * @throws IllegalArgumentException - if the length of the range argument is not 2
     * @throws IllegalArgumentException - if the start offset value in the range argument is less than 1
     * @throws IllegalArgumentException - if the stop offset value in the 
     * range argument is larger than -1 and less than the start offset value
     * @throws IllegalArgumentException - if the contentType  argument is not 
     * either null or a valid MIME type according to [RFC2045 ]
     */
    public FileSelector(String fileName, String contentType, String hash, int size, int[] range) {
        if (fileName == null && contentType == null && hash == null && size == -1) {
            throw new IllegalArgumentException("fileName, contentType, and hash are null and size is -1");
        }
        if (range != null) {
            if (range.length != 2) {
                throw new IllegalArgumentException("The length of the range argument is not 2");
            }
            if (range[0] < 1) {
                throw new IllegalArgumentException("The start offset value in the range argument is less than 1");
            }
            if (range[1] > -1 && range[1] < range[0]) {
                throw new IllegalArgumentException("The stop offset value in the range argument is larger than -1 and less than the start offset value");
            }
        }
        if(contentType != null && !UtilsMIME.isValidContentType(contentType)) {
            throw new IllegalArgumentException("The contentType argument is not either null or a valid MIME type according to [RFC2045 ]");
        }
        
        this.fileName = fileName;
        this.contentType = contentType;
        this.hash = hash;
        this.size = size;
        this.range = range;
        
        this.fileId = UtilsMSRP.generateUniqueFileId();
    }
    
    /**
     * Returns the content MIME type of the file.
     * 
     * @return the content MIME type or null if not set
     */
    public String getContentType() {
        return contentType;
    }
    
    /**
     * Returns the identifier of this FileSelector.
     * 
     * @return the identifier 
     */
    public String getFileId() {
        return fileId;
    }
    
    /**
     * Returns the file name.
     * 
     * @return the file name or null if not set
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * Returns the hash computation of the file as a string with the 
     * following syntax: hash-algorithm : hash-value, where hash-value 
     * is a byte string with each byte in upper-case hex, separated by colons.
     * 
     * @return the file hash or null if not set
     */
    public String getHash() {
        return hash;
    }
    
    /**
     * Returns the file range as an array containing the start offset 
     * and stop offset of the file. A negative stop offset value 
     * indicates an unspecified stop offset. 
     * 
     * @return the file range  or null  if not set 
     */
    public int[] getRange() {
        return range;
    }
    
    /**
     * Returns the size of the file in bytes.
     * 
     * @return the file size or -1 if not set 
     */
    public int getSize() {
        return size;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
