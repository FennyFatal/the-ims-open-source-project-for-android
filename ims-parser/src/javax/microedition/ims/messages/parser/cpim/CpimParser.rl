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

package javax.microedition.ims.messages.parser.cpim;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.messages.wrappers.cpim.CpimMessage;
import javax.microedition.ims.messages.wrappers.cpim.ImUri;

public class CpimParser{

%%{
	machine cpim;
	alphtype char;

	action mark {
		mark = p;
	}

	action reset_uri {
		displayName = null;
	}

	action set_displayname {
		displayName = arrayToString(mark, p);
	}

	action set_subject {
		message.addSubject(lang, arrayToString(mark, p));
	}

	action set_lang {
        lang = arrayToString(mark, p);
    }

    action add_require {
         message.addRequire(arrayToString(mark, p));
    }

	action set_to {
	   ImUri toUri = new ImUri( displayName, arrayToString(mark, p) );
        message.setTo(toUri);
	}

	action set_ns {
       ImUri nsUri = new ImUri( displayName, arrayToString(mark, p) );
       message.setNameSpace(nsUri);
    }

	action add_cc {
       ImUri ccUri = new ImUri( displayName, arrayToString(mark, p) );
       message.addCc(ccUri);
    }

	action set_from {
		ImUri fromUri = new ImUri( displayName, arrayToString(mark, p) );
        message.setFrom(fromUri);
	}

	action set_time {
		message.setTimestampIso8601( arrayToString(mark, p) );
	}

	action set_content {
	    byte[] tmp = new byte[ p - mark ];
        System.arraycopy( data, mark, tmp, 0, p - mark);
		message.setContent(tmp);
	}

	CTL = 0x00..0x1f | 0x7f;
	CR = 0x0d;
	LF = 0x0a;
	CRLF = CR LF;
	ALPHA = 0x41..0x5A | 0x61..0x7A;
	DIGIT = 0x30..0x39;
	HEXDIG = DIGIT | "A"i | "B"i | "C"i | "D"i | "E"i | "F"i;
	DQUOTE = 0x22;
	SP = 0x20;

	UTF8_multi = 0xc0..0xdf 0x80..0xbf |
	             0xe0..0xef 0x80..0xbf 0x80..0xbf |
	             0xf0..0xf7 0x80..0xbf 0x80..0xbf 0x80..0xbf |
	             0xf8..0xfb 0x80..0xbf 0x80..0xbf 0x80..0xbf 0x80..0xbf |
	             0xfc..0xfd 0x80..0xbf 0x80..0xbf 0x80..0xbf 0x80..0xbf 0x80..0xbf;

	UTF8_no_CTL = 0x20..0x7e | UTF8_multi;
	UCS_high = UTF8_multi;
	UCS_no_CTL = UTF8_no_CTL;

	Subtag = (ALPHA | DIGIT){1,8};
	Primary_subtag = ALPHA{1,8};
	Language_tag = Primary_subtag ("-" Subtag)?;

	Escape = "\\" ( ('u'i HEXDIG HEXDIG HEXDIG HEXDIG) |    # UCS codepoint
	               ('b'i) |                                 # Backspace
	               ('t'i) |                                 # Tab
	               ('n'i) |                                 # Linefeed
	               ('r'i) |                                 # Return
	               ( DQUOTE ) |
	               ("'") |
	               ("\\") );
	HEADERCHAR = UCS_no_CTL | Escape;
	NAMECHAR = 0x21 | 0x23..0x27 | 0x2a..0x2b | 0x2d | 0x5e..0x60 | 0x7c | 0x7e | ALPHA | DIGIT;
	TOKENCHAR = NAMECHAR | "." | UCS_high;

	Str_char = 0x20..0x21 | 0x23..0x5b | 0x5d..0x7e | UCS_high;
	String = DQUOTE (Str_char | Escape) DQUOTE;
	Number = DIGIT+;
	Token = TOKENCHAR+;
	Name = NAMECHAR+;

	Header_value = HEADERCHAR*;

	Param_value = Token | Number | String;
	Param_name = Name;
	Ext_param = Param_name "=" Param_value;
	Lang_param = "lang="i Language_tag >mark %set_lang;
	Parameter = Lang_param | Ext_param;

	URI = Header_value -- ">";

	date_fullyear = DIGIT{4};
	date_month = DIGIT DIGIT;
	date_mday = DIGIT DIGIT;
	time_hour = DIGIT DIGIT;
	time_minute = DIGIT DIGIT;
	time_second = DIGIT DIGIT;
	time_secfrac = "." DIGIT+;
	time_numoffset = ("+" | "-") time_hour ":" time_minute;
	time_offset = "Z"i | time_numoffset;
	partial_time = time_hour ":" time_minute ":" time_second (time_secfrac)?;
	full_date = date_fullyear "-" date_month "-" date_mday;
	full_time = partial_time time_offset;
	date_time = full_date "T"i full_time;
	DateTime = "DateTime: "i date_time >mark %set_time;

	Formal_name = (Token SP)+ >mark %set_displayname | String >mark %set_displayname SP;

	Subject = "Subject:"i (";" Lang_param)? SP (HEADERCHAR*) >mark %set_subject;

	Name_prefix = Name >mark %set_displayname;
	Header_name = (Name_prefix ".")? Name;

	MyHeaderName = Header_name - ("From" | "To" | "cc" | "DateTime" | "Subject" | "NS" | "Require");

	Header = MyHeaderName ":" (";" Parameter)* SP Header_value CRLF;

	Require = "Require: "i Header_name >mark %add_require ("," Header_name >mark %add_require)*;

	NS = "NS: "i %reset_uri (Name_prefix)? "<" URI >mark %set_ns ">";

	To = "To: "i %reset_uri (Formal_name)? "<" URI >mark %set_to ">";
	From = "From: "i %reset_uri  (Formal_name)? "<" URI >mark %set_from ">";
	cc = "cc: "i %reset_uri (Formal_name)? "<" URI >mark %add_cc ">";


	Existing_Header = (From |
	                  To |
	                  cc |
	                  DateTime |
	                  Subject |
	                  NS |
	                  Require) CRLF;

	CPIM_Header = Existing_Header | Header;

	Content = any*;

	CPIM_Message := CPIM_Header+ CRLF Content >mark %set_content;
}%%

%%write data;

