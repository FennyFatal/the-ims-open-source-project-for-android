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

package javax.microedition.ims.core.xdm;

import org.w3c.dom.Document;

import javax.microedition.ims.common.IMSEntityType;
import javax.microedition.ims.common.IMSID;
import javax.microedition.ims.common.IMSStringID;
import javax.microedition.ims.common.util.StringUtils;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com) Date: 19.4.2010 Time: 17.32.43
 */
public class XDMResponseImpl implements XDMResponse {
    private final Document doc;
    private final byte[] content;
    private final String etag;
    private final String mimeType;
    private String stringContent;

    private final IMSID imsid = new IMSStringID(new Object().toString());
    private static final String DEFAULT_CHARSET = "UTF-8";

    public XDMResponseImpl(final Document doc, final byte[] content,
                           final String etag, final String mimeType) {

        //for response on DELETE or PUT operations document is not defined
        if ((doc == null && content != null)
                || (doc != null && content == null)) {
            throw new IllegalArgumentException(
                    "Doc and content must be either both null or both not null. Now values equal: doc = "
                            + doc + ", content = " + content);
        }

        this.doc = doc;
        this.content = content;
        this.etag = etag;
        this.mimeType = mimeType;

        this.stringContent = content != null ? buildContent(content) : null;
    }

    private String buildContent(final byte[] content) {
        final String strinContent;

        String xmlEncoding = StringUtils.grabEncodingFromXMLBytes(content);
        if (xmlEncoding == null) {
            xmlEncoding = DEFAULT_CHARSET;
        }

        try {
            strinContent = new String(content, xmlEncoding);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return strinContent;
    }

    public Document getDoc() {
        return doc;
    }

    public String buildContent() {
        return stringContent;
    }

    public byte[] buildByteContent() {
        return content;
    }

    public String getEtag() {
        return etag;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public String shortDescription() {
        return mimeType + etag;
    }
    
    public IMSID getIMSEntityId() {
        return imsid;
    }

    public IMSEntityType getEntityType() {
        return IMSEntityType.XDM;
    }

    public String toString() {
        return "XDMResponseImpl{" + "content='" + Arrays.toString(content)
                + '\'' + ", etag='" + etag + '\'' + ", mimeType='" + mimeType
                + '\'' + '}';
    }

    @Override
    public void expire() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public boolean isExpired() {
        return false;
    }
}
