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

package javax.microedition.ims.core.transaction;

import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.core.xdm.XDMResponse;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;
import javax.microedition.ims.messages.wrappers.sip.Request;
import javax.microedition.ims.messages.wrappers.sip.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 24-Feb-2010
 * Time: 17:05:24
 */
final class TransactionTypeData {

    /**
     * TransactionType.SIP_INVITE_CLIENT data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> INVITE_CLIENT_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_INVITE, Response.class);
                    put(MessageType.SIP_PRACK, Response.class);
                    put(MessageType.SIP_CANCEL, Response.class);
                }
            };

    /**
     * TransactionType.SIP_REINVITE_CLIENT data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> REINVITE_CLIENT_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_INVITE, Response.class);
                    put(MessageType.SIP_PRACK, Response.class);
                    put(MessageType.SIP_CANCEL, Response.class);
                }
            };


    /**
     * TransactionType.SIP_INVITE_SERVER data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> INVITE_SERVER_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_PRACK, Request.class);
                    put(MessageType.SIP_ACK, Request.class);
                    put(MessageType.SIP_INVITE, Request.class);
                    put(MessageType.SIP_UPDATE, Request.class);
                    put(MessageType.SIP_CANCEL, Request.class);
                }
            };

    /**
     * TransactionType.SIP_REINVITE_SERVER data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> REINVITE_SERVER_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_PRACK, Request.class);
                    put(MessageType.SIP_ACK, Request.class);
                }
            };


    /**
     * TransactionType.SIP_LOGIN data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> LOGIN_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_REGISTER, Response.class);
                }
            };

    /**
     * TransactionType.SIP_LOGOUT data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> LOGOUT_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_REGISTER, Response.class);
                }
            };

    /**
     * TransactionType.SIP_LOGIN data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> SUBSCRIBE_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_SUBSCRIBE, Response.class);
                }
            };

    /**
     * TransactionType.SIP_LOGOUT data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> UNSUBSCRIBE_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_SUBSCRIBE, Response.class);
                }
            };


    /**
     * TransactionType.SIP_BYE_CLIENT data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> BYE_CLIENT_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_BYE, Response.class);
                }
            };

    /**
     * TransactionType.SIP_BYE_SERVER data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> BYE_SERVER_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_ACK, Request.class);
                }
            };


    /**
     * TransactionType.SIP_REFER_CLIENT data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> REFER_CLIENT_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_REFER, Response.class);
                }
            };

    /**
     * TransactionType.SIP_REFER_SERVER data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> REFER_SERVER_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_ACK, Request.class);
                }
            };

    /**
     * TransactionType.SIP_NOTIFY_CLIENT data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> NOTIFY_CLIENT_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_NOTIFY, Response.class);
                }
            };

    /**
     * TransactionType.SIP_NOTIFY_SERVER data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> NOTIFY_SERVER_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_ACK, Request.class);
                }
            };

    /**
     * TransactionType.SIP_MESSAGE_SERVER data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> MESSAGE_SERVER_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_MESSAGE, Request.class);
                }
            };

    /**
     * TransactionType.SIP_MESSAGE_SERVER data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> MESSAGE_CLIENT_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_MESSAGE, Response.class);
                }
            };

    /**
     * TransactionType.SIP_MESSAGE_SERVER data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> OPTIONS_SERVER_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_OPTIONS, Request.class);
                }
            };

    /**
     * TransactionType.SIP_OPTIONS_CLIENT data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> OPTIONS_CLIENT_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_OPTIONS, Response.class);
                }
            };

    /**
     * TransactionType.SIP_PUBLISH_CLIENT data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> PUBLISH_CLIENT_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.SIP_PUBLISH, Response.class);
                }
            };

    /**
     * TransactionType.SIP_BYE_CLIENT data
     */
    static final Map<MessageType, Class<? extends IMSMessage>> XDM_REQUEST_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.XDM_REQUEST, XDMResponse.class);
                }
            };

    static final Map<MessageType, Class<? extends IMSMessage>> MSRP_SEND_APPLICABLE_MESSAGES =
            new HashMap<MessageType, Class<? extends IMSMessage>>() {
                {
                    put(MessageType.MSRP_SEND, MsrpMessage.class);
                    put(MessageType.MSRP_REPORT, MsrpMessage.class);
                    put(MessageType.MSRP_STATUS, MsrpMessage.class);
                }
            };

    /* static final Map<MessageType, Class<? extends IMSMessage>> MSRP_REPORT_APPLICABLE_MESSAGES =
        new HashMap<MessageType, Class<? extends IMSMessage>>() {
        {
            put(MessageType.MSRP_REPORT, MsrpMessage.class);
        }
    };*/

    private TransactionTypeData() {
    }
}
