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

package javax.microedition.ims.android.core;

import javax.microedition.ims.android.core.SessionImpl.StateCode;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.messages.history.MessageHistory;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation {@link ServiceMethodImpl} for SESSION.
 *
 * @author ext-akhomush
 */
public class SessionServiceMethodImpl extends ServiceMethodImpl {
    private final AtomicReference<StateCode> state;

    public SessionServiceMethodImpl(String remoteUserId, String remoteUserDisplayName,
                                    MessageHistory history, AtomicReference<StateCode> state) {
        super(remoteUserId, remoteUserDisplayName, history);

        assert state != null;
        this.state = state;
    }

    
    protected MessageType getMethodById(int methodId) {
        MessageType method = null;
        switch (methodId) {
            case MessageImpl.MethodId.CAPABILITIES_QUERY:
                method = MessageType.SIP_OPTIONS;
                break;
            case MessageImpl.MethodId.PAGEMESSAGE_SEND:
                method = MessageType.SIP_MESSAGE;
                break;
            case MessageImpl.MethodId.PUBLICATION_UNPUBLISH:
            case MessageImpl.MethodId.PUBLICATION_PUBLISH:
                method = MessageType.SIP_PUBLISH;
                break;
            case MessageImpl.MethodId.REFERENCE_REFER:
                method = MessageType.SIP_REFER;
                break;
            case MessageImpl.MethodId.SESSION_START:
                method = MessageType.SIP_INVITE;
                break;
            case MessageImpl.MethodId.SESSION_UPDATE:
                if (state.get() == StateCode.STATE_RENEGOTIATING) {
                    method = MessageType.SIP_UPDATE;
                }
                else {
                    method = MessageType.SIP_INVITE;
                }
                break;
            case MessageImpl.MethodId.SESSION_TERMINATE:
                if (state.get() == StateCode.STATE_ESTABLISHED) {
                    method = MessageType.SIP_BYE;
                }
                else {
                    method = MessageType.SIP_CANCEL;
                }
                break;
            case MessageImpl.MethodId.SUBSCRIPTION_UNSUBSCRIBE:
            case MessageImpl.MethodId.SUBSCRIPTION_SUBSCRIBE:
                method = MessageType.SIP_SUBSCRIBE;
                break;
            default:
                Logger.log("Unhandled methodId:" + methodId, TAG);
                assert false : "Unhandled methodId:" + methodId;
        }
        return method;
    }
}
