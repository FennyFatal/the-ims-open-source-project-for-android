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

package javax.microedition.ims.messages.history;

import javax.microedition.ims.common.MimeType;
import javax.microedition.ims.messages.utils.BodyPartUtils;
import javax.microedition.ims.messages.wrappers.sip.*;
import java.util.Collection;
import java.util.Iterator;


/**
 * This class is used for building MessageData objects using BaseSipMessage object.
 *
 * @author ext-achirko
 */
public class MessageDataBuilder {

    /**
     * Builds MessageData wrapper using BaseSipMessage and remote party
     *
     * @param request     - sources messsage
     * @param remoteParty - address of remote side for current SESSION
     * @return - generated Messagedata wrapper
     */
    public static MessageData buildMessageData(Request request, boolean income) {
        MessageDataImpl ret = new MessageDataImpl();
        ret.setMethod(request.getMethod());
        addHeaders(request, ret);
        addBody(request, ret);
        ret.setState(income ? MessageData.STATE_RECEIVED : MessageData.STATE_SENT);
        return ret;
    }

    /**
     * Builds MessageData wrapper using BaseSipMessage and remote party
     *
     * @param response    - sources messsage
     * @param remoteParty - address of remote side for current SESSION
     * @return - generated Messagedata wrapper
     */
    public static MessageDataImpl buildMessageData(Response response, boolean income) {
        MessageDataImpl ret = new MessageDataImpl();
        ret.setReasonPhrase(response.getReasonPhrase());
        ret.setStatusCode(response.getStatusCode());
        ret.setMethod(response.getMethod());
        addHeaders(response, ret);
        addBody(response, ret);
        ret.setState(income ? MessageData.STATE_RECEIVED : MessageData.STATE_SENT);
        return ret;
    }

    /**
     * Copies body from source message to wrapper
     *
     * @param src    - source message
     * @param target - target wrapper
     */
    private static void addBody(BaseSipMessage src, MessageDataImpl target) {
        if (src.getBody() != null && src.getBody().length > 0) {
            if (src.getContentType().getValue() == null || !src.getContentType().getValue().startsWith(MimeType.MULTIPART.stringValue())) { //TODO handle case when MULTIPART is not set
                target.createBodyPart().setContent(src.getBody());
                target.getBodyParts()[0].addHeader(Header.Content_Type.stringValue(), src.getContentType().buildContent());
            }
            else {
                target.setBodyParts(BodyPartUtils.parseBody(src.getBody(), src.getContentType().getParamsList().get("boundary").getValue()));
            }
        }

    }

    /*
    * Headers not to be added
    *
    * Authentication-Info, Authorization, Max-Forwards, Min-Expires,
    * Proxy-Authenticate, Proxy-Authorization, Record-Route,
    * Security-Client, Security-Server, Security-Verify, Service-Route, Via
    *
    * to be added:
    *
    * private int CSeq = -1;
   private String callId, ETag, ifMatch, subject, method, userAgent;
   private SessionExpiresHeader sessionExpires;
   private UriHeader from, to, referredBy;
   private byte[] body;
   private long EXPIRES = -1;
   private ParamHeader subscriptionState, event, contentType;
   private ParamListDefaultImpl acceptContact, rejectContact, customHeaders;
   private List<String> allowEvents, privacy, allow, require, supported; //StringList ?
   private List<UriHeader> routes, pAssociatedUris, pAssertedIdentities, historyInfo;
   private ContactsList contacts;
   private int contentLength;
    */

