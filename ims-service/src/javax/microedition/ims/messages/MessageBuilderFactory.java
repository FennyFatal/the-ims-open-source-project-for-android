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

package javax.microedition.ims.messages;

import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.msrp.MSRPTransactionDescriptor;
import javax.microedition.ims.messages.builder.*;
import javax.microedition.ims.messages.builder.msrp.*;


/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 11-Dec-2009
 * Time: 11:52:46
 */
public final class MessageBuilderFactory {

    private static final String LOG_TAG = "MessageBuilderFactory";
    private final Dialog dialog;
    private final StackContext context;

    //TODO: separate in two classes
    private final MSRPTransactionDescriptor descriptor;

    //* Sip */
    public MessageBuilderFactory(final Dialog dialog, final StackContext context) {
        assert dialog != null;
        assert context != null;

        this.descriptor = null;
        this.dialog = dialog;
        this.context = context;
    }

    //* Msrp */
    public MessageBuilderFactory(final MSRPTransactionDescriptor descriptor, final StackContext context) {
        assert descriptor != null;
        assert context != null;

        this.descriptor = descriptor;
        this.dialog = descriptor.getMsrpSession().getMsrpDialog();
        this.context = context;
    }

    public IRequestMessageBuilder getRequestBuilder(MessageType type) {
        IRequestMessageBuilder ret = null;
        switch (type) {
            case SIP_REGISTER:
                ret = new RegisterMessageBuilder(dialog, context);
                break;
            case SIP_INVITE:
                ret = new InviteMessageBuilder(dialog, context);
                break;
            case SIP_ACK:
                ret = new AckMessageBuilder(dialog, context);
                break;
            case SIP_BYE:
                ret = new ByeMessageBuilder(dialog, context);
                break;
            case SIP_CANCEL:
                ret = new CancelMessageBuilder(dialog, context);
                break;
            case SIP_PRACK:
                ret = new PrackMessageBuilder(dialog, context);
                break;
            case SIP_REFER:
                ret = new ReferMessageBuilder(dialog, context);
                break;
            case SIP_SUBSCRIBE:
                ret = new SubscribeMessageBuilder(dialog, context);
                break;
            case SIP_NOTIFY:
                ret = new NotifyMessageBuilder(dialog, context);
                break;
            case SIP_MESSAGE:
                ret = new PageMessageBuilder(dialog, context);
                break;
            case SIP_OPTIONS:
                ret = new OptionsMessageBuilder(dialog, context);
                break;
            case SIP_PUBLISH:
                ret = new PublishMessageBuilder(dialog, context);
                break;
            case SIP_UPDATE:
                ret = new UpdateMessageBuilder(dialog, context);
                break;
            default:
                assert false : "Unknown request builder type: " + type;
                break;
        }
        return ret;
    }

    public IMsrpMessageBuilder getMsrpBuilder(MessageType type) {
        IMsrpMessageBuilder ret = null;
        switch (type) {
            case MSRP_SEND:
                ret = new MsrpSendMesssageBuilder(descriptor, context);
                break;
            default:
                assert false : "Unknown request builder type: " + type;
                break;
        }
        return ret;
    }

    public IResponseMessageBuilder getResponseBuilder() {
        return new ResponseMessageBuilder(dialog, context);
    }

    public IMsrpResponseAndReportMessageBuilder getMsrpResponseBuilder(MessageType type) {
        IMsrpResponseAndReportMessageBuilder ret = null;
        switch (type) {
            case MSRP_STATUS:
                ret = new MsrpResponseMessageBuilder(dialog, context);
                break;
            case MSRP_REPORT:
                ret = new MsrpReportMesssageBuilder(descriptor.getMsrpSession(), context);
                break;
            default:
                assert false : "Unknown request builder type: " + type;
                break;
        }
        return ret;
    }
}
