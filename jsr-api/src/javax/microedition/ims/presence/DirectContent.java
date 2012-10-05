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

package javax.microedition.ims.presence;

import com.android.ims.util.Utils;

import java.io.*;
import java.util.Arrays;

/**
 * The DirectContent class can be used to handle the adding and retrieving of
 * direct content that may be published along with a presence document. 
 *
 *
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation
 *
 * @author Andrei Khomushko
 * 
 */
public class DirectContent {
    private final String cid;
    private final String contentType;
    private final byte[] content;

    private CustomByteArrayOutputStream os;
    private InputStream is;

    /**
     * Creates a new DirectContent.
     * 
     * @param contentType
     *            - the content to set
     * @param content
     *            - the content MIME type
     * 
     * @throws IllegalArgumentException
     *             - if the contentType argument does not follow [RFC2045]
     *             syntax
     */
    public DirectContent(String contentType, byte[] content) {
        this(Utils.generatePseudoUUID(), contentType, content);
    }

    private DirectContent(String cid, String contentType, byte[] content) {
        this.cid = cid;
        this.content = content;

        if (contentType == null) {
            // TODO add validation
            throw new IllegalArgumentException(
                    "The contentType argument is null");
        }
        this.contentType = contentType;
    }

    /**
     * Returns the content identifier (CID) of this DirectContent.
     * 
     * @return the content identifier
     */
    public String getCid() {
        return cid;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getContent() {
        final byte[] retValue;

        if (os != null && os.isClosed()) {
            retValue = os.toByteArray();
        } else {
            retValue = content;
        }

        return retValue;
    }

    /**
     * Opens an OutputStream to write the content of this DirectContent. If the
     * OutputStream is not closed, the content will not be included when adding
     * the DirectContent to the PresenceDocument.
     * 
     * When this method is invoked, any previous content in this DirectContent
     * will be overwritten. The content type will remain the same.
     * 
     * @return an OutputStream to write the content
     * @throws IOException
     *             - if the OutputStream could not be opened or if the
     *             OutputStream is already open
     */
    public OutputStream openOutputStream() throws IOException {
        if (os == null) {
            os = new CustomByteArrayOutputStream();
        } else {
            os.reset();
        }

        return os;
    }

    private class CustomByteArrayOutputStream extends ByteArrayOutputStream {
        private boolean closed;

        
        public void close() throws IOException {
            super.close();
            this.closed = true;

        }

        
        public synchronized void reset() {
            super.reset();
            this.closed = false;
        }

        boolean isClosed() {
            return closed;
        }
    };

    /**
     * Opens an InputStream to read the content of this DirectContent.
     * 
     * @return an InputStream to read the content
     * @throws IOException
     *             - if the InputStream could not be opened
     */
    public InputStream openInputStream() throws IOException {
        if (is == null) {
            is = new ByteArrayInputStream(content);
        }
        return is;
    }
    
    
    public String toString() {
        return "DirectContent [cid=" + cid + ", content="
                + Arrays.toString(content) + ", contentType=" + contentType
                + ", is=" + is + ", os=" + os + "]";
    }
}
