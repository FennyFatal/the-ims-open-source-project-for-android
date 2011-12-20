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

package javax.microedition.ims.core.sipservice.subscribe;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 1.6.2010
 * Time: 17.49.53
 */
public final class RemoteStateDefault implements RemoteState {

    public static final RemoteState MOCK_TERMINATED =
            new RemoteStateBuilder().value(RemoteStateValue.TERMINATED).build();

    public static final RemoteState MOCK_ACTIVE =
            new RemoteStateBuilder().value(RemoteStateValue.ACTIVE).build();

    private final RemoteStateValue value;
    private final Integer expires;
    private final Integer retryAfter;
    private final RemoteStateReason reason;

    private RemoteStateDefault(final RemoteStateBuilder builder) {

        if (builder.value == null) {
            throw new IllegalArgumentException("'Value' cannot be null. Now it has value " + builder.value);
        }

        this.value = builder.value;
        this.expires = builder.expires;
        this.retryAfter = builder.retryAfter;
        this.reason = builder.reason;
    }

    
    public RemoteStateValue getValue() {
        return value;
    }

    
    public Integer getExpires() {
        return expires;
    }

    
    public Integer getRetryAfter() {
        return retryAfter;
    }

    
    public RemoteStateReason getReason() {
        return reason;
    }

    
    public String stringValue() {

        StringBuilder retValue = new StringBuilder().append(value.stringValue());

        if (expires != null) {
            retValue.append(";").append(RemoteStateParam.EXPIRES.stringValue()).append("=").append(expires);
        }
        if (retryAfter != null) {
            retValue.append(";").append(RemoteStateParam.RETRY_AFTER.stringValue()).append("=").append(retryAfter);
        }
        if (reason != null) {
            retValue.append(";").append(RemoteStateParam.REASON.stringValue()).append("=").append(reason);
        }

        return retValue.toString();
    }

    
    public String toString() {
        return "RemoteStateDefault{" +
                "value=" + value +
                ", expires=" + expires +
                ", retryAfter=" + retryAfter +
                ", reason=" + reason +
                '}';
    }

    public static class RemoteStateBuilder {
        private RemoteStateValue value;
        private Integer expires;
        private Integer retryAfter;
        private RemoteStateReason reason;

        public RemoteStateBuilder value(final RemoteStateValue value) {
            this.value = value;
            return this;
        }

        public RemoteStateBuilder value(final String value) {
            return value(RemoteStateValue.parse(value));
        }

        public RemoteStateBuilder expires(final Integer expires) {
            this.expires = expires;
            return this;
        }

        public RemoteStateBuilder expires(final String expires) {
            return expires(Integer.parseInt(expires));
        }

        public RemoteStateBuilder retryAfter(final Integer retryAfter) {
            this.retryAfter = retryAfter;
            return this;
        }

        public RemoteStateBuilder retryAfter(final String retryAfter) {
            return this.retryAfter(Integer.parseInt(retryAfter));
        }

        public RemoteStateBuilder reason(final RemoteStateReason reason) {
            this.reason = reason;
            return this;
        }

        public RemoteStateBuilder reason(final String reason) {
            return this.reason(RemoteStateReason.parse(reason));
        }

        public RemoteStateBuilder param(final RemoteStateParam param, final String value) {

            switch (param) {
                case EXPIRES: {
                    expires(value);
                }
                break;

                case REASON: {
                    reason(value);
                }
                break;

                case RETRY_AFTER: {
                    retryAfter(value);
                }
                break;

                default: {
                    assert false : "Unreachable branch";
                }
                break;
            }

            return this;
        }

        public RemoteState build() {
            return new RemoteStateDefault(this);
        }
    }
}
