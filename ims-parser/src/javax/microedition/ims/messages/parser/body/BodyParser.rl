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

package javax.microedition.ims.messages.parser.body;

import javax.microedition.ims.messages.parser.ParserUtils;
import javax.microedition.ims.messages.wrappers.body.BodyHeader;
import javax.microedition.ims.messages.wrappers.body.BodyPart;
import javax.microedition.ims.messages.wrappers.common.Param;


public class BodyParser{

%%{
	machine body;
	alphtype char;

	action mark {
		m_Mark = p;
	}

	action do_finish {
		fbreak;
	}

	action add_header {
		curHeader = new BodyHeader();
		curHeader.setName( arrayToString(m_Mark, p) );
	}

	action set_header_value {
		curHeader.setValue(arrayToString(m_Mark, p));
	}

	action add_param {
		curParam = new Param(arrayToString(m_Mark, p));
	}

	action set_param_value {
		curParam.setValue(arrayToString(m_Mark, p));
	}

	action set_param_quoted_value {
		curParam.setValue(ParserUtils.decodeDisplayName(arrayToString(m_Mark+1, p-2)));
	}

	action do_add_param {
		curHeader.getParams().set( curParam );
	}

	action do_add_header {
		bodyPart.addHeader( curHeader );
	}



	ALPHA = alpha;
	CHAR = ascii;
	CR = 0x0d;
	LF = 0x0a;
	CRLF = CR LF;   # Do NOT also allow just \n !
	CTL = cntrl | 0x7f;
	DIGIT = digit;
	DQUOTE = 0x22;
	HEXDIG = xdigit;
	HTAB = 0x09;
	SP = 0x20;
	VCHAR = 0x21..0x7e;
	WSP = SP | HTAB;
	LWSP = (WSP | CRLF WSP)*;

	LWS = (WSP* CRLF)? WSP+;
	SWS = LWS?;

	alphanum = ALPHA | DIGIT;
	escaped = "%" HEXDIG HEXDIG;

	specials = [()<>@,;:".] | "\\" | "[" | "]";
	delimiters = specials | LWS;

	SEMI = SWS ";" SWS;
	EQUAL = SWS "=" SWS;
	HCOLON = (SP | HTAB)* ":" <: SWS;

	UTF8_CONT = 0x80..0xBF;
	UTF8_NONASCII = 0xC0..0xDF UTF8_CONT{1} |
		0xE0..0xEF UTF8_CONT{2} |
		0xF0..0xF7 UTF8_CONT{3} |
		0xF8..0xFB UTF8_CONT{4} |
		0xFC..0xFD UTF8_CONT{5};

	token = (alphanum | [;\-/.!%*_+'`~])+;
	header_value = (any -- [ <>;\r\n])+;  #(alphanum | ["< >@:\-/.!%*_+'`~])+;
	qdtext = LWS | 0x21 | 0x23..0x5b | 0x5d..0x7e | UTF8_NONASCII;
	quoted_pair = '\\' (0x00..0x09 | 0x0b..0x0c | 0x0e..0x7f);
	quoted_string = SWS DQUOTE (qdtext | quoted_pair)* DQUOTE;

	pval = token >mark %set_param_value | quoted_string >mark %set_param_quoted_value;
	pname = token;
	params = pname >mark %add_param (EQUAL pval)?;
	header_name = token;

	normal_header_with_params = header_value >mark %set_header_value (SEMI params %do_add_param)*;

	uri_header = ("<" (any -- [\r\n])+ ">") >mark %set_header_value;

	xheader = header_name >mark %add_header HCOLON :>
		(normal_header_with_params |
		uri_header);


	headers := (xheader %do_add_header CRLF)* CRLF @do_finish;
}%%

%% write data;
	protected static String arrayToString(int mark, int p) {
		byte[] tmp = new byte[ p - mark ];
		System.arraycopy( data, mark, tmp, 0, p - mark);
		String result = new String( tmp );
		return result;
	}

	protected static byte[] data;

	public static BodyPart parse(byte[] input) {
		data = input;

        int m_Mark = 0;
        int cs = 0;                  // Ragel keeps the state in "cs"
        int p = 0;                   // Current index into data is "p"
        int pe = data.length;        // Length of data
        Param curParam = null;
        BodyHeader curHeader = null;


        BodyPart bodyPart = new BodyPart();




	%% write init;

	%% write exec;

	if (cs == body_error) {
		System.out.println("Your input did not comply with the grammar");
        System.out.println("body parsing error at " + p);
		return null;
	} else if (cs < body_first_final) {
		System.out.println("body attribute incomplete");
		return null;
	} else {
            System.out.println("body parsed successfully!");
            //byte[] temp  = new byte[p - m_Mark];
            //System.arraycopy(data, m_Mark, temp, 0, temp.length);
            byte[] temp = new byte[pe - p];
            System.arraycopy(data, p, temp, 0, temp.length);
            bodyPart.setContent(temp);
            return bodyPart;

        }
    }

    /*public static void main(String[] args) {
         String input = "SIP_MESSAGE sip:android1001@demo.movial.com SIP/2.0\n" +
         "Call-ID: 3b52b47c-e82a-46f6-9789-69ea0b89ffff\n" +
         "CSeq: 1 SIP_MESSAGE\n" +
         "To: <sip:android1001@demo.movial.com>\n" +
         "From: \"android1000\" <sip:android1000@demo.movial.com>;tag=7246f0ff-5c84-4442-9492-6e28b289f36d\n" +
         "Max-Forwards: 70\n" +
         "Supported: 100rel, eventlist\n" +
         "User-Agent: Movial Communicator/7.2.90.4875\n" +
         "Accept: text/plain\n" +
         "Content-Type: multipart/alternative; boundary=\"----- =_aaaaaaaaaa0\"\n" +
         "Content-Length: 1000\n" +
         "\n" +
         "------- =_aaaaaaaaaa0\r\n" +
         "Content-Type: text/plain; test=value\r\n" +
         "Content-ID: <1283.780402430.2@ora.com>\r\n" +
         "\r\n" +
         "We've just released the new third edition of \"MH & xmh: Email for\r\n" +
         "Users & Programmers.\"  Changes include:\r\n" +
         "- MIME (Multimedia) mail\r\n" +
         "- The popular MH-E GNU Emacs front-end to MH\r\n" +
         "...omitted...\r\n" +
         "\r\n" +
         "------- =_aaaaaaaaaa0\r\n" +
         "Content-Type: text/enriched\r\n" +
         "Content-ID: <1283.780402430.3@ora.com>\r\n" +
         "\r\n" +
         "We've just released the new third edition of <italic>MH & xmh: Email\r\n" +
         "for Users & Programmers</italic>. Changes include:\r\n" +
         "\r\n" +
         "<indent>\r\n" +
         "- MIME (Multimedia) mail\r\n" +
         "\r\n" +
         "- The popular MH-E GNU Emacs front-end to MH\r\n" +
         "...omitted...\r\n" +
         "\r\n" +
         "------- =_aaaaaaaaaa0\r\n" +
         "Content-Type: application/postscript\r\n" +
         "Content-ID: <1283.780402430.4@ora.com>\r\n" +
         "Content-Transfer-Encoding: quoted-printable\r\n" +
         "\r\n" +
         " %!PS-Adobe-3.0\r\n" +
         " %%Creator: groff version 1.09\r\n" +
         " ...omitted...\r\n" +
         "  %%Trailer\r\n" +
         "end\r\n" +
         " %%EOF\r\n" +
         "\r\n" +
         "------- =_aaaaaaaaaa0--";

         input = "SIP_NOTIFY sip:tch2_user_9320214@172.17.50.24:5061 SIP/2.0\r\n" +
         "Max-Forwards: 67\r\n" +
         "Via: SIP/2.0/TCP 10.80.63.172:5060;branch=z9hG4bK51tgprgfgnglbij8md8bh7w11\r\n" +
         "To: <tel:+4689320214>;tag=f26860e07d9389eeaaa1cb56dfaff864\r\n" +
         "From: <sip:tch2_user_9320214@tch2.imt.se;pres-list=rcs>;tag=h7g4Esbg_g77gdbd5-3ufa\r\n" +
         "Call-ID: 5421918aae3fc298db4deba70c5b18b5\r\n" +
         "CSeq: 2 SIP_NOTIFY\r\n" +
         "Contact: <sip:sgc_c@10.80.63.172;transport=udp>\r\n" +
         "Event: presence\r\n" +
         "Require: eventlist\r\n" +
         "Subscription-State: active;expires=3599\r\n" +
         "Content-Type:multipart/related;type=\"application/rlmi+xml\";start=\"<sip:tch2_user_9320214@tch2.imt.se;pres-list=rcs>\";boundary=\"----=_Part_9038_28964864.1269514860737\"\r\n" +
         "Content-Length: 2155\r\n" +
         "\r\n" +
         "------=_Part_9038_28964864.1269514860737\r\n" +
         "Content-Type: application/rlmi+xml;charset=\"UTF-8\"\r\n" +
         "Content-Transfer-Encoding: binary\r\n" +
         "Content-ID: <sip:tch2_user_9320214@tch2.imt.se;pres-list=rcs>\r\n" +
         "\r\n" +
         "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
         "<list uri=\"sip:tch2_user_9320214@tch2.imt.se;pres-list=rcs\" version=\"0\" fullState=\"true\" xmlns=\"urn:ietf:params:xml:ns:rlmi\"><resource uri=\"tel:+4689320214\"><instance id=\"1\" state=\"active\" cid=\"tel:+4689320214\"/>" +
         "</resource><resource uri=\"sip:tch2_user_9320215@tch2.imt.se\"><name xml:lang=\"\">fifteen</name><instance id=\"1\" state=\"active\" " +
         "cid=\"sip:tch2_user_9320215@tch2.imt.se\"/></resource><resource uri=\"tel:+4689320220\"><instance id=\"1\" state=\"pending\"/>" +
         "</resource><resource uri=\"tel:+4689320229\"><name xml:lang=\"\">kimmo 229</name><instance id=\"1\" state=\"pending\"/></resource>" +
         "<resource uri=\"tel:+4689320200\"><name xml:lang=\"\">Turkka 200</name><instance id=\"1\" state=\"pending\"/></resource>" +
         "<resource uri=\"tel:+4689320201\"><name xml:lang=\"\">Turkka 201</name><instance id=\"1\" state=\"active\" cid=\"tel:+4689320201\"/></resource>" +
         "<resource uri=\"tel:+4689320202\"><name xml:lang=\"\">Turkka 202</name><instance id=\"1\" state=\"pending\"/></resource>" +
         "<resource uri=\"tel:+4689320228\"><instance id=\"1\" state=\"pending\"/></resource></list>\r\n" +
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

         BaseSipMessage msg = MessageParser.parse(input);
         if(msg != null){
             System.out.println("Message parsed sucessfully\n"+msg.getContent());
         }
         String boundary = msg.getContentType().getParamsList().get("boundary").getValue();
         if(boundary != null){
             System.out.println("boundary: "+boundary+"\n");
         }
         List<BodyPartData> parts = BodyPartUtils.parseBody(msg.getBody(), boundary);
         System.out.println(parts.toString());
     }*/

}
