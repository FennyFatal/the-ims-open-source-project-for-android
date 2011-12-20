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

import javax.microedition.ims.common.Algorithm;
import javax.microedition.ims.common.ChallengeType;


public final class AuthenticationChallenge implements AuthChallenge {
    public static final byte QOP_AUTH = 1;
    public static final byte QOP_AUTH_INT = 2;
    public static final AuthenticationChallenge EMPTY_CHALLENGE = getEmptyChallenge();

    private final AuthType type;
    private final String realm;
    private final String nonce;
    private final String opaque;
    private final byte qop;
    private final Algorithm algorithm;
    private final boolean stale;
    private final String nonceCount;
    private final String nextNonce;
    private final String cNonce;
    private final String response;
    private final String uri;
    private final String username;
    private final ChallengeType challengeType;
    private final String content;
    private static final String DIGEST_PREFIX = "Digest";


    public AuthenticationChallenge(final Builder builder) {
        type = builder.type;
        if (type == null) {
            final String errMsg = "Type can't be null. Now it has value " + type + ". Value comes from " + builder;
            throw new IllegalArgumentException(errMsg);
        }

        realm = builder.realm;
        /*
        if (realm == null) {
            final String errMsg = "Realm can't be null. Now it has value " + realm + ". Value comes from " + builder;
            throw new IllegalArgumentException(errMsg);
        }
        */

        nonce = builder.nonce;
        /*if (nonce == null) {
            final String errMsg = "Nonce can't be null. Now it has value " + nonce + ". Value comes from " + builder;
            throw new IllegalArgumentException(errMsg);
        }*/

        opaque = builder.opaque;
        /*if (opaque == null) {
            final String errMsg = "Opaque can't be null. Now it has value " + opaque + ". Value comes from " + builder;
            throw new IllegalArgumentException(errMsg);
        }*/

        this.qop = builder.qop;

        algorithm = builder.algorithm;
        /*
        if (algorithm == null) {
            final String errMsg = "Algorithm can't be null. Now it has value " + algorithm + ". Value comes from " + builder;
            throw new IllegalArgumentException(errMsg);
        }
        */

        this.stale = builder.stale;

        nonceCount = builder.nonceCount;
        /*
        if (nonceCount == null) {
            final String errMsg = "NonceCount can't be null. Now it has value " + nonceCount + ". Value comes from " + builder;
            throw new IllegalArgumentException(errMsg);
        }
        */

        nextNonce = builder.nextNonce;
        /*if (nextNonce == null) {
            final String errMsg = "NextNonce can't be null. Now it has value " + nextNonce + ". Value comes from " + builder;
            throw new IllegalArgumentException(errMsg);
        }*/

        cNonce = builder.cNonce;
        /*
        if (cNonce == null) {
            final String errMsg = "cNonce can't be null. Now it has value " + cNonce + ". Value comes from " + builder;
            throw new IllegalArgumentException(errMsg);
        }
        */

        response = builder.response;
        /*
        if (response == null) {
            final String errMsg = "Response can't be null. Now it has value " + response + ". Value comes from " + builder;
            throw new IllegalArgumentException(errMsg);
        }
        */

        uri = builder.uri;
        /*
        if (uri == null) {
            final String errMsg = "Uri can't be null. Now it has value " + uri + ". Value comes from " + builder;
            throw new IllegalArgumentException(errMsg);
        }
        */

        username = builder.username;
        /*
        if (username == null) {
            final String errMsg = "Username can't be null. Now it has value " + username + ". Value comes from " + builder;
            throw new IllegalArgumentException(errMsg);
        }
        */

        challengeType = builder.challengeType;
        if (challengeType == null) {
            final String errMsg = "ChallengeType can't be null. Now it has value " + challengeType + ". Value comes from " + builder;
            throw new IllegalArgumentException(errMsg);
        }
        content = doBuildContent();
    }

    public AuthChallenge copyOf(final ChallengeType challengeType) {
        return doGetBuilder().challengeType(challengeType).build();
    }

    public Builder asBuilder() {
        return doGetBuilder();
    }

    private Builder doGetBuilder() {
        return new Builder().
                type(type).
                realm(realm).
                nonce(nonce).
                opaque(opaque).
                qop(qop).
                algorithm(algorithm).
                stale(stale).
                nonceCount(nonceCount).
                nextNonce(nextNonce).
                cNonce(cNonce).
                response(response).
                uri(uri).
                username(username).
                challengeType(challengeType);
    }

    
    public String getNextNonce() {
        return nextNonce;
    }

    public String getcNonce() {
        return cNonce;
    }


    
    public AuthType getType() {
        return type;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }


    
    public boolean isStale() {
        return stale;
    }

    public String getNonceCount() {
        return nonceCount;
    }

    public String getRealm() {
        return realm;
    }

