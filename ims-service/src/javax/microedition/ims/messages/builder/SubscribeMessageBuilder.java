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

package javax.microedition.ims.messages.builder;

import javax.microedition.ims.FeatureMapper;
import javax.microedition.ims.common.EventPackage;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.common.MimeType;
import javax.microedition.ims.config.UserInfo;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.connection.GsmLocationInfo;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.sipservice.Privacy;
import javax.microedition.ims.core.sipservice.PrivacyInfo;
import javax.microedition.ims.core.sipservice.refer.Refer;
import javax.microedition.ims.core.sipservice.subscribe.SubscriptionInfo;
import javax.microedition.ims.messages.wrappers.common.Uri;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Header;
import javax.microedition.ims.messages.wrappers.sip.Request;
import javax.microedition.ims.messages.wrappers.sip.Response;

public class SubscribeMessageBuilder extends RequestMessageBuilder {

    public SubscribeMessageBuilder(Dialog dialog, StackContext context) {
        super(dialog, context);
    }

    protected BaseSipMessage.Builder buildCustomMessage() {
        /*
        SIP_REFER sip:b@atlanta.example.com SIP/2.0
        Via: SIP/2.0/UDP agenta.atlanta.example.com;branch=z9hG4bK2293940223
        To: <sip:b@atlanta.example.com>
        From: <sip:a@atlanta.example.com>;tag=193402342
        Call-ID: 898234234@agenta.atlanta.example.com
        CSeq: 93809823 SIP_REFER
        Max-Forwards: 70
        ReferImpl-To: (whatever URI)
        Contact: sip:a@atlanta.example.com
        Content-Length: 0
        */

        /*
        SUBSCRIBE sip:ptolemy@rosettastone.org SIP/2.0
        Via SIP/2.0/UDP proxy.elasticity.co.uk:5060;branch=z9hG4bK348471123
        Via SIP/2.0/UDP parlour.elasticity.co.uk:5060;branch=z9hG4bKABDA;received=192.0.3.4
        Max-Forwards: 69
        To: <sip:Ptolemy@rosettastone.org>
        From: Thomas Young <sip:tyoung@elasticity.co.uk>;tag=1814
        Call-ID: 452k59252058dkfj34924lk34
        CSeq: 3412 SUBSCRIBE
        Allow-Events: dialog
        Contact: <sip:tyoung@parlour.elasticity.co.uk>
        Event: dialog
        Content-Length: 0
        */

        Request originalMessage = dialog.getMessageHistory().getFirstMessage();
        Response responseMessage = dialog.getMessageHistory().findLastResponseByMethod(MessageType.SIP_SUBSCRIBE);

        //Request retValue = new Request(null);
        BaseSipMessage.Builder retValue = new BaseSipMessage.Builder(BaseSipMessage.Builder.Type.REQUEST);

        //SIP_REFER sip:b@atlanta.example.com SIP/2.0
        setMethodHeader(MessageType.SIP_SUBSCRIBE, retValue);

        //final UriHeader requestUri = SipUriParser.parseUri(dialog.getRemoteParty());
        final Uri requestUri = constructUri(dialog.getRemoteParty());
        //ParamListDefaultImpl requestUriParamsList = SipUriParser.parseUri(dialog.getRemoteParty()).getParamsList();

        buildRequestUri(retValue);
        //TODO for test
        //retValue.getRequestUri().getParamsList().set("auid", "resource-lists");


        //Via: SIP/2.0/UDP agenta.atlanta.example.com;branch=z9hG4bK2293940223
        generateViaHeader(context.getConfig(), retValue);


        //To: <sip:b@atlanta.example.com>
        //Route:
        if (responseMessage == null) {
            if (originalMessage != null) {
                buildTo(retValue, originalMessage);

                addRoutes(retValue, originalMessage.getRecordRoutes());
            }
            else {
                //final Uri toUri = MessageUtils.createURI(getFROMUserInfo(dialog)).buildUri();
                //addToHeader(toUri, retValue);
                addToHeader(requestUri, retValue);
            }
        }
        else {
            buildTo(retValue, responseMessage);

            addRoutes(retValue, responseMessage.getRecordRoutes());
        }

        //UriHeader toURI = SipUriParser.parseUri(dialog.getRemoteParty());
        //addToHeader(toURI, retValue);

        //inserting params from incoming URI
        /* Map<String, Param> params = requestUriParamsList.getParams();
        if (params != null) {
            for (Param param : params.values()) {
                retValue.getRequestUri().getParamsList().set(param.getKey(), param.getValue());
                retValue.getTo().getUriBuilder().getParamsList().set(param.getKey(), param.getValue());
            }
        }*/


        //From: <sip:a@atlanta.example.com>;tag=193402342
        addFromHeader(dialog, retValue);
/*        //TODO
        UriHeader uriHeaderFrom = new UriHeader();
        uriHeaderFrom.setUri(fromURI);
        uriHeaderFrom.setTag(dialog.getLocalTag());
        retValue.setFrom(uriHeaderFrom);
*/

        //Call-ID: 898234234@agenta.atlanta.example.com
        addCallIdHeader(dialog, retValue);
        //CSeq: 93809823 SIP_REFER
        addCSeqHeader(dialog, retValue);
        //Max-Forwards: 70
        addMaxForwardsHeader(context.getConfig(), retValue);

        final UserInfo preferredIdentity = context.getConfig().getPreferredIdentity();
        if (preferredIdentity != null) {
            //retValue.addCustomHeader(Header.PPreferredIdentity, preferredIdentity.toUri());
        }

        final PrivacyInfo privacyInfo = context.getConfig().getPrivacyInfo();
        if (privacyInfo != null && privacyInfo.get().size() != 0) {
            final String privacyHeaderValue = Privacy.toString(privacyInfo.get());
            //retValue.addCustomHeader(Header.Privacy, privacyHeaderValue);
        }

        //ReferImpl-To: (whatever URI)
        final SubscriptionInfo subscriptionInfo = (SubscriptionInfo) dialog.getCustomParameter(Dialog.ParamKey.SUBSCRIBE_INFO);
        EventPackage eventPackage = null;
        if (subscriptionInfo != null) {
            eventPackage = subscriptionInfo.getEvent();

            final Number expireTime = (Number) dialog.getCustomParameter(Dialog.ParamKey.SUBSCRIPTION_EXPIRES);

            if (expireTime != null && expireTime.longValue() > 0) {
                retValue.customHeader(Header.Allow_Events, eventPackage.stringValue());
            }
            retValue.customHeader(Header.Event, eventPackage.stringValue());

            addExpiresHeader(retValue, expireTime.longValue());

            byte[] body = subscriptionInfo.getBody().getBytes();
            retValue.body(body);

            if (body.length > 0) {
                retValue.contentType(MimeType.APP_RESOURCE_LISTS_XML.stringValue()+"; charset=\"utf-8\"");
            }
        }

        //Contact: sip:a@atlanta.example.com
//        generateContactHeader(context.getConfig(), retValue, "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3gpp-service.ims.icsi.mmtel\"");
        generateContactHeader(dialog.getContactHeaders(), context.getConfig(), context.getStackRegistry(), retValue);
        //Content-Length: 0

        final GsmLocationInfo locationInfo = context.getEnvironment().getGsmLocationService().getGsmLocationInfo();
        addPAccessNetworkHeader(locationInfo, retValue);

        //???
        addAuthorizationHeader(retValue, MessageType.SIP_REFER);
        //???
        addUserAgentHeader(context.getConfig(), retValue);

        //retValue.addSupported(HeaderValues.SUPPORTED_EVENTLIST);
        //addSupported(retValue);
        /*retValue.addCustomHeader("Accept", "application/reginfo+xml");
        retValue.addCustomHeader("Accept", "application/pidf+xml");
        retValue.addCustomHeader("Accept", "application/rlmi+xml");
        retValue.addCustomHeader("Accept", "application/xcap-diff+xml");
        retValue.addCustomHeader("Accept", "application/watcherinfo+xml");
        retValue.addCustomHeader("Accept", "multipart/related");*/
        if (eventPackage != null) {
            final FeatureMapper mapper = context.getConfig().getFeatureMapper();
            final MimeType[] mimeTypes = mapper.getTypesByEventPackage(eventPackage);
            for (MimeType mimeType : mimeTypes) {
                retValue.customHeader("Accept", mimeType.stringValue());
            }

        }

        //application/resource-lists+xml;

        /*
        dialog.getOutgoingSdpMessage().setSessionAddress(context.getConnectionManager().getInetAddress());
        retValue.setBody(dialog.getOutgoingSdpMessage().getContent().getBytes());
        */

        return retValue;
    }

