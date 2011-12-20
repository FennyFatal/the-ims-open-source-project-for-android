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
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.messages.wrappers.common.Uri;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;

public class OptionsMessageBuilder extends RequestMessageBuilder {

    public OptionsMessageBuilder(Dialog dialog, StackContext context) {
        super(dialog, context);
    }

    protected BaseSipMessage.Builder buildCustomMessage() {
        /**
         * OPTIONS sip:carol@chicago.com SIP/2.0 
         * Via: SIP/2.0/UDP pc33.atlanta.com;branch=z9hG4bKhjhs8ass877 
         * Max-Forwards: 70 
         * To: <sip:carol@chicago.com> 
         * From: Alice <sip:alice@atlanta.com>;tag=1928301774 
         * Call-ID: a84b4c76e66710 
         * CSeq: 63104 OPTIONS 
         * Contact: <sip:alice@pc33.atlanta.com> 
         * Accept: application/sdp Content-Length: 0
         */

        //Request retValue = new Request();
        BaseSipMessage.Builder retValue = new BaseSipMessage.Builder(BaseSipMessage.Builder.Type.REQUEST);

        Uri requestUri = null;

        if (dialog.getMessageHistory().getRemoteContact() != null) {
            requestUri = dialog.getMessageHistory().getRemoteContact();
        }
        else {
            //requestUri = SipUriParser.parseUri(dialog.getRemoteParty()).getUri();
            requestUri = constructUri(dialog.getRemoteParty());
        }

        setRequestUriHeader(requestUri, retValue);
        setMethodHeader(MessageType.SIP_OPTIONS, retValue);

        generateViaHeader(context.getConfig(), retValue);
        addMaxForwardsHeader(context.getConfig(), retValue);
        addFromHeader(dialog, retValue);
        addToHeader(requestUri, retValue);
        addCallIdHeader(dialog, retValue);
        addCSeqHeader(dialog, retValue);
        generateContactHeader(context.getConfig(), retValue);
        addAuthorizationHeader(retValue, MessageType.SIP_INVITE);
        addAcceptHeader(retValue, MimeType.APP_SDP);

        return retValue;
    }

}
