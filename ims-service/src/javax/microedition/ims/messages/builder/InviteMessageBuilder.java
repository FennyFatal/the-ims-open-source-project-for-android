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

import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.common.MimeType;
import javax.microedition.ims.common.SDPType;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.connection.GsmLocationInfo;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.Dialog.DialogState;
import javax.microedition.ims.messages.history.BodyPartData;
import javax.microedition.ims.messages.utils.BodyPartUtils;
import javax.microedition.ims.messages.wrappers.common.Uri;
import javax.microedition.ims.messages.wrappers.sip.*;
import java.util.Collection;
import javax.microedition.ims.common.Logger;

public class InviteMessageBuilder extends RequestMessageBuilder {

    public InviteMessageBuilder(final Dialog dialog, final StackContext context) {
        super(dialog, context);
    }


    /*
      *
 SIP_INVITE sip:movial6@dummy.com SIP/2.0
 Via: SIP/2.0/TCP 62.236.91.3:38108;branch=z9hG4bKsutnjpu;rport;alias
 To: <sip:movial6@dummy.com>
 From: <sip:movial5@dummy.com>;tag=96d6-d37c3260-f4c8a6f2-c2c3
 MaxForwards: 70
 DialogCallID: 987asjd97y7atg
 CSeq: 986759 SIP_INVITE
 Content-Length: 0
 User-Agent: Movial Client

      */
    protected BaseSipMessage.Builder buildCustomMessage() {
        //Request retValue = new Request();
        BaseSipMessage.Builder retValue = new BaseSipMessage.Builder(BaseSipMessage.Builder.Type.REQUEST);

        //final Uri requestUri = SipUriParser.parseUri(DIALOG.getRemoteParty()).getUriBuilder();
        Uri requestUri;

        /*requestUri = dialog.getMessageHistory().getRemoteContact() != null ?
                dialog.getMessageHistory().getRemoteContact() :
                constructUri(dialog.getRemoteParty());
			*/
		String rParty = dialog.getRemoteParty();
		if (dialog.getOutgoingSdpMessage().typeSupported(SDPType.VOIP) && !(rParty.contains("urn:service:sos"))) { //only voip calls
		   rParty += ";user=phone";
		}
        Uri remoteContact = dialog.getMessageHistory().getRemoteContact();
        Logger.log("InviteMessageBuilder", remoteContact != null? " is not null" : " is null");
        Logger.log("InviteMessageBuilder", "remoteContact = " + remoteContact);
        Logger.log("InviteMessageBuilder", dialog.getMessageHistory().toString());
        Logger.log("InviteMessageBuilder", dialog.toString());

        requestUri = remoteContact != null ? remoteContact : constructUri(rParty);

        Response lastResponse = dialog.getMessageHistory().findLastResponseByMethod(MessageType.SIP_INVITE);
        Collection<UriHeader> recordRoutes = lastResponse != null ? lastResponse.getRecordRoutes() : null;
        addRoutes(retValue, recordRoutes);
/*		if(lastResponse != null) {
			for(int i = recordRoutes.size() - 1; i >= 0; i--) {
				retValue.addRoute(recordRoutes.get(i));
			}
		}
*/
        setRequestUriHeader(requestUri, retValue);
        setMethodHeader(MessageType.SIP_INVITE, retValue);
        generateViaHeader(context.getConfig(), retValue);
        addMaxForwardsHeader(context.getConfig(), retValue);
        addFromHeader(dialog, retValue);

        if (dialog.getState() == DialogState.EARLY) {


            if (!context.getConfig().getSpecialUris().contains(dialog.getRemoteParty())) {
                //toUri = SipUriParser.parseUri(requestUri.getShortURINoParams());
                addToHeader(constructUri(requestUri.getShortURINoParams()), retValue);
            }
            else {
                //toUri = requestUriHeader;
                addToHeader(requestUri, retValue);
            }



            if (dialog.getOutgoingSdpMessage().typeSupported(SDPType.VOIP)) {
                retValue.customHeader(Header.P_Early_Media, "supported");
            }

        }
        else if (dialog.getState() == DialogState.STATED) {
            final UriHeader toUriHeader = constructUriHeader(dialog.getRemoteParty());
            UriHeader.UriHeaderBuilder uriHeaderTo = new UriHeader.UriHeaderBuilder(toUriHeader);

            String remoteTag = dialog.getRemoteTag();
            assert remoteTag != null;

            uriHeaderTo.tag(remoteTag);
            addToHeader(uriHeaderTo, retValue);
        }

        addCallIdHeader(dialog, retValue);
        addUserAgentHeader(context.getConfig(), retValue);
        addCSeqHeader(dialog, retValue);

        String sipInstance = extractSipInstance();
        generateContactHeader(context.getConfig(), context.getStackRegistry(), retValue, sipInstance);

        final GsmLocationInfo locationInfo = context.getEnvironment().getGsmLocationService().getGsmLocationInfo();
        addPLastAccessNetworkHeader(locationInfo, retValue);
        addPAccessNetworkHeader(locationInfo, retValue);
        
        addAuthorizationHeader(retValue, MessageType.SIP_INVITE);

        if (dialog.getOutgoingSdpMessage().typeSupported(SDPType.MSRP)) {
            retValue.customHeader(Header.AcceptContact.stringValue(), "*;+g.oma.sip-im");
        }

        buildAcceptContactForClient(retValue, dialog.getLocalParty());

        //TODO for test, client should set this HEADER if need, remove it before final delivery
        //addRequire(retValue);

        //addSupported(retValue);
        addAllow(retValue);
        if (context.getConfig().useInviteRefresh()) {
            Refresher ref = null;//context.getConfig().getRefresher();
            long sessionExpires = context.getConfig().getSessionExpiresTime();
            long minSessionExpires = context.getConfig().getMinSessionExpiresTime();
            if (dialog.getSessionRefreshData() != null) {
                ref = dialog.getSessionRefreshData().getRefresher()/* != null ? dialog.getSessionRefreshData().getRefresher() : ref*/;
                sessionExpires = dialog.getSessionRefreshData().getExpiresValue() > 0 ? dialog.getSessionRefreshData().getExpiresValue() : sessionExpires;
                minSessionExpires = dialog.getSessionRefreshData().getMinExpiresValue() > 0 ? dialog.getSessionRefreshData().getMinExpiresValue() : minSessionExpires;
            }

            addSessionExpires(retValue, ref, sessionExpires);
            addMinSessionExpires(retValue, minSessionExpires);
            //addRequire(retValue, HeaderValues.TIMER);
        }


        //TODO set IP4 or IP6 address later
        dialog.getOutgoingSdpMessage().setSessionAddress(context.getEnvironment().getConnectionManager().getInetAddress());
        addBody(
                retValue,
                dialog.getOutgoingSdpMessage().getContent().getBytes(),
                MimeType.APP_SDP.stringValue()
        );

        return retValue;
    }
    
    protected void addBody(final BaseSipMessage.Builder retValue, final byte[] body, final String contentType) {
        BodyPartData[] parts = dialog.getMessageHistory().nextRequestMessage().getBodyParts();

        if (parts != null && parts.length > 0) {
            ParamHeader.ParamHeaderBuilder contentTypeHeader = new ParamHeader.ParamHeaderBuilder(
                    MimeType.MULTIPART_MIXED.stringValue() /*ContentType.MULTIPART_MIME.stringValue()*/
            );

            String boundary = BodyPartUtils.generateBoundary();
            contentTypeHeader.param("boundary", "\"" + boundary + "\"");

            retValue.contentType(contentTypeHeader.build().buildContent());

            BodyPartData extraPart = dialog.getMessageHistory().nextRequestMessage().createBodyPart(0);
            extraPart.setContent(body);
            extraPart.addHeader(Header.Content_Type.stringValue(), contentType);
            retValue.body(
                    BodyPartUtils.getBodyPartsAsByteArray(
                            dialog.getMessageHistory().nextRequestMessage(),
                            boundary
                    )
            );
        }
        else {
            retValue.body(body);
            retValue.contentType(contentType);
        }
    }
}
