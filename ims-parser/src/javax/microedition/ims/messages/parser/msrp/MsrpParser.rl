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
import javax.microedition.ims.messages.wrappers.msrp.ChunkTerminator;
import javax.microedition.ims.messages.wrappers.msrp.FailureReport;
import javax.microedition.ims.messages.wrappers.msrp.MessageState;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessageType;
import javax.microedition.ims.messages.wrappers.msrp.MsrpUri;
import javax.microedition.ims.messages.wrappers.msrp.SuccessReport;

public class MsrpParser{

%%{
	machine msrp;
	alphtype char;

	action mark {
		m_Mark = p;
	}

	action set_trans_id {
		transactionId = arrayToString(m_Mark, p);
	}

	action has_content {
		hasContent = true;
	}

	action create_uri {
		currentUri = new MsrpUri();
		currentUri.setPrefix( arrayToString(m_Mark, p) );
	}

	action set_username {
		currentUri.setUsername( arrayToString(m_Mark, p) );
	}

	action set_domain {
		currentUri.setDomain( arrayToString(m_Mark, p) );
	}

	action set_id {
		currentUri.setId( arrayToString(m_Mark, p) );
	}

	action set_transport {
		currentUri.setTransport( arrayToString(m_Mark, p) );
	}

	action set_port {
		currentUri.setPort(ParserUtils.toNumber(arrayToString(m_Mark, p), 0, 10));
	}

	action set_fail_no {
		failureReport = FailureReport.FailureReportNo;
	}

	action set_fail_yes {
		failureReport = FailureReport.FailureReportYes;
	}

	action set_fail_partial {
		failureReport = FailureReport.FailureReportPartial;
	}

	action set_success_yes {
		successReport = SuccessReport.SuccessReportYes;
	}

	action set_success_no {
		successReport = SuccessReport.SuccessReportNo;
	}

	action set_as_from {
		fromPath = currentUri;
		currentUri = new MsrpUri();
	}

	action set_as_to {
		toPath = currentUri;
		currentUri = new MsrpUri();
	}	
	
	action set_range_end {
		byteRangeEnd = arrayToString(m_Mark, p);
	}
	
	action set_range_start {
		byteRangeStart = ParserUtils.toNumber(arrayToString(m_Mark, p), 0, 10);
	}

	action set_message_id {
		messageId = arrayToString(m_Mark, p);
	}

	action set_total_size {
		totalSize = ParserUtils.toNumber(arrayToString(m_Mark, p), 0, 10);
	}

	action set_status_code {
		code = ParserUtils.toNumber(arrayToString(m_Mark, p), 0, 10);
	}
	
	
	action set_code {
		code = ParserUtils.toNumber(arrayToString(m_Mark, p), 0, 10);
	}

	action set_reason_phrase {
		reasonPhrase = arrayToString(m_Mark, p);
	}

	action set_content_type {
		contentType = arrayToString(m_Mark, p);
	}

	action end_of_headers {
	
		currentMessage = new MsrpMessage();
		
		System.out.println("messageId: "+messageId);

		currentMessage.setFromPath( fromPath );
		currentMessage.setMessageId( messageId );
		currentMessage.setTransactionId( transactionId );
		currentMessage.setToPath( toPath );

		currentMessage.setType( type);				
		currentMessage.setContentType( contentType );
		currentMessage.setFailureReport( failureReport );
		currentMessage.setSuccessReport( successReport );				

			
		if (type == MsrpMessageType.STATUS) {			
			currentMessage.setCode( code);
			currentMessage.setReasonPhrase( reasonPhrase);				
		} else if (type == MsrpMessageType.REPORT) {
			currentMessage.setCode( code);
			currentMessage.setReasonPhrase( reasonPhrase);		
			currentMessage.setPrevProgress( byteRangeStart);	
			currentMessage.setCurrentProgress( byteRangeEnd);			
			currentMessage.setTotalSize( totalSize );	
		} else if (type == MsrpMessageType.SEND){			
			currentMessage.setPrevProgress( byteRangeStart);	
			currentMessage.setCurrentProgress( byteRangeEnd);			
			currentMessage.setTotalSize( totalSize );
			int  bodyEnd = data.length - 7 - 2 - transactionId.length() - 1 - 2;
            int bodyStart = p + 4;
            if(bodyEnd - bodyStart < 0){
                bodyStart = bodyEnd;
            }
            byte[] tmp = new byte[ bodyEnd - bodyStart ];
            System.arraycopy( data, bodyStart, tmp, 0, bodyEnd - bodyStart);
            currentMessage.setContent(0, tmp);
		}
		
	}



	action start_data {
		fetchingData = true;
		startOfContent = p;
		//m_Mark = 0;
	}

	action endline_id {
		String eid = arrayToString(m_Mark, p -3);
		String content = arrayToString(startOfContent, m_Mark);

		String od = arrayToString(m_Mark - 7 - 2, p); //TODO refactor
		
		if (!(eid == transactionId && (!hasContent || (od.charAt(0) == '\r' && od.charAt(1) == '\n')))) {
			m_Mark = 0;
			if (p < pe) {
				fgoto data;
			} else {
				/* At the end of this */
				fbreak;
			}
		}

		if (!hasContent) {

			/* Message does not have any content */

		} else {
			int count = m_Mark - startOfContent - 7 - 2;
			assert count >= 0 : "BUG count >= 0";

			/* Pass remaining data to message */

			if (currentMessage != null) {
				currentMessage.setContent( byteRangeStart, cutArray( startOfContent, m_Mark - 7 - 2 ) );
			}
		}
		
		/*switch (terminator) {
			case NotSet:
			case Continues:
				break;
			case Aborted:
				currentMessage.abortMessage();
				break;
			case Finished:
				currentMessage.done();
				break;
		}*/

		fetchingData = false;
		startOfContent = 0;
	}


	action continuation {
		if (currentMessage != null) {
			currentMessage.chunkReceived(transactionId );

			String flag = arrayToString(p-1, p);
			if ("#".equals(flag))
				terminator = ChunkTerminator.ABORTED;
			else if ("$".equals(flag))
				terminator = ChunkTerminator.FINISHED;
			else if ("+".equals(flag))
				terminator = ChunkTerminator.CONTINUES;
				
			currentMessage.setTerminator(terminator);
		}
	}

	ALPHA = alpha;
	BIT = "0" | "1";
	CHAR = ascii;
	CR = 0x0d;
	LF = 0x0a;
	CRLF = CR LF;
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

	hval = utf8text;
	hname = ALPHA token*;

	qdtext = SP | HTAB | 0x21 | 0x23..0x5B | 0x5D..0x7E | UTF8_NONASCII;

	qd_esc = (BACKSLASH BACKSLASH) | (BACKSLASH DQUOTE);

	quoted_string = DQUOTE (qdtext | qd_esc)* DQUOTE;

	status_code = DIGIT DIGIT DIGIT;

	pname = token;
	pval = token | quoted_string;
	gen_param = pname ("=" pval)?;

	type = token;
	subtype = token;
	media_type = type "/" subtype (";" gen_param)*;

	# MOST IMPORTANT PART OF THE GRAMMAR
	# Swallow as much as possible as the body, including CRLF's, until you are 100% sure you
	# are seeing an endline.
	# "Longest-match kleene star"
	data = OCTET**;

	URI_parameter = token ("=" token)?;

	transport = "tcp"i | ALPHANUM+;

	Content_Type = "Content-Type:"i SP media_type >mark %set_content_type;

	# RFC 2045

	mime_extension_field= "Content-"i token ":" SP token;
	Content_Description = "Content-Description:"i SP token;
	Content_Disposition = "Content-Disposition:"i SP token;
	Content_ID = "Content-ID:"i SP token;     # RFC822 sucks, we're not going to define this down to the atom

	Other_Mime_header = Content_ID |
	                    Content_Description |
	                    Content_Disposition |
	                    mime_extension_field;

	#content_end = data >start_data CRLF;

	content_stuff = (Other_Mime_header CRLF)* Content_Type %end_of_headers CRLF CRLF %has_content data >start_data CRLF;

	namespace = DIGIT DIGIT DIGIT;

	# Other_Mime_header = Content_id

	ident_char = ALPHANUM | "." | "-" | "+" | "%" | "=";

	ident = ALPHANUM ident_char{2,31};

	transact_id = ident;

	comment = utf8text;

	continuation_flag = "+" | "$" | "#";

	#end_line = "-------" transact_id >mark %endline_id continuation_flag %continuation CRLF;
	end_line = "-------" transact_id >mark continuation_flag %continuation CRLF %endline_id;

	ext_header = hname ":" SP hval;

	session_id = (unreserved | "+" | "=" | "/")+;

	Status = "Status:"i SP namespace SP status_code >mark %set_code (SP comment >mark %set_reason_phrase )?;

	total = DIGIT+ >mark %set_total_size | "*";

	range_end = (DIGIT+ | "*") >mark %set_range_end;

	range_start = DIGIT+ >mark %set_range_start;

	Byte_Range = "Byte-Range:"i SP range_start "-" range_end "/" total;

	Failure_Report = "Failure-Report:"i SP ("yes"i %set_fail_yes | "no"i %set_fail_no | "partial"i %set_fail_partial);

	Success_Report = "Success-Report:"i SP ("yes"i %set_success_yes | "no"i %set_success_no);

	Message_ID = "Message-ID:"i SP ident >mark %set_message_id;

	msrp_scheme = "msrp"i | "msrps"i;

	MSRP_URI = msrp_scheme >mark %create_uri "://" authority ("/" session_id >mark %set_id)? ";" transport >mark %set_transport (";" URI_parameter)*;

	From_Path = "From-Path:"i SP MSRP_URI %set_as_from (SP MSRP_URI)*;

	To_Path = "To-Path:"i SP MSRP_URI %set_as_to (SP MSRP_URI)*;

	header = Message_ID |
	         Success_Report |
	         Failure_Report |
	         Byte_Range |
	         Status |
	         ext_header;

	headers = To_Path CRLF From_Path CRLF (header CRLF)*;

	other_method = UPALPHA+;

	method = "SEND" %{ type = MsrpMessageType.SEND; } | "REPORT" %{ type = MsrpMessageType.REPORT; } | other_method;

	req_start = "MSRP" SP transact_id >mark %set_trans_id SP method CRLF; # "MSRP" is case sensitive

	resp_start = "MSRP" SP transact_id >mark %set_trans_id SP
		status_code >mark %set_status_code %{ type = MsrpMessageType.STATUS; } (SP comment >mark %set_reason_phrase)? CRLF;   # "MSRP" is case sensitive

	msrp_request = req_start headers content_stuff end_line
	             | req_start headers %end_of_headers end_line;

	msrp_response = resp_start %{ type = MsrpMessageType.STATUS; } headers %end_of_headers end_line;

	msrp_message := msrp_request | msrp_response;

	#msrp_stream := (msrp_message %mes_done )*;
}%%

%%write data;

