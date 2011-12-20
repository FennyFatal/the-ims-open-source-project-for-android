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
import javax.microedition.ims.messages.wrappers.common.ResponseClass;

public final class Response extends BaseSipMessage {
    private final ResponseClass responseClass;

    private final String reasonPhrase;
    private final int statusCode;

    private final String stringValue;
    private final byte[] byteContent;
    private final String content;

    private final String responseLine;
    private final String shortDescription;
    private final int preComputedHash;

    public Response(final Builder builder) {
        super(builder);

        this.statusCode = builder.statusCode;
        this.reasonPhrase = builder.reasonPhrase;
        this.responseClass = ResponseClass.createByCode(statusCode);
        this.responseLine = doBuildResponseLine();
        this.preComputedHash = doComputeHash();

        this.stringValue = doBuildStringValue();
        this.byteContent = doBuildByteContent();
        this.content = new String(byteContent);
        this.shortDescription = doBuildShortDescription();
    }

    public ResponseClass getResponseClass() {
        return responseClass;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }
    
    public Builder getBuilder() {
        final Builder builder = super.getBuilder();
        builder.statusCode = this.statusCode;
        builder.reasonPhrase = this.reasonPhrase;
        builder.msgType(Builder.Type.RESPONSE);

        return builder;
    }

    
    public String toString() {
        return stringValue;
    }

    private String doBuildStringValue() {
        return "ResponseMessage [statusCode=" + statusCode
                + ", reasonPhrase=" + reasonPhrase + ", server=" + getServer() + ", \ns()="
                + super.toString() + "]";
    }

    public byte[] buildByteContent() {
        return byteContent;
    }

    
    public String buildContent() {
        return content;
    }

    private byte[] doBuildByteContent() {
        StringBuilder sb = new StringBuilder();
        sb.append(getResponseLine()).append(StringUtils.SPACE).append(StringUtils.SIP_TERMINATOR);
        sb.append(new String(super.buildByteContent())).append(StringUtils.SIP_TERMINATOR);

        byte[] headers = sb.toString().getBytes();
        int headersLength = headers.length;
        byte[] retValue = null;
        if (getBody() != null && getBody().length > 0) {
            retValue = new byte[headersLength + getBody().length];
            System.arraycopy(headers, 0, retValue, 0, headersLength);
            System.arraycopy(getBody(), 0, retValue, headersLength, getBody().length);
        }
        else {
            retValue = headers;
        }

        return retValue;
    }

    public String getResponseLine() {
        return responseLine;
    }

    private String doBuildResponseLine() {
        return new StringBuilder().append(StringUtils.SIP_VERSION).append(StringUtils.SPACE).append(statusCode).
                append(StringUtils.SPACE).append(reasonPhrase).toString();
    }

    public String shortDescription() {
        return shortDescription;
    }

    private String doBuildShortDescription() {
        return "resp " + getStatusCode() + " " + getReasonPhrase() + " " + getMethod() + ":" + getCallId();
    }


    public boolean isEqualTo(final Response msg) {
        boolean retValue = false;

        if (msg != null) {
            boolean sameResponseCode = getStatusCode() == msg.getStatusCode();
            boolean sameReasonPhrase = getReasonPhrase().equals(msg.getReasonPhrase());
            retValue = super.isEqualTo(msg) && sameReasonPhrase && sameResponseCode;
        }

        return retValue;
    }

    public int calcHash() {
        return preComputedHash;
    }

    private int doComputeHash() {
        int result = super.calcHash();
        result = 31 * result + statusCode;
        result = 31 * result + (reasonPhrase != null ? reasonPhrase.hashCode() : 0);
        return result;
    }
}