    /*    public void buildRoutes(Request retValue, BaseSipMessage source) {
            List<UriHeader> recordRoutes = source.getRecordRoutes();
            for (int i = recordRoutes.size() - 1; i >= 0; i--) {
                retValue.addRoute(recordRoutes.get(i));
            }
        }
    */
    private static String getReferToHeaderValue(final Refer refer) {
        if (refer.getReferMethod() == null) {
            return refer.getReferTo();
        }
        return "<" + refer.getReferTo() + ";method=" + refer.getReferMethod() + ">";
    }

    public void buildRequestUri(final BaseSipMessage.Builder retValue) {
        final Uri byeRequestURI;

        if (dialog.getMessageHistory().getRemoteContact() != null) {
            byeRequestURI = dialog.getMessageHistory().getRemoteContact();
        }
        else {
            //byeRequestURI = SipUriParser.parseUri(dialog.getRemoteParty()).getUri();
            byeRequestURI = constructUri(dialog.getRemoteParty());
        }
        setRequestUriHeader(byeRequestURI, retValue);
    }

    public void buildTo(final BaseSipMessage.Builder retValue, final BaseSipMessage source) {
        //TODO: why it's needed to check the RemoteParty from dialog
        if (source.getTo().buildContent().contains(dialog.getRemoteParty())) {
            addToHeader(source.getTo(), retValue);
        }
        else {
            //TODO: temporarily add this condition for presence SUBSCRIBE
            if (dialog.getRemoteParty()!=null && dialog.getRemoteParty().contains("list=phbk"))
                addToHeader(source.getTo(), retValue);
            else
                addToHeader(source.getFrom(), retValue);
        }
    }
}
