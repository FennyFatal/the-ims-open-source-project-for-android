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
import javax.microedition.ims.messages.wrappers.common.Uri;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;


public class PageMessageBuilder extends RequestMessageBuilder {

    public PageMessageBuilder(final Dialog dialog, final StackContext context) {
        super(dialog, context);
    }


    /*
    *
SIP_MESSAGE sip:user2@domain.com SIP/2.0
  Via: SIP/2.0/TCP user1pc.domain.com;branch=z9hG4bK776sgdkse
  Max-Forwards: 70
  From: sip:user1@domain.com;tag=49583
  To: sip:user2@domain.com
  Call-ID: asd88asd77a@1.2.3.4
  CSeq: 1 SIP_MESSAGE
  Content-Type: text/plain
  Content-Length: 18

  Watson, come here.

    */
    protected BaseSipMessage.Builder buildCustomMessage() {
        //Request retValue = new Request(null);
        BaseSipMessage.Builder retValue = new BaseSipMessage.Builder(BaseSipMessage.Builder.Type.REQUEST);

        //final UriHeader uriHeader = SipUriParser.parseUri(dialog.getRemoteParty() + ";user=phone");
        //final UriHeader uriHeader = constructRequestUri(dialog.getRemoteParty() + ";user=phone");
        final Uri requestUri = constructUri(dialog.getRemoteParty() + ";user=phone");
        
        //requestUri.setParamsList(uriHeader.getParamsList());
        //String uriii = requestUri.buildContent();

        setRequestUriHeader(requestUri, retValue);
        setMethodHeader(MessageType.SIP_MESSAGE, retValue);
        generateViaHeader(context.getConfig(), retValue);
        addMaxForwardsHeader(context.getConfig(), retValue);
        addFromHeader(dialog, retValue);

        final Uri toUri = constructUri(requestUri.getShortURINoParams());

        addToHeader(toUri, retValue);
        addCallIdHeader(dialog, retValue);
        addUserAgentHeader(context.getConfig(), retValue);
        addCSeqHeader(dialog, retValue);
        
        addPreferedIdentity(retValue);

        final GsmLocationInfo locationInfo = context.getEnvironment().getGsmLocationService().getGsmLocationInfo();
        addPAccessNetworkHeader(locationInfo, retValue);
        
        addAuthorizationHeader(retValue, MessageType.SIP_MESSAGE);

//        buildAcceptContactForClient(retValue, dialog.getLocalParty());
        buildAcceptContactForClient(retValue, dialog);

        //TODO for test
        //retValue.addCustomHeader(Header.Content_Type.stringValue(), "text/plain");
        //retValue.addSupported("100rel");
        //retValue.addSupported("eventlist");
        //retValue.addCustomHeader(Header.Accept.stringValue(), "text/plain");
        //retValue.addCustomHeader(Header.AcceptContact.stringValue(), "*;audio;video;application;app-subtype=\"test\";events=\"presence\";+g.3gpp.icsi-ref=\"urn:3gpp:3gpp-service.ims.icsi.mmtel\"");
        //retValue.addCustomHeader(Header.AcceptContact.stringValue(), "*;events=\"refer\"");
        //retValue.addCustomHeader(Header.AcceptContact.stringValue(), "*;+g.3gpp.iari-ref=\"urn:IMSAPI:javax.microedition.ims.engine.test\";require;+g.3gpp.icsi-ref=\"urn:3gpp:org.3gpp.icsi;require\"");
        generateContactHeader(context.getConfig(), retValue);

        return retValue;
    }
}