    /**
     * Copies headers from source message to wrapper
     *
     * @param src    - source message
     * @param target - target wrapper
     */
    private static void addHeaders(BaseSipMessage src, MessageData target) {


        appendUriHeadersList(target, src.getRoutes(), Header.Route);

        if (src.getFrom() != null) {
            target.addHeader(Header.From.stringValue(), src.getFrom().buildContent());
        }

        if (src.getTo() != null) {
            target.addHeader(Header.To.stringValue(), src.getTo().buildContent());
        }

        if (src.getCallId() != null) {
            target.addHeader(Header.Call_ID.stringValue(), src.getCallId());
        }

        if (src.getReferredBy() != null && src.getReferredBy().getUri().getDomain() != null) {
            target.addHeader(Header.Referred_By.stringValue(), src.getReferredBy().buildContent());
        }

        if (src.getcSeq() >= 0) {
            StringBuilder sb = new StringBuilder();
            target.addHeader(Header.CSeq.stringValue(), sb.append(src.getcSeq()).append(" ").append(src.getMethod()).toString());
            sb.setLength(0);
        }

        if (src.getSubject() != null) {
            target.addHeader(Header.Subject.stringValue(), src.getSubject());
        }
        if (src.getExpires() >= 0) {
            target.addHeader(Header.Expires.stringValue(), String.valueOf(src.getExpires()));
        }

        if (src.getContacts() != null) {
            if (!src.getContacts().getContactsList().isEmpty()) {
                for (final UriHeader aContactsList : src.getContacts().getContactsList()) {
                    target.addHeader(Header.Contact.stringValue(), aContactsList.buildContent());
                }

            }
        }


        appendStringsList(target, src.getAllow(), Header.Allow);
        appendStringsList(target, src.getPrivacy(), Header.Privacy);
        appendStringsList(target, src.getRequire(), Header.Require);
        appendStringsList(target, src.getAllowEvents(), Header.Allow_Events);
        appendStringsList(target, src.getSupported(), Header.Supported);

        if (src.getUserAgent() != null) {
            target.addHeader(Header.UserAgent.stringValue(), src.getUserAgent());
        }

        appendUriHeadersList(target, src.getpAssertedIdentities(), Header.PAssertedIdentities);
        appendUriHeadersList(target, src.getHistoryInfo(), Header.HistoryInfo);
        appendUriHeadersList(target, src.getpAssociatedUris(), Header.PAssociatedUris);


        if (src.getSessionExpires() != null && src.getSessionExpires().getExpiresValue() >= 0) {
            String value = String.valueOf(src.getSessionExpires().getExpiresValue());
            if (src.getSessionExpires().getRefresher() != null) {
                value += Header.RefresherParam.stringValue() + src.getSessionExpires().getRefresher().stringValue();
            }
            target.addHeader(Header.Session_Expires.stringValue(), value);
        }

        if (src.getSessionExpires() != null && src.getSessionExpires().getMinExpiresValue() >= 0) {
            target.addHeader(Header.Min_SE.stringValue(), String.valueOf(src.getSessionExpires().getMinExpiresValue()));
        }

        if (src.getEvent() != null && src.getEvent().getValue() != null) {
            target.addHeader(Header.Event.stringValue(), src.getEvent().buildContent());
        }

        if (src.getSubscriptionState() != null && src.getSubscriptionState().getValue() != null) {
            target.addHeader(Header.SubscriptionState.stringValue(), src.getSubscriptionState().buildContent());
        }

        if (src.geteTag() != null) {
            target.addHeader(Header.SIP_ETag.stringValue(), src.geteTag());
        }

        if (src.getIfMatch() != null) {
            target.addHeader(Header.SIP_If_Match.stringValue(), src.getIfMatch());
        }

        if (src.getAcceptContact() != null && !src.getAcceptContact().getParams().isEmpty()) {
            target.addHeader(Header.AcceptContact.stringValue(), src.getAcceptContact().buildContent());
        }

        if (src.getRejectContact() != null && !src.getRejectContact().getParams().isEmpty()) {
            target.addHeader(Header.RejectContact.stringValue(), src.getRejectContact().buildContent());
        }

        if (src.getCustomHeaders() != null && !src.getCustomHeaders().keySet().isEmpty()) {
            Iterator<String> it = src.getCustomHeaders().keySet().iterator();
            String key = null;
            StringBuilder sb = new StringBuilder();
            while (it.hasNext()) {
                key = it.next();
                for (String value : src.getCustomHeader(key)) {
                    sb.append(value);
                }
                target.addHeader(key, sb.toString());
                sb.setLength(0);
            }
        }

        if ((src.getBody() == null || src.getBody().length == 0) && (src.getContentType() != null && src.getContentType().getValue() != null)) {
            target.addHeader(Header.Content_Type.stringValue(), src.getContentType().buildContent());
        }

        target.addHeader(Header.Content_Length.stringValue(), src.getBody() != null ? String.valueOf(src.getBody().length) : "0");

    }

    private static void appendUriHeadersList(MessageData data, Collection<UriHeader> list, Header headerName) {
        StringBuilder sb = new StringBuilder();
        if (list != null && !list.isEmpty()) {
            Iterator<UriHeader> it = list.iterator();
            while (it.hasNext()) {
                sb.append(it.next().buildContent());
                if (it.hasNext()) {
                    sb.append(" ,");
                }
            }
            data.addHeader(headerName.stringValue(), sb.toString());
        }
    }

    private static void appendStringsList(MessageData data, Collection<String> list, Header headerName) {
        StringBuilder sb = new StringBuilder();
        if (list != null && !list.isEmpty()) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                sb.append(it.next());
                if (it.hasNext()) {
                    sb.append(",");
                }
            }
            data.addHeader(headerName.stringValue(), sb.toString());
        }

    }


}
