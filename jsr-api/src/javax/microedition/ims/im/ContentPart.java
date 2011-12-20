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

import java.io.*;

/**
 * A ContentPart carries a content that can be set through the constructor 
 * or an output stream. After creating a ContentPart it can be attached 
 * to a Message by using the addContentPart method.
 * <p/>
 * Examples
 * <p/>
 * This example creates a simple textual content part.
 * <p/>
 * <pre>
 *  ContentPart contentPart = new ContentPart("My name is Alice".getBytes(),
 *      "text/plain");
 * </pre>
 */
public class ContentPart {
    
    private byte[] content;
    private String contentType;
    private String disposition;
    
    private ByteArrayOutputStream byteArrayOutputStream;
    
    /**
     * Constructor for a new ContentPart  to be added to a Message.
     * 
     * @param content - the content to send, null can be used to indicate an empty content part
     * @param contentType - the content MIME type
     * 
     * @throws IllegalArgumentException - if the contentType  argument is not a valid MIME type according to [RFC2045]
     */
    public ContentPart(byte[] content, String contentType) {
        if(!UtilsMIME.isValidContentType(contentType)) {
            throw new IllegalArgumentException("The contentType  argument is not a valid MIME type according to [RFC2045]");
        }
        
        this.content = content;
        this.contentType = contentType;
    }
    
    /**
     * Returns the content of this ContentPart .
     * 
     * @return the content, or null if the content has not been set
     */
    public byte[] getContent() {
        if (content != null) {
            return content;
        } else if (byteArrayOutputStream != null) {
            return byteArrayOutputStream.toByteArray();
        }
        return content;
    }
    
    /**
     * Returns the content disposition,  for example render or attachment.
     * 
     * @return the content disposition or null if not set
     */
    public String getContentDisposition() {
        return disposition;
    }
    
    /**
     * Returns the content MIME type of this ContentPart.
     * 
     * @return the content MIME type
     */
    public String getContentType() {
        return contentType;
    }
    
    /**
     * Returns the size of the content of this ContentPart in bytes.
     * 
     * @return the size in bytes
     */
    public int getSize() {
        int size = 0;
        
        if (content == null) {
            if (byteArrayOutputStream != null) {
                size = byteArrayOutputStream.toByteArray().length;
            }
        } else {
            size = content.length;
        }
        
        return size;
    }
    
    /**
     * Opens an InputStream to read the content of this ContentPart.
     * 
     * @return an InputStream to read the content
     * @throws IOException - if the InputStream could not be opened
     */
    public InputStream openInputStream() throws IOException {
        ByteArrayInputStream byteArrayInputStream;
        
        if (content == null) {
            if (byteArrayOutputStream != null) {
                byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            } else {
                throw new IOException("The InputStream could not be opened");
            }
        } else {
            byteArrayInputStream = new ByteArrayInputStream(content);
        }
        
        return byteArrayInputStream;
    }
    
    /**
     * Opens an OutputStream to write the content of this ContentPart. The 
     * content will be included in the ContentPart when the OutputStream is 
     * closed. Until the OutputStream has been closed, the content of the 
     * ContentPart will be empty.
     * <p/>
     * When this method is invoked, any previous content in this ContentPart 
     * is overwritten. The content type will remain the same.
     *  
     * @return an OutputStream to write the content
     * @throws IOException - if the OutputStream  could not be opened or if the OutputStream is already open
     */
    public OutputStream openOutputStream() throws IOException {
        content = null;
        if (byteArrayOutputStream != null) {
            throw new IOException("The OutputStream is already open");
        }
        byteArrayOutputStream = new ByteArrayOutputStream();
        return byteArrayOutputStream;
    }
    
    /**
     * Sets the content disposition, for example render or attachment. 
     * A null value removes any existing value.
     * 
     * @param disposition - the content disposition
     */
    public void setContentDisposition(String disposition) {
        this.disposition = disposition;
    }

}
