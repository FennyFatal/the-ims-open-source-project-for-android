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

import javax.microedition.ims.common.*;
import javax.microedition.ims.common.util.CollectionsUtils;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.registry.ClientRegistry;
import javax.microedition.ims.core.registry.CommonRegistry;
import javax.microedition.ims.core.registry.RegistryUtils;
import javax.microedition.ims.messages.utils.MessageUtils;
import javax.microedition.ims.messages.utils.StatusCode;
import javax.microedition.ims.messages.wrappers.sdp.ConnectionInfo;
import javax.microedition.ims.messages.wrappers.sdp.SdpMessage;
import javax.microedition.ims.messages.wrappers.sip.*;
import java.util.Collection;
import java.util.List;

public final class ResponseMessageBuilder extends BaseMessageBuilder implements IResponseMessageBuilder {
    public ResponseMessageBuilder(final Dialog dialog, final StackContext context) {
        super(dialog, context);
    }


    public Response buildMessage(final BaseSipMessage request, final int statusCode, final String reasonPhrase) {
        BaseSipMessage.Builder builder = buildCustomMessage(request, statusCode, reasonPhrase);
        addClientDataToMessage(builder, true);
        return (Response) builder.build();
    }

    protected BaseSipMessage.Builder buildCustomMessage(
            final BaseSipMessage sipMsg,
            final int statusCode,
            final String reasonPhrase) {

        //Logger.log("At response builder:" + sipMsg.getMethod() +" "+ statusCode);
        assert sipMsg != null : "Request cannot be null";
        assert statusCode > 0;
        /*
       SIP/2.0 180 Ringing
   Via: SIP/2.0/UDP client.atlanta.example.com:5060;branch=z9hG4bK74bf9
    ;received=192.0.2.101
   Record-Route: <sip:ss2.biloxi.example.com;lr>,
    <sip:ss1.atlanta.example.com;lr>
   From: Alice <sip:alice@atlanta.example.com>;tag=9fxced76sl
   To: Bob <sip:bob@biloxi.example.com>;tag=314159
   Call-ID: 2xTb9vxSit55XU7p8@atlanta.example.com
   CSeq: 1 SIP_INVITE
   Contact: <sip:bob@client.biloxi.example.com>
   Content-Length: 0

     SIP/2.0 200 OK
   Via: SIP/2.0/UDP client.atlanta.example.com:5060;branch=z9hG4bK74bf9
    ;received=192.0.2.101
   From: Alice <sip:alice@atlanta.example.com>;tag=9fxced76sl
   To: Bob <sip:bob@biloxi.example.com>
   Call-ID: 2xTb9vxSit55XU7p8@atlanta.example.com
   CSeq: 1 SIP_CANCEL
   Content-Length: 0
         */

        //Response retValue = new Response();
        BaseSipMessage.Builder retValue = new BaseSipMessage.Builder(BaseSipMessage.Builder.Type.RESPONSE);

        retValue.statusCode(statusCode);

        String responseMessage = reasonPhrase == null ? MessageUtils.getMessageByCode(statusCode) : reasonPhrase;
        retValue.reasonPhrase(responseMessage);

        Collection<UriHeader> recordRoutes = sipMsg.getRecordRoutes();
        //for(int i = recordRoutes.size() - 1; i >= 0; i--) {
        for (UriHeader recordRoute : recordRoutes) {
            retValue.recordRoute(recordRoute);
        }

        for (Via via : sipMsg.getVias()) {
            retValue.via(via);
        }

        /** TODO AK 
         * The 100 (Trying) response is constructed according to the procedures in Section 8.2.6, 
         *  except that the insertion of tags in the To HEADER field of the response
         *  (when NONE was present in the sipMsg) is downgraded from MAY to SHOULD NOT.
         */

        //UriHeader to = sipMsg.getTo();
        UriHeader.UriHeaderBuilder to = new UriHeader.UriHeaderBuilder(sipMsg.getTo());
        if (statusCode != StatusCode.TRYING) {
            to.tag(dialog.getLocalTag());
        }
        //retValue.to(sipMsg instanceof Request ? to : new UriHeader.UriHeaderBuilder(sipMsg.getFrom()));
        addToHeader(sipMsg instanceof Request ? to : new UriHeader.UriHeaderBuilder(sipMsg.getFrom()), retValue);

        addFromHeader(sipMsg instanceof Request ? sipMsg.getFrom() : sipMsg.getTo(), retValue);

        generateContactHeader(context.getConfig(), retValue, false);
        addMaxForwardsHeader(context.getConfig(), retValue);
        addCallIdHeader(dialog, retValue);

        retValue.cSeq(sipMsg.getcSeq());
        retValue.method(sipMsg.getMethod());
        //addSupported(retValue);

        if (statusCode > 100 && statusCode < 200) {
            boolean requireRel = CollectionsUtils.contains(sipMsg.getRequire(), OptionFeature._100REL.getName())
                    || CollectionsUtils.contains(sipMsg.getSupported(), OptionFeature._100REL.getName());

            if (requireRel) {
                addRequire(retValue, OptionFeature._100REL.getName());
                addRSeqHeader(dialog, retValue);
            }
        }

        addRetryAfterHeader(retValue);

        MessageType requestType = MessageType.parse(sipMsg.getMethod());
        if (statusCode == StatusCode.OK) {
            if (MessageType.SIP_INVITE == requestType) {
                if (!context.getConfig().useResourceReservation() || dialog.getIncomingSdpMessage().typeSupported(SDPType.MSRP)) {
                    addBody(retValue);
                }
                //INVITE refresh code
                //TODO check if it's 200 ok for initial invite
                if (sipMsg.getSessionExpires() != null) {
                    addSessionExpires(retValue, context.getConfig().getRefresher(), sipMsg.getSessionExpires().getExpiresValue());
                }
            } else if (MessageType.SIP_OPTIONS == requestType) {
                /**
                 * Allow: INVITE, ACK, CANCEL, OPTIONS, BYE 
                 * Accept: application/sdp 
                 * Accept-Encoding: gzip 
                 * Accept-Language: en 
                 * Supported: foo
                 */
                CommonRegistry commonRegistry = context.getStackRegistry().getCommonRegistry();
                ClientRegistry[] clientRegistries = context.getStackRegistry().getClientRegistries();

                addAcceptEncoding(retValue, commonRegistry.getEncodings());
                addAcceptLanguages(retValue, commonRegistry.getLanguages());
                addAccept(retValue, RegistryUtils.getContentTypes(clientRegistries));
                //addSupported(retValue, RegistryUtils.getRegEntities(commonRegistry));

                List<String> acceptTypeValue = sipMsg.getCustomHeader(Header.Accept);
                if (!acceptTypeValue.isEmpty() && MimeType.APP_SDP.stringValue().equals(acceptTypeValue.get(0))) {
                    addBody(retValue);
                }
            }
        } else if (statusCode == StatusCode.CALL_SESSION_PROGRESS) {
            addBody(retValue);
        }

        if (MessageType.SIP_OPTIONS == requestType || statusCode == StatusCode.METHOD_NOT_ALLOWED) {
            addAllow(retValue);
        }

        return retValue;
    }

