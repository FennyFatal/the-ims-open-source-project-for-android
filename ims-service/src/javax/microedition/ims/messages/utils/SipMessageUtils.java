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

import javax.microedition.ims.common.MimeType;
import javax.microedition.ims.messages.history.BodyPartData;
import javax.microedition.ims.messages.parser.sdp.SdpParser;
import javax.microedition.ims.messages.wrappers.sdp.SdpMessage;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Header;
import javax.microedition.ims.messages.wrappers.sip.ParamHeader;
import java.util.List;


public class SipMessageUtils {
    public static boolean checkIsMultipart(ParamHeader contentType) {
        return !(contentType == null || contentType.getValue() == null) &&
                contentType.getValue().toLowerCase().startsWith(MimeType.MULTIPART.stringValue());
    }

    public static SdpMessage getSdpFromMessage(BaseSipMessage msg) {
        SdpMessage sdp = null;
        if (MimeType.APP_SDP.stringValue().equals(msg.getContentType().getValue())) {
            sdp = SdpParser.parse(new String(msg.getBody()));
        }
        else if (SipMessageUtils.checkIsMultipart(msg.getContentType())) {
            String boundary = msg.getContentType().getParamsList().get("boundary").getValue();
            List<BodyPartData> parts = BodyPartUtils.parseBody(msg.getBody(), boundary);
            for (BodyPartData part : parts) {
                if (MimeType.APP_SDP.stringValue().equals(part.getHeader(Header.Content_Type.stringValue()))) {
                    sdp = SdpParser.parse(new String(part.getContent()));
                }
            }
        }
        return sdp;
    }

/*	public static void main(String[] args) {
		String input = "SIP_NOTIFY sip:tch2_user_9320214@172.17.50.24:5061 SIP/2.0\r\n" +
		"Max-Forwards: 67\r\n" +
		"Via: SIP/2.0/TCP 10.80.63.172:5060;branch=z9hG4bK51tgprgfgnglbij8md8bh7w11\r\n" +
		"To: <tel:+4689320214>;tag=f26860e07d9389eeaaa1cb56dfaff864\r\n" +
		"From: <sip:tch2_user_9320214@tch2.imt.se;pres-list=rcs>;tag=h7g4Esbg_g77gdbd5-3ufa\r\n" +
		"Call-ID: 5421918aae3fc298db4deba70c5b18b5\r\n" +
		"CSeq: 2 SIP_NOTIFY\r\n" +
		"Contact: <sip:sgc_c@10.80.63.172;transport=udp>\r\n" +
		"Event: presence\r\n" +
		"Require: eventlist\r\n" +
		"Subscription-StateHolder: active;EXPIRES=3599\r\n" +
		"Content-Type:multipart/mime;type=\"application/rlmi+xml\";start=\"<sip:tch2_user_9320214@tch2.imt.se;pres-list=rcs>\";boundary=\"----=_Part_9038_28964864.1269514860737\"\r\n" +
		"Content-Length: 2155\r\n" +
		"\r\n" +
		"------=_Part_9038_28964864.1269514860737\r\n" +
		"Content-Type: application/sdp\r\n" +
		"Content-Length: 541\r\n" +
		"\r\n" +
		"v=0\r\n" +
		"o=- 1267788242227 1267788242227 IN IP4 10.0.2.15\r\n" +
		"s=-\r\n" +
		"t=0 0\r\n" +
		"m=application 0 TCP 0 8 97\n" +
		"i=media 1\r\n" +
		"c=IN IP4 127.0.0.1\r\n" +
		"a=rtpmap:0 PCMU/8000\r\n" +
		"a=rtpmap:8 PCMA/8000\r\n" +
		"a=rtpmap:97 iLBC/8000\r\n" +
		"m=application 33333 TCP 31 32\r\n" +
		"i=media 2\r\n" +
		"c=IN IP4 127.0.0.1\r\n" +
		"a=rtpmap:31 H261/90000\r\n" +
		"a=rtpmap:32 MPV/90000\r\n" +
		"m=application 33333 TCP 41 41\r\n" +
		"i=media 3\r\n" +
		"c=IN IP4 127.0.0.1\r\n" +
		"a=rtpmap:41 H264/60000\r\n" +
		"a=rtpmap:42 MPV/60000\r\n" +
		"m=application 33333 TCP 0 8 97\r\n" +
		"i=media 1\r\n" +
		"c=IN IP4 127.0.0.1\r\n" +
		"a=rtpmap:0 PCMU/8000\r\n" +
		"a=rtpmap:8 PCMA/8000\r\n" +
		"a=rtpmap:97 iLBC/8000\r\n" +
		"------=_Part_9038_28964864.1269514860737\r\n" +
		"Content-Type: application/pidf+xml;charset=\"UTF-8\"\r\n" +
		"Content-Transfer-Encoding: binary\r\n" +
		"Content-ID: <tel:+4689320214>\r\n" +
		"\r\n" +
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?><presence xmlns=\"urn:ietf:params:xml:ns:pidf\" entity=\"tel:+4689320214\"/>\r\n" +
		"------=_Part_9038_28964864.1269514860737\r\n" +
		"Content-Type: application/pidf+xml;charset=\"UTF-8\"\r\n" +
		"Content-Transfer-Encoding: binary\r\n" +
		"Content-ID: <sip:tch2_user_9320215@tch2.imt.se>\r\n" +
		"\r\n" +
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?><presence xmlns=\"urn:ietf:params:xml:ns:pidf\" entity=\"sip:tch2_user_9320215@tch2.imt.se\"/>\r\n" +
		"------=_Part_9038_28964864.1269514860737\r\n" +
		"Content-Type: application/pidf+xml;charset=\"UTF-8\"\r\n" +
		"Content-Transfer-Encoding: binary\r\n" +
		"Content-ID: <tel:+4689320201>\r\n" +
		"\r\n" +
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?><presence xmlns=\"urn:ietf:params:xml:ns:pidf\" entity=\"tel:+4689320201\"/>\r\n" +
		"\r\n" +
		"------=_Part_9038_28964864.1269514860737--";

		SdpMessage sdp = getSdpFromMessage(MessageParser.parse(input));
		System.out.println("result sdp"+ (sdp != null ? sdp.getContent() : " null"));
	}*/
}