    protected static String arrayToString(int mark, int p) {

        byte[] tmp = new byte[ p - mark ];
        System.arraycopy( data, mark, tmp, 0, p - mark);
        String result = new String( tmp );
        return result;
    }

    protected volatile static byte[] data;


    public static synchronized CpimMessage parse(final String input) {
        Logger.log("msrp message parsing started. Your input is: \n"+input);

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

    public static synchronized CpimMessage parse(final byte[] input) {

    if(input == null || input.length == 0){
            return null;
            //throw new IllegalArgumentException("Wrong data to parse. input == null || input.length == 0");
        }

        //Logger.log("Message to parse :"+new String(input));
        data = input;

	int p = 0;
        int pe = data.length;
        int eof = pe;
	int mark = 0;
	int  cs = 0;
	CpimMessage message= new CpimMessage();

	String displayName = null, lang = null;

	%%write init;
	%%write exec;

	if (cs == cpim_error) {
		System.out.println("Your input did not comply with the grammar");
            System.out.println("CPIM parsing error at " + p +" "+arrayToString(p, p+5));
            return null;
	} else if (cs < cpim_first_final) {
		System.out.println("CPIM not complete. But parsed");
	} else {
	   System.out.println("Parsed successfully");
	}


    /*
	if (m_To.isNull() || m_To->Prefix().isEmpty() ||
	    m_From.isNull() || m_From->Prefix().isEmpty()) {
		DError << "Unknown prefix perhaps? Could not find To and/or From headers";
		return false;
	}
    */
	   return message;
	}

	  public static void main(String[] args) {
        String s = "From: MR SANDERS <im:piglet@100akerwood.com>\r\n" +
                "To: Depressed Donkey <im:eeyore@100akerwood.com>\r\n" +
                "DateTime: 2000-12-13T13:40:00+08:00\r\n" +
                "Subject: the weather will be fine today\r\n" +
                "Subject:;lang=fr beau temps prevu pour aujourd'hui\r\n" +
                "cc: Winnie the Pooh <im:pooh@100akerwood.com>\r\n" +
                "cc: <im:tigger@100akerwood.com>\r\n"+
                "NS: MyFeatures <mid:MessageFeatures@id.foo.com>\r\n"+
                //"Require: MyFeatures.VitalMessageOption MyFeatures.VitalMessageOption: Confirmation-requested\r\n" +
                "\r\n" +
                "Content-type: text/xml; charset=utf-8\r\n" +
                "Content-ID: <1234567890@foo.com>\r\n" +
                "\r\n" +
                "<body>\r\n" +
                "Here is the text of my message.\r\n" +
                "</body>\r\n";

        CpimMessage m = CpimParser.parse(s);

        if(m != null){
            System.out.println("message: \r\n"+m.toString());
        }

    }
}
