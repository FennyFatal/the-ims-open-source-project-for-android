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
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.messages.wrappers.common.Uri;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Request;
import javax.microedition.ims.messages.wrappers.sip.Response;

public class AckMessageBuilder extends RequestMessageBuilder {

    public AckMessageBuilder(final Dialog dialog, final StackContext context) {
        super(dialog, context);
    }

    protected BaseSipMessage.Builder buildCustomMessage() {
        /*
        SIP_ACK sip:vladimir@protei.ru SIP/2.0
        Via: SIP/2.0/UDP pc33.niits.ru;branch=z9hG4bKkjshdyff
        To: Vladimir <sip:vladimir@protei.ru>;tag=99sa0xk
        From: Anton <sip:anton@niits.ru>;tag=88sja8x
        MaxForwards: 70
        DialogCallID: 987asjd97y7atg
        CSeq: 986759 SIP_ACK
         */


        //for client transaction

        //Call-ID, From, Request-URI copy from original request
        //TO - copy from response 
        //Via - copy first Via from request
        //CSeq - copy from request
        //Route - copy from request
        //if response 415 - copy Accept from response

        //Request retValue = new Request(null);
        BaseSipMessage.Builder retValue = new BaseSipMessage.Builder(BaseSipMessage.Builder.Type.REQUEST);

        Request originalMessage = dialog.getMessageHistory().getFirstMessage();
        Response lastResponse = dialog.getMessageHistory().findLastResponse();

        retValue.method(MessageType.SIP_ACK.stringValue());

        //TODO review, originalMessage.getContacts().getContacts().get(0) - return own address
        //BaseSipMessage messageForContact = (ringingResponse == null? originalMessage: ringingResponse);
        //UriHeader contact = messageForContact.getContacts().getContacts().get(0);
        Uri requestUri = null;

        if (dialog.getMessageHistory().getRemoteContact() != null) {
            requestUri = dialog.getMessageHistory().getRemoteContact();
        }
        else if (MessageType.parse(lastResponse.getMethod()) == MessageType.SIP_REGISTER) {
            //TODO AK review 
            requestUri = originalMessage.getRequestUri();
        }
        else {
            requestUri = constructUri(dialog.getRemoteParty());
        }

        setRequestUriHeader(requestUri, retValue);

        addRoutes(retValue, lastResponse.getRecordRoutes());
/*        if(lastResponse != null) {
            List<UriHeader> recordRoutes = lastResponse.getRecordRoutes();
            for(int i = recordRoutes.size() - 1; i >= 0; i--) {
                retValue.addRoute(recordRoutes.get(i));
            }
        }
*/
        Configuration config = context.getConfig();
        addUserAgentHeader(config, retValue);
        addViaHeader(originalMessage.getVias().get(0), retValue);
        addToHeader(lastResponse.getTo(), retValue);
        //addFromHeader(originalMessage.getFrom(), retValue);
        addFromHeader(dialog, retValue);
        addMaxForwardsHeader(config, retValue);
        addCallIdHeader(originalMessage.getCallId(), retValue);
        addCSeqHeader(lastResponse.getcSeq(), retValue);

        return retValue;
    }

}
