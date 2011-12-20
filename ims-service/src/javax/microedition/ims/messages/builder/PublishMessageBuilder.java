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
import javax.microedition.ims.core.dialog.Dialog.ParamKey;
import javax.microedition.ims.core.sipservice.publish.PublishInfo;
import javax.microedition.ims.messages.wrappers.common.Uri;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;

public class PublishMessageBuilder extends RequestMessageBuilder {

    public PublishMessageBuilder(Dialog dialog, StackContext context) {
        super(dialog, context);
    }


    protected BaseSipMessage.Builder buildCustomMessage() {

        //Request retValue = new Request();
        BaseSipMessage.Builder retValue = new BaseSipMessage.Builder(BaseSipMessage.Builder.Type.REQUEST);

        setMethodHeader(MessageType.SIP_PUBLISH, retValue);
        //final Uri requestUri = SipUriParser.parseUri(dialog.getRemoteParty()).getUri();
        final Uri requestUri = constructUri(dialog.getRemoteParty());

        setRequestUriHeader(requestUri, retValue);
        generateViaHeader(context.getConfig(), retValue);
        addToHeader(requestUri, retValue);
        addFromHeader(dialog, retValue);
        addCallIdHeader(dialog, retValue);
        addCSeqHeader(dialog, retValue);

        // Supported: 100rel, eventlist
        //addSupported(retValue);
        // retValue.addSupported(HeaderValues.SUPPORTED_EVENTLIST);

        PublishInfo publishInfo = (PublishInfo) dialog
                .getCustomParameter(ParamKey.PUBLISH_INFO);
        assert publishInfo != null : "Dialog does not contain publish information";

        retValue.event(publishInfo.getEventType().stringValue());

        /**
         * Operation | Body? | SIP-If-Match? | Expires Value |
         * +-----------+-------+---------------+---------------+ | Initial | yes
         * | no | > 0 | | Refresh | no | yes | > 0 | | Modify | yes | yes | > 0
         * | | Remove | no | yes | 0 |
         * +-----------+-------+---------------+--------------
         */
        switch (publishInfo.getPublishType()) {
            case INITIAL:
                retValue.expires((Long) dialog.getCustomParameter(ParamKey.PUBLISH_EXPIRES));
                retValue.contentType(publishInfo.getContentType().stringValue());
                retValue.body(publishInfo.getBody());
                break;
            case MODIFY:
                retValue.expires((Long) dialog.getCustomParameter(ParamKey.PUBLISH_EXPIRES));
                retValue.ifMatch(publishInfo.getETag());
                retValue.contentType(publishInfo.getContentType().stringValue());
                retValue.body(publishInfo.getBody());
                break;
            case REFRESH:
                retValue.expires((Long) dialog.getCustomParameter(ParamKey.PUBLISH_EXPIRES));
                retValue.ifMatch(publishInfo.getETag());
                break;

            case REMOVE:
                retValue.ifMatch(publishInfo.getETag());
                retValue.expires(0);
                break;

            default:
                assert false : "Unknown publish type";
        }

        addAuthorizationHeader(retValue, MessageType.SIP_PUBLISH);
        addUserAgentHeader(context.getConfig(), retValue);
        return retValue;
    }

}
