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

import javax.microedition.ims.common.Protocol;
import javax.microedition.ims.common.util.StringUtils;


public class Via extends UriHeader {
    private final Protocol protocol;
    private final String content;
    private final String stringValue;

    public Via(final Builder builder) {
        super(
                (UriHeaderBuilder) new UriHeaderBuilder()
                        .tag(builder.tag)
                        .uriBuilder(builder.uriBuilder)
                        .value(builder.value)
                        .paramsList(builder.paramsList)
        );

        this.protocol = builder.protocol;
        this.content = doBuildeContent();
        this.stringValue = doBuildStringValue();
    }

    public Protocol getProtocol() {
        return protocol;
    }

    
    public String toString() {
        return stringValue;
    }

    private String doBuildStringValue() {
        return "Via [protocol=" + protocol + ", \ns()=" + super.toString() + "]";
    }

    
    public String buildContent() {
        return content;
    }

    private String doBuildeContent() {
        StringBuilder retValue = new StringBuilder();
        retValue.append(StringUtils.SIP_VERSION).append("/").append(getProtocol()).append(StringUtils.SPACE).append(getUri().getDomain());
        if (getUri().getPort() > 0) {
            retValue.append(":").append(getUri().getPort());
        }
        retValue.append(getParamsList().getContent(";"));
        retValue.append(getUri().getHeaders().getContent(";"));
        return retValue.toString();
    }

    public static class Builder extends UriHeaderBuilder {
        private Protocol protocol;

        public Builder() {
        }

        public Builder(final Via via) {
            super(via);
            this.protocol = via.getProtocol();
        }

        public Builder protocol(final Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Via build() {
            return new Via(this);
        }

        public String toString() {
            return "Builder{" +
                    "protocol=" + protocol +
                    '}';
        }
    }

}
