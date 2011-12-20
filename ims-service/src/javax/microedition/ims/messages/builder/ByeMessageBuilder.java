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

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.messages.wrappers.common.Uri;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Request;
import javax.microedition.ims.messages.wrappers.sip.Response;

public class ByeMessageBuilder extends RequestMessageBuilder {

    public ByeMessageBuilder(final Dialog dialog, final StackContext context) {
        super(dialog, context);
    }

    protected BaseSipMessage.Builder buildCustomMessage() {
        /*
        SIP_BYE sip:alice@pc33.atlanta.com SIP/2.0
Via: SIP/2.0/UDP 192.0.2.4;branch=z9hG4bKnashds10
Max-Forwards: 70
From: Bob <sip:bob@biloxi.com>;tag=a6c85cf
To: Alice <sip:alice@atlanta.com>;tag=1928301774
Call-ID: a84b4c76e66710
CSeq: 231 SIP_BYE
Content-Length: 0
             */

        Request originalMessage = dialog.getMessageHistory().getFirstMessage();
        Response responseMessage = dialog.getMessageHistory().findLastResponse();
        //Response ringingResponse = (Response)DIALOG.getCustomParameter(Dialog.ParamKey.RINGING_MESSAGE);

        ByeBuilder<? extends BaseSipMessage> byeBuilder = createByeBuilder(originalMessage, responseMessage);
        Logger.log("Bye builder: " + byeBuilder.getClass().getSimpleName());

        byeBuilder.buildMethod();
        byeBuilder.buildRequestUri();
        byeBuilder.buildRoutes();
        byeBuilder.buildUserAgent();
        byeBuilder.buildVia();
        byeBuilder.buildFrom();
        byeBuilder.buildTo();
        byeBuilder.buildMaxForwards();
        byeBuilder.buildCallId();
        byeBuilder.buildCSeq();

        return byeBuilder.getByeRequest().getBuilder();
    }

    private ByeBuilder<? extends BaseSipMessage> createByeBuilder(Request originalMessage, Response responseMessage) {
        return responseMessage == null ? new UASByeBuilder(originalMessage) : new UACByeBuilder(responseMessage);
    }


    /**
     * This class responsible for building Bye request.
     *
     * @author ext-akhomush
     * @param <T> - source message for building SIP_BYE request
     */
    abstract class ByeBuilder<T extends BaseSipMessage> {
        protected final BaseSipMessage.Builder byeBuilder = new BaseSipMessage.Builder(BaseSipMessage.Builder.Type.REQUEST);

        protected final T source;

        public ByeBuilder(T source) {
            assert source != null;
            this.source = source;
        }

        public void buildMethod() {
            setMethodHeader(MessageType.SIP_BYE, byeBuilder);
        }

        public void buildRequestUri() {
            final Uri byeRequestURI;

            if (dialog.getMessageHistory().getRemoteContact() != null) {
                byeRequestURI = dialog.getMessageHistory().getRemoteContact();
            }
            else {
                //byeRequestURI = SipUriParser.parseUri(dialog.getRemoteParty()).getUri();
                byeRequestURI = constructUri(dialog.getRemoteParty());
            }
            setRequestUriHeader(byeRequestURI, byeBuilder);
        }

        public void buildRoutes() {
            addRoutes(byeBuilder, source.getRecordRoutes());
/*            List<UriHeader> recordRoutes = source.getRecordRoutes();
            for (int i = recordRoutes.size() - 1; i >= 0; i--) {
                byeBuilder.addRoute(recordRoutes.get(i));
            }
*/
        }

        public void buildUserAgent() {
            addUserAgentHeader(context.getConfig(), byeBuilder);
        }

        public void buildMaxForwards() {
            addMaxForwardsHeader(context.getConfig(), byeBuilder);
        }

        public void buildCallId() {
            addCallIdHeader(source.getCallId(), byeBuilder);
        }

        public void buildCSeq() {
            addCSeqHeader(dialog, byeBuilder);
        }

        public Request getByeRequest() {
            return (Request) byeBuilder.build();
        }

        public void buildFrom() {
            if (source.getTo().buildContent().contains(dialog.getRemoteParty())) {
                addFromHeader(source.getFrom(), byeBuilder);
            }
            else {
                addFromHeader(source.getTo(), byeBuilder);
            }
        }

        public void buildTo() {
            if (source.getTo().buildContent().contains(dialog.getRemoteParty())) {
                addToHeader(source.getTo(), byeBuilder);
            }
            else {
                addToHeader(source.getFrom(), byeBuilder);
            }
        }

        public abstract void buildVia();
    }

    /**
     * This class responsible for building SIP_BYE for UAC transaction.
     * Class uses SIP_RESPONSE message from remote party as source.
     *
     * @author ext-akhomush
     */
    class UACByeBuilder extends ByeBuilder<Response> {

        //for client transaction

        //Call-ID, From, Request-URI copy from original request
        //TO - copy from response 
        //Via - copy first Via from request
        //CSeq - copy from request
        //Route - copy from request
        //if response 415 - copy Accept from response

        public UACByeBuilder(Response source) {
            super(source);
        }

        public void buildVia() {
            generateViaHeader(context.getConfig(), byeBuilder);
        }
    }

    /**
     * This class responsible for building SIP_BYE for UAS transaction.
     * Class uses SIP_INVITE from remote party as source.
     *
     * @author ext-akhomush
     */
    class UASByeBuilder extends ByeBuilder<Request> {

        public UASByeBuilder(Request source) {
            super(source);
        }

        public void buildVia() {
            generateViaHeader(context.getConfig(), byeBuilder);
        }
    }
}    
