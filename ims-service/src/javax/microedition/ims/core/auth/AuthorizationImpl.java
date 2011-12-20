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

package javax.microedition.ims.core.auth;

import android.util.Base64;
import javax.microedition.ims.common.*;
import javax.microedition.ims.common.Logger.Tag;
import javax.microedition.ims.common.util.SIPUtil;
import javax.microedition.ims.messages.wrappers.sip.AuthChallenge;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 11-Jan-2010
 * Time: 15:57:36
 */
public class AuthorizationImpl implements AuthorizationData {
    private final String userName;
    private final String password;
    private final String digestUri;
    private final AuthChallenge challenge;
    private final ChallengeType authType;
    private final byte qop;
    private final String ha1;

    /**
     * nonce-count
     * This MUST be specified if a qop directive is sent (see above), and
     * MUST NOT be specified if the server did not send a qop directive in
     * the WWW-Authenticate header field.  The nc-value is the hexadecimal
     * count of the number of requests (including the current request)
     * that the client has sent with the nonce value in this request.  For
     * example, in the first request sent in response to a given nonce
     * value, the client sends "nc=00000001".  The purpose of this
     * directive is to allow the server to detect request replays by
     * maintaining its own copy of this count - if the same nc-value is
     * seen twice, then the request is a replay.   See the description
     * below of the construction of the request-digest value.
     */
    private final SubsequentNumberGenerator generator;
    private final AKAAuthProvider akaAuthProvider;
/*
    private final String body = "v=0\r\n" +
            "o=bob 2890844526 2890844526 IN IP4 media.biloxi.com\r\n" +
            "s=\r\n" +
            "c=IN IP4 media.biloxi.com\r\n" +
            "t=0 0\r\n" +
            "m=audio 49170 RTP/AVP 0\r\n" +
            "a=rtpmap:0 PCMU/8000\r\n" +
            "m=video 51372 RTP/AVP 31\r\n" +
            "a=rtpmap:31 H261/90000\r\n" +
            "m=video 53000 RTP/AVP 32\r\n" +
            "a=rtpmap:32 MPV/90000";*/

    private volatile String lastNC;

    /**
     * cnonce
     * This MUST be specified if a qop directive is sent (see above), and
     * MUST NOT be specified if the server did not send a qop directive in
     * the WWW-Authenticate header field.  The cnonce-value is an opaque
     * quoted string value provided by the client and used by both client
     * and server to avoid chosen plaintext attacks, to provide mutual
     * authentication, and to provide some message integrity protection.
     * See the descriptions below of the calculation of the response-
     * digest and request-digest values.
     */
    private volatile String lastCNonce;

    public AuthorizationImpl(
            final AKAAuthProvider akaAuthProvider,
            final String userName,
            final String password,
            final String digestUri,
            final AuthChallenge challenge,
            final byte qop,
            final ChallengeType authType
    ) throws AuthorizationException{
        this.akaAuthProvider = akaAuthProvider;

        this.userName = userName;
        //this.userName = "bob";
        this.password = password;
        //this.password = "zanzibar";
        this.digestUri = digestUri;
        //this.digestUri = "sip:bob@biloxi.com";
        this.challenge = challenge;
        this.authType = authType;
        //this.nonce = "dcd98b7102dd2f0e8b11d0f600bfb0c093";

        this.qop = qop;
        //this.qop = 0;
        //this.ha1 = AuthUtil.calcHA1(this.userName, this.password, "biloxi.com");
        this.generator = new DefaultSubsequentNumberGenerator(1);

        String ha1 = null;
        if (challenge != null) {
            final Algorithm algorithm = challenge.getAlgorithm();

            if (Algorithm.AKAv1_MD5 == algorithm) {
                //ha1 = AuthUtil.calcHA1(userName, challenge.getRealm(), akaValue);
                String akaValue;
                try {
                    akaValue = calcAKAValue();
                } catch (AuthCalculationException e) {
                    Logger.log(Tag.COMMON, "AuthorizationImpl, exception = " + e.getMessage());
                    throw new AuthorizationException(e.getMessage(), e);
                }
                ha1 = AuthUtil.calcHA1AKA(akaAuthProvider.getImpi().toFullName(), challenge.getRealm(), akaValue);
            } else {
                ha1 = AuthUtil.calcHA1(userName, challenge.getRealm(), password);
            }
        }
        this.ha1 = ha1;
    }

    public AuthChallenge getChallenge() {
        return challenge;
    }


