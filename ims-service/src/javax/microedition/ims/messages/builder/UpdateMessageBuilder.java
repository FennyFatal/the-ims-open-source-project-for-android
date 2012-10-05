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
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.connection.GsmLocationInfo;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.messages.wrappers.common.Uri;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Response;
import javax.microedition.ims.messages.wrappers.sip.UriHeader;
import java.util.Collection;


public class UpdateMessageBuilder extends RequestMessageBuilder {

    public UpdateMessageBuilder(final Dialog dialog, final StackContext context) {
        super(dialog, context);
    }

    protected BaseSipMessage.Builder buildCustomMessage() {
        //Request retValue = new Request(null);
        BaseSipMessage.Builder retValue = new BaseSipMessage.Builder(BaseSipMessage.Builder.Type.REQUEST);

        Uri requestUri = null;

        if (dialog.getMessageHistory().getRemoteContact() != null) {
            requestUri = dialog.getMessageHistory().getRemoteContact();
        }
        else {
            //requestUri = SipUriParser.parseUri(dialog.getRemoteParty()).getUri();
            requestUri = constructUri(dialog.getRemoteParty());
        }


        Response lastResponse = dialog.getMessageHistory().findLastResponseByMethod(MessageType.SIP_INVITE);
        Collection<UriHeader> recordRoutes = lastResponse != null ? lastResponse.getRecordRoutes() : null;
        addRoutes(retValue, recordRoutes);

        if (lastResponse != null) {

/*            for(int i = recordRoutes.size() - 1; i >= 0; i--) {
                retValue.addRoute(recordRoutes.get(i));
            }
*/
            addToHeader(lastResponse.getTo(), retValue);
        }

        setRequestUriHeader(requestUri, retValue);
        setMethodHeader(MessageType.SIP_UPDATE, retValue);
        generateViaHeader(context.getConfig(), retValue);
        addMaxForwardsHeader(context.getConfig(), retValue);
        addFromHeader(dialog, retValue);


        addCallIdHeader(dialog, retValue);
        addUserAgentHeader(context.getConfig(), retValue);
        addCSeqHeader(dialog, retValue);
        generateContactHeader(context.getConfig(), retValue);
        addAuthorizationHeader(retValue, MessageType.SIP_UPDATE);

        final GsmLocationInfo locationInfo = context.getEnvironment().getGsmLocationService().getGsmLocationInfo();
        addPAccessNetworkHeader(locationInfo, retValue);

        /*      if(dialog.getOutgoingSdpMessage().typeSupported(SDPType.MSRP)){
            retValue.addCustomHeader(Header.AcceptContact.stringValue(), "*;+g.oma.sip-im");
        }*/


        //TODO for test, client should set this HEADER if need, remove it before final delivery
        //addRequire(retValue);

        //addSupported(retValue);
        addAllow(retValue);

        //retValue.setContentType(new ParamHeader(ContentType.SDP.stringValue()));
        retValue.contentType(MimeType.APP_SDP.stringValue());

        //TODO set IP4 or IP6 address later
        dialog.getOutgoingSdpMessage().setSessionAddress(context.getEnvironment().getConnectionManager().getInetAddress());
        retValue.body(dialog.getOutgoingSdpMessage().getContent().getBytes());

        return retValue;
    }

}
