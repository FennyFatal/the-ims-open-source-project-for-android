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

package javax.microedition.ims.messages.parser.message;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.messages.parser.ParserUtils;
import javax.microedition.ims.messages.wrappers.common.ParamList;
import javax.microedition.ims.messages.wrappers.common.ParamListDefaultImpl;
import javax.microedition.ims.messages.wrappers.sip.SipUri;
import javax.microedition.ims.messages.wrappers.sip.UriHeader;



public class SipUriParser {

%%{
	machine uri_parser;
	alphtype byte;
	
	action mark {
		m_Mark = p;
	}

	action set_scheme {
       //uri.getUriBuilder().setPrefix(arrayToString(m_Mark, p));
       uriBuilder.prefix(arrayToString(m_Mark, p));
    }


	action set_dn {
		uriBuilder.displayName(arrayToString(m_Mark, p + 1));
	}

	action set_dn_quoted {
	 //uri.getUriBuilder().setDisplayName(arrayToString(m_Mark + 1, p - 1));
     uriBuilder.displayName(arrayToString(m_Mark + 1, p - 1));
	}

	action set_username {
		//uri.getUriBuilder().setUsername(arrayToString(m_Mark, p));
        uriBuilder.username(arrayToString(m_Mark, p));
	}

	action set_pw {
		//uri.getUriBuilder().setPassword(arrayToString(m_Mark, p));
      uriBuilder.password(arrayToString(m_Mark, p));
	}

	action set_domain {
		//uri.getUriBuilder().setDomain(arrayToString(m_Mark, p));
       uriBuilder.domain(arrayToString(m_Mark, p));
	}

	action set_port {
	  //uri.getUriBuilder().setPort(ParserUtils.toNumber(arrayToString(m_Mark, p), 0, 10));
      uriBuilder.port(ParserUtils.toNumber(arrayToString(m_Mark, p), 0, 10));
    }


	action set_create_param_name {
	  m_CurParam = arrayToString(m_Mark, p);
      //Logger.log(Logger.Tag.PARSER,"m_CurParam: "+m_CurParam);
      //uri.getUriBuilder().getParamsList().set(m_CurParam);
      uriParamList.set(m_CurParam);
	}

	action set_param_value {
		String tmp = arrayToString(m_Mark, p);
      //Logger.log(Logger.Tag.PARSER,"val: "+tmp);
      //uri.getUriBuilder().getParamsList().set(m_CurParam, tmp);
      uriParamList.set(m_CurParam, tmp);
	}

	# ABNF core rules direct from RFC 2234

	ALPHA = alpha;
	BIT = "0" | "1";
	CHAR = ascii;
	CR = 0x0d;
	LF = 0x0a;
	CRLF = (CR LF | LF); # Also accept unix line endings
	CTL = cntrl | 0x7f;
	DIGIT = digit;
	DQUOTE = 0x22;
	HEXDIG = xdigit;
	HTAB = 0x09;
	SP = 0x20;
	VCHAR = 0x21..0x7E;
	WSP = SP | HTAB;
	LWSP = (WSP | CRLF WSP)*;


	# SIP basic rules from RFC 3261

	alphanum = ALPHA | DIGIT;
	reserved = [;/?:@&=+$,];
	mark = [\-_.!~*'()];
	unreserved = alphanum | mark;
	escaped = "%" HEXDIG HEXDIG;


	# Manage line folding

	LWS = (WSP* CRLF)? WSP+;
	SWS = LWS?;

	HCOLON = (SP | HTAB)* ":" <: SWS;

	TEXT_UTF8_CHAR = 0x21..0x7E;
	TEXT_UTF8_TRIM = TEXT_UTF8_CHAR+ (LWS* TEXT_UTF8_CHAR)*;
	UTF8_CONT = 0x80..0xBF;
	UTF8_NONASCII = 0xC0..0xDF UTF8_CONT{1} |
	                0xE0..0xEF UTF8_CONT{2} |
	                0xF0..0xF7 UTF8_CONT{3} |
	                0xF8..0xFB UTF8_CONT{4} |
	                0xFC..0xFD UTF8_CONT{5};

	LHEX = DIGIT | [a-f];

	token = (alphanum | [\-.!%*_+'`~])+;
	seperators = [()<>@,;:/?={}\]\[\\] | DQUOTE | SP | HTAB;
	word = (alphanum | [\-.!%*_+'`~()<>:/?{}\[\]\\] | DQUOTE)+;

	STAR = SWS "*" SWS;
	SLASH = SWS "/" SWS;
	EQUAL = SWS "=" SWS;
	LPAREN = SWS "(" SWS;
	RPAREN = SWS ")" SWS;
	RAQUOT = SWS ">" SWS;
	LAQUOT = SWS "<" SWS;
	COMMA = SWS "," SWS;
	SEMI = SWS ";" SWS;
	COLON = SWS ":" SWS;
	LDQUOT = SWS DQUOTE;
	RDQUOT = DQUOTE SWS;

	ctext = 0x21..0x27 | 0x2A..0x5B | 0x5D..0x7E | UTF8_NONASCII | LWS;
	qdtext = LWS | 0x21 | 0x23..0x5B | 0x5D..0x7E | UTF8_NONASCII;
	quoted_pair = '\\' (0x00..0x09 | 0x0B..0x0C | 0x0E..0x7F);
	quoted_string = SWS DQUOTE (qdtext | quoted_pair)* DQUOTE;

	# Comments are not needed
	#comment_end = RPAREN @recurse_down ;
	#comment = LPAREN (ctext | quoted_pair | LPAREN @recurse_down )* comment_end;

	INVITE = "INVITE";
	ACK = "ACK";
	OPTIONS = "OPTIONS";
	BYE = "BYE";
	CANCEL = "CANCEL";
	REGISTER = "REGISTER";  # All case SENSITIVE

	extension_method = token;
	Method = extension_method; #INVITE | ACK | OPTIONS | BYE | CANCEL | REGISTER | extension_method;

	IPv4address = DIGIT{1,3} "." DIGIT{1,3} "." DIGIT{1,3} "." DIGIT{1,3};
	hex4 = HEXDIG{1,4};
	hexseq = hex4 (":" hex4)*;
	hexpart = hexseq | hexseq "::" hexseq? | "::" hexseq?;
	IPv6address = hexpart (":" IPv4address)?;
	IPv6reference = "[" IPv6address "]";
	port = DIGIT+;
	ttl = DIGIT{1,3};  # 0-255

	toplabel = ALPHA | ALPHA (alphanum | "-")* alphanum;
	domainlabel = alphanum | alphanum (alphanum | "-")* alphanum;
	hostname = (domainlabel ".")* toplabel "."?;
	host = hostname | IPv4address | IPv6reference;
	hostport = host >mark %(set_domain) (COLON port >mark %set_port )?;

	gen_value = token | host | quoted_string;
#	generic_param = token >mark %set_create_pname (EQUAL gen_value >mark %set_pvalue)?;
#	generic_uri_param = token >mark %set_create_param_name (EQUAL gen_value >mark %set_param_value)?;
	protocol_version = token;
	other_transport = token;

	delta_seconds = DIGIT+;

	hnv_unreserved = [\[\]/?:+$];
	hname = (hnv_unreserved | unreserved | escaped)+;
	hvalue = (hnv_unreserved | unreserved | escaped)*;
	header = hname "=" hvalue;
	headers = "?" header ("&" header);

	param_unreserved = [\[\]/:&+$];
	paramchar = param_unreserved | unreserved | escaped;
	pname = paramchar+;
	pvalue = paramchar+;
	other_param = pname >mark %set_create_param_name (EQUAL pvalue >mark %set_param_value)?;
#	maddr_param = "maddr=" host;
#	method_param = "method="i Method;
#	lr_param = "lr"i;
#	ttl_param = "ttl="i ttl;
#	other_user = token;
#	user_param = "user="i other_user; #("phone"i | "ip"i | other_user);
	transport_param = "transport="i other_transport; #("udp"i | "tcp"i | "sctp"i | "tls"i | other_transport);
	uri_parameter = other_param; #transport_param | user_param | method_param | ttl_param |
	                             #maddr_param | lr_param | other_param;
	uri_parameters = (SEMI uri_parameter)*;
	password = (unreserved | escaped | [&=+$,])*;
	user_unreserved = [&=+$,;?/];
	user = (unreserved | escaped | user_unreserved )+;
	# userinfo = (user | telephone_subscriber) (COLON password)? "@";
	userinfo = user >mark %set_username (COLON password >mark %set_pw)? "@";

	reg_name = (unreserved | escaped | [$,;:@&=+])+;
	srvr = ((userinfo "@")? hostport)?;
	authority = srvr | reg_name;
	pchar = unreserved | escaped | [:@&=+$,];
	param = pchar*;
	segment = pchar* (";" param)*;
	path_segments = segment ("/" segment)*;
	uric_no_slash = unreserved | escaped | [;?:@&=+$,];
	uric = reserved | unreserved | escaped;
	query = uric*;
	opaque_part = uric_no_slash uric*;
	abs_path = "/" path_segments;
	net_path = "//" authority abs_path?;
	hier_part = (net_path | abs_path) ("?" query)?;

	display_name = (token LWS)+ >mark @set_dn | quoted_string >mark @set_dn_quoted;

	sip_prefix = "sip"i | "sips"i;
	tel_prefix = "tel"i;
	prefix = sip_prefix | tel_prefix; 

	parameter = ";" SWS* pname (SWS* "=" SWS* pvalue)?;
	visual_seperator = "-" | "." | "(" | ")";
	phonedigit_hex = HEXDIG | "*" | "#" | visual_seperator;
	phonedigit = DIGIT | visual_seperator;
	local_number_digits = phonedigit_hex * (HEXDIG | "*" | "#") phonedigit_hex*;
	global_number_digits = "+" phonedigit* DIGIT phonedigit*;
	par = ";" other_param;
	local_number = local_number_digits >mark %set_username par*;
	global_number = global_number_digits >mark %set_username par*;
	telephone_subscriber = global_number | local_number;



	full_wo = sip_prefix >mark %set_scheme COLON userinfo? hostport uri_parameters |
	          tel_prefix >mark %set_scheme COLON telephone_subscriber;
	full_w = display_name? LAQUOT full_wo RAQUOT;

	main := full_wo | full_w;

}%%

%% write data;

	protected static String arrayToString(int mark, int p) {
		byte[] tmp = new byte[p - mark];
        System.arraycopy(data, mark, tmp, 0, p - mark);
        String result = new String(tmp);
        //Logger.log(Logger.Tag.PARSER,result);
	 return result;
	}


	protected static byte[] data;
	private static UriHeader uri ; //test object just to save data inside, real wrappers needed
	private static String m_CurParam;

/*	public static void main(String[] args) {
        if (args.length < 1) {
            Logger.log(Logger.Tag.PARSER,"You need to give me some input");
        }
        String input = "";
        for(int i = 0; i < args.length; i++){
            input += args[i]+" ";
        }

        input="Mr. Watson <sip:watson@worcester.bell-telephone.com; q=0.7; expires=3600; Mr. Watson <mailto:watson@bell-telephone.com> ;q=0.1>";
        input = "sip:kimmo@core.com hello";
        Logger.log(Logger.Tag.PARSER,"Your input is: '" + input + "'");

         possible tests
         * sip:joe.bloggs@212.123.1.213
            sip:support@phonesystem.3cx.com
            sip:22444032@phonesystem.3cx.com
            <sip:pete@core.fi>
            tel:+1-201-555-0213;hello;my;name=yo
            tel:+1-201-555-0213
            tel:+123456
            sip:kimmo@core.com
            "Displayname" <sip:kimmo@core.com;lr;a=b>
            sip:kimmo@core.com
            sip:kimmo@core.com hello


         parseUri(input);

    }*/


	public static UriHeader parseUri(String input) {
	    //uri = new UriHeader();
        SipUri.SipUriBuilder uriBuilder = new SipUri.SipUriBuilder();
        ParamList uriParamList = new ParamListDefaultImpl();

        Logger.log(Logger.Tag.PARSER, "Your input is: '" + input + "'");


		try {
			data = input.getBytes("UTF-8");
		} catch (Exception e) {
            Logger.log(Logger.Tag.PARSER, "Unsupported encoding");
            return null;
        }


		int m_Mark = 0;


		// These are for Ragel

		int cs = 0;                  // Ragel keeps the state in "cs"
		int p = 0;                   // Current index into data is "p"
		int pe = data.length;        // Length of data
		int eof = pe;                // Depending on the grammar, it may not be clear wether you are really "done"
		                             // or not. In that case, "eof" is either 0 which means that there is still
		                             // data coming, or it equals "pe" which means that when the end is reached,
		                             // the parser must really be in a success state or else the message failed.
		                             // (So eof == 0 means, wait for more data before giving up, just in case)

		%% write init;         # Set up initial state

		%% write exec;         # Run the parser

		// Now we check the result
		// The names are generated based on the parser name, which for us is "uri_parser"

		// Error state is clear
        final SipUri.SipUriBuilder sipUriBuilder = (SipUri.SipUriBuilder) uriBuilder.paramList(uriParamList);
        uri = new UriHeader.UriHeaderBuilder().uriBuilder(sipUriBuilder).build();

        if (cs == uri_parser_error) {
            Logger.log(Logger.Tag.PARSER, "Your input did not comply with the grammar");
            Logger.log(Logger.Tag.PARSER, "The failure occured at position " + p);


            // Less than "uri_parser_first_final" means we're not "done" yet
            // (There can be many many "final" states

        }
        else if (cs < uri_parser_first_final) {
            Logger.log(Logger.Tag.PARSER, "Your input is valid so far, but not complete");
            return uri;
            // Otherwise, all good

        }
        else {
            Logger.log(Logger.Tag.PARSER, "Parsed successfully!");
            return uri;
        }
        return null;

	}

}
