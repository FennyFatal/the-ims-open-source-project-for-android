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
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.connection.GsmLocationInfo;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.sipservice.refer.Refer;
import javax.microedition.ims.messages.wrappers.common.Uri;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Header;
import javax.microedition.ims.messages.wrappers.sip.Request;
import javax.microedition.ims.messages.wrappers.sip.Response;

public class ReferMessageBuilder extends RequestMessageBuilder {

    public ReferMessageBuilder(Dialog dialog, StackContext context) {
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

        Request originalMessage = dialog.getMessageHistory().getFirstMessage();
        Response responseMessage = dialog.getMessageHistory().findLastResponse();


        //Request retValue = new Request(null);
        BaseSipMessage.Builder retValue = new BaseSipMessage.Builder(BaseSipMessage.Builder.Type.REQUEST);

        //SIP_REFER sip:b@atlanta.example.com SIP/2.0
        setMethodHeader(MessageType.SIP_REFER, retValue);

        //final Uri requestUri = SipUriParser.parseUri(dialog.getRemoteParty()).getUri();
        final Uri requestUri = constructUri(dialog.getRemoteParty());

        buildRequestUri(retValue);


        //Via: SIP/2.0/UDP agenta.atlanta.example.com;branch=z9hG4bK2293940223
        generateViaHeader(context.getConfig(), retValue);


        //To: <sip:b@atlanta.example.com>
        //Route:
//        if (responseMessage == null) {
//            if (originalMessage != null) {
//                buildTo(retValue, originalMessage);
//
//                addRoutes(retValue, originalMessage.getRecordRoutes());
//            }
//            else {
//                addToHeader(requestUri, retValue);
//            }
//        }
//        else {
//            buildTo(retValue, responseMessage);
//
//            addRoutes(retValue, responseMessage.getRecordRoutes());
//        }
        // in case of an established session (200 OK response received)
        // the REFER's to header should be the same as that of the 200 OK response
        if (responseMessage == null) {
            addToHeader(requestUri, retValue);
        }
        else {
            addToHeader(responseMessage.getTo(), retValue);
            addRoutes(retValue, responseMessage.getRecordRoutes());
        }


        //From: <sip:a@atlanta.example.com>;tag=193402342
//        addFromHeader(dialog, retValue);
        // in case of an established session (200 OK response received)
        // the REFER's from header should be the same as that of the 200 OK response
        if (responseMessage == null) {
            addFromHeader(dialog, retValue);
        }
        else {
            addFromHeader(responseMessage.getFrom(), retValue);
        }


        //Call-ID: 898234234@agenta.atlanta.example.com
        addCallIdHeader(dialog, retValue);
        //CSeq: 93809823 SIP_REFER
        addCSeqHeader(dialog, retValue);
        //Max-Forwards: 70
        addMaxForwardsHeader(context.getConfig(), retValue);

        //ReferImpl-To: (whatever URI)
        retValue.customHeader(Header.ReferTo, getReferToHeaderValue(dialog.getReferTo()));

        //Contact: sip:a@atlanta.example.com
        generateContactHeader(context.getConfig(), retValue);
        //Content-Length: 0

        final GsmLocationInfo locationInfo = context.getEnvironment().getGsmLocationService().getGsmLocationInfo();
        addPAccessNetworkHeader(locationInfo, retValue);

        //???
        addAuthorizationHeader(retValue, MessageType.SIP_REFER);
        //???
        addUserAgentHeader(context.getConfig(), retValue);

        return retValue;
    }

    /*    public void buildRoutes(Request retValue, BaseSipMessage source) {
            List<UriHeader> recordRoutes = source.getRecordRoutes();
            for(int i = recordRoutes.size() - 1; i >= 0; i--) {
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

        //TODO AK value for 'To' header can be differ to dialog.remote.party
/*        if (source.getTo().buildContent().contains(dialog.getRemoteParty())) {
            addToHeader(source.getTo(), retValue);
        }
        else {
            addToHeader(source.getFrom(), retValue);
        }
*/
        switch (dialog.getInitiateParty()) {
            case LOCAL:
                addToHeader(source.getTo(), retValue);
                break;
            case REMOTE:
                addToHeader(source.getFrom(), retValue);
                break;
        }
    }

}
