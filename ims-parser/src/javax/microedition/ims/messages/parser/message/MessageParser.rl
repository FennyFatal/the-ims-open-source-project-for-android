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
import javax.microedition.ims.common.Protocol;
import javax.microedition.ims.messages.parser.ParserUtils;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Header;
import javax.microedition.ims.messages.wrappers.sip.ParamHeader;
import javax.microedition.ims.messages.wrappers.sip.Request;
import javax.microedition.ims.messages.wrappers.sip.Response;
import javax.microedition.ims.messages.wrappers.sip.SipUri;
import javax.microedition.ims.messages.wrappers.sip.UriHeader;
import javax.microedition.ims.messages.wrappers.sip.Via;
import javax.microedition.ims.messages.wrappers.sip.Refresher;



public class MessageParser{

	private static boolean debug = false;

	private static void constraint(Object b, String error) {
		if(b == null){
			Logger.log(Logger.Tag.PARSER,"!Error!: "+error);
		}
		
	}

	
	private static void check(String value) {
		if(!debug) return;
		int dataleft = pe - p;
		Logger.log(Logger.Tag.PARSER,"Parsing: "+value + "       line- " + currentLine + "       dataleft: " + arrayToString( p, p+ (dataleft > 20 ? 20 : dataleft)));
	}
	
%%{
	machine message;
	alphtype char;

	action mark {
		check("mark");
		m_Mark = p;
	}

	action set_temp {
		temp = arrayToString( m_Mark, p);
	}

	action set_cseq {
		check("set_cseq");
		String tmp = arrayToString( m_Mark, p);
		msgBuilder.cSeq(ParserUtils.toNumber(tmp, -1, 10));//base.setCSeq( ParserUtils.toNumber(tmp, -1, 10));
	}

	action prepare_req_uri {
		check("prepare_req_uri");		
		uriBuilder = new SipUri.SipUriBuilder();//uri = new SipUri();
	}

	action undo_urihdr {
		uriHeaderBuilder = null;//uriHeader = null;
		uriBuilder = null;//uri = null;
		msgBuilder.getContactsBuilder().asterisk(true);//base.getContacts().setAsterisk(true);
	}

	action prepare_urihdr {
		check("prepare_urihdr");		
		uriHeaderBuilder = new UriHeader.UriHeaderBuilder();//uriHeader = new UriHeader();
		uriBuilder = new SipUri.SipUriBuilder();//uri = uriHeader.getUri();
	}

	action prepare_urihdr_route {
		check("prepare_urihdr_route");		
		uriHeaderBuilder = new UriHeader.UriHeaderBuilder();//uriHeader = new UriHeader();
		uriBuilder = new SipUri.SipUriBuilder();//uri = uriHeader.getUri();
	}


	action done_req_uri {
		check("done_req_uri");		
		msgBuilder.requestUri(uriBuilder.buildUri());//request.setRequestUri(uri);
		uriBuilder = null;//uri = null;
	}

	action set_to {
		check("set_to");		
		msgBuilder.to(uriHeaderBuilder.uriBuilder(uriBuilder));//base.setTo(uriHeader);
		uriBuilder = null;//uri = null;
		uriBuilder = null;//uri = null;
	}

	action set_from {
		check("set_from");		
		msgBuilder.from(uriHeaderBuilder.uriBuilder(uriBuilder));//base.setFrom(uriHeader);
		uriBuilder = null;//uri = null;
		uriBuilder = null;//uri = null;
	}

	action add_contact {
		check("add_contact");		
		msgBuilder.getContactsBuilder().contact(uriHeaderBuilder.uriBuilder(uriBuilder).build());//base.getContacts().add(uriHeader);
		uriBuilder = null;//uri = null;
		uriBuilder = null;//uri = null;
	}

	action add_route {
		check("add_route");		
		msgBuilder.route(uriHeaderBuilder.uriBuilder(uriBuilder).build());//base.getRoutes().add(uriHeader);
		uriBuilder = null;//uri = null;
		uriBuilder = null;//uri = null;
	}

	action add_pau {
		check("add_p_associated_uri");		
		msgBuilder.pAssociatedUri(uriHeaderBuilder.uriBuilder(uriBuilder).build());//base.getpAssociatedUris().add(uriHeader);
		uriBuilder = null;//uri = null;
		uriBuilder = null;//uri = null;
	}

	action add_pai {
		check("add_p_asserted_identity");		
		msgBuilder.assertedIdentity(uriHeaderBuilder.uriBuilder(uriBuilder).build());//base.getpAssertedIdentities().add(uriHeader);
		uriBuilder = null;//uri = null;
		uriBuilder = null;//uri = null;
	}

	
	action add_serviceroute {
		check("add_serviceroute");		
		msgBuilder.serviceRoute(uriHeaderBuilder.uriBuilder(uriBuilder).build());//base.getServiceRoutes().add(uriHeader);
		uriBuilder = null;//uri = null;
		uriBuilder = null;//uri = null;
	}
	


	action add_recroute {
		check("add_recroute");		
		msgBuilder.recordRoute(uriHeaderBuilder.uriBuilder(uriBuilder).build());//base.getRecordRoutes().add(uriHeader);
		uriBuilder = null;//uri = null;
		uriBuilder = null;//uri = null;
	}

	action set_referred_by {
		check("set_referred_by");
		msgBuilder.referredBy(uriHeaderBuilder);//base.setReferredBy( uriHeader );
		uriBuilder = null;//uri = null;
		uriBuilder = null;//uri = null;
	}

	action set_referredby_cid {		
		uriHeaderBuilder.param("cid", arrayToString( m_Mark, p));//uriHeader.getParamsList().set("cid", arrayToString( m_Mark, p) );
	}

	action add_history_info {
		check("add_history_info");		
		msgBuilder.historyInfo(uriHeaderBuilder.uriBuilder(uriBuilder).build());//base.getHistoryInfo().add(uriHeader);
		uriBuilder = null;//uri = null;
		uriBuilder = null;//uri = null;
	}

	action set_content_length {
		check("set_content_length");
		String tmp = arrayToString( m_Mark, p);
		contentLength = ParserUtils.toNumber(tmp, -1, 10);
		msgBuilder.contentLength(contentLength);//base.setContentLength(contentLength);
	}
	
	
	action set_max_forwards {
		check("set_max_forwards");
		String tmp = arrayToString( m_Mark, p);
		int res = ParserUtils.toNumber(tmp, -1, 10);
		constraint( res >= 0, "Illegal Max-Forwards value" );
		msgBuilder.maxForwards( res );//base.setMaxForwards( res );
	}

	action set_method {
		check("set_method");
		/* Create a request */		
		//request = new Request();
		msgBuilder.msgType(BaseSipMessage.Builder.Type.REQUEST);//base = request;
		msgBuilder.method( arrayToString( m_Mark, p) );//request.setMethod( arrayToString( m_Mark, p) );
	}

	action cseq_method {
		check("cseq_method");
		String m = arrayToString( m_Mark, p);
		if (msgBuilder.getMethod() != null) {
			constraint( msgBuilder.getMethod() == m, "Request-URI and CSeq methods do not match" );
		} else {
			//response.setMethod( m );

            msgBuilder.method(m);
		}
		/*
		if (request != null) {
			constraint( request.getMethod() == m, "Request-URI and CSeq methods do not match" );
		} else {
			response.setMethod( m );
		}
		*/
	}

	action create_response {
		check("create_response");
		/* Create a response */		
		//response = new Response();
		msgBuilder.msgType(BaseSipMessage.Builder.Type.RESPONSE);//base = response;
	}
			

	action set_sip {
		check("set_sip");		
		uriBuilder.prefix("sip");//uri.setPrefix("sip");
	}
	action set_sips {
		check("set_sips");		
		uriBuilder.prefix("sips");//uri.setPrefix("sips");
	}
	action set_tel {
		check("set_tel");		
		uriBuilder.prefix("tel");//uri.setPrefix("tel");
	}

	# Do not decode
	action set_scheme {
		check("set_scheme");		
		uriBuilder.prefix( arrayToString( m_Mark, p) );//uri.setPrefix( arrayToString( m_Mark, p) );
	}

	action set_dn {
		check("set_dn");		
		uriBuilder.displayName( ParserUtils.decodeDisplayName( arrayToString( m_Mark, p+1)));//uri.setDisplayName( ParserUtils.decodeDisplayName( arrayToString( m_Mark, p+1)));
	}
	action set_dn_quoted {		
		uriBuilder.displayName( ParserUtils.decodeDisplayName( ParserUtils.decodeDisplayName( arrayToString( m_Mark + 1, p) )));//uri.setDisplayName( ParserUtils.decodeDisplayName( arrayToString( m_Mark + 1, p) ));
	}

	action set_callid {
		check("set_callid");		
		msgBuilder.callId( arrayToString( m_Mark, p) );//base.setCallId( arrayToString( m_Mark, p) );
	}

	action add_allow {
		check("add_allow");		
		msgBuilder.allow( arrayToString( m_Mark, p) );//base.getAllow().add( arrayToString( m_Mark, p) );
	}

	action add_require {
		check("add_require");		
		msgBuilder.require( arrayToString( m_Mark, p) );//base.getRequire().add( arrayToString( m_Mark, p) );
	}

	action add_supported {
		check("add_supported");		
		msgBuilder.supported( arrayToString( m_Mark, p) );//base.getSupported().add( arrayToString( m_Mark, p) );
	}

	action add_priv {
		check("add_priv");		
		msgBuilder.privacy( arrayToString( m_Mark, p) );//base.getPrivacy().add( arrayToString( m_Mark, p) );
	}

	# Needs to be decoded
	action set_username {
		check("set_username");		
		//uri.setUsername( ParserUtils.urlDecode( arrayToString( m_Mark, p) ));
		//Stack client code must perform encode/decode operations.
		//If decode operation made at Ragel level some stack code may become faulty.
		//beacuse this url is not allowed in rfc3261 grammar. In stack internals we must use encoded uri's
		uriBuilder.username(  arrayToString( m_Mark, p) );//uri.setUsername(  arrayToString( m_Mark, p) );
	}

	action copy_temp_to_username {
		//uri.setUsername( ParserUtils.urlDecode( temp ) );
		uriBuilder.username( temp );//uri.setUsername( temp );
	}
	
	action set_sosuri_to_username {		
		uriBuilder.username( temp );//uri.setUsername( temp );
	}

	# Needs to be decoded
	action set_pw {
		check("set_pw");		
		uriBuilder.password( ParserUtils.urlDecode( arrayToString( m_Mark, p) ));//uri.setPassword( ParserUtils.urlDecode( arrayToString( m_Mark, p) ));
	}

	# Should not be decoded
	action set_domain {
		check("set_domain");		
		uriBuilder.domain( arrayToString( m_Mark, p) );//uri.setDomain( arrayToString( m_Mark, p) );
	}

	# Should not be decoded
	action set_tag {
		check("set_tag");		
		uriHeaderBuilder.tag( arrayToString( m_Mark, p) );//uriHeader.setTag( arrayToString( m_Mark, p) );
	}

	action set_etag {
		check("set_etag");		
		msgBuilder.eTag( arrayToString( m_Mark, p) );//base.setETag( arrayToString( m_Mark, p) );
	}

	action set_ifmatch {
		check("set_ifmatch");		
		msgBuilder.ifMatch( arrayToString( m_Mark, p) );//base.setIfMatch( arrayToString( m_Mark, p) );
	}

	action set_status_code {
		check("set_status_code");		
		int code = ParserUtils.toNumber(arrayToString( m_Mark, p), 01, 10);
		constraint( code > 99 && code < 1000, "Illegal status code" );
		msgBuilder.statusCode( code );//response.setStatusCode( code );
	}

	action set_expires {		
		msgBuilder.expires( ParserUtils.toNumber(arrayToString( m_Mark, p), 01, 10) );//base.setExpires( ParserUtils.toNumber(arrayToString( m_Mark, p), 01, 10) );
	}

	action set_minexpires {		
		msgBuilder.minExpires( ParserUtils.toNumber(arrayToString( m_Mark, p), 01, 10) );//base.setMinExpires( ParserUtils.toNumber(arrayToString( m_Mark, p), 01, 10) );
	}

	# Needs to be decoded
	action set_reason_phrase {
		check("set_reason_phrase");		
		msgBuilder.reasonPhrase( ParserUtils.urlDecode( arrayToString( m_Mark, p) ) );//response.setReasonPhrase( ParserUtils.urlDecode( arrayToString( m_Mark, p) ) );
	}

	action set_port {
		check("set_port");		
		uriBuilder.port( ParserUtils.toNumber(arrayToString( m_Mark, p), 01, 10) );//uri.setPort( ParserUtils.toNumber(arrayToString( m_Mark, p), 01, 10) );
	}

	action set_se_uas {
		check("set_se_uas");		
		msgBuilder.getSessionExpiresBuilder().refresher(Refresher.UAS);//base.getSessionExpires().setRefresher(Refresher.UAS);
	}

	action set_se_uac {
		check("set_se_uac");		
		msgBuilder.getSessionExpiresBuilder().refresher(Refresher.UAC);//base.getSessionExpires().setRefresher(Refresher.UAC);
	}

	action set_se_seconds {
		check("set_se_seconds");		
		msgBuilder.getSessionExpiresBuilder().expiresValue(ParserUtils.toNumber(arrayToString( m_Mark, p), 01, 10));//base.getSessionExpires().setExpiresValue( ParserUtils.toNumber(arrayToString( m_Mark, p), 01, 10) );
	}

	action add_se_param {
		check("add_se_param");
		if (curHValName.toLowerCase() != "refresher") {
			if (curHValValue == null || curHValValue.length() == 0){
				//base.getSessionExpires().getParamsList().set( curHValName );

                msgBuilder.getSessionExpiresBuilder().param(curHValName);
            }
			else{
				//base.getSessionExpires().getParamsList().set( curHValName, curHValValue );

                msgBuilder.getSessionExpiresBuilder().param(curHValName, curHValValue);
            }
		}
				/*
		if (curHValName.toLowerCase() != "refresher") {
			if (curHValValue == null || curHValValue.length() == 0)
				base.getSessionExpires().getParamsList().set( curHValName );
			else
				base.getSessionExpires().getParamsList().set( curHValName, curHValValue );
		}
		*/
	}

	action add_accept_contact_param {
	    if (curHValValue == null || curHValValue.length() == 0){
			//paramHeader.getParamsList().set( curHValName );

            paramHeaderBuilder.param(curHValName);
        }
		else{
			//paramHeader.getParamsList().set( curHValName, curHValValue );

            paramHeaderBuilder.param( curHValName, curHValValue );
        }

		/*
		if (curHValValue == null || curHValValue.length() == 0)
			paramHeader.getParamsList().set( curHValName );
		else
			paramHeader.getParamsList().set( curHValName, curHValValue );
			*/
	}

	action add_reject_contact_param {
		check("add_reject_contact_param");
		if (curHValValue.length() == 0){
			//paramHeader.getParamsList().set( curHValName );

            paramHeaderBuilder.param(curHValName);
        }
		else{
			//paramHeader.getParamsList().set( curHValName, curHValValue );

            paramHeaderBuilder.param( curHValName, curHValValue );
        }
		/*
		if (curHValValue.length() == 0)
			paramHeader.getParamsList().set( curHValName );
		else
			paramHeader.getParamsList().set( curHValName, curHValValue );
			*/
	}


	# Needs to be decoded
	action set_create_uriheader_name {
		check("set_create_uriheader_name");		
		curParam = ParserUtils.urlDecode( arrayToString( m_Mark, p) );
		uriBuilder.param(curParam);//uri.getHeaders().set( curParam );
	}

	# Needs to be decoded
	action set_uriheader {
		check("set_uriheader");		
		String tmp = ParserUtils.urlDecode( arrayToString( m_Mark, p) );
		uriBuilder.param( curParam, tmp );//uri.getHeaders().set( curParam, tmp );
	}

	# URI parameter value, needs to be decoded
	action set_create_param_name {
		check("set_create_param_name");		
		curParam = ParserUtils.urlDecode( arrayToString( m_Mark, p) );
		uriBuilder.param( curParam );//uri.getParamsList().set( curParam );
	}

	# URI parameter value, needs to be decoded
	action set_param_value {
		check("set_param_value");		
		String tmp = ParserUtils.urlDecode( arrayToString( m_Mark, p)) ;
		uriBuilder.param( curParam, tmp );//uri.getParamsList().set( curParam, tmp );
	}

	# Should NOT be decoded
	action set_create_hvalname {
		check("set_create_hvalname");
		curHValName = arrayToString( m_Mark, p);//curHValName = arrayToString( m_Mark, p);
	}

	# Should NOT be decoded
	action set_hvalvalue {
		check("set_hvalvalue");
		curHValValue = arrayToString( m_Mark, p);//curHValValue = arrayToString( m_Mark, p);
	}

	# Should NOT be decoded
	action set_unquote_hvalvalue {
		check("set_unquote_hvalvalue");
		curHValValue = ParserUtils.decodeDisplayName( arrayToString( m_Mark + 1, p - 1) );
	}


	action set_substate_param {
	    if (curHValValue.length() == 0){
			//base.getSubscriptionState().getParamsList().set( curHValName );

            msgBuilder.getSubscriptionStateBuilder().param(curHValName);
        }
		else{
			//base.getSubscriptionState().getParamsList().set( curHValName, curHValValue );

            msgBuilder.getSubscriptionStateBuilder().param(curHValName, curHValValue);
        }
	/*
		if (curHValValue.length() == 0)
			base.getSubscriptionState().getParamsList().set( curHValName );
		else
			base.getSubscriptionState().getParamsList().set( curHValName, curHValValue );
			*/
	}

	action set_event_param {
	    if (curHValValue.length() == 0){
			//base.getEvent().getParamsList().set( curHValName );

            msgBuilder.getEventBuilder().param(curHValName);
        }
		else{
			//base.getEvent().getParamsList().set( curHValName, curHValValue );

            msgBuilder.getEventBuilder().param(curHValName, curHValValue);
        }
	/*
		if (curHValValue.length() == 0)
			base.getEvent().getParamsList().set( curHValName );
		else
			base.getEvent().getParamsList().set( curHValName, curHValValue );
			*/
	}

	# Should NOT be decoded
	action set_ctype_value {
		curHValValue = arrayToString( m_Mark, p);
		msgBuilder.getContentTypeBuilder().param(curHValName);//base.getContentType().getParamsList().set( curHValName );
		msgBuilder.getContentTypeBuilder().param(curHValName, curHValValue);//base.getContentType().getParamsList().set( curHValName, curHValValue );
	}

	action set_ctype_value_quoted {
		curHValValue = ParserUtils.decodeDisplayName( arrayToString( m_Mark + 1, p - 1) );
		msgBuilder.getContentTypeBuilder().param(curHValName);//base.getContentType().getParamsList().set( curHValName );
		msgBuilder.getContentTypeBuilder().param(curHValName, curHValValue);//base.getContentType().getParamsList().set( curHValName, curHValValue );
	}

	action set_subject {
		check("set_subject");			
		msgBuilder.subject( arrayToString( m_Mark, p) );//base.setSubject( arrayToString( m_Mark, p) );
	}

	action set_useragent {
		check("set_useragent");
		//if(request != null)
			msgBuilder.userAgent( arrayToString( m_Mark, p) );//request.setUserAgent( arrayToString( m_Mark, p) );
	}
	action set_server {
		check("set_server");		
		msgBuilder.server( arrayToString( m_Mark, p) );//base.setServer( arrayToString( m_Mark, p) );
	}
	action set_event_type {
		check("set_event_type");		
		msgBuilder.event( arrayToString( m_Mark, p) );//base.getEvent().setValue( arrayToString( m_Mark, p) );
	}
	action add_event_type {
		check("add_event_type");		
		msgBuilder.allowEvents( arrayToString( m_Mark, p) );//base.getAllowEvents().add( arrayToString( m_Mark, p) );
	}
	action set_ctype {				
		msgBuilder.contentType( arrayToString( m_Mark, p) );//base.getContentType().setValue( arrayToString( m_Mark, p) );
	}


	action set_substate_value {
		check("set_substate_value");		
		msgBuilder.subscriptionState( arrayToString( m_Mark, p) );//base.getSubscriptionState().setValue( arrayToString( m_Mark, p) );
	}

	# Do not decode
	action set_create_pname {
		check("set_create_pname");		
		curParam = arrayToString( m_Mark, p);
		uriHeaderBuilder.param( curParam );//uriHeader.getParamsList().set( curParam );
	}

	# Do not decode
	action set_pvalue {
		check("set_pvalue");		
		uriHeaderBuilder.param( curParam, arrayToString( m_Mark, p) );//uriHeader.getParamsList().set( curParam, arrayToString( m_Mark, p) );
	}

	action set_historyinfo_index {
		check("set_historyinfo_index");		
		uriHeaderBuilder.param("index", arrayToString( m_Mark, p) );//uriHeader.getParamsList().set("index", arrayToString( m_Mark, p) );
	}

	action init_via {
		check("init_via");		
		viaBuilder = new Via.Builder();//via = new Via();
		String transport = arrayToString( m_Mark, p);
		transport = transport.toUpperCase();
		if (transport.equals(Protocol.UDP.toString())) {
			viaBuilder.protocol(Protocol.UDP);//via.setProtocol(Protocol.UDP);
		} if (transport.equals(Protocol.TCP.toString())) {
			viaBuilder.protocol(Protocol.TCP);//via.setProtocol(Protocol.TCP);
		} if (transport.equals(Protocol.TLS.toString())) {
			viaBuilder.protocol(Protocol.TLS);//via.setProtocol(Protocol.TLS);
		} else {
			constraint( false, "Illegal Transport value in Via header" );
		}
		uriHeaderBuilder = viaBuilder;//uriHeader = via;
		uriBuilder = uriHeaderBuilder.getUriBuilder();// uri = uriHeader.getUri();
	}

	action prepare_ac {
		check("prepare_ac");		
		paramHeaderBuilder = new ParamHeader.ParamHeaderBuilder();//paramHeader = new ParamHeader();
		paramHeaderBuilder.value("*");//paramHeader.setValue("*");
	}

	action prepare_rc {
		check("prepare_rc");		
		paramHeaderBuilder = new ParamHeader.ParamHeaderBuilder();//paramHeader = new ParamHeader();
		paramHeaderBuilder.value("*");//paramHeader.setValue("*");
	}

	action add_ac {
		check("add_ac");		//base.getAcceptContact().append( paramHeader );
		msgBuilder.customHeader(Header.AcceptContact, paramHeaderBuilder.build().buildContent());//base.addCustomHeader(Header.AcceptContact, paramHeader.buildContent());

	}

	action add_rc {
		check("add_rc");		
		msgBuilder.rejectContact(paramHeaderBuilder.getParamsList());//base.getRejectContact().append( paramHeader );
	}

	action set_via_final {
		check("set_via_final");		
		msgBuilder.via(viaBuilder);//base.getVias().add(via);
		viaBuilder = null;//via = null;
		uriBuilder = null;//uri = null;
		uriBuilder = null;//uri = null;
	}

	action custom_header_name {
		check("custom_header_name");
		customHeaderName = arrayToString( m_Mark, p);
	}

	action custom_header {
		check("custom_header");
		msgBuilder.customHeader( customHeaderName, arrayToString( m_Mark, p));//base.addCustomHeader( customHeaderName, arrayToString( m_Mark, p));
	}
		

	action newline {
		currentLine++;
		startOfCurrentLine = p;
	}

	action do_finish {
		fbreak;
	}

	action skip_body {
		DError << "Loading body of size " << contentLength;
		char * st = p + 1;
		u32 left = pe - st;
		if (contentLength <= left) {
			if (contentLength > 0)
				base.setBody(dataCopy( st, st + contentLength));

			if (st + contentLength <= pe)
				fexec st + m_ContentLength;
			else
				fbreak;
			DError << "Succesfully parsed a message";
			fgoto main;
		} else {
			DError << "In the middle of a body";
		}
	}

	# ABNF core rules direct from RFC 2234

	ALPHA = alpha;
	BIT = "0" | "1";
	CHAR = ascii;
	CR = 0x0d;
	LF = 0x0a;
	CRLF = (CR LF | LF) >newline; # Also accept unix line endings
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

	UTF8_CONT = 0x80..0xBF;
	UTF8_NONASCII = 0xC0..0xDF UTF8_CONT{1} |
	                0xE0..0xEF UTF8_CONT{2} |
	                0xF0..0xF7 UTF8_CONT{3} |
	                0xF8..0xFB UTF8_CONT{4} |
	                0xFC..0xFD UTF8_CONT{5};

	TEXT_UTF8_CHAR = 0x21..0x7E | UTF8_NONASCII;
	TEXT_UTF8_TRIM = TEXT_UTF8_CHAR+ (LWS* TEXT_UTF8_CHAR )*;

	LHEX = DIGIT | [a-f];

	token = (alphanum | [\-.!%*_+'`~])+;
	seperators = [()<>@,;:/?={}\]\[\\] | DQUOTE | SP | HTAB;
	word = (alphanum | [\-.!%*_+'`~()<>:/?{}\[\]\\] | DQUOTE)+;

	STAR = SWS "*" SWS;
	SLASH = SWS "/" SWS;
	EQUAL = SWS "=" SWS;
	LPAREN = SWS "(" SWS;
	RPAREN = SWS ")" SWS;
	RAQUOT = ">" SWS;
	LAQUOT = SWS "<";
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
	hostport = host >mark %(set_domain) (":" port >mark %set_port )?;

	gen_value = token | host | quoted_string;
	generic_param = token >mark %set_create_pname (EQUAL gen_value >mark %set_pvalue)?;
	generic_uri_param = token >mark %set_create_param_name (EQUAL gen_value >mark %set_param_value)?;
	protocol_version = token;
	other_transport = token;

	delta_seconds = DIGIT+;

	hnv_unreserved = [\[\]/?:+$];
	hname = (hnv_unreserved | unreserved | escaped)+;
	hvalue = (hnv_unreserved | unreserved | escaped)*;
	header = hname >mark %set_create_uriheader_name "=" hvalue >mark %set_uriheader;
	headers = "?" header ("&" header)*;

	param_unreserved = [\[\]/:&+$];
	paramchar = param_unreserved | unreserved | escaped;
	pname = paramchar+;
	pvalue = paramchar+;
	other_param = pname >mark %set_create_param_name ("=" pvalue >mark %set_param_value)?;
#	maddr_param = "maddr=" host;
#	method_param = "method="i Method;
#	lr_param = "lr"i;
#	ttl_param = "ttl="i ttl;
#	other_user = token;
#	user_param = "user="i other_user; #("phone"i | "ip"i | other_user);
	transport_param = "transport="i other_transport; #("udp"i | "tcp"i | "sctp"i | "tls"i | other_transport);
	uri_parameter = other_param; #transport_param | user_param | method_param | ttl_param |
	                             #maddr_param | lr_param | other_param;
	uri_parameters = (";" uri_parameter)*;
	password = (unreserved | escaped | [&=+$,])*;
	user_unreserved = [&=+$,;?/];
	user = (unreserved | escaped | user_unreserved )+;
	# userinfo = (user | telephone_subscriber) (":" password)? "@";
	userinfo = user >mark %set_temp (":" password >mark %set_pw)? "@" >copy_temp_to_username;
	SIP_URI = "sip:"i %set_sip userinfo? hostport; 
	SIPS_URI = "sips:"i  %set_sips userinfo? hostport; 
	SIP_URI_wparams = "sip:"i %set_sip userinfo? hostport uri_parameters headers?; 
	SIPS_URI_wparams = "sips:"i  %set_sips userinfo? hostport uri_parameters headers?; 

	visual_separator = "-" | "." | "(" | ")";
	phonedigit_hex = HEXDIG | "*" | "#" | visual_separator;
	phonedigit = DIGIT | visual_separator;
	local_number_digits = phonedigit_hex * (HEXDIG | "*" | "#") phonedigit_hex*;
	global_number_digits = "+" phonedigit* DIGIT phonedigit*;
	par = ";" other_param;
	local_number = local_number_digits >mark %set_username par*;
	global_number = global_number_digits >mark %set_username par*;
	telephone_subscriber = global_number | local_number;


	TEL_URI = "tel:"i %set_tel telephone_subscriber;
	TEL_URI_wparams = "tel:"i %set_tel telephone_subscriber uri_parameters;


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
	scheme = ALPHA (ALPHA | DIGIT | [+-.])*;
	absolute_URI = scheme >mark %set_scheme ":" (hier_part | opaque_part);
	SOS_URI =  "urn:service:sos" >mark %set_temp %set_sosuri_to_username;

	display_name = (token LWS)+ >mark @set_dn | quoted_string >mark @set_dn_quoted;
	addr_spec = SIP_URI | SIPS_URI | TEL_URI | SOS_URI; # | absolute_URI ;
	addr_spec_wparams = SIP_URI_wparams | SIPS_URI_wparams | TEL_URI_wparams  |SOS_URI; #| absolute_URI;
	name_addr = display_name? LAQUOT addr_spec_wparams RAQUOT;

	# Call_ID
	callid = word ( "@" word )?;
	Call_ID = ("Call-ID"i | "i"i) HCOLON :> callid >mark %set_callid;

	# Content-Encoding
	content_coding = token;
	Content_Encoding = ("Content-Encoding"i | "e"i) HCOLON :> content_coding (COMMA content_coding)*;

	# Content-Length
	Content_Length = ("Content-Length"i | "l"i) HCOLON :> DIGIT+ >mark %set_content_length;



	# RFC 3840
	base_tags = "audio"i | "automata"i | "class"i | "duplex"i | "data"i | "control"i | "mobility"i |
	            "description"i | "events"i | "priority"i | "methods"i | "schemes"i | "application"i |
	            "video"i | "language"i | "type"i | "isfocus"i | "actor"i | "text"i | "extensions"i;
	ftag_name = ALPHA (ALPHA | DIGIT | "!" | "'" | "." | "-" | "%")*;
	other_tags = "+" ftag_name;
	enc_feature_tag = base_tags | other_tags;
	
	token_nobang = (alphanum | "-" | "." | "%" | "*" | "_" | "+" | "`" | "'" | "~")+;
	boolean = "TRUE"i | "FALSE"i;  # NOT CASE SENSITIVE EVEN THOUGH RFC MAKES IT LOOK LIKE IT
	number = ("+" | "-")? DIGIT+ ("." DIGIT*)?;
	qdtext_no_abkt = LWS | 0x21 | 0x23..0x3B | 0x3D | 0x3F..0x5B | 0x5D..0x7E | UTF8_NONASCII;
	string_value = "<" (qdtext_no_abkt | quoted_pair) ">";
	numeric_relation = ">=" | "<=" | "=" | (number ":");
	numeric = "#" numeric_relation number;
	tag_value = "!"? (token_nobang | boolean | numeric);

	# Same as generic param, but not part of a URI

	# Note that the tag-value-list uses an actual comma instead of the
	# COMMA construction because it appears within a quoted string, where
	# line folding cannot take place.

	tag_value_list = tag_value ("," tag_value);
	feature_param = enc_feature_tag >mark %set_create_hvalname (EQUAL (LDQUOT (tag_value_list | string_value) RDQUOT) >mark %set_hvalvalue)?;

	req_param = "require"i >mark %set_create_hvalname;
	explicit_param = "explicit"i >mark %set_create_hvalname;

	generic_param_sep = token >mark %set_create_hvalname (EQUAL gen_value >mark %set_hvalvalue)?;


	rc_params = generic_param_sep;   # feature_param | generic_param_sep;
	ac_params = generic_param_sep;   # feature_param | req_param | explicit_param | generic_param_sep;

	ac_value = STAR (SEMI ac_params %add_accept_contact_param)*;
	rc_value = STAR (SEMI rc_params %add_reject_contact_param)*;

	Accept_Contact = ("Accept-Contact"i | "a"i) HCOLON ac_value >prepare_ac %add_ac
		(COMMA ac_value >prepare_ac %add_ac)*;
	Reject_Contact = ("Reject-Contact"i | "j"i) HCOLON rc_value >prepare_rc %add_rc 
		(COMMA rc_value >prepare_rc %add_rc)*;



	# Contact
	contact_extension = generic_param;
	qvalue = ("0" ("." DIGIT{0,3})?) | ("1" ("." "0"{0,3})?);
	c_p_q = "q"i EQUAL qvalue;
	c_p_q_expires = "expires"i EQUAL delta_seconds;
	contact_params = c_p_q | c_p_q_expires | contact_extension;
	contact_param = (name_addr | addr_spec) (SEMI contact_params)*;
	Contact = ("Contact"i | "m"i) HCOLON <: (STAR %undo_urihdr | (contact_param >prepare_urihdr %add_contact (COMMA <: contact_param >prepare_urihdr %add_contact )*));




	priv_value = "header"i | "session"i | "user"i | "none"i | "critical"i | token;
	Privacy = "Privacy"i HCOLON priv_value >mark %add_priv (SEMI priv_value >mark %add_priv )*;




	# Content-Type
	iana_token = token;
	ietf_token = token;
	x_token = "x-" token;
	extension_token = ietf_token | x_token;
	m_subtype = extension_token | iana_token;
	composite_type = extension_token; # "message"i | "multipart"i | extension_token;
	m_attribute = token;
	m_value = token >mark %set_ctype_value | quoted_string >mark %set_ctype_value_quoted;
	m_parameter = m_attribute >mark %set_create_hvalname EQUAL m_value;
	discrete_type = extension_token; #"text"i | "image"i | "audio"i | "video"i | "application"i | extension_token;
	m_type = discrete_type | composite_type;
	media_type = (m_type SLASH m_subtype) >mark %set_ctype (SEMI m_parameter)*;
	Content_Type = ("Content-Type"i | "c"i) HCOLON :> media_type;

	# CSeq
	CSeq = "CSeq"i HCOLON :> DIGIT+ >mark %set_cseq LWS Method >mark %cseq_method;

	# Date
	time = DIGIT{2} ":" DIGIT{2} ":" DIGIT{2};
	month = "Jan"i | "Feb"i | "Mar"i | "Apr"i | "May"i | "Jun"i | "Jul"i |
	        "Aug"i | "Sep"i | "Oct"i | "Nov"i | "Dec"i;
	date1 = DIGIT{2} SP month SP DIGIT{4};
	wkday = "Mon"i | "Tue"i | "Wed"i | "Thu"i | "Fri"i | "Sat"i | "Sun"i;
	rfc1123_date = wkday "," SP date1 SP time SP "GMT"i;
	SIP_date = rfc1123_date;
	Date = "Date"i HCOLON :> SIP_date;

	# Expires
	Expires = "Expires"i HCOLON :> delta_seconds >mark %set_expires;

	Min_Expires = "Min-Expires"i HCOLON :> delta_seconds >mark %set_minexpires;

	event_reason_value = token;

	hval_param = token >mark %set_create_hvalname (EQUAL gen_value >mark %set_hvalvalue)?;
	subexp_params = ("reason"i >mark %set_create_hvalname EQUAL
	                           event_reason_value >mark %set_hvalvalue) |
	                ("expires"i >mark %set_create_hvalname EQUAL
	                           delta_seconds >mark %set_hvalvalue) |
	                ("retry-after"i >mark %set_create_hvalname EQUAL 
	                           delta_seconds >mark %set_hvalvalue) |
	                hval_param;
	substate_value = token;

	Subscription_State = "Subscription-State"i HCOLON :> substate_value >mark %set_substate_value (SEMI :> subexp_params >mark %set_substate_param)*;


	event_param = hval_param >mark %set_event_param; # | ("id"i EQUAL token); // automatic
	token_nodot = (alphanum | "-" | "!" | "%" | "*" | "_" | "+" | "`" | "'" | "~")+;
	event_template = token_nodot;
	event_package = token_nodot;
	event_type = event_package ("." event_template)*;

	Event = ("Event"i | "o"i) HCOLON :> event_type >mark %set_event_type (SEMI event_param)*;

	Allow_Events = ("Allow-Events"i | "u"i) HCOLON :> event_type >mark %add_event_type (COMMA event_type >mark %add_event_type)*;


	# Allow
	Allow = "Allow"i HCOLON (Method >mark %add_allow (COMMA Method >mark %add_allow )*)?;

	# From
	tag_param = "tag"i EQUAL token >mark %set_tag;
	from_param = tag_param | generic_param;
	from_spec = ( name_addr | addr_spec ) (SEMI from_param)*;
	From = ("From"i | "f"i) HCOLON <: from_spec >prepare_urihdr %set_from;

	# Max-Forwards
	Max_Forwards = "Max-Forwards"i HCOLON :> DIGIT+ >mark %set_max_forwards;

	# Record-Route
	rr_param = generic_param;
	rec_route = name_addr (SEMI rr_param)*;
	Record_Route = "Record-Route"i HCOLON <: rec_route >prepare_urihdr %add_recroute (COMMA :> rec_route >prepare_urihdr %add_recroute)*;

	# History-Info

	hi_extension = generic_param;
	hi_index = "index"i EQUAL DIGIT+ >mark ("." DIGIT+)* %set_historyinfo_index;
	hi_param = hi_index | hi_extension;
	hi_targeted_to_uri = name_addr;
	hi_entry = hi_targeted_to_uri >prepare_urihdr (SEMI hi_param)* %add_history_info;
	HistoryInfo = "History-Info"i HCOLON :> hi_entry (COMMA hi_entry)*;


	# Referred-By
	atom = (alphanum | "-" | "!" | "%" | "*" | "_" | "+" | "'" | "'" | "~")+;
	dot_atom = atom ("." atom)*;
	sip_clean_msg_id = LDQUOT dot_atom "@" (dot_atom | host) RDQUOT;
	referredby_id_param = "cid"i EQUAL sip_clean_msg_id >mark %set_referredby_cid;
	referrer_uri = name_addr | addr_spec;
	refby_hdr = referrer_uri (SEMI (referredby_id_param | generic_param))*;
	Referred_By = ("Referred-By"i | "b"i) HCOLON <: refby_hdr >prepare_urihdr %set_referred_by;

	# Require
	option_tag = token;
	Require = "Require"i HCOLON :> option_tag >mark %add_require (COMMA option_tag >mark %add_require)*;

	# Route
	route_param = name_addr (SEMI rr_param)*;
	Route = "Route"i HCOLON <: route_param >prepare_urihdr %add_route (COMMA :> route_param >prepare_urihdr %add_route)*;

	sr_value = name_addr (SEMI rr_param)*;
	Service_Route = "Service-Route"i HCOLON :> sr_value >prepare_urihdr %add_serviceroute
		(COMMA :> sr_value >prepare_urihdr %add_serviceroute)*;




	# P-Asserted-Identity

	PAssertedID_value = name_addr | addr_spec;
	P_Asserted_Identity = "P-Asserted-Identity"i HCOLON <: PAssertedID_value >prepare_urihdr %add_pai
		(COMMA PAssertedID_value >prepare_urihdr %add_pai )*;




	# P-Associated-Uri

	ai_param = generic_param;
	p_aso_uri_spec = name_addr (SEMI ai_param )*;

	P_Associated_Uri = "P-Associated-Uri"i HCOLON :> SP* (p_aso_uri_spec >prepare_urihdr %add_pau)* SP*
		(COMMA :> p_aso_uri_spec >prepare_urihdr %add_pau )*;



	# Session-Expires
	refresher_param = "refresher"i EQUAL ("uas"i %set_se_uas | "uac"i %set_se_uac);
	se_params = refresher_param | generic_param_sep %add_se_param;
	Session_Expires = ("Session-Expires"i | "x"i) HCOLON delta_seconds >mark %set_se_seconds (SEMI se_params >mark %add_se_param)*;



	# Server
	product_version = token;
	product = token (SLASH product_version)?;
	comment = TEXT_UTF8_TRIM;
	server_val = product | comment;
	Server = "Server"i HCOLON :> server_val >mark (LWS server_val)*  %set_server;

	# Subject
	Subject = ("Subject"i | "s"i) HCOLON :> TEXT_UTF8_TRIM? >mark %set_subject;

	# Supported
	Supported = ("Supported"i | "k"i) HCOLON :> (option_tag >mark %add_supported (COMMA option_tag >mark %add_supported)* )?;

	entity_tag = token;
	SIP_Etag = "SIP-Etag"i HCOLON :> entity_tag >mark %set_etag;
	SIP_If_Match = "SIP-If-Match"i HCOLON :> entity_tag >mark %set_ifmatch;

	# Timestamp
	delay = DIGIT* ("." DIGIT*)?;
	Timestamp = "Timestamp"i HCOLON :> DIGIT+ ("." DIGIT*)? (LWS delay)?;

	# To
	to_param = tag_param | generic_param;
	to_spec = (name_addr | addr_spec ) (SEMI to_param)*;
	To = ("To"i | "t"i) HCOLON <: to_spec >prepare_urihdr %set_to;

	# User-Agent
	User_Agent = "User-Agent"i HCOLON :> TEXT_UTF8_TRIM? >mark %set_useragent;

	# Via
	protocol_name = "SIP"i | token;
	via_ttl = "ttl"i EQUAL ttl;
	via_maddr = "maddr"i EQUAL host;
	via_received = "received"i EQUAL (IPv4address | IPv6address);
	via_branch = "branch"i EQUAL token;
	via_generic_param = token >mark %set_create_pname (EQUAL gen_value >mark %set_pvalue)?;
	via_extension = via_generic_param;
	via_params = via_extension; #via_ttl | via_maddr | via_received | via_branch | via_extension;
	sent_by = host >mark %set_domain (COLON port >mark %set_port)?;
	transport = other_transport; # "UDP"i | "TCP"i | "TLS"i | "SCTP"i | other_transport;
	sent_protocol = protocol_name SLASH protocol_version SLASH transport > mark %init_via;
	via_parm = sent_protocol LWS sent_by (SEMI via_params)* %set_via_final;
	Via = ("Via"i | "v"i) HCOLON <: via_parm (COMMA via_parm)*;


	# extension header
	header_name = token - ("Via"i | "v"i | "Subject"i | "s"i | "Supported"i | "k"i | "Timestamp"i | "To"i | "t"i |
	                        "User-Agent"i | "Event"i | "o"i | "Server"i | "CSeq"i | "Content-Length"i | 
	                        "l"i | "Expires"i | "Min-Expires"i | "From"i | "Allow-Events"i | "u"i | 
	                        "Service-Route" | "Subscription-State"i | "SIP-Etag"i | "SIP-If-Match"i |
	                        "f"i | "Record-Route"i | "Route"i | "Required"i | "Content-Encoding"i | "e"i |
	                        "Contact"i | "m"i | "Call-Id"i | "i"i | "Content-Type"i | "c"i |
	                        "Max-Forwards"i | "History-Info"i | "P-Associated-Uri"i | 
	                        "Allow"i |
	                        "a"i | "Accept-Contact"i |
	                        "j"i | "Reject-Contact"i |
	                        "x"i | "Session-Expires"i |
	                        "b"i | "Referred-By"i |
	                        "Privacy"i |
	                        "Require"i |
	                        "Supported"i |
	                        "P-Asserted-Identity"i );
	
	header_value = (TEXT_UTF8_CHAR | UTF8_CONT | LWS)*;

	extension_header = header_name >mark %custom_header_name HCOLON :> header_value >mark %custom_header;
	message_header = (Accept_Contact |
	                  Allow |
	                  Allow_Events |
	                  Call_ID |
	                  Contact |
	                  Content_Encoding |
	                  Content_Length |
	                  Content_Type |
	                  CSeq |
	                  Date |
	                  Expires |
	                  Event |
	                  From |
	                  HistoryInfo |
	                  Max_Forwards |
	                  Min_Expires |
	                  Privacy |
	                  P_Asserted_Identity |
	                  P_Associated_Uri |
	                  Record_Route |
	                  Referred_By |
	                  Reject_Contact |
	                  Require |
	                  Route |
	                  Server |
	                  Service_Route |
	                  Session_Expires |
	                  Subject |
	                  SIP_Etag |
	                  SIP_If_Match |
	                  Subscription_State |
	                  Supported |
	                  Timestamp |
	                  To | 
	                  User_Agent |
	                  Via |
	                  extension_header
	                 ) CRLF;

	Request_URI = SIP_URI_wparams | SIPS_URI_wparams | TEL_URI_wparams | absolute_URI;
	SIP_Version = "SIP"i "/" DIGIT "." DIGIT;

	Request_Line = Method >mark %set_method SP >prepare_req_uri
	               Request_URI %done_req_uri SP SIP_Version CRLF;

	Status_Code = DIGIT{3};
	Reason_Phrase = (reserved | unreserved | escaped | UTF8_NONASCII | UTF8_CONT | SP | HTAB)*;
	Status_Line = SIP_Version SP >create_response Status_Code >mark %set_status_code SP Reason_Phrase >mark %set_reason_phrase CRLF;

	Request = Request_Line message_header* CRLF;
	Response = Status_Line message_header* CRLF;

	SIP_message = Request |
	              Response;

	SIP_complete_message = (CR | LF | ' ' | 0x09)* SIP_message;
	main := SIP_complete_message @do_finish;

}%%

%% write data;

protected static String arrayToString(int mark, int p) {
		// Java is a verbose pain the ass. Our C++ is much more compact.
		// So, donkey-show to copy the currently marked bytes back into a string,
		// just so we can display what we just parsed. You will have to deal with this
		// if you store everything internally in real unicode strings instead of UTF-8 which
		// is what we usually do. (We convert from TP::Bytes to QString via a UTF-8 conversion)

		byte[] tmp = new byte[ p - mark ];
		System.arraycopy( data, mark, tmp, 0, p - mark);
		String result = new String( tmp );
		//System.out.println(p+" "+result+" "+mark);		
		return result;
	}


	protected volatile static byte[] data;


	public static void main(String[] args){
		String input = "INVITE sip:android1004@10.0.2.15:5061;transport=TCP SIP/2.0\n" +
		"Record-Route: <sip:217.149.57.49;transport=tcp;lr;ftag=be4dd3f8-ac39-4707-807e-469db05cdeed;pm;n2>\n" +
		"Via: SIP/2.0/TCP 217.149.57.49;branch=z9hG4bK3699.799e8723.0;i=1e02\n" +
		"Via: SIP/2.0/TCP 10.0.2.15:5061;received=93.84.113.146;branch=z9hG4bKb6514686-357f-4d07-bf9c-40a3066bc752;rport=2414\n" +
		"Max-Forwards: 69\n" +
		"From: <sip:android1001@demo.movial.com>;tag=be4dd3f8-ac39-4707-807e-469db05cdeed\n" +
		"To: <sip:android1004@demo.movial.com>\n" +
		"Call-ID: 0411be5a-7ae2-436b-8dc2-65c8bf46c036@10.0.2.15\n" +
		"CSeq: 2 INVITE\n" +
		"Contact: <sip:android1001@93.84.113.146:2414;transport=TCP>\n" +
		"Allow: INVITE,NOTIFY,MESSAGE,ACK,BYE,CANCEL,PRACK\n" +
		"Require: 100rel\n" +
		"Supported: 100rel\n" +
		"User-Agent: Movial\n" +
		"Subscription-State: active\n" +
		"Content-Type: application/sdp\n" +
		"Content-Length: 541\n\n" +
		"v=0\n" +
		"o=- 1267788242227 1267788242227 IN IP4 10.0.2.15\n" +
		"s=-\n" +
		"t=0 0\n" +
		"m=application 0 TCP 0 8 97\n" +
		"i=media 1\n" +
		"c=IN IP4 127.0.0.1\n" +
		"a=rtpmap:0 PCMU/8000\n" +
		"a=rtpmap:8 PCMA/8000\n" +
		"a=rtpmap:97 iLBC/8000\n" +
		"m=application 33333 TCP 31 32\n" +
		"i=media 2\n" +
		"c=IN IP4 127.0.0.1\n" +
		"a=rtpmap:31 H261/90000\n" +
		"a=rtpmap:32 MPV/90000\n" +
		"m=application 33333 TCP 41 41\n" +
		"i=media 3\n" +
		"c=IN IP4 127.0.0.1\n" +
		"a=rtpmap:41 H264/60000\n" +
		"a=rtpmap:42 MPV/60000\n" +
		"m=application 33333 TCP 0 8 97\n" +
		"i=media 1\n" +
		"c=IN IP4 127.0.0.1\n" +
		"a=rtpmap:0 PCMU/8000\n" +
		"a=rtpmap:8 PCMA/8000\n" +
		"a=rtpmap:97 iLBC/8000\n";

		input ="REFER sip:b@atlanta.example.com SIP/2.0\n" +
		"Via: SIP/2.0/UDP agenta.atlanta.example.com;branch=z9hG4bK2293940223\n" +
		"To: <sip:b@atlanta.example.com>\n" +
		"From: <sip:a@atlanta.example.com>;tag=193402342\n" +
		"Call-ID: 898234234@agenta.atlanta.example.com\n" +
		"CSeq: 93809823 REFER\n" +
		"Max-Forwards: 70\n" +
		"Refer-To: (sip:c@atlanta.example.com\n" +
		"Contact: sip:a@atlanta.example.com\n" +
		"Content-Length: 0\n\n";

		input = "NOTIFY sip:user@userpc.example.com SIP/2.0\n" +
		"To: sip:user@example.com\n" +
		"From: sip:alice@wonderland.com\n" +
		"Call-ID: knsd08alas9dy@3.4.5.6\n" +
		"CSeq: 1 NOTIFY\n" +
		"Content-Type: application/xpidf+xml\n" +
		"\n" +
		"<?xml version=\"1.0\"?>\n" +
		"<!DOCTYPE presence\n" +
		"PUBLIC  \"xpidf.dtd\">\n" +
		"	    <presence>\n" +
		"	    <presentity uri=\"sip:alice@wonderland.com;method=\"SUBSCRIBE\">\n" +
		"<atom id=\"779js0a98\">\n" +
		"<address uri=\"sip:alice@wonderland.com;method=INVITE\">\n" +
		"<status status=\"closed\"/>\n" +
		"</address>\n" +
		"</atom>\n" +
		"</presentity>\n" +
		"</presence>";

		input = "MESSAGE sip:android1008@demo.movial.com SIP/2.0\n" +
		"Call-ID: 99430573-83f7-4551-814d-c321504a9661\n" +
		"CSeq: 1 MESSAGE\n" +
		"To: <sip:android1008@demo.movial.com>\n" +
		"From: \"android1000\" <sip:android1000@demo.movial.com>;tag=7c9880af-cc94-4e8e-8343-f0ea43d2f68f\n" +
		"Max-Forwards: 70\n" +
		"Supported: 100rel, eventlist\n" +
		"User-Agent: Movial Communicator/7.2.90.4875\n" +
		"Accept: text/plain\n" +
		"Content-Type: text/plain; charset=UTF-8\n" +
		"Content-Length: 1\n" +
		"Via: SIP/2.0/UDP 62.236.91.3:5060;branch=z9hG4bK-4a84f79d-9783-4f3b-a8a6-8aeaf3bad1bb.1;rport\n" +
		"\n" +
		"3";
		
		

		input = "SIP/2.0 200 OK\r\n" +
        "Via: SIP/2.0/TCP 10.0.2.15:5061;received=121.33.201.170;branch=z9hG4bK455b94d5-e9a4-457a-964f-360779be95d2;rport=3816\r\n" +
        "From: <sip:79262948587@multifon.ru>;tag=25e5347a-078a-42ca-9324-c879443a1275\r\n" +
        "To: <sip:79262948587@multifon.ru>;tag=aprqpm5q6q1-3mo7ob2000060\r\n" +
        "Call-ID: 3d9760e6-2100-40b8-ac3f-fa527d659d0c@10.0.2.15\r\n" +
        "CSeq: 3 REGISTER\r\n" +
        "P-Associated-URI: \r\n" +
        "Contact: <sip:79262948587@10.0.2.15:5061;transport=TCP>;expires=100\r\n" +
        "Service-Route: <sip:79262948587@193.201.229.35:5060;transport=tcp;lr>\r\n" +
        "Content-Length: 0\r\n\r\n";

		BaseSipMessage msg = null/*parse(input)*/;
		/*if(msg != null){
			System.out.println("Result"+msg.buildContent());
		}*/
		
		input = "SIP/2.0 200 OK\r\n" +
        "Via: SIP/2.0/TCP 10.0.2.15:5061;received=121.33.201.170;branch=z9hG4bK455b94d5-e9a4-457a-964f-360779be95d2;rport=3816\r\n" +
        "From: <sip:79262948587@multifon.ru>;tag=25e5347a-078a-42ca-9324-c879443a1275\r\n" +
        "To: <sip:79262948587@multifon.ru>;tag=aprqpm5q6q1-3mo7ob2000060\r\n" +
        "Call-ID: 3d9760e6-2100-40b8-ac3f-fa527d659d0c@10.0.2.15\r\n" +
        "CSeq: 3 REGISTER\r\n" +
        "P-Associated-URI:\r\n" +
        "Contact: <sip:79262948587@10.0.2.15:5061;transport=TCP>;expires=100\r\n" +
        "Service-Route: <sip:79262948587@193.201.229.35:5060;transport=tcp;lr>\r\n" +
        "Content-Length: 0\r\n\r\n";

		/* msg = parse(input);
		if(msg != null){
			System.out.println("Result"+msg.buildContent());
		}*/
		
		input = "SIP/2.0 200 OK\r\n" +
        "Via: SIP/2.0/TCP 10.0.2.15:5061;received=121.33.201.170;branch=z9hG4bK455b94d5-e9a4-457a-964f-360779be95d2;rport=3816\r\n" +
        "From: <sip:79262948587@multifon.ru>;tag=25e5347a-078a-42ca-9324-c879443a1275\r\n" +
        "To: <sip:79262948587@multifon.ru>;tag=aprqpm5q6q1-3mo7ob2000060\r\n" +
        "Call-ID: 3d9760e6-2100-40b8-ac3f-fa527d659d0c@10.0.2.15\r\n" +
        "CSeq: 3 REGISTER\r\n" +
        "P-Associated-URI: <sip:79262948587@10.0.2.15:5061;transport=TCP>\r\n" +
        "Contact: <sip:79262948587@10.0.2.15:5061;transport=TCP>;expires=100\r\n" +
        "Service-Route: <sip:79262948587@193.201.229.35:5060;transport=tcp;lr>\r\n" +
        "Content-Length: 0\r\n\r\n";

		/* msg = parse(input);
		if(msg != null){
			System.out.println("Result"+msg.buildContent());
		}*/
		
		input = "SIP/2.0 200 OK\r\n" +
        "Via: SIP/2.0/TCP 10.0.2.15:5061;received=121.33.201.170;branch=z9hG4bK455b94d5-e9a4-457a-964f-360779be95d2;rport=3816\r\n" +
        "From: <sip:79262948587@multifon.ru>;tag=25e5347a-078a-42ca-9324-c879443a1275\r\n" +
        "To: <sip:79262948587@multifon.ru>;tag=aprqpm5q6q1-3mo7ob2000060\r\n" +
        "Call-ID: 3d9760e6-2100-40b8-ac3f-fa527d659d0c@10.0.2.15\r\n" +
        "CSeq: 3 REGISTER\r\n" +
        "P-Associated-URI:SHA-1001 <sip:9725511001@dev.mavenir.lab>\r\n" +
        "Contact: <sip:79262948587@10.0.2.15:5061;transport=TCP>;expires=100\r\n" +
        "Service-Route: <sip:79262948587@193.201.229.35:5060;transport=tcp;lr>\r\n" +
        "Content-Length: 0\r\n\r\n";
        
        input = "SIP/2.0 100 Trying\r\n" +
				"To: <urn:service:sos>\r\n" +
				"Call-ID: 7edd6100-6501-4406-998c-cada6868c061@10.10.2.17\r\n" +
				"From: <sip:12345678@dummy.com>;tag=0fb3d2d5-7b6b-4ac8-be9d-eec30c503527\r\n" +
				"CSeq: 1 INVITE\r\n" +
				"Via: SIP/2.0/TCP 10.10.2.17:5061;received=195.222.87.225;branch=z9hG4bK-ee1c32a9-382a-4341-b352-38fd14c6a293;rport=18668\r\n" +
				"Content-Length: 0\r\n";

        input = "NOTIFY sip:movial5@10.0.2.15:5061;transport=TCP SIP/2.0\r\n" +
        "Via: SIP/2.0/TCP 217.69.182.90:5060;branch=z9hG4bKk67sfc00704g9g8j9520.1\r\n" +
        "i: a7416f19-b8ad-4072-b42d-387ef7bbfb36@10.0.2.15\r\n" +
        "CSeq: 3 NOTIFY\r\n" +
        "f: <sip:movial5@dummy.com>;tag=2176\r\n" +
        "t: <sip:movial5@dummy.com>;tag=8abe4f50-a5b5-4cd1-83e9-e55817489846\r\n" +
        "Max-Forwards: 67\r\n" +
        "m: <sip:movial5@217.69.182.90:5060;transport=tcp>\r\n" +
        "o: presence\r\n" +
        "c: application/pidf+xml\r\n" +
        "Subscription-State: active;expires=3599;min-interval=0\r\n" +
        "l: 308\r\n\r\n";
        
        input = "SIP/2.0 200 OK\r\n" +
        "l: 308\r\n\r\n";

		input = "SIP/2.0 401 Unauthorized\r\n" +
			"To: <sip:5102177260@demosbc.metaswitch.com>;tag=172.24.136.13+1+0+a2399e2\r\n" +
			"From: <sip:5102177260@demosbc.metaswitch.com>;tag=eb8c27fa-2413-4f24-b916-13a69b9ef59c\r\n" +
			"Via: SIP/2.0/TCP 10.0.2.15:5061;branch=z9hG4bK-32451ca5-0011-4d2d-9f29-6cf833725812\r\n" +
			"Call-ID: 3b499098-ed09-4417-a756-64181a53648e@10.0.2.15\r\n" +
			"CSeq: 1 REGISTER\r\n" +
			"Organization: MetaSwitch\r\n" +
			"Server: DC-SIP/2.0\r\n" +
			"Www-Authenticate: Digest realm=\"demosbc.metaswitch.com\",nonce=\"1bb69e4b920a\",stale=false,algorithm=MD5,qop=\"auth\"\r\n" +
			"Content-Length: 0\r\n\r\n";
				
				
		 msg = parse(input);
		if(msg != null){
			System.out.println("Result"+msg.buildContent());
		}

	}
	/*	private static void cleanVariables(){
	    data = null;
        cs = 0;                  
        p = 0;                   
        pe = 0;        
        eof = 0; 
        offset = 0; 
        state = ParserState.INITIAL; 
	    customHeaderName = null; 
	    temp = null;
	    currentLine = 0;
	    startOfCurrentLine = 0;
	    contentLength = 0;
	    base = null;
	    via = null;
	    request = null;
	    response = null;
	    uriBuilder = null;//uri = null;
	    uriBuilder = null;//uri = null;
	    curHValName = null; 
	    curHValValue = null; 
	    curParam = null;
	    paramHeader = null; 
	}*/
	private volatile static int currentLine, p, pe;                   // Current index into data is "p"


	public synchronized static BaseSipMessage parse(String input ) {
		Logger.log(Logger.Tag.PARSER,"parsing started: \n");	
		byte[] dataTemp  = null;

		try {
			dataTemp = input.getBytes("UTF-8");
		} catch (Exception e) {
			Logger.log(Logger.Tag.PARSER,"Unsupported encoding");
			return null;
		}
		return parse(dataTemp);
	}


	public synchronized static BaseSipMessage parse(final byte[] input) {	
		
		if(input == null || input.length == 0){
			throw new IllegalArgumentException("Wrong data to parse");
		}

		data = input;
		
		int cs = 0;                  // Ragel keeps the state in "cs"
		p = 0;                   // Current index into data is "p"
		pe = 0;        // Length of data, SO YES data[pe] IS AN INVALID INDEX THIS IS CORRECT!
		int eof = 0; 
		int offset = 0; 
		ParserState state = ParserState.INITIAL;


		// Whenever we see something interesting, remember the current index. Then, when we've decided
		// we really saw something interesting, fetch the part between "mark" and "current value of p"

		int m_Mark = 0;

		String customHeaderName = null, temp = null;
		int startOfCurrentLine = 0 , contentLength = 0;

		BaseSipMessage.Builder msgBuilder = new BaseSipMessage.Builder();

		//Via via = null;
        Via.Builder viaBuilder = null;
		Request request = null;
		Response response = null;

        //SipUri uri = null;
		//UriHeader uriHeader = null;
        SipUri.SipUriBuilder uriBuilder = null;
        UriHeader.UriHeaderBuilder uriHeaderBuilder = null;

		String curHValName = null, curHValValue = null, curParam = null;
		//final ParamHeader paramHeader = null;
        ParamHeader.ParamHeaderBuilder paramHeaderBuilder = null;

		/*
		BaseSipMessage base = new BaseSipMessage();   
		Via via = null;
		Request request = null;
		Response response = null;
		SipUri uriBuilder = null;//uri = null;
		UriHeader uriBuilder = null;//uri = null;
		String curHValName = null, curHValValue = null, curParam = null;
		ParamHeader paramHeader = null;
		 */
		//cleanVariables();

		currentLine = 0;

		p = offset;                   // Current index into data is "p"
		pe = data.length;        // Length of data
		eof = pe;                // Depending on the grammar, it may not be clear wether you are really "done"
		// or not. In that case, "eof" is either 0 which means that there is still
		// data coming, or it equals "pe" which means that when the end is reached,
		// the parser must really be in a success state or else the message failed.
		// (So eof == 0 means, wait for more data before giving up, just in case)                                	   
	    while (data.length > p || (contentLength == 0 && state == ParserState.PARSING_BODY)) {	 
	    			//Logger.log(Logger.Tag.PARSER,"data.length: "+data.length+" p: "+p);	             
	        if (state == ParserState.INITIAL) {
				Logger.log(Logger.Tag.PARSER,"INITIAL state");
	            /* Initializing new message */
	            p = 0;
	            pe = p + data.length;
	            
	            m_Mark = 0;
	            cs = 0;
	            offset = 0;
	            currentLine = 0;
	            startOfCurrentLine = p;

	            state = ParserState.PARSING_HEADERS;

	            %% write init;

	        } else if (state == ParserState.PARSING_BODY) {
				//Logger.log(Logger.Tag.PARSER,"Parsing body");
	            int remaining = data.length - p; //TODO need to introduce conentLength here
				//Logger.log(Logger.Tag.PARSER,"remaining "+remaining);	            
	            if (remaining > 0) {
					byte[] tempBody = new byte[remaining];
	                System.arraycopy( data, p, tempBody, 0, remaining); // TODO get body 
	                
	                if (tempBody.length > 0) {
	                   msgBuilder.body(tempBody);//base.setBody(tempBody);
						//Logger.log(Logger.Tag.PARSER,"Body: "+new String(tempBody));
	                }	               


					/*if (request != null) {
    	                    return request;
    	                } else {	                  
    	                   return response;
    	             }
                      */
                      break;				
	            } else {
	               p = data.length;
	                break;
	            }

	        } else if (state == ParserState.PARSING_HEADERS) {
				//Logger.log(Logger.Tag.PARSER,"PARSING_HEADERS");
	            /* Additional data */
	            %% write exec;
	            
	         // Now we check the result
	            // The names are generated based on the parser name, which for us is "message_parser"

	            // Error state is clear	       

	            if (cs == message_error) {
	                Logger.log(Logger.Tag.PARSER,"Your input did not comply with the grammar");
					Logger.log(Logger.Tag.PARSER,"The failure occured at position " + p);
					if(p < data.length && p - 10 > -1){
						Logger.log(Logger.Tag.PARSER,"near text is:  " + arrayToString(p-10, p));
					}
					Logger.log(Logger.Tag.PARSER,"data to parse was: \n"+new String(data));
	                return null;

	            } else if (cs < message_first_final) {
	               	Logger.log(Logger.Tag.PARSER,"Your input is valid so far, but not complete");
	                 state = ParserState.PARSING_BODY;
	            } else {
	                Logger.log(Logger.Tag.PARSER,"Headers parsed successfully!");
					//Logger.log(Logger.Tag.PARSER,"Results: "+base.getContent());
	                state = ParserState.PARSING_BODY;

	            }

	        }

	    }

/*
	    if (request != null) {
    	                    return request;
    	                } else {	                  
    	                   return response;
    	             }
    	             */
    	             final BaseSipMessage retValue = msgBuilder.build();

        if(retValue instanceof Request){
            request = (Request) retValue;
        }
        else if (retValue instanceof Response){
            response = (Response) retValue;
        }
        else{
            assert false: "unknow retValue type";
        }
        return retValue;
	}
	
	
}



