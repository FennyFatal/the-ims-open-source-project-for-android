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

package javax.microedition.ims.messages.utils;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.Protocol;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 19.8.2010
 * Time: 12.46.04
 * To change this template use File | Settings | File Templates.
 */
public final class MessageData {
    private final String contactDomain;
    private final Integer contactPort;
    private final Long contactExpires;
    private final Protocol contactTransport;
    private final Long expires;

    private MessageData(final Builder builder) {
        this.contactDomain = builder.contactDomain;
        this.contactPort = builder.contactPort;
        this.contactExpires = builder.contactExpires;
        this.contactTransport = builder.contactTransport;
        this.expires = builder.expires;
    }

    public String getContactDomain() {
        return contactDomain;
    }

    public Integer getContactPort() {
        return contactPort;
    }

    public Long getContactExpires() {
        return contactExpires;
    }

    public Protocol getContactTransport() {
        return contactTransport;
    }

    public Long getExpires() {
        return expires;
    }

    public static class Builder {
        private String contactDomain;
        private Integer contactPort;
        private Long contactExpires;
        private Protocol contactTransport;
        private Long expires;

        public Builder() {
        }

        public MessageData build() {
            return new MessageData(this);
        }

        public Builder contactDomain(final String contactDomain) {
            this.contactDomain = contactDomain;
            return this;
        }

        public Builder contactPort(final int port) {
            this.contactPort = port >= 0 ? port : null;
            return this;
        }

        public Builder contactExpires(final String expires) {
            try {
                this.contactExpires = Long.parseLong(expires);
            }
            catch (NumberFormatException e) {
                Logger.log(Logger.Tag.WARNING, "Unparsable contact expires number " + expires);
            }
            return this;
        }

        public Builder contactTransport(final Protocol contactTransport) {
            this.contactTransport = contactTransport;
            return this;
        }

        public Builder expires(final long expires) {
            this.expires = expires >= 0 ? expires : null;
            return this;
        }
    }
}
