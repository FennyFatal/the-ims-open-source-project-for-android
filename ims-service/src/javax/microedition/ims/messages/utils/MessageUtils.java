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

import javax.microedition.ims.common.Protocol;
import javax.microedition.ims.config.UserInfo;
import javax.microedition.ims.messages.wrappers.common.Param;
import javax.microedition.ims.messages.wrappers.common.ParamList;
import javax.microedition.ims.messages.wrappers.common.ParamListDefaultImpl;
import javax.microedition.ims.messages.wrappers.common.Uri;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.ContactsList;
import javax.microedition.ims.messages.wrappers.sip.SipUri;
import javax.microedition.ims.messages.wrappers.sip.UriHeader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static javax.microedition.ims.messages.utils.StatusCode.*;

public final class MessageUtils {
    private static Map<Integer, String> statusCodeMessages = new HashMap<Integer, String>();
    private static final String SIPS_PREFIX = "sips";

    static {
        statusCodeMessages.put(TRYING, "Trying");
        statusCodeMessages.put(RINGING, "Ringing");
        statusCodeMessages.put(CALL_BEING_FORWARDED, "Call Being Forwarded");
        statusCodeMessages.put(CALL_QUEUED, "Call Queued");
        statusCodeMessages.put(CALL_SESSION_PROGRESS, "Session Progress");
        statusCodeMessages.put(OK, "OK");
        statusCodeMessages.put(ACCEPTED, "Accepted");
        statusCodeMessages.put(MULTIPLE_CHOICES, "Multiple Choices");
        statusCodeMessages.put(MOVED_PERMANENTLY, "Moved Permanently");
        statusCodeMessages.put(MOVED_TEMPORARILY, "Moved Temporarily");
        statusCodeMessages.put(USE_PROXY, "Use Proxy");
        statusCodeMessages.put(ALTERNATIVE_SERVICE, "Alternative Service");
        statusCodeMessages.put(BAD_REQUEST, "Bad Request");
        statusCodeMessages.put(UNATHORIZED, "Unauthorized");
        statusCodeMessages.put(PAYMENT_REQUIRED, "Payment Required");
        statusCodeMessages.put(FORBIDDEN, "Forbidden");
        statusCodeMessages.put(NOT_FOUND, "Not Found");
        statusCodeMessages.put(METHOD_NOT_ALLOWED, "Method Not Allowed");
        statusCodeMessages.put(NOT_ACCEPTABLE, "Not Acceptable");
        statusCodeMessages.put(PROXY_AUTH_REQUIRED, "Proxy Authentication Required");
        statusCodeMessages.put(REQUEST_TIMEOUT, "Request Timeout");
        statusCodeMessages.put(CONFLICT, "Conflict");
        statusCodeMessages.put(GONE, "Gone");
        statusCodeMessages.put(LENGTH_REQUIRED, "Length Required");
        statusCodeMessages.put(REQUEST_ENTITY_TOO_LARGE, "Request Entity Too Large");
        statusCodeMessages.put(REQUEST_URI_TOO_LONG, "Request URI Too Long");
        statusCodeMessages.put(UNSUPPORTED_MEDIA_TIME, "Unsupported Media Type");
        statusCodeMessages.put(UNSUPPORTED_URI_SCHEMA, "Unsupported URI Scheme");
        statusCodeMessages.put(BAD_EXTENTION, "Bad Extension");
        statusCodeMessages.put(EXTENTION_REQUIRED, "Extension Required");
        statusCodeMessages.put(INTERVAL_TOO_BRIEF, "Interval Too Brief");
        statusCodeMessages.put(TEMPORARY_UNAVAILABLE, "Temporarily Unavailable");
        statusCodeMessages.put(CALL_OR_TRANSACTION_DOESNOT_EXISTS, "Call/Transaction Does Not Exist");
        statusCodeMessages.put(LOOP_DETECTED, "Loop Detected");
        statusCodeMessages.put(TOO_MANY_HOPS, "Too Many Hops");
        statusCodeMessages.put(ADDRESS_INCOMPLETE, "Address Incomplete");
        statusCodeMessages.put(AMBIGUOUS, "Ambiguous");
        statusCodeMessages.put(BUSY_HERE, "Busy Here");
        statusCodeMessages.put(REQUEST_TERMINATED, "Request Terminated");
        statusCodeMessages.put(NOT_ACCEPTABLE_HERE, "Not Acceptable Here");
        statusCodeMessages.put(REQUEST_PENDING, "Request Pending");
        statusCodeMessages.put(UNDECIPHERABLE, "Undecipherable");
        statusCodeMessages.put(SERVER_INTERNAL_ERROR, "Server Internal Error");
        statusCodeMessages.put(NOT_IMPLEMENTED, "Not Implemented");
        statusCodeMessages.put(BAD_GATEWAY, "Bad Gateway");
        statusCodeMessages.put(SERVICE_UNAVAILABLE, "Service Unavailable");
        statusCodeMessages.put(SERVER_TIMEOUT, "Server TimeOut");
        statusCodeMessages.put(VERSION_NOT_SUPPORTED, "Version Not Supported");
        statusCodeMessages.put(MESSAGE_TOO_LARGE, "Message Too Large");
        statusCodeMessages.put(BUSY_EVERYWHERE, "Busy Everywhere");
        statusCodeMessages.put(DECLINED, "Declined");
        statusCodeMessages.put(STATUS_DOESNOT_EXISTS_ANYWHERE, "Does Not Exist Anywhere");
    }

