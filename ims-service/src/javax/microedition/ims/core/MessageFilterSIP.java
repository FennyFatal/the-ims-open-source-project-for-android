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

package javax.microedition.ims.core;


import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dispatcher.Filter;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Request;
import javax.microedition.ims.messages.wrappers.sip.Response;
import javax.microedition.ims.util.MessageUtilHolder;
import javax.microedition.ims.util.SipMessageUtil;
import java.util.*;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 24-Feb-2010
 * Time: 15:39:33
 */
public class MessageFilterSIP implements Filter<BaseSipMessage> {

    private final IMSEntity entity;
    private final Set<String> allowedBranches;
    private final Map<MessageType, Class<? extends IMSMessage>> applicableMessages;

    public MessageFilterSIP(
            final IMSEntity entity,
            final Map<MessageType, Class<? extends IMSMessage>> applicableMessages) {

        this.entity = entity;

        this.allowedBranches = Collections.synchronizedSet(new HashSet<String>(10));


        final Map<MessageType, Class<? extends IMSMessage>> msgMap =
                applicableMessages == null || applicableMessages.size() == 0 ?
                        createDefaultMessageMap() :
                        applicableMessages;

        this.applicableMessages = Collections.unmodifiableMap(new HashMap<MessageType, Class<? extends IMSMessage>>(msgMap));
    }

    public MessageFilterSIP(
            final IMSEntity entity,
            final Map<MessageType, Class<? extends IMSMessage>> applicableMessages,
            final String branch) {

        this(entity, applicableMessages);
        this.allowedBranches.add(branch);
    }

    private Map<MessageType, Class<? extends IMSMessage>> createDefaultMessageMap() {
        final Map<MessageType, Class<? extends IMSMessage>> msgMap;
        final MessageType[] messageTypes = MessageType.values();
        msgMap = new HashMap<MessageType, Class<? extends IMSMessage>>(messageTypes.length);

        for (MessageType messageType : messageTypes) {
            msgMap.put(messageType, BaseSipMessage.class);
        }
        return msgMap;
    }


    public boolean isApplicable(final BaseSipMessage msg) {

        assert allowedBranches.size() > 0 : "No branches were added by outgoing messages";

        boolean retValue = false;

        if (entity.getIMSEntityId().equals(msg.getIMSEntityId())) {
            MessageType messageType = MessageType.parse(msg.getMethod());

            final String incomingMessageBranch = getBranch(msg);

            Logger.log(Logger.Tag.MESSAGE_DISPATCHER, "filter branch. allowed branches=" + allowedBranches + " message branch=" +
                    incomingMessageBranch + "(" + msg.shortDescription() + ")");

            retValue = applicableMessages.containsKey(messageType) &&
                    applicableMessages.get(messageType).isAssignableFrom(msg.getClass());

            //TODO: this is hot fix for PRACK messages. PRACK must be implemented as separate transaction.
            //if(MessageType.SIP_PRACK != messageType && MessageType.SIP_INVITE != messageType && MessageType.SIP_ACK != messageType){

            if (MessageType.SIP_PRACK == messageType) {
                if (msg instanceof Request) {
                    final Dialog dialog = (Dialog) entity;
                    boolean hasAddressee = false;
                    final Response response = dialog.getMessageHistory().findPrackAddressee((Request) msg);
                    if (response != null) {
                        final String prackAddresseeBranch = getBranch(response);
                        hasAddressee = allowedBranches.contains(prackAddresseeBranch);
                    }

                    retValue = retValue && hasAddressee;
                }
            }
            else if (MessageType.SIP_ACK == messageType) {
            }
            else {
                retValue = retValue && allowedBranches.contains(incomingMessageBranch);
            }
        }

        return retValue;
    }

    public void update(final BaseSipMessage msg) {
        final String msgBranch = getBranch(msg);
        allowedBranches.add(msgBranch);
    }

    private String getBranch(BaseSipMessage msg) {
        String retValue;

        final SipMessageUtil<BaseSipMessage> sipMessageUtil = MessageUtilHolder.getSIPMessageUtil();
        retValue = sipMessageUtil.getMessageBranch(msg);
        //assert retValue != null : "Branch not found for message: " + msg;

        if (retValue == null) {
            Logger.log(Logger.Tag.WARNING, "Branch not found for message: " + msg);
        }

        return retValue;
    }


    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("MessageFilterSIP");
        sb.append("{entity=").append(entity);
        sb.append(", applicableMessages=").append(applicableMessages);
        sb.append('}');
        return sb.toString();
    }
}