    private void addBody(final BaseSipMessage.Builder retValue) {
        //TODO review it
        //retValue.setContentType(new ParamHeader(ContentType.SDP.stringValue()));
        //final ParamHeader paramHeader = new ParamHeader.ParamHeaderBuilder(ContentType.SDP.stringValue()).build();
        retValue.contentType(MimeType.APP_SDP.stringValue());

        final SdpMessage sdpMessage = dialog.getOutgoingSdpMessage();
        final String localAddress = context.getEnvironment().getConnectionManager().getInetAddress();

        sdpMessage.setSessionAddress(localAddress);

        if (sdpMessage.getConnectionInfo() == null) {
            sdpMessage.setConnectionInfo(new ConnectionInfo(localAddress, true));
        }

        String body = sdpMessage.getContent();
        Logger.log("At response builder:" + body);

        retValue.body(body.getBytes());
    }

    /*  private byte[] getSDP(String localAddress){
        byte[] sdp = new byte[0];

        String sdpText = "";
        //String sdpText = "v=0\no=Foo 0 0 IN IP4 127.0.0.1\ns=-\nc=IN IP4 127.0.0.1\nt=0 0\nm=audio 10000 RTP/AVP 0";
        String[] values = new String[] {"v=0", "o=- 6603 1 IN IP4 " + localAddress, "s=-", "t=0 0 " , "m=audio 8500 RTP/AVP 0 ","c=IN IP4 "+ localAddress,  "a=rtpmap:0 PCMU/8000",
                "a=sendrecv", "a=X-alt:0 1.00 : dummy-USER dummy-password " +localAddress + " 8500"};

        for(int i = 0; i < values.length; i++) {
            sdpText += values[i];
            if(i < values.length) {
                sdpText += "\n";     
            }
        }
        try {
            sdp = sdpText.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {            
            e.printStackTrace();
        }
        return sdp;
    }*/

    /*    private void addSupported(Response retValue, String[] supportedEntities) {
            retValue.getSupported().addAll(Arrays.asList(supportedEntities));
        }
    */
    private void addAcceptLanguages(final BaseSipMessage.Builder retValue, final String[] languages) {
        String acceptLanguagesValue = CollectionsUtils.concatenate(languages, ", ");

        if (acceptLanguagesValue != null) {
            retValue.customHeader(Header.Accept_Language, acceptLanguagesValue);
        }
    }

    private void addAcceptEncoding(final BaseSipMessage.Builder retValue, String[] encodings) {
        String acceptEncodingsValue = CollectionsUtils.concatenate(encodings, ", ");

        if (acceptEncodingsValue != null) {
            retValue.customHeader(Header.Accept_Encoding, acceptEncodingsValue);
        }
    }

    private void addAccept(final BaseSipMessage.Builder retValue, final MimeType[] contentTypes) {

        String acceptValue = CollectionsUtils.concatenate(
                contentTypes,
                ", ",
                new CollectionsUtils.Transformer<MimeType, String>() {
                    public String transform(MimeType contentType) {
                        return contentType.stringValue();
                    }
                }
        );

        if (acceptValue != null) {
            retValue.customHeader(Header.Accept, acceptValue);
        }
    }

    private void addAccept(final BaseSipMessage.Builder retValue, final String[] contentTypes) {
        String acceptValue = CollectionsUtils.concatenate(contentTypes, ", ");
        if (acceptValue != null) {
            retValue.customHeader(Header.Accept, acceptValue);
        }
    }
}
