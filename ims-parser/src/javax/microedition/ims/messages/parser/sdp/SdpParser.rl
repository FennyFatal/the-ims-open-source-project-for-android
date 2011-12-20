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

package javax.microedition.ims.messages.parser.sdp;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.messages.parser.ParserUtils;
import javax.microedition.ims.messages.wrappers.sdp.*;


public class SdpParser{

%%{
	machine sdp;
	alphtype char;

	action mark {
		m_Mark = p;
	}

	action set_proto_version {
		mes.setVersion(ParserUtils.toNumber(arrayToString(m_Mark, p), 0, 10));
	}

	action set_username {
		mes.setUsername( arrayToString(m_Mark, p) );
	}

	action set_session_id {
		mes.setSessionId(ParserUtils.toLongNumber(arrayToString(m_Mark, p), 0, 10));
	}

	action set_session_version {
		mes.setSessionVersion(ParserUtils.toLongNumber(arrayToString(m_Mark, p), 0, 10));
	}

	action set_session_addr_type {
		String t = arrayToString(m_Mark, p);
		if (t.equals("IP6"))
			mes.setAddrType( AddrType.IP6 );
	}

	action set_session_address {
		mes.setSessionAddress( arrayToString(m_Mark, p) );
	}

	action set_session_name {
		mes.setSessionName( arrayToString(m_Mark, p) );
	}

	action set_information {
		if (inMedia){
			curMedia.setInformation(arrayToString(m_Mark, p) );
		} else{
			mes.setSessionInformation( arrayToString(m_Mark, p) );
		}
	}

	action set_uri {
		mes.setUri( arrayToString(m_Mark, p) );
	}

	action add_email_address {
		mes.getEmailAddresses().add(arrayToString(m_Mark, p) );
	}

	action add_phone_number {
		mes.getPhoneNumbers().add(arrayToString(m_Mark, p) );
	}

	action set_conn_addr_type {
		String t = arrayToString(m_Mark, p);
		if (t.equals("IP6"))  {
			curConnectionInfo.setAddrType( AddrType.IP6 );
		} else {
			curConnectionInfo.setAddrType( AddrType.IP4 );
		}
	}

	action set_conn_address {
		curConnectionInfo.setAddress( arrayToString(m_Mark, p) );
		mes.setConnectionInfo( curConnectionInfo );
	}

	action init_media {
		curMedia = new Media();
		inMedia = true;
	}

	action reset_connection_info {
		curConnectionInfo = new ConnectionInfo();
	}

	action set_media_type {
		curMedia.setType( arrayToString(m_Mark, p) );
	}

	action set_media_port {
		curMedia.setPort(ParserUtils.toNumber(arrayToString(m_Mark, p), 0, 10));
	}

	action set_media_numports {
		curMedia.setNumberOfPorts(ParserUtils.toNumber(arrayToString(m_Mark, p), 0, 10));
	}

	action set_media_protocol {
		curMedia.setProtocol(arrayToString(m_Mark, p) );
	}

	action add_media_format {
		curMedia.getFormats().add(arrayToString(m_Mark, p) );
	}

	action set_media_connection_info {
		curMedia.setConnectionInfo( curConnectionInfo );
	}

	action add_media_to_message {
		mes.getMedias().add( curMedia );
	}

	action set_att_field_name {
		attField = arrayToString(m_Mark, p);
		attValue = null;
	}

	action set_att_value {
		attValue = arrayToString(m_Mark, p);
	}

	action add_attribute {
		Attribute att= new Attribute();
		att.setName(attField );
		if (attValue != null) {
			att.setValue( attValue );
		}
		if (inMedia){
			curMedia.addAttribute(att);
		}  else{
			mes.getAttributes().add(att);
		}
	}

	action set_bandwidth_type {
		bandwidth.setType( arrayToString(m_Mark, p) );
	}

	action set_bandwidth_value {
		bandwidth.setBandwidth(ParserUtils.toLongNumber(arrayToString(m_Mark, p), 0, 10));
		if (inMedia) {
			curMedia.setBandwidth( bandwidth );
		} else {
			mes.addBandwidth( bandwidth );
		}
	}

	action set_start_time {
		timing.setStartTime(ParserUtils.toLongNumber(arrayToString(m_Mark, p), 0, 10));
	}

	action set_stop_time {
		timing.setEndTime(ParserUtils.toLongNumber(arrayToString(m_Mark, p), 0, 10));
	}

	action add_timing {
		mes.getTimings().add( timing );
		timing = new Timing();
	}

	action set_repeat_interval {
		repeat.setInterval(ParserUtils.toLongNumber(arrayToString(m_Mark, p), 0, 10));
	}

	action set_repeat_duration {
		repeat.setDuration(ParserUtils.toLongNumber(arrayToString(m_Mark, p), 0, 10));
	}

	action add_repeat_value {
		repeat.getOffsets().add( new Long(ParserUtils.toLongNumber(arrayToString(m_Mark, p), 0, 10)));
	}

	action set_zone_adj_time {
	}

	action set_zone_adj_ttime {
	}

	action add_repeat {
		timing.getRepeats().add( repeat );
		repeat = new TimingRepeat();
	}

	action add_adj_time {
		adj.setOffset(ParserUtils.toLongNumber(arrayToString(m_Mark, p), 0, 10));
	}

	action add_adj_value {
		adj.setAdjustment(ParserUtils.toLongNumber(arrayToString(m_Mark, p), 0, 10));
		mes.getZoneAdjustments().add( adj );
		adj = new ZoneAdjustment();
	}

	action set_key_field {
		EncryptionKey keys = new EncryptionKey();
		keys.setValue( arrayToString(m_Mark, p) );
		if (inMedia){
			curMedia.setEncryptionKeys( keys );
		}else{
			mes.setEncryptionKeys( keys );
		}
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
	POS_DIGIT = 0x31..0x39;

	alpha_numeric = ALPHA | DIGIT;

	proto_version = 0x76 "=" DIGIT+ >mark %set_proto_version CRLF;

	non_ws_string = (VCHAR - (0x80..0xFF))+;

	username = non_ws_string;

	sess_id = DIGIT+;

	sess_version = DIGIT+;

	token_char = 0x21 | 0x23..0x27 | 0x2A..0x2B | 0x2D..0x2E | 0x30..0x39 |
	             0x41..0x5A | 0x5E..0x7E;

	token = token_char+;

	integer = POS_DIGIT DIGIT*;

	ttl = (POS_DIGIT DIGIT{2}) | "0";

	nettype = token;

	addrtype = token;

	ml = ("22" ("4" | "5" | "6" | "7" | "8" | "9")) |
	     ("23" DIGIT);

	hex4 = HEXDIG{1,4};

	hexseq = hex4 (":" hex4)*;

	hexpart = hexseq | hexseq "::" (hexseq)? | "::" (hexseq)?;

	decimal_uchar = DIGIT | POS_DIGIT DIGIT |
	                ("1" DIGIT{2}) |
	                ("2" ("0" | "1" | "2" | "3" | "4") DIGIT) |
	                ("2" "5" ("0" | "1" | "2" | "3" | "4" | "5"));

	IP4_multicast = ml ("." decimal_uchar){3} "/" ttl ("/" integer)?;

	IP6_multicast = hexpart ("/" integer);

	bl = decimal_uchar;

	IP4_address = bl ("." decimal_uchar){3};

	IP6_address = hexpart (":" IP4_address)?;

	FQDN = (alpha_numeric | "-" | "."){4};

	extn_addr = non_ws_string;

	unicast_address = IP4_address | IP6_address | FQDN | extn_addr;

	origin_field = 0x6f "=" username >mark %set_username SP sess_id >mark %set_session_id
	               SP sess_version >mark %set_session_version SP
	               nettype SP addrtype >mark %set_session_addr_type 
	               SP unicast_address >mark %set_session_address CRLF;

	multicast_address = IP4_multicast | IP6_multicast | FQDN | extn_addr;

	byte_string = (0x01..0x09 | 0x0B..0x0C | 0x0E..0xFF)+;

	text = byte_string;

	session_name_field = 0x73 "=" text >mark %set_session_name CRLF;

	information_field = (0x69 "=" text >mark %set_information CRLF)?;

	uri = non_ws_string;

	uri_field = (0x75 "=" uri >mark %set_uri CRLF)?;

	email_address = (VCHAR | SP)+;

	email_fields = (0x65 "=" email_address >mark %add_email_address CRLF)*;

	phone = "+"? DIGIT (SP | "-" | DIGIT)+;

	connection_address = multicast_address | unicast_address;

	email_safe = 0x01..0x09 | 0x0B..0x0C | 0x0E..0x27 |
	             0x2A..0x2B | 0x3D | 0x3F..0xFF;

	phone_number = phone SP* "(" email_safe+ ")" |
	               email_safe+ "<" phone ">" |
	               phone;

	phone_fields = (0x70 "=" phone_number >mark %add_phone_number CRLF)*;

	connection_field = 0x63 "=" nettype SP addrtype >mark %set_conn_addr_type SP 
	                   connection_address >mark %set_conn_address CRLF;

	bwtype = token;

	bandwidth = DIGIT+;

	bandwidth_fields = (0x62 "=" bwtype >mark %set_bandwidth_type ":" bandwidth >mark %set_bandwidth_value CRLF)*;

	time = POS_DIGIT DIGIT{9,};

	start_time = time | "0";

	stop_time = time | "0";

	fixed_len_time_unit = 0x64 | 0x68 | 0x6d | 0x73;

	repeat_interval = POS_DIGIT DIGIT* (fixed_len_time_unit)?;

	typed_time = DIGIT+ (fixed_len_time_unit)?;

	zone_adjustments = 0x7a "=" time >mark %add_adj_time SP ("-"? typed_time) >mark %add_adj_value
	                   (SP time >mark %add_adj_time SP ("-"? typed_time) >mark %add_adj_value)*;

	repeat_fields = 0x72 "=" repeat_interval >mark %set_repeat_interval 
	                 SP typed_time >mark %set_repeat_duration
	                (SP typed_time >mark %add_repeat_value)+;

	time_fields = (0x74 "=" start_time >mark %set_start_time SP stop_time >mark %set_stop_time
	                (CRLF repeat_fields %add_repeat)* CRLF %add_timing)+
	                (zone_adjustments CRLF)?;

	base64_char = ALPHA | DIGIT | "+" | "/";

	base64_pad = base64_char{2} "==" |
	             base64_char{3} "=";

	base64_unit = base64_char{4};

	base64 = base64_unit* (base64_pad)?;

	key_type = "prompt" |
	           "clear:" text |
	           "base64:" base64 |
	           "uri:" uri;

	key_field = (0x6b "=" key_type >mark %set_key_field CRLF)?;

	att_field = token;

	att_value = byte_string;

	attribute = att_field >mark %set_att_field_name ":" att_value >mark %set_att_value | att_field >mark %set_att_field_name;

	attribute_fields = (0x61 "=" attribute %add_attribute CRLF)*;

	media = token;

	port = DIGIT+;

	fmt = token;

	proto = token ("/" token)*;

	media_field = 0x6d "=" >init_media media >mark %set_media_type 
	              SP port >mark %set_media_port ("/" integer >mark %set_media_numports )?
	              SP proto >mark %set_media_protocol (SP fmt >mark %add_media_format)+ CRLF;

	media_descriptions = (media_field
	                      information_field
	                      (connection_field >reset_connection_info %set_media_connection_info)*
	                      bandwidth_fields
	                      key_field
	                      attribute_fields %add_media_to_message)*;

	sdp_message = proto_version
	              origin_field
	              session_name_field
	              information_field
	              uri_field
	              email_fields
	              phone_fields
	              connection_field?
	              bandwidth_fields
	              time_fields
	              key_field
	              attribute_fields
	              media_descriptions;

	main := sdp_message;

}%%

%% write data;

	protected static String arrayToString(int mark, int p) {

		byte[] tmp = new byte[ p - mark ];
		System.arraycopy( data, mark, tmp, 0, p - mark);
		String result = new String( tmp );
		return result;
	}
	protected static byte[] data;
	protected static int cs = 0;                  // Ragel keeps the state in "cs"
	protected static int p = 0;                   // Current index into data is "p"
	protected static int pe = 0;        // Length of data, SO YES data[pe] IS AN INVALID INDEX THIS IS CORRECT!
	protected static int eof = 0; 
	
	private static void cleanVariables(){
	    data = null;
        cs = 0;                  
        p = 0;                   
        pe = 0;        
        eof = 0;         
	}


	public static SdpMessage parse(String input) {
	        //Logger.log(Logger.Tag.PARSER,"parsing started. Your input is: "+input);

	cleanVariables();

	if(input == null || input.length() == 0){
	     return null;
	 } else if(!input.endsWith("\n")){
	     input += "\n";
	 }
	
	 try {
            data = input.getBytes("UTF-8");
        } catch (Exception e) {
            Logger.log(Logger.Tag.PARSER, "Unsupported encoding");
            return null;
        }


        // Whenever we see something interesting, remember the current index. Then, when we've decided
        // we really saw something interesting, fetch the part between "mark" and "current value of p"

        int m_Mark = 0;
        cs = 0;                  // Ragel keeps the state in "cs"
        p = 0;                   // Current index into data is "p"
        pe = data.length;        // Length of data
        eof = pe;  
	
	boolean inMedia = false;
	Media curMedia = new Media();
	String attField = null, attValue = null;
	Bandwidth bandwidth = new Bandwidth();
	Timing timing = new Timing();
	TimingRepeat repeat = new TimingRepeat();
	ZoneAdjustment adj = new ZoneAdjustment();
	ConnectionInfo  curConnectionInfo = new ConnectionInfo();

	SdpMessage mes = new SdpMessage();

	%% write init;
	%% write exec;
	
				        if (cs == sdp_error) {
            Logger.log(Logger.Tag.PARSER, "Your input did not comply with the grammar");
            Logger.log(Logger.Tag.PARSER, "SDP Parsing error at " + p + " near text:" + arrayToString(p - 2, p + 2));
            return null;

        }
        else if (cs < sdp_first_final) {
            Logger.log(Logger.Tag.PARSER, "SDP message is incomplete, but parsed ok");
            Logger.log("Result", mes.getContent());

            return mes;

        }
        else {
            Logger.log(Logger.Tag.PARSER, "SDP parsed successfully!");
            return mes;

        }

}


public static void main(String[] args)
{
	String input = "v=0\r\n"+
"o=jdoe 2890844526 2890842807 IN IP4 10.47.16.5\r\n"+
"s=SDP Seminar\r\n"+
"i=A Seminar on the session description protocol\r\n"+
"u=http://www.example.com/seminars/sdp.pdf\r\n"+
"e=j.doe@example.com (Jane Doe)\r\n"+
"c=IN IP4 224.2.17.12/127\r\n"+
"t=2873397496 2873404696\r\n"+
"a=recvonly\r\n"+
"m=video 49170 RTP/AVP 100 99 97\r\n"+
"a=rtpmap:97 H264/90000\r\n"+
"a=fmtp:97 profile-level-id=42A01E; packetization-mode=0;sprop-parameter-sets=Z0IACpZTBYmI,aMljiA==,As0DEWlsIOp==,KyzFGleR\r\n"+
"a=rtpmap:99 H264/90000\r\n"+
"a=fmtp:99 profile-level-id=42A01E; packetization-mode=1;sprop-parameter-sets=Z0IACpZTBYmI,aMljiA==,As0DEWlsIOp==,KyzFGleR; max-rcmd-nalu-size=3980\r\n"+
"a=rtpmap:100 H264/90000\r\n"+
"a=fmtp:100 profile-level-id=42A01E; packetization-mode=2;sprop-parameter-sets=Z0IACpZTBYmI,aMljiA==,As0DEWlsIOp==,KyzFGleR; sprop-interleaving-depth=60;sprop-deint-buf-req=86000; sprop-init-buf-time=156320;deint-buf-cap=128000; max-rcmd-nalu-size=3980\r\n";

	System.out.println(parse(input));
}

}
