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

import javax.microedition.ims.common.util.StringUtils;
import javax.microedition.ims.messages.wrappers.sip.AuthenticationChallenge;
import java.math.BigInteger;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 11-Jan-2010
 * Time: 11:24:24
 */

//Examples: http://potaroo.net/ietf/all-ids/draft-smith-sip-auth-examples-00.txt
public final class AuthUtil {
    private AuthUtil() {
    }

    public static String calcHA1(final String userName, final String realm, final String password) {
        return DigestUtils.md5Hex(userName + ":" + realm + ":" + password);
    }

    public static String calcHA1AKA(final String userName, final String realm, final String passwordHEX) {

        final String paramFirstPart = userName + ":" + realm + ":";
        final String paramFirstPartHEX = String.format("%x", new BigInteger(paramFirstPart.getBytes()));
        final String ha1Param = paramFirstPartHEX + passwordHEX;

        return DigestUtils.md5Hex(StringUtils.hexStringToByteArray(ha1Param));
    }

    public static String calcHA1MD5Sess(final String userName, final String realm, final String password, final String nonce, final String cnonce) {
        return DigestUtils.md5Hex(calcHA1(userName, realm, password) + ":" + nonce + ":" + cnonce);
    }

    //see examples at http://potaroo.net/ietf/all-ids/draft-smith-sip-auth-examples-00.txt

    public static String calculateAuthResponse(final String ha1, final String ha2, final String nonce, final String nonceCount, final String clientNonce, final byte qop) {
        final String response;
        if (qop == 0) {
            response = DigestUtils.md5Hex(ha1 + ":" + nonce + ":" + ha2);
        }
        else {
            String qopString = ((qop & AuthenticationChallenge.QOP_AUTH_INT) != 0) ? "auth-int" : "auth";
            response = DigestUtils.md5Hex(ha1 + ":" + nonce + ":" + nonceCount + ":" + clientNonce + ":" + qopString + ":" + ha2);
        }
        return response;
    }

    public static String calcQopHA2(final String method, byte[] entityBody, final String digestUri, byte qop) {
        String ha2 = null;

        if (qop == AuthenticationChallenge.QOP_AUTH_INT) {
            //If the qop directive's value is "auth-int" , then HA2 is

            //String haBody = DigestUtils.md5Hex(new byte[]{0xA, 0xD});
            ha2 = calcAuthIntHA2(method, entityBody, digestUri);
        }
        else {
            //If the qop directive's value is "auth" or is unspecified, then HA2 is
            ha2 = calcAuthEmptyHA2(method, digestUri);
        }
        return ha2;
    }


    //If the qop directive's value is "auth-int" , then HA2 is

    public static String calcAuthIntHA2(final String method, byte[] entityBody, final String digestUri) {
        String retValue;

        String haBody;
        if (entityBody == null || entityBody.length == 0) {
            haBody = DigestUtils.md5Hex("");
        }
        else {
            haBody = DigestUtils.md5Hex(entityBody);
        }

        //String haBody = DigestUtils.md5Hex(new byte[]{0xA, 0xD});
        retValue = DigestUtils.md5Hex(method + ":" + digestUri + ":" + haBody);

        return retValue;
    }

    public static String creatNounceCounterString(int number) {
        return String.format("%08x", number);
    }

    public static String calcAuthEmptyHA2(final String method, final String digestUri) {
        //If the qop directive's value is "auth" or is unspecified, then HA2 is
        return DigestUtils.md5Hex(method + ":" + digestUri);
    }

    /*public static AuthChallenge buildChallenge(String str) {

        AuthChallenge retValue;

        if (str != null) {
            String realm = getValue("realm", str);
            String nonce = getValue("nonce", str);
            String opaque = getValue("opaque", str);
            String stale = getValue("stale", str);
            String algorithm = getValue("algorithm", str);

            retValue = new AuthenticationChallenge(AuthType.DIGEST, realm, nonce, opaque, Boolean.valueOf(stale), Algorithm.MD5);
        }
        else {
            retValue = buildChallenge();
        }

        return retValue;
    }

    public static AuthChallenge buildChallenge() {

        return new AuthenticationChallenge(AuthType.DIGEST, realm, nonce, opaque, Boolean.valueOf(staleFlag), Algorithm.MD5);
    }

    private static String getValue(String param, String source) {

        String retValue = null;

        String patternString = param + "=\"?(.*?)[\",$]";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(source);
        if (matcher.find()) {
            retValue = matcher.group(1);
        }

        return retValue;
    }*/
}
