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

import javax.microedition.ims.common.util.MultiValueMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Wrapper class for SIP-message headers and body. Is using for providing message history data in simple way.
 *
 * @author ext-achirko
 */
public class MessageDataImpl implements MessageData {

    private String reasonPhrase, method;
    private MultiValueMap<String, String> headers;
    private List<BodyPartData> parts;
    private int state = STATE_UNSENT, statusCode;

    public MessageDataImpl() {
        headers = new MultiValueMap<String, String>();
        parts = new ArrayList<BodyPartData>();
    }

    /* (non-Javadoc)
      * @see javax.microedition.ims.messages.history.MessageDataI#addHeader(java.lang.String, java.lang.String)
      */

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    /* (non-Javadoc)
      * @see javax.microedition.ims.messages.history.MessageDataI#createBodyPart()
      */

    public BodyPartData createBodyPart() {
        BodyPartData data = new BodyPartData();
        parts.add(data);
        return data;
    }

    public BodyPartData createBodyPart(int index) {
        BodyPartData data = new BodyPartData();
        parts.add(index, data);
        return data;
    }

    /* (non-Javadoc)
      * @see javax.microedition.ims.messages.history.MessageDataI#getBodyParts()
      */

    public BodyPartData[] getBodyParts() {
        BodyPartData[] array = parts.toArray(new BodyPartData[parts.size()]);
        return array;
    }

    /* (non-Javadoc)
      * @see javax.microedition.ims.messages.history.MessageDataI#getHeaders(java.lang.String)
      */

    public String[] getHeaders(String key) {
        Collection<String> values = headers.get(key);
        return values == null ? new String[0] : values.toArray(new String[0]);
    }

    /**
     * Returns all existing keys.
     *
     * @return Collection<String> - collection of keys
     */
    public Collection<String> getHeadersKeys() {
        return headers.keySet();
    }

    /* (non-Javadoc)
      * @see javax.microedition.ims.messages.history.MessageDataI#getMethod()
      */

    public String getMethod() {
        return method;

    }

    /* (non-Javadoc)
      * @see javax.microedition.ims.messages.history.MessageDataI#getReasonPhrase()
      */

    public String getReasonPhrase() {
        return reasonPhrase;

    }

    /* (non-Javadoc)
      * @see javax.microedition.ims.messages.history.MessageDataI#getState()
      */

    public int getState() {
        return state;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    /* (non-Javadoc)
      * @see javax.microedition.ims.messages.history.MessageDataI#getStatusCode()
      */

    public int getStatusCode() {
        return statusCode;

    }

    /**
     * Cleans the object content
     */
    public void clean() {
        headers.clear();
        parts.clear();
        parts = null;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String toString() {
        return "MessageData[headers: " + headers + " state: " + state + " code: " +
                statusCode + " phrase: " + reasonPhrase + "]";
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setBodyParts(final List<BodyPartData> parts) {
        this.parts = new ArrayList<BodyPartData>(parts);
    }
}
