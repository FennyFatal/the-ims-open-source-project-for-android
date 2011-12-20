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
import javax.microedition.ims.common.util.StringUtils;
import javax.microedition.ims.messages.history.BodyPartData;
import javax.microedition.ims.messages.history.MessageData;
import javax.microedition.ims.messages.parser.body.BodyParser;
import javax.microedition.ims.messages.wrappers.body.BodyHeader;
import javax.microedition.ims.messages.wrappers.body.BodyPart;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BodyPartUtils {

    private static final String BOUNDARY_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXZY'()+_,-./:=? ";

    public static final byte[] getBodyPartsAsByteArray(MessageData userMessageData, String boundary) {
        StringBuilder content = new StringBuilder();
        content.append("--").append(boundary).append(StringUtils.SIP_TERMINATOR);
        for (BodyPartData bodyPart : userMessageData.getBodyParts()) {
            if (bodyPart.getHeadersKeys().size() == 0) {
                content.append(StringUtils.SIP_TERMINATOR);
            }
            else {
                for (String key : bodyPart.getHeadersKeys()) {
                    content.append(key).append(": ").append(bodyPart.getHeader(key)).append(StringUtils.SIP_TERMINATOR);
                }
            }
            content.append(StringUtils.SIP_TERMINATOR).append(new String(bodyPart.getContent())).append(StringUtils.SIP_TERMINATOR).append("--").append(boundary);
            if (bodyPart != userMessageData.getBodyParts()[userMessageData.getBodyParts().length - 1]) {
                content.append(StringUtils.SIP_TERMINATOR);
            }
        }
        content.append("--");
        return content.toString().getBytes();
    }

    public static final String generateBoundary() {
        String ret = new String();
        Random rs = new Random();
        int l = (rs.nextInt(BOUNDARY_CHARS.length()) % 70);
        for (int i = 0; i < l; i++) {
            ret += BOUNDARY_CHARS.charAt(rs.nextInt(BOUNDARY_CHARS.length()) % BOUNDARY_CHARS.length());
        }
        ret += BOUNDARY_CHARS.charAt(rs.nextInt(BOUNDARY_CHARS.length()) % BOUNDARY_CHARS.length() - 1); // space is not allowed to be the last character

        return ret;
    }

    public static List<BodyPartData> parseBody(byte[] body, String boundary) {
        List<BodyPartData> ret = new ArrayList<BodyPartData>();

        if (boundary == null || boundary.length() == 0) {
            Logger.log("Boundary cannot be empty");
            assert false;
            return ret;
        }

        if (body == null || body.length == 0) {
            Logger.log("Body cannot be empty");
            assert false;
            return ret;
        }

        String fullBoundary = "--" + boundary, preambule, epilogue;
        String bodyAsString = new String(body);

        int bLen = fullBoundary.length();
        int firstOfs = bodyAsString.indexOf(fullBoundary);
        if (firstOfs == -1) {
            Logger.log("Boundary not found");
            return ret;
        }
        else if (firstOfs > 0) {
            byte[] temp = new byte[firstOfs];
            System.arraycopy(body, 0, temp, 0, temp.length);
            preambule = new String(temp);
        }

        firstOfs += bLen + 2;

        int nextOfs;
        while ((nextOfs = bodyAsString.indexOf(fullBoundary, firstOfs)) >= 0) {
            byte[] temp = new byte[nextOfs - firstOfs];
            System.arraycopy(body, firstOfs, temp, 0, temp.length);
            System.out.println("Body-content:" + new String(temp));
            BodyPart bodyPart = BodyParser.parse(temp);
            if (bodyPart == null) {

                Logger.log("Parsing body part failed");
                return ret;
            }
            ret.add(convertBodyPart(bodyPart));
            firstOfs = nextOfs + bLen + 2;
        }
        //firstOfs -= 2;
        /*if (TP::Bytes::Use( b.Ptr() + firstofs, 2 ) != "--") {
              DInfo << "Unterminated message: " << b.Ptr() + firstofs;
              return false;
          }*/

        if (firstOfs + bLen + 2 < body.length) {
            byte[] temp = new byte[firstOfs];
            System.arraycopy(body, firstOfs + bLen + 2, temp, 0, temp.length);
            epilogue = new String(temp);
        }
        return ret;
    }

    private static BodyPartData convertBodyPart(BodyPart bodyPart) {
        assert bodyPart != null : "Cannot convert empty body part";
        if (bodyPart == null) {
            return null;
        }
        BodyPartData ret = new BodyPartData();
        ret.setContent(bodyPart.getContent());
        for (BodyHeader header : bodyPart.getHeaders()) {
            ret.addHeader(header.getName(), header.getValueWithParams());
        }
        return ret;
    }

}
