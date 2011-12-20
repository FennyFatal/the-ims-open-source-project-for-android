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
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Request;

public class CancelMessageBuilder extends RequestMessageBuilder {

    public CancelMessageBuilder(final Dialog dialog, final StackContext context) {
        super(dialog, context);
    }

    protected BaseSipMessage.Builder buildCustomMessage() {
        /*
   SIP_CANCEL sip:bob@biloxi.example.com SIP/2.0
     Via: SIP/2.0/UDP client.atlanta.example.com:5060;branch=z9hG4bK74bf9
     Max-Forwards: 70
     From: Alice <sip:alice@atlanta.example.com>;tag=9fxced76sl
     To: Bob <sip:bob@biloxi.example.com>
     Route: <sip:ss1.atlanta.example.com;lr>
     Call-ID: 2xTb9vxSit55XU7p8@atlanta.example.com
     CSeq: 1 SIP_CANCEL
     Content-Length: 0
           */

        /*
           * A SIP_CANCEL request SHOULD NOT be sent to cancel a request other than
     SIP_INVITE. This builder generates cancel to last sent SIP_INVITE.
           */
        //Request retValue = new Request();
        BaseSipMessage.Builder retValue = new BaseSipMessage.Builder(BaseSipMessage.Builder.Type.REQUEST);

        Request inviteToCancel = dialog.getMessageHistory().findLastOutRequestByMethod(MessageType.SIP_INVITE);


        setMethodHeader(MessageType.SIP_CANCEL, retValue);
        setRequestUriHeader(inviteToCancel.getRequestUri(), retValue);
        addAuthorizationHeader(retValue, MessageType.SIP_CANCEL);
        addUserAgentHeader(context.getConfig(), retValue);
        addViaHeader(inviteToCancel.getVias().get(0), retValue);
        addFromHeader(dialog, retValue);

        addToHeader(inviteToCancel.getRequestUri(), retValue);
        addMaxForwardsHeader(context.getConfig(), retValue);
        addCallIdHeader(dialog, retValue);
        retValue.cSeq(inviteToCancel.getcSeq());

        //generateContactHeader(context.getConfig(), retValue);

        return retValue;
    }
}
