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

package javax.microedition.ims.messages.wrappers.sip;

import javax.microedition.ims.common.util.StringUtils;
import javax.microedition.ims.messages.wrappers.common.Uri;


public final class Request extends BaseSipMessage {

    private final Uri requestUri;
    private final String shortDescription;
    private final byte[] byteContent;
    private final int preComputedHash;
    private final String content;

    public Request(final Builder builder) {
        super(builder);

        this.requestUri = builder.requestUri;
        this.preComputedHash = doComputeHash();

        this.byteContent = doBuildByteContent();
        this.content = new String(byteContent);
        this.shortDescription = doBuildShortDescription();
    }


    public Uri getRequestUri() {
        return requestUri;
    }

    public String shortDescription() {
        return shortDescription;
    }

    private String doBuildShortDescription() {
        return "req " + getMethod() + ":" + getCallId();
    }

    
    public Builder getBuilder() {
        final Builder builder = super.getBuilder();
        builder.requestUri = this.requestUri;
        builder.msgType(Builder.Type.REQUEST);

        return builder;
    }

    public byte[] buildByteContent() {
        return byteContent;
    }

    
    public String buildContent() {
        return content;
    }

    private byte[] doBuildByteContent() {
        assert requestUri != null : "Request URI shouldn't be null";

        StringBuilder sb = new StringBuilder();
        sb.append(getMethod()).append(StringUtils.SPACE).
                append(requestUri.getShortURI()).append(StringUtils.SPACE).
                append(StringUtils.SIP_VERSION).append(StringUtils.SIP_TERMINATOR);

        sb.append(new String(super.buildByteContent())).append(StringUtils.SIP_TERMINATOR); //CRLF before BODY;
        byte[] headers = sb.toString().getBytes();
        int headersLength = headers.length, bodyLength = 0;
        byte[] retValue = null;
        if (getBody() != null && getBody().length > 0) {
            bodyLength = getBody().length;
            retValue = new byte[headersLength + bodyLength];
            System.arraycopy(headers, 0, retValue, 0, headersLength);
            System.arraycopy(getBody(), 0, retValue, headersLength, bodyLength);
        }
        else {
            retValue = headers;
        }

        return retValue;
    }

    public boolean isEqualTo(final Request msg) {
        boolean retValue = false;

        if (msg != null) {
            boolean sameRequestURI = getRequestUri().getShortURI().equals(msg.getRequestUri().getShortURI());
            retValue = super.isEqualTo(msg) && sameRequestURI;
        }

        return retValue;
    }

    
    public int calcHash() {
        return preComputedHash;
    }

    private int doComputeHash() {
        int result = super.calcHash();

        String shortURI = getRequestUri().getShortURI();
        result = 31 * result + (shortURI != null ? shortURI.hashCode() : 0);

        return result;
    }
}
