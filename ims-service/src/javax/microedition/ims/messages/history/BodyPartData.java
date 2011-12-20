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

package javax.microedition.ims.messages.history;

import javax.microedition.ims.common.Logger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class stores message body parts and custom headers associated with them
 *
 * @author ext-achirko
 */
public class BodyPartData {

    /*
      * If a Message has one MessageBodyPart then the Message will have the body
      * content and content type set to the MessageBodyPart content and content type.
 If a Message has several MessageBodyParts, then each MessageBodyPart
 ( recursive MessageBodyPart are not possible) is mapped to a part in the SIP
 message body (content and headers, including the content type).
 The message body content type is by default set to multipart/mixed,
 but can be overridden by the application to be of another subtype to multipart.

 NOTE: For the case where the IMS engine has an SDP attached to the SIP message, then that is handled as if it were a MessageBodyPart not accessible to the application.


 For the receiving side:


 A SIP message with a body where content type is not multipart
 is handled as a Message with one MessageBodyPart and that content type.
 A multipart message (of any subtype) is parsed and each part on the top
 level is mapped to a MessageBodyPart (nested multi-part messages cannot be mapped
 to recursive MessageBodyParts).

 Note: Any SDP part discovered here shall not be mapped to a MessageBodyPart
 accessible to the application, unless it is part of an SIP_OPTIONS response.

      */

    private String contentType;
    private byte[] data;
    private Map<String, String> headers;

    /**
     * Derfault constructor
     */
    public BodyPartData() {
        headers = new HashMap<String, String>();
    }

    /**
     * Returns HEADER value by key
     *
     * @param key - key parameter
     * @return - HEADER value
     */
    public String getHeader(String key) {
        return headers.get(key);
    }

    /**
     * Adds HEADER value using key
     *
     * @param key   - key to use
     * @param value - value to add
     */
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * Returns set of existing keys
     *
     * @return - Set of keys
     */
    public Set<String> getHeadersKeys() {
        return headers.keySet();
    }

    /**
     * Sets content
     *
     * @param content - byte array
     */
    public void setContent(byte[] content) {
        Logger.log("SetContent:" + new String(content));
        this.data = content;
    }

    /**
     * Returns content
     *
     * @return - byte array
     */
    public byte[] getContent() {
        return data;
    }
    
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    public String toString() {
        return "BodyPartData [contentType=" + contentType + ", data="
                + Arrays.toString(data) + ", headers=" + headers + "]";
    }
}
