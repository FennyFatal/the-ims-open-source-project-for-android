package javax.microedition.ims.messages.parser.message;

import javax.microedition.ims.common.Algorithm;
import javax.microedition.ims.messages.wrappers.sip.AuthChallenge;
import javax.microedition.ims.messages.wrappers.sip.AuthType;
import javax.microedition.ims.messages.wrappers.sip.AuthenticationChallenge;


public class ChallengeParser {

%%{
	machine auth_challenge;

	action mark {
		mark = p;
	}
	action setnonce {
		aInfo.nextNonce(arrayToString( mark+1 , p - 1 ));
		
		//DCustom(AuthDebug) << "Nonce: " << m_Nonce;
	}
	action setbasic {
		aInfo.type(AuthType.BASIC);
	}
	action setdigest {
		aInfo.type(AuthType.DIGEST);
	}

	action setrealm {
		aInfo.realm(arrayToString( mark+1 , p  - 1 ));
		//DCustom(AuthDebug) << "Realm: " << m_Realm;
	}
	action setopaque {
		aInfo.opaque(arrayToString( mark+1 , p  - 1 ));
		//DCustom(AuthDebug) << "Opaque: " << m_Opaque;
	}
	action setauth {
		//DCustom(AuthDebug) << "Qop includes auth";
		aInfo.qop(AuthenticationChallenge.QOP_AUTH); //TODO finish
	}
	action setauthint {
		//DCustom(AuthDebug) << "Qop includes auth-int";
		aInfo.qop(AuthenticationChallenge.QOP_AUTH_INT); //TODO finish
	}
	action setmd5 {
		//DCustom(AuthDebug) << "Protocol is MD5";
		aInfo.algorithm(Algorithm.MD5);
	}

	action setmd5aka{
		aInfo.algorithm(Algorithm.AKAv1_MD5);//fix 1
	}
	action setmd5sess {
		//DCustom(AuthDebug) << "Protocol is MD5-Sess";
		aInfo.algorithm(Algorithm.MD5Sess);
	}
	action setstaletrue {
		//DCustom(AuthDebug) << "Stale";
		aInfo.stale(true);
	}
	action setstalefalse {
		//DCustom(AuthDebug) << "Not stale";
		aInfo.stale(false);
	}
	action setnc {
		String nc = arrayToString(mark, p );
		aInfo.nonceCount(ParserUtils.toNumber(nc, 0, 16 ));
		//DCustom(AuthDebug) << "Noncecount " << nc;
	}

	# RFC 2616 definitions (HTTP 1.1)
	# On the left the RFC name, on the right the built-in Ragel type
	#

	#OCTET = any;
	CHAR = ascii;
	UPALPHA = upper;
	LOALPHA = lower;
	CTL = cntrl | '\127';
	CR = 0x0D;
	LF = 0x0A;
	SP = 0x20;
	HT = 0x09;
	QT = "\"";
	LHEX = [0-9a-f];

	CRLF = CR LF;
	LWS = (CRLF | LF)? (SP | HT)+;

	# The RFC definition sucks. TEXT is defined as a single char, but somehow is allowed to include
	# LWS (which can be multiple chars), since LWS is usually (in those cases where it is part of a
	# TEXT token) reduced to SP.
	# We work around this.
	TEXT = ^CTL;
	HEX = xdigit;

	seperators = [()<>@,;:\"/?={}] | "[" | "]" | SP | HT;
	token = ^( seperators | CTL )+;

	quoted_pair = '\\' CHAR;

	ctext = TEXT - [()] | LWS;   # Here we compensate for the LWS requirement (place 1)

	# comment is recursive, try to avoid
	# Defined in HTTP grammar but not needed for authentication
	# comment = "(" (ctext | quoted_pair | comment)* ")";
	# 

	qdtext = TEXT - QT | LWS;   # Here we compensate again (place 2)
	quoted_string = QT (qdtext | quoted_pair)* QT;

	# RFC 2617 definitions
	# We support both basic and digest

	auth_scheme = token;
	auth_param = token "="i (token | quoted_string);

	SWS = (SP | HT)+;
	SEP = LWS* "," LWS*;

	realm_value = quoted_string;
	realm = "realm"i SWS? "=" SWS? realm_value >mark %setrealm;

	# We do not parse the URI completely here
	URI = TEXT+; # absoluteURI | abs_path;
	domain = "domain"i SWS? "=" SWS? QT URI ( SP+ URI )* QT;

	nonce_value = quoted_string;
	nonce = "nonce"i SWS? "=" SWS? nonce_value >mark %setnonce;

	opaque = "opaque"i SWS? "=" SWS? quoted_string >mark %setopaque;
	stale = "stale"i SWS? "=" SWS? ("true"i %setstaletrue | "false"i %setstalefalse );

	algorithm = "algorithm"i SWS? "=" SWS? ("md5"i %setmd5 | "md5-sess"i %setmd5sess | "akav1-md5"i %setmd5aka | token);
	qop_value = "auth"i %setauth | "auth-int"i %setauthint | token;
	qop_options = "qop"i SWS? "=" SWS? QT qop_value ( SEP qop_value )* QT;

	digest_option = (realm | domain | nonce | opaque | stale | algorithm | qop_options | auth_param);
	digest_challenge = digest_option ( SEP digest_option )* SWS?;

	challenge = "Basic"i %setbasic LWS realm | "Digest"i %setdigest LWS digest_challenge;

	# Second grammar - Authentication-DInfo

	message_qop = "qop"i SWS? "=" SWS? qop_value;

	nc_value = LHEX{8};
	nonce_count = "nc"i SWS? "=" SWS? nc_value >mark %setnc;

	cnonce_value = nonce_value;
	cnonce = "cnonce"i SWS? "=" SWS? cnonce_value; # >mark %setcnonce;

	nextnonce = "nextnonce"i SWS? "=" SWS? nonce_value >mark %setnonce;
	response_digest = QT LHEX* QT;
	response_auth = "rspauth"i SWS? "=" SWS? response_digest;

	authinfo_option = (nextnonce | message_qop | response_auth | cnonce | nonce_count);
	authinfo = authinfo_option ( SEP authinfo_option )* SWS?;

	# The machines, authentication, and authentication-info

	auth := challenge;
}%%

%% write data;

	protected static byte[] data;
	protected static int cs = 0;                  // Ragel keeps the state in "cs"
	protected static int p = 0;                   // Current index into data is "p"
	protected static int pe = 0;        // Length of data, SO YES data[pe] IS AN INVALID INDEX THIS IS CORRECT!
	protected static int eof = 0; 
	protected static int offset = 0; 
	protected static ParserState state = ParserState.INITIAL;	
	protected static AuthenticationChallenge.Builder aInfo = AuthenticationChallenge.EMPTY_CHALLENGE.asBuilder();
	
	  protected static String arrayToString(int mark, int p) {
		// Java is a verbose pain the ass. Our C++ is much more compact.
		// So, donkey-show to copy the currently marked bytes back into a string,
		// just so we can display what we just parsed. You will have to deal with this
		// if you store everything internally in real unicode strings instead of UTF-8 which
		// is what we usually do. (We convert from TP::Bytes to QString via a UTF-8 conversion)

		byte[] tmp = new byte[ p - mark ];
		System.arraycopy( data, mark, tmp, 0, p - mark);
		String result = new String( tmp );
		return result;
	}

 public static void main(String[] args) {
        consume(null);
    }

	  public static AuthChallenge consume(String input) {
	      /*WWW-Authenticate:*/
    //input = "Digest realm=\"atlanta.com\",domain=\"sip:boxesbybob.com\", qop=\"auth\",nonce=\"f84f1cec41e6cbe5aea9c8e88d359\",opaque=\"\", stale=FALSE, algorithm=MD5";
	try {
            data = input.getBytes("UTF-8");
        } catch (Exception e) {
            System.out.println("Unsupported encoding");
            return null;
        }
        aInfo = AuthenticationChallenge.EMPTY_CHALLENGE.asBuilder();
        System.out.println("Input: "+input);

        // Whenever we see something interesting, remember the current index. Then, when we've decided
        // we really saw something interesting, fetch the part between "mark" and "current value of p"

        int mark = 0;


        // These are for Ragel
		offset = 0; 
        state = ParserState.INITIAL;
        cs = 0;                  // Ragel keeps the state in "cs"
        p = offset;                   // Current index into data is "p"
        pe = data.length;        // Length of data
        eof = pe;

	%% write init;
	aInfo.algorithm(Algorithm.MD5);
	%% write exec;

	if (cs == auth_challenge_error) {
		System.out.println("DError parsing authorization challenge: ");
		System.out.println("The failure occured at position " + p);
		return null;
	} else if (cs < auth_challenge_first_final || pe != p) {
		System.out.println("Authorization challenge parse incomplete");
		return null;
	} else {	                
		System.out.println("Parsed successfully!");
	    System.out.println("Results: "+aInfo);	              
	 }
	return aInfo.build();
}
}