    public String getNonce() {
        return nonce;
    }

    public String getOpaque() {
        return opaque;
    }

    public byte getQop() {
        return qop;
    }

    public ChallengeType getChallengeType() {
        return challengeType;
    }

    public String toString() {
        return content;
    }

    
    public String buildContent() {
        return content;
    }

    private String doBuildContent() {
        StringBuilder sb = new StringBuilder();
        //"DIGEST realm=\"atlanta.com\",domain=\"sip:boxesbybob.com\", qop=\"auth\",nonce=\"f84f1cec41e6cbe5aea9c8e88d359\",opaque=\"\", stale=FALSE, algorithm=MD5";
        //sb.append(type.stringValue());
        sb.append(DIGEST_PREFIX);

        if (username != null) {
            sb.append(" username=\"").append(username).append("\",");
        }

        if (realm != null) {
            sb.append(" realm=\"").append(realm).append("\",");
        }

        final String nonceValue = nonce != null ? nonce : nextNonce;
        sb.append(" nonce=\"").append(nonceValue == null ? "" : nonceValue).append("\"");

        if (uri != null) {
            sb.append(", uri=\"").append(uri).append("\"");
        }
        
        if (qop == 1) {
            sb.append(", qop=").append("auth");
        }
        else if (qop == 2) {
            sb.append(", qop=").append("auth-int");
        }
        else if (qop == 3) {
            sb.append(", qop=").append("auth-int");
        }

        if (qop != 0) {
            if (nonceCount != null) {
                sb.append(", nc=").append(nonceCount);
            }
            if (cNonce != null) {
                sb.append(", cnonce=\"").append(cNonce).append("\"");
            }
        }

        if (response != null) {
            sb.append(", response=\"").append(response).append("\"");
        }

        if (opaque != null) {
            sb.append(", opaque=\"").append(opaque).append("\"");
        }
        if ((qop != 0 || algorithm == Algorithm.AKAv1_MD5) && algorithm != null) {
            sb.append(", algorithm=").append(algorithm.stringValue());
        }

        return sb.toString();
    }

    private static AuthenticationChallenge getEmptyChallenge() {
        return (AuthenticationChallenge) new Builder().
                type(AuthType.DIGEST).
                realm(null).
                nonce(null).
                opaque(null).
                qop((byte) 0).
                algorithm(null).
                stale(false).
                nonceCount(null).
                nextNonce(null).
                cNonce(null).
                response(null).
                uri(null).
                username(null).
                challengeType(ChallengeType.UAS).
                build();
    }

    /*public void setChallengeType(final ChallengeType challengeType) {
        this.challengeType = challengeType;
    }*/

    public static class Builder {
        private AuthType type;
        private String realm;
        private String nonce;
        private String opaque;
        private byte qop;
        private Algorithm algorithm;
        private boolean stale;
        private String nonceCount;
        private String nextNonce;
        private String cNonce;
        private String response;
        private String uri;
        private String username;
        private ChallengeType challengeType;

        public Builder() {
        }

        public Builder type(AuthType type) {
            this.type = type;
            return this;
        }

        public Builder realm(String realm) {
            this.realm = realm;
            return this;
        }

        public Builder nonce(String nonce) {
            this.nonce = nonce;
            return this;
        }

        public Builder opaque(String opaque) {
            this.opaque = opaque;
            return this;
        }

        public Builder qop(byte qop) {
            this.qop = qop;
            return this;
        }

        public Builder algorithm(Algorithm algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public Builder stale(boolean stale) {
            this.stale = stale;
            return this;
        }

        public Builder nonceCount(String nonceCount) {
            this.nonceCount = nonceCount;
            return this;
        }

        public Builder nextNonce(String nextNonce) {
            this.nextNonce = nextNonce;
            return this;
        }

        public Builder cNonce(String cNonce) {
            this.cNonce = cNonce;
            return this;
        }

        public Builder response(String response) {
            this.response = response;
            return this;
        }

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder challengeType(ChallengeType challengeType) {
            this.challengeType = challengeType;
            return this;
        }

        public AuthChallenge build() {
            return new AuthenticationChallenge(this);
        }

        public String toString() {
            return "Builder{" +
                    "type=" + type +
                    ", realm='" + realm + '\'' +
                    ", nonce='" + nonce + '\'' +
                    ", opaque='" + opaque + '\'' +
                    ", qop=" + qop +
                    ", algorithm=" + algorithm +
                    ", stale=" + stale +
                    ", nonceCount='" + nonceCount + '\'' +
                    ", nextNonce='" + nextNonce + '\'' +
                    ", cNonce='" + cNonce + '\'' +
                    ", response='" + response + '\'' +
                    ", uri='" + uri + '\'' +
                    ", username='" + username + '\'' +
                    ", challengeType=" + challengeType +
                    '}';
        }
    }
}