    private MessageUtils() {
        assert false;
    }

    /*    public static String getName(final String userName) {
            if(!userName.contains("@")) return null;
           return userName.split("@")[0];
        }

        public static String getDomain(final String userName) {
            if(!userName.contains("@")) return null;
            return userName.split("@")[1];
        }
    */
    public static SipUri.SipUriBuilder createURI(
            final String user,
            final String domain,
            final String prefix) {

        return createURI(user, domain, null, prefix);
    }

    public static SipUri.SipUriBuilder createURI(final UserInfo userInfo) {
        return createURI(userInfo.getName(), userInfo.getDomain(), userInfo.getSchema());
    }

    public static SipUri.SipUriBuilder createURI(
            final String user,
            final String domain,
            final Integer port,
            final String prefix) {

        SipUri.SipUriBuilder retValue = new SipUri.SipUriBuilder();

        retValue.prefix(prefix);

        if (user != null) {
            retValue.username(user);
        }

        if (domain != null) {
            retValue.domain(domain);
        }

        if (port != null) {
            retValue.port(port);
        }

        return retValue;
    }

    public static SipUri.SipUriBuilder createURI(
            final String user,
            final String domain,
            final Integer port,
            final Map<String, String> params,
            final Map<String, String> headers) {

        SipUri.SipUriBuilder retValue = new SipUri.SipUriBuilder();
        retValue.prefix("sip");

        if (user != null) {
            retValue.username(user);
        }

        if (domain != null) {
            retValue.domain(domain);
        }

        if (port != null) {
            retValue.port(port);
        }

        if (params != null) {
            ParamList paramList = new ParamListDefaultImpl();
            for (String paramName : params.keySet()) {
                String paramValue = params.get(paramName);
                paramList.set(paramName, paramValue);
            }
            retValue.paramsList(paramList);
        }

        if (headers != null) {
            ParamList paramList = new ParamListDefaultImpl();
            for (String headerName : headers.keySet()) {
                String headerValue = headers.get(headerName);
                paramList.set(headerName, headerValue);
            }
            retValue.headers(paramList);
        }

        return retValue;
    }

    public static String getMessageByCode(int responseCode) {
        String message = statusCodeMessages.get(responseCode);
        return message == null ? "Unknown" : message;
    }

    public static MessageData grabMessageData(final BaseSipMessage msg) {
        MessageData retValue = null;

        if (msg != null) {
            MessageData.Builder builder = new MessageData.Builder();
            final ContactsList contacts = msg.getContacts();

            if (contacts != null) {
                final Collection<UriHeader> contactsList = contacts.getContactsList();

                if (contactsList != null && contactsList.size() > 0) {
                    final UriHeader uriHeader = new ArrayList<UriHeader>(contactsList).get(0);
                    final Uri uri = uriHeader.getUri();

                    if (uri != null) {
                        Protocol transport = extractTransport(uriHeader, uri);
                        builder.
                                contactDomain(uri.getDomain()).
                                contactPort(uri.getPort()).
                                contactExpires(lookupParamInUriHeader("expires", uriHeader)).
                                contactTransport(transport).
                                expires(msg.getExpires());

                    }
                }
            }
            retValue = builder.build();
        }

        return retValue;
    }

    private static Protocol extractTransport(final UriHeader uriHeader, final Uri uri) {
        final Protocol retValue;

        boolean isSecureTransport = SIPS_PREFIX.equalsIgnoreCase(uri.getPrefix());
        final String transportValue = lookupParamInUriHeader("transport", uriHeader);
        retValue = isSecureTransport ?
                Protocol.TLS :
                transportValue == null ? null : Protocol.valueOf(transportValue);

        return retValue;
    }

    private static String lookupParamInUriHeader(final String name, final UriHeader uriHeader) {
        String retValue = null;

        if (uriHeader != null) {
            retValue = lookupParamInUri(name, uriHeader.getUri());
            if (retValue == null) {
                retValue = lookupParamInParamList(name, uriHeader.getParamsList());
            }
        }

        return retValue;
    }

    private static String lookupParamInUri(final String name, final Uri uri) {
        String retValue = null;

        if (uri != null) {
            retValue = lookupParamInParamList(name, uri.getHeaders());
            if (retValue == null) {
                retValue = lookupParamInParamList(name, uri.getParamsList());
            }
        }

        return retValue;
    }

    private static String lookupParamInParamList(final String name, final ParamList paramList) {
        String retValue = null;

        if (paramList != null) {
            final Param param = paramList.get(name);
            if (param != null) {
                retValue = param.getValue();
            }
        }

        return retValue;
    }
}