    public byte[] getBody() {
        return new byte[0];
        //return body;
    }

    @Override
    public String calculateAuthResponse(final String method, byte[] body) {

        setLastNC(AuthUtil.creatNounceCounterString(generator.next()));
        //setLastNC(AuthUtil.creatNounceCounterString(1));

        //setLastNC(String.format("%08x", 1));
        setLastCNonce(SIPUtil.randomClientNonce());
        //setLastCNonce("0a4f113b");

        //return AuthUtil.calcHA2("SIP_REGISTER", body, digestUri, challenge.getQop());
        //return AuthUtil.calcHA2("SIP_INVITE", body, digestUri, challenge.getQop());
        final String retValue;

        if (challenge != null) {
            retValue = AuthUtil.calculateAuthResponse(
                    ha1,
                    AuthUtil.calcQopHA2(method, body, digestUri, qop),
                    //calcHA2((this.body).getBytes()),
                    challenge.getNonce() == null ? challenge.getNextNonce() : challenge.getNonce(),
                    getLastNC(),
                    getLastCNonce(),
                    qop
            );
        } else {
            retValue = "";
        }

        return retValue;
    }

    private String calcAKAValue() throws AuthCalculationException{

        String retValue = null;

        final String nonce = challenge.getNonce() == null ? challenge.getNextNonce() : challenge.getNonce();
        //final String nonce = "aP+ocDIhSrQtOAR28vLz66nl2cVvjwAA9f+pdPS8f5g=";

        Logger.log(Logger.Tag.COMMON, "nonce = " + nonce);

        final byte[] decodedNonce = Base64.decode(nonce, Base64.DEFAULT);

        Logger.log(Logger.Tag.COMMON, "decodedNonceHex = " + encode(decodedNonce, true));

        assert decodedNonce.length == 32 : "invalid auth challange";

        final byte[] rand = new byte[16];
        final byte[] autn = new byte[16];

        System.arraycopy(decodedNonce, 0, rand, 0, 16);
        System.arraycopy(decodedNonce, 16, autn, 0, 16);

        Logger.log(Logger.Tag.COMMON, "randHex = " + encode(rand, true));
        Logger.log(Logger.Tag.COMMON, "autnHex = " + encode(autn, true));

        final AKAResponse response = akaAuthProvider.calculateAkaResponse(rand, autn);

        Logger.log(Logger.Tag.COMMON, "akaResponse = " + response);

        //retValue = response == null ? retValue : Base64.encodeToString(response.getRes(), Base64.DEFAULT);
        retValue = encode(response.getRes(), false);

        Logger.log(Logger.Tag.COMMON, " finalAuthResponse before cutting= " + retValue);

        retValue = retValue.toUpperCase();

        Logger.log(Logger.Tag.COMMON, " finalAuthResponse= " + retValue);
        Logger.log(Logger.Tag.COMMON, " modified by Laboda 4. RES.toUpperCase()");
        return retValue;
    }

    public static String encode(byte[] b, boolean needSpaces) {
        char[] Hexhars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            s.append(Hexhars[v >> 4]);
            s.append(Hexhars[v & 0xf]);
            if (needSpaces) {
                if (i % 2 == 1) {
                    s.append(" ");
                }
            }
        }
        return s.toString();
    }

    public String getLastNC() {
        return lastNC;
    }

    private void setLastNC(final String lastNC) {
        this.lastNC = lastNC;
    }

    public String getLastCNonce() {
        return lastCNonce;
    }

    private void setLastCNonce(final String lastCNonce) {
        this.lastCNonce = lastCNonce;
    }

    public byte getQop() {
        return qop;
    }

    public ChallengeType getChallengeType() {
        return authType;
    }

    @Override
    public String toString() {
        return "AuthorizationImpl{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", digestUri='" + digestUri + '\'' +
                ", challenge=" + challenge +
                ", generator=" + generator +
                '}';
    }

    public static void main(String[] args) {

        final String res = "";
        final String realm = "dummy.com";
        final String userName = "12345678@dummy.com";
        final String method = "REGISTER";
        final String digestURI = "sip:dummy.com";
        final String nonce = "=";

        final String ha1 = AuthUtil.calcHA1AKA(userName, realm, res);


        final String ha2 = DigestUtils.md5Hex(method + ":" + digestURI);
        final String response = DigestUtils.md5Hex(ha1 + ":" + nonce + ":" + ha2);

        System.out.println(response);
    }
}
