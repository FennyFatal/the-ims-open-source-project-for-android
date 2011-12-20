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

package javax.microedition.ims.messages.parser.msrp;

import javax.microedition.ims.messages.parser.ParserUtils;
import javax.microedition.ims.messages.wrappers.msrp.MsrpUri;

public class MsrpUriParser{

%%{
	machine msrp_uri;
	alphtype char;

	action mark {
		m_Mark = p;
	}

	action set_scheme {
		uri.setPrefix(arrayToString(m_Mark, p));
	}
	action set_username {
		uri.setUsername(arrayToString(m_Mark, p));
	}
	action set_domain {
		uri.setDomain(arrayToString(m_Mark, p));
	}
	action set_id {
		uri.setId(arrayToString(m_Mark, p));
	}
	action set_transport {
		uri.setTransport(arrayToString(m_Mark, p));
	}
	action set_port {
		uri.setPort(ParserUtils.toNumber(arrayToString(m_Mark, p), 0, 10));
	}

	ALPHA = alpha;
	BIT = "0" | "1";
	CHAR = ascii;
	CR = 0x0d;
	LF = 0x0a;
	CRLF = (CR LF | LF);           # Also accept unix line endings
	CTL = cntrl | 0x7f;
	DIGIT = digit;
	DQUOTE = 0x22;
	HEXDIG = xdigit;
	HTAB = 0x09;
	SP = 0x20;
	VCHAR = 0x21..0x7E;
	WSP = SP | HTAB;
	LWSP = (WSP | CRLF WSP)*;

	# RFC 3986

	unreserved = ALPHA | DIGIT | "-" | "." | "_" | "~";
	sub_delims = "!" | "$" | "&" | "'" | "(" | ")" | "*" | "+" | "," | ";" | "=";
	IPvFuture = "v"i HEXDIG+ "." (unreserved | sub_delims | ":")+;
	pct_encoded = "%" HEXDIG HEXDIG;
	port = DIGIT*;
	reg_name = (unreserved | pct_encoded | sub_delims)*;
	dec_octet = DIGIT | 0x31..0x39 | "1" DIGIT DIGIT | "2" 0x30..0x34 DIGIT | "25" 0x30..0x35;
	IPv4address = dec_octet "." dec_octet "." dec_octet "." dec_octet;
	h16 = HEXDIG{1,4};
	ls32 = (h16 ":" h16) | IPv4address;
	IPv6address =              (h16 ":"){6} ls32 |
	                      "::" (h16 ":"){5} ls32 |
	(              h16 )? "::" (h16 ":"){4} ls32 |
	( (h16 ":")    h16 )? "::" (h16 ":"){3} ls32 |
	( (h16 ":"){2} h16 )? "::" (h16 ":"){2} ls32 |
	( (h16 ":"){3} h16 )? "::"  h16 ":"     ls32 |
	( (h16 ":"){4} h16 )? "::"              ls32 |
	( (h16 ":"){5} h16 )? "::"              h16
	( (h16 ":"){6} h16 )? "::"                   ;

	IP_literal = "[" (IPv6address | IPvFuture) "]";
	host = IP_literal | IPv4address | reg_name;
	userinfo = (unreserved | pct_encoded | sub_delims | ":")*;
	authority = (userinfo >mark %set_username "@")? host >mark %set_domain (":" port >mark %set_port)?;

	# RFC 4975

	ALPHANUM = ALPHA | DIGIT;
	BACKSLASH = '\\';
	UPALPHA = 0x31..0x5A;
	OCTET = any;

	UTF8_CONT = 0x80..0xFB;
	UTF8_NONASCII = 0xC0..0xDF  UTF8_CONT |
	                0xE0..0xEF  UTF8_CONT UTF8_CONT |
	                0xF0..0xF7  UTF8_CONT UTF8_CONT UTF8_CONT |
	                0xF8..0xFB  UTF8_CONT UTF8_CONT UTF8_CONT UTF8_CONT |
	                0xFC..0xFD  UTF8_CONT UTF8_CONT UTF8_CONT UTF8_CONT UTF8_CONT;

	utf8text = (HTAB | 0x20..0x7E | UTF8_NONASCII)*;

	token = (0x21 | 0x23..0x27 | 0x2A..0x2B | 0x2D..0x2E | 0x30..0x39 | 0x41..0x5A | 0x5E..0x7E)+;

	URI_parameter = token ("=" token)?;

	transport = "tcp"i | ALPHANUM+;

	ident_char = ALPHANUM | "." | "-" | "+" | "%" | "=";

	ident = ALPHANUM ident_char{3,31};

	transact_id = ident;

	comment = utf8text;

	continuation_flag = "+" | "$" | "#";

	end_line = "-------" transact_id continuation_flag CRLF;

	session_id = (unreserved | "+" | "=" | "/")+;

	msrp_scheme = "msrp"i | "msrps"i;

	MSRP_URI := msrp_scheme >mark %set_scheme "://" authority ("/" session_id >mark %set_id)? ";" 
	            transport >mark %set_transport (";" URI_parameter)*;
}%%

%%write data;

	protected static String arrayToString(int mark, int p) {

		byte[] tmp = new byte[ p - mark ];
		System.arraycopy( data, mark, tmp, 0, p - mark);
		String result = new String( tmp );
		return result;
	}

	protected static byte[] data;
	
	public static MsrpUri parse(final String input) {
	
	System.out.println("parsing started. Your input is: "+input);
	
	 	try {
            data = input.getBytes("UTF-8");
        } catch (Exception e) {
            System.out.println("Unsupported encoding");
            return null;
        }

	MsrpUri uri = new MsrpUri();

	int cs = 0;
	int p = 0;
	int pe = data.length;
	int eof = pe;
	int m_Mark = 0;

	%%write init;

	%%write exec;

	if (cs == msrp_uri_error) {
		 System.out.println("MsrpUri invalid");
         System.out.println("SDP Parsing error at " + p);
	     return null;
	}else if (cs < msrp_uri_first_final || pe != p) {
		System.out.println("MsrpUri is incomplete, but parsed ok");
	    return uri;
	}else {
	    System.out.println("MsrpUri parsed successfully!");
	    return uri;
	}
	}

	public static void main(String[] args) {
        String input = "msrp://62.236.91.3:8501/fJSfHDrHO28S849940wy;tcp";
        MsrpUri uri = MsrpUriParser.parse(input);
        if (uri != null) {
            System.out.println("uri: " + uri.buildContent());
        }
    }


}

