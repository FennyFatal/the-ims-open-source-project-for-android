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
import javax.microedition.ims.messages.wrappers.sip.Header;
import javax.microedition.ims.messages.wrappers.sip.Response;
import javax.microedition.ims.messages.wrappers.sip.UriHeader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class responsible for building Prack message.
 *
 * @author ext-akhomush
 */
public class PrackMessageBuilder extends RequestMessageBuilder {

    public PrackMessageBuilder(final Dialog dialog, final StackContext context) {
        super(dialog, context);
    }

    protected BaseSipMessage.Builder buildCustomMessage() {
        /*
        SIP_PRACK sip:+7210000011@192.168.163.166;USER=phone SIP/2.0
		To: <sip:+7210000011@192.168.163.166>
		From: <sip:unavailable@siptlarge2.ericsson.ie>;tag=y350ebuif
		Call-ID: 97kz4kf9qcc5i2p@192.168.163.114
		CSeq: 12222 SIP_PRACK
		Max-Forwards: 70
		RAck: 12321 12221 SIP_INVITE
		Via: SIP/2.0/UDP siptlarge2.ericsson.ie:5060;branch=z9hG4bKr0494ria4yldxyyrnj
		Content-Length: 0
        */

        //Request retValue = new Request();
        BaseSipMessage.Builder retValue = new BaseSipMessage.Builder(BaseSipMessage.Builder.Type.REQUEST);

        //SIP_RESPONSE witch contains HEADER require with value 100rel and status code 101...199
        if (!(dialog.getMessageHistory().getLastMessage() instanceof Response)) {
            Logger.log("We are waiting response message");
            try {
                assert false;
            }
            catch (Error e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        Response responseMessage = (Response) dialog.getMessageHistory().getLastMessage();

        //status line
        setMethodHeader(MessageType.SIP_PRACK, retValue);
        retValue.method(MessageType.SIP_PRACK.stringValue());

        Uri requestUri = null;
        if (responseMessage == null) {
            //requestUri = SipUriParser.parseUri(dialog.getRemoteParty()).getUri();
            requestUri = constructUri(dialog.getRemoteParty());
        }
        else {
            final List<UriHeader> uriHeaders = new ArrayList<UriHeader>(responseMessage.getContacts().getContactsList());
            requestUri = uriHeaders.get(0).getUri();
        }
        setRequestUriHeader(requestUri, retValue);

        addRoutes(retValue, responseMessage.getRecordRoutes());

        addFromHeader(responseMessage.getFrom(), retValue);
        addToHeader(responseMessage.getTo(), retValue);
        addCallIdHeader(responseMessage.getCallId(), retValue);
        addCSeqHeader(dialog, retValue);
        addMaxForwardsHeader(context.getConfig(), retValue);
        generateViaHeader(context.getConfig(), retValue);

        //RAck
        List<String> rSeqList = responseMessage.getCustomHeader(Header.RSeq);
        final int rSeq = Integer.parseInt(rSeqList.get(0).trim());

        addRAckHeader(rSeq, responseMessage.getcSeq(), MessageType.SIP_INVITE, retValue);

        return retValue;
    }

    private void addRAckHeader(
            final int rSeq,
            final int cSeq,
            final MessageType method,
            final BaseSipMessage.Builder retValue) {

        retValue.customHeader(Header.RAck, String.format("%s %s %s", rSeq, cSeq, method.stringValue()));
    }
}
