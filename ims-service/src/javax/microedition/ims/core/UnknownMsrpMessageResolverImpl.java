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

import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.core.dispatcher.UnknownMessageResolver;
import javax.microedition.ims.core.dispatcher.UnknownMessageResolverAdapter;
import javax.microedition.ims.core.msrp.IncomingMsrpMessageHandler;
import javax.microedition.ims.core.sipservice.invite.DialogStateException;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


class UnknownMsrpMessageResolverImpl implements UnknownMessageResolver<MsrpMessage> {
    private static final Set<MessageType> TRANSACTION_INITIATE_MESSAGES = new HashSet<MessageType>(Arrays.asList(
            MessageType.MSRP_SEND
    ));

    private final IncomingMsrpMessageHandler<MsrpMessage> incomingRequestHandler;

    private final UnknownMessageResolver<MsrpMessage> incomingSendResolver = new UnknownMessageResolverAdapter<MsrpMessage>() {
        
        public void resolveMessage(final MsrpMessage msg) throws DialogStateException {

            // Dialog DIALOG = StackHelper.searchDialogForIncomingMessage(stackContext, msg);

            MessageType messageMethodType = MessageType.parse(msg.getType().name());
            if (/*DIALOG != null && */messageMethodType == MessageType.MSRP_SEND) {
                incomingRequestHandler.handleIncomingSendMessage(msg);
            }
        }
    };

    private final UnknownMessageResolver<MsrpMessage> incomingReportResolver = new UnknownMessageResolverAdapter<MsrpMessage>() {
        
        public void resolveMessage(final MsrpMessage msg) throws DialogStateException {

            //Dialog DIALOG = StackHelper.searchDialogForIncomingMessage(stackContext, msg);

            /* if (DIALOG == null) {
                throw new DialogStateException(null, Error.REQUEST_FOR_UNKNOWN_DIALOG, msg);
            }*/

            //if (DIALOG != null) {
            incomingRequestHandler.handleIncomingReportMessage(msg);
            //} 
        }
    };

    private final UnknownMessageResolver<MsrpMessage> incomingStatusResolver = new UnknownMessageResolverAdapter<MsrpMessage>() {
        
        public void resolveMessage(final MsrpMessage msg) throws DialogStateException {

            //Dialog DIALOG = StackHelper.searchDialogForIncomingMessage(stackContext, msg);

            /* if (DIALOG == null) {
                throw new DialogStateException(null, Error.REQUEST_FOR_UNKNOWN_DIALOG, msg);
            }*/

            //if (DIALOG != null) {
            //incomingRequestHandler.handleIncomingReportMessage(msg);
            //} 
        }
    };


    public UnknownMsrpMessageResolverImpl(final StackContext stackContext, final IncomingMsrpMessageHandler<MsrpMessage> incomingRequestHandler) {
        assert incomingRequestHandler != null;
        this.incomingRequestHandler = incomingRequestHandler;
    }

    
    public void resolveMessage(final MsrpMessage msg) throws DialogStateException {
        UnknownMessageResolver<MsrpMessage> resolver = getResolver(msg);
        if (resolver != null) {
            resolver.resolveMessage(msg);
        }
        else {
            assert false : "Unsupported message type message: " + msg.shortDescription();
        }
    }

    private UnknownMessageResolver<MsrpMessage> getResolver(final MsrpMessage msg) {
        UnknownMessageResolver<MsrpMessage> retValue = null;

        final MessageType messageType = MessageType.parse(msg.getType().name());
        if (MessageType.MSRP_SEND == messageType) {
            retValue = incomingSendResolver;
        }
        else if (MessageType.MSRP_STATUS == messageType) {
            retValue = incomingStatusResolver;
        }
        else if (MessageType.MSRP_REPORT == messageType) {
            retValue = incomingReportResolver;
        }

        else {
            assert false : "Unsupported message type message: " + msg.shortDescription();
        }
        return retValue;
    }

    
    public boolean isPossiblyServerTransactionInitiatorMessage(final MsrpMessage msg) {
        return TRANSACTION_INITIATE_MESSAGES.contains(MessageType.parse(msg.getType().name()));
    }
}
