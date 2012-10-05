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
import javax.microedition.ims.core.sipservice.subscribe.NotifyInfo;
import javax.microedition.ims.messages.wrappers.common.Uri;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;

public class NotifyMessageBuilder extends RequestMessageBuilder {

    public NotifyMessageBuilder(Dialog dialog, StackContext context) {
        super(dialog, context);
    }

    protected BaseSipMessage.Builder buildCustomMessage() {
/*
SIP_NOTIFY sip:a@atlanta.example.com SIP/2.0
Via: SIP/2.0/UDP agentb.atlanta.example.com;branch=z9hG4bK9922ef992-25
To: <sip:a@atlanta.example.com>;tag=193402342
From: <sip:b@atlanta.example.com>;tag=4992881234
Call-ID: 898234234@agenta.atlanta.example.com
CSeq: 1993402 SIP_NOTIFY
Max-Forwards: 70
Event: REFER
Subscription-StateHolder: active;EXPIRES=(depends on ReferImpl-To URI)
Contact: sip:b@atlanta.example.com
Content-Type: message/sipfrag;version=2.0
Content-Length: 20

SIP/2.0 100 Trying
*/

/*
SIP_NOTIFY sip:a@atlanta.example.com SIP/2.0
Via: SIP/2.0/UDP agentb.atlanta.example.com;branch=z9hG4bK9323394234
To: <sip:a@atlanta.example.com>;tag=193402342
From: <sip:b@atlanta.example.com>;tag=4992881234
Call-ID: 898234234@agenta.atlanta.example.com
CSeq: 1993403 SIP_NOTIFY
Max-Forwards: 70
Event: REFER
Subscription-StateHolder: terminated;REASON=NORESOURCE
Contact: sip:b@atlanta.example.com
Content-Type: message/sipfrag;version=2.0
Content-Length: 16

SIP/2.0 200 OK
*/

        //Request retValue = new Request();
        BaseSipMessage.Builder retValue = new BaseSipMessage.Builder(BaseSipMessage.Builder.Type.REQUEST);

        //SIP_NOTIFY sip:a@atlanta.example.com SIP/2.0
        setMethodHeader(MessageType.SIP_NOTIFY, retValue);

        //final Uri requestUri = SipUriParser.parseUri(dialog.getRemoteParty()).getUri();
        final Uri requestUri = constructUri(dialog.getRemoteParty());

        setRequestUriHeader(requestUri, retValue);
        //Via: SIP/2.0/UDP agentb.atlanta.example.com;branch=z9hG4bK9323394234
        generateViaHeader(context.getConfig(), retValue);
        //To: <sip:a@atlanta.example.com>;tag=193402342
        addToHeader(requestUri, retValue);
        //From: <sip:b@atlanta.example.com>;tag=4992881234
        addFromHeader(dialog, retValue);
        //Call-ID: 898234234@agenta.atlanta.example.com
        addCallIdHeader(dialog, retValue);
        //CSeq: 1993403 SIP_NOTIFY
        addCSeqHeader(dialog, retValue);
        //Max-Forwards: 70
        addMaxForwardsHeader(context.getConfig(), retValue);

        //Event: REFER
        NotifyInfo notifyInfo = (NotifyInfo) dialog.getCustomParameter(Dialog.ParamKey.NOTIFY_INFO);
        retValue.getEventBuilder().value(notifyInfo.getEventPackage().stringValue());
        //Subscription-StateHolder: terminated;REASON=NORESOURCE
        retValue.getSubscriptionStateBuilder().value(notifyInfo.getNotifySubscriptionState() + ";EXPIRES=" + 60);


        //Contact: sip:b@atlanta.example.com
        generateContactHeader(context.getConfig(), retValue);
        //Content-Type: message/sipfrag;version=2.0
        retValue.contentType(MimeType.MSG_SIP_FRAG.stringValue());
        /* Content-Length: 16
         * 
         * SIP/2.0 200 OK
         */
        String[] notifyBodyMessages = notifyInfo.getNotifyBodyMessages();
        assert notifyBodyMessages != null && notifyBodyMessages.length > 0 : "notifyBodyMessages will contain at least one body message";
        retValue.body(notifyBodyMessages[0].getBytes());

        final GsmLocationInfo locationInfo = context.getEnvironment().getGsmLocationService().getGsmLocationInfo();
        addPAccessNetworkHeader(locationInfo, retValue);

        //???
        addAuthorizationHeader(retValue, MessageType.SIP_NOTIFY);
        //???
        //byeBuilder.buildRoutes();
        //???
        addUserAgentHeader(context.getConfig(), retValue);

        return retValue;
    }

}