	protected static String arrayToString(int mark, int p) {

		byte[] tmp = new byte[ p - mark ];
		System.arraycopy( data, mark, tmp, 0, p - mark);
		String result = new String( tmp );
		return result;
	}
	
	protected static byte[] cutArray(int mark, int p) {

		byte[] tmp = new byte[ p - mark ];
		System.arraycopy( data, mark, tmp, 0, p - mark);		
		return tmp;
	}


	
		protected volatile static byte[] data;
		

	public static synchronized MsrpMessage parse(final String input) {
		System.out.println("msrp message parsing started. Your input is: \n"+input);

		if(input == null || input.length() == 0){
			throw new IllegalArgumentException("Wrong string to parse. input == null || input.length() == 0");
		}
		byte[] dataTemp = null;
		try {
			dataTemp = input.getBytes("UTF-8");
		} catch (Exception e) {
			System.out.println("Unsupported encoding");
			return null;
		}

		return parse(dataTemp);
	}

	public static synchronized MsrpMessage parse(final byte[] input) {		

		if(input == null || input.length == 0){
			return null;
			//throw new IllegalArgumentException("Wrong data to parse. input == null || input.length == 0");
		}

		//Logger.log("Message to parse :"+new String(input));
		data = input;

        
        int  cs = 0;
        ChunkTerminator  terminator = ChunkTerminator.NOT_SET;        
        int m_Mark = 0;
        int contentDiff = -1;
        int diff = 0;
        int byteRangeStart = 0;
        String byteRangeEnd = null;
        boolean fetchingData = false;

        String messageId = null;
        String transactionId = null;
        MsrpMessageType type = MsrpMessageType.NotSet;
        int code = 0;
        long totalSize = 0;
        String reasonPhrase = null;
        MsrpUri fromPath = null;
        MsrpUri toPath = null;
        FailureReport failureReport = FailureReport.NotSet;
        SuccessReport successReport = SuccessReport.NotSet;
        String contentType = null;
        MsrpMessage   currentMessage = null;

        boolean valid = false;
        boolean hasContent = false;
        MsrpUri currentUri = null;
        int  startOfContent = 0;  
        
        
        int p = 0; 
        int pe = data.length;
        int eof = pe;
        
       
     %%write init;

	 %%write exec;

	if (cs == msrp_error) {
		valid = false;
		System.out.println("Your input did not comply with the grammar");
        System.out.println("MSRP parsing error at " + p);		
		return null;
	} else if (cs < msrp_first_final) {		

		if (!fetchingData) {
			/* Still somewhere inside headers */
			diff = p - m_Mark;
			//data.eatFromBeginning( data.length - diff );

		} else {

			diff = p - startOfContent;
			/*if (m_Mark == 0) {
				contentDiff = -1;
			} else {
				contentDiff = m_Mark - startOfContent;
			}

			if (startOfContent > data.Ptr()) { //TODO fix
				data.eatFromBeginning( startOfContent - data.Ptr() );
			}


			if (m_Mark == 0) {
				int endline_length = 2 + 2 + 7 + transactionId.length() + 1;
				int avail = p - startOfContent - endline_length;

				if (avail > 2048) {
					if (currentMessage != null) {
						currentMessage.setContent( byteRangeStart, arrayToString( startOfContent, avail ) );
					}
					byteRangeStart += avail;

					assert diff > avail : "BUG diff > avail";
					diff -= avail;

					data.eatFromBeginning( avail );
				}


			}*/

		}

	} else {
		System.out.println("Parsed successfully");
		/* Reset parser for next message */
		// %%write init;

	}
	return currentMessage;
}

	
	public static void main(String[] args){
		String input = "MSRP btjg27vh1h SEND\r\n" +
		"To-Path: msrp://95.130.218.67:2855/500valjht2;tcp\r\n" +
		"From-Path: msrp://62.236.91.3:8502/lVWkNRrMK26T964354ng;tcp\r\n" +
		"Message-ID: 7389-7bfb-5418-10116\r\n" +
		"Byte-Range: 1-*/110\r\n" +
		"Content-Type: message/cpim\r\n" +
		"\r\n" +
		"From: <sip:movial5@dummy.com>\r\n" +
		"To: <sip:movial6@dummy.com>\r\n" +
		"\r\n" +
		"Content-Type: text/plain\r\n" +
		"\r\n" +
		"hello helsinki\r\n" +
		"-------btjg27vh1h$\r\n";

		MsrpMessage msg = MsrpParser.parse(input);
		if(msg != null){
			System.out.println(msg.buildContent());
		}


		input ="MSRP mdai29to6q 200 OK\r\n" +
		"To-Path: msrp://62.236.91.3:8501/fJSfHDrHO28S849940wy;tcp\r\n" +
		"From-Path: msrp://95.130.218.67:2855/24f1vvjht1;tcp\r\n" +
		"-------mdai29to6q$\r\n";

		msg = MsrpParser.parse(input);
		if(msg != null){
			System.out.println(msg.buildContent());
		}

		input= "MSRP yh67 REPORT\r\n" +
		"To-Path: msrps://b.example.net:9000/aeiug;tcp\r\n" +
		//"msrps://a.example.org:9000/kjfjan;tcp \\\r\n" +
		//"msrps://alice.example.org:7965/bar;tcp\r\n" +
		"From-Path: msrps://bob.example.net:8145/foo;tcp\r\n" +
		"Message-ID: 87652\r\n" +
		"Byte-Range: 1-39/39\r\n" +
		"Status: 000 200 OK\r\n" +
		"-------yh67$\r\n";

		msg = MsrpParser.parse(input);
		if(msg != null){
			System.out.println(msg.buildContent());
		}

		input = "MSRP b38m7ipwvuglEEKO9CxuWk35g5TZA SEND\r\n" +
		"To-Path: msrp://19980039991@66.226.206.165:11726/YkaPsASjskCap8awiXdhiOekS;tcp\r\n" +
		"From-Path: msrp://19980039999@172.17.172.206:8509/u08oPp9ZA12wh84Lg;tcp\r\n" +
		"Message-ID: mF4G08\r\n" +
		"Byte-Range: 1-2048/3660\r\n" +
		"Content-Type: image/jpeg\r\n" +
		"\r\n" +
		"JFIFC\r\n" +
		"\r\n" +
		",$&1'-=-157:::#+?D?8C49:7C\r\n" +
		"\r\n" +
		"\r\n" +
		"7%%77777777777777777777777777777777777777777777777777\r\n" +
		"-------b38m7ipwvuglEEKO9CxuWk35g5TZA+\r\n";



		msg = MsrpParser.parse(input);
		if(msg != null){
			System.out.println(msg.buildContent());
		}

		input = "MSRP PZmJ6PUtmceykDfqHnp0meBYm6O7yI SEND\r\n" +
		"To-Path: msrp://19980039991@66.226.206.165:11276/NjRKmh64U3jfcmh92s7dV9FhU;tcp\r\n" +
		"From-Path: msrp://19980039999@172.17.172.206:8509/7jKpINUgJBUAfrPK1vjO;tcp\r\n" +
		"Message-ID: rp5lmb4n4k7Ki8Hf\r\n" +
		"Byte-Range: 2049-3072/10240\r\n" +
		"Content-Type: foo/bar\r\n" +
		"\r\n" +
		"03\r\n" +
		"0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\r\n" +
		"0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\r\n" +
		"0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\r\n" +
		"0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\r\n" +
		"0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\r\n" +
		"0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\r\n" +
		"0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\r\n" +
		"0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\r\n" +
		"0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\r\n" +
		"0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\r\n" +
		"\r\n" +
		"-------PZmJ6PUtmceykDfqHnp0meBYm6O7yI+\r\n";

		msg = MsrpParser.parse(input);
		if(msg != null){
			System.out.println(new String(msg.getBody()));
			System.out.println("---");

		}


	}
}



