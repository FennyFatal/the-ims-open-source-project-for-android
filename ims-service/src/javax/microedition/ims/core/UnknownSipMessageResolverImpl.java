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

import javax.microedition.ims.StackHelper;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dispatcher.UnknownMessageResolver;
import javax.microedition.ims.core.dispatcher.UnknownMessageResolverAdapter;
import javax.microedition.ims.core.sipservice.invite.DialogStateException;
import javax.microedition.ims.core.sipservice.invite.DialogStateException.Error;
import javax.microedition.ims.core.sipservice.invite.IncomingRequestHandler;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Request;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 17-Feb-2010
 * Time: 13:00:20
 */
class UnknownSipMessageResolverImpl implements UnknownMessageResolver<BaseSipMessage> {
    private static final Set<MessageType> TRANSACTION_INITIATE_MESSAGES = new HashSet<MessageType>(Arrays.asList(
            MessageType.SIP_INVITE, MessageType.SIP_BYE, MessageType.SIP_UPDATE
    ));

    private final IncomingRequestHandler<Request> incomingRequestHandler;

    private final UnknownMessageResolver<Request> incomingInviteResolver = new UnknownMessageResolverAdapter<Request>() {
        
        public void resolveMessage(final Request msg) throws DialogStateException {

            Dialog dialog = StackHelper.searchDialogForIncomingMessage(stackContext, msg);

            MessageType messageMethodType = MessageType.parse(msg.getMethod());
            if (dialog != null && messageMethodType == MessageType.SIP_INVITE) {
                incomingRequestHandler.handleIncomingReInvite(msg);
            }
            else if (dialog != null && messageMethodType == MessageType.SIP_CANCEL) {
                incomingRequestHandler.handleIncomingCancel(msg);
            }
            else {
                incomingRequestHandler.handleIncomingInvite(msg);
            }
        }
    };

    private final UnknownMessageResolver<Request> incomingCancelResolver = new UnknownMessageResolverAdapter<Request>() {
        
        public void resolveMessage(final Request msg) throws DialogStateException {

            Dialog dialog = StackHelper.searchDialogForIncomingMessage(stackContext, msg);

            if (dialog == null) {
                throw new DialogStateException(null, Error.REQUEST_FOR_UNKNOWN_DIALOG, msg);
            }

            if (dialog != null) {
                incomingRequestHandler.handleIncomingCancel(msg);
            }
        }
    };

    private final UnknownMessageResolver<Request> incomingUpdateResolver = new UnknownMessageResolverAdapter<Request>() {
        
        public void resolveMessage(final Request msg) throws DialogStateException {

            Dialog dialog = StackHelper.searchDialogForIncomingMessage(stackContext, msg);

            if (dialog == null) {
                throw new DialogStateException(null, Error.REQUEST_FOR_UNKNOWN_DIALOG, msg);
            }

            if (dialog != null) {
                incomingRequestHandler.handleIncomingUpdate(msg);
            }
        }
    };

    private final UnknownMessageResolver<Request> incomingByeResolver = new UnknownMessageResolverAdapter<Request>() {
        
        public void resolveMessage(final Request msg) throws DialogStateException {

            Dialog dialog = stackContext.getDialogStorage().findDialogForMessage(msg);

            if (dialog == null || Dialog.DialogState.EARLY == dialog.getState()) {
                throw new DialogStateException(null, Error.REQUEST_FOR_UNKNOWN_DIALOG, msg);
            }

            incomingRequestHandler.handleIncomingBye(msg);
        }
    };

    private final UnknownMessageResolver<Request> incomingReferResolver = new UnknownMessageResolverAdapter<Request>() {
        
        public void resolveMessage(Request msg) throws DialogStateException {

            Dialog dialog = stackContext.getDialogStorage().findDialogForMessage(msg);
            //TODO Dialog checks

            incomingRequestHandler.handleIncomingRefer(msg);
        }
    };

    private final UnknownMessageResolver<Request> incomingNotifyResolver = new UnknownMessageResolverAdapter<Request>() {
        
        public void resolveMessage(Request msg) throws DialogStateException {

            Dialog dialog = stackContext.getDialogStorage().findDialogForMessage(msg);
            if (dialog == null) {
                throw new DialogStateException(null, Error.REQUEST_FOR_UNKNOWN_DIALOG, msg);
            }

            incomingRequestHandler.handleIncomingNotify(msg);
        }
    };

    private final UnknownMessageResolver<Request> incomingPageMessageResolver = new UnknownMessageResolverAdapter<Request>() {
        public void resolveMessage(Request msg) throws DialogStateException {
            //Dialog DIALOG = stackContext.getDialogStorage().findDialogForMessage(msg);
            incomingRequestHandler.handleIncomingPageMessage(msg);
        }
    };

    private final UnknownMessageResolver<Request> incomingOptionsResolver = new UnknownMessageResolverAdapter<Request>() {
        public void resolveMessage(Request msg) throws DialogStateException {
            //Dialog DIALOG = stackContext.getDialogStorage().findDialogForMessage(msg);
            incomingRequestHandler.handleIncomingOptionsMessage(msg);
        }
    };

    private final UnknownMessageResolver<Request> lateMessageResolver = new UnknownMessageResolverAdapter<Request>() {
        
        public void resolveMessage(final Request msg) throws DialogStateException {
            final MessageType messageType = MessageType.parse(msg.getMethod());
            if (MessageType.SIP_PRACK == messageType) {
                throw new DialogStateException(null, Error.REQUEST_FOR_UNKNOWN_DIALOG, msg);
            }
            else {
                Logger.log(Logger.Tag.WARNING, "Message come but there is nobody to address it. Msg = " + msg.shortDescription());
            }

        }
    };
    private final StackContext stackContext;

    public UnknownSipMessageResolverImpl(final StackContext stackContext, final IncomingRequestHandler<Request> incomingRequestHandler) {
        assert incomingRequestHandler != null;
        this.stackContext = stackContext;
        this.incomingRequestHandler = incomingRequestHandler;
    }

    
    public void resolveMessage(final BaseSipMessage msg) throws DialogStateException {
        if (msg instanceof Request) {
            Request requestMessage = (Request) msg;

            UnknownMessageResolver<Request> resolver = getResolver(requestMessage);
            if (resolver != null) {
                resolver.resolveMessage(requestMessage);
            }
            else {
                assert false : "Unsupported message type message: " + msg.shortDescription();
            }
        }
        else {

            //   18.1.2 Receiving Responses
            //   If there are any client transactions in existence, the client
            //   transport uses the matching procedures of Section 17.1.3 to attempt
            //   to match the response to an existing transaction.  If there is a
            //   match, the response MUST be passed to that transaction.  Otherwise,
            //   the response MUST be passed to the core (whether it be stateless
            //   proxy, stateful proxy, or UA) for further processing.  Handling of
            //   these "stray" responses is dependent on the core (a proxy will
            //   forward them, while a UA will discard, for example).

            //silently discard unknown response according to RFC3261
            //TODO: put assert that can check that there are no transactions for message
            Logger.log(Logger.Tag.WARNING, "Response for unknown transaction. Silently discard : " + msg.shortDescription());
        }
    }

    private UnknownMessageResolver<Request> getResolver(final Request msg) {
        UnknownMessageResolver<Request> retValue = null;

        final MessageType messageType = MessageType.parse(msg.getMethod());
        if (MessageType.SIP_INVITE == messageType) {
            retValue = incomingInviteResolver;
        }
        else if (MessageType.SIP_BYE == messageType) {
            retValue = incomingByeResolver;
        }
        else if (MessageType.SIP_REFER == messageType) {
            retValue = incomingReferResolver;
        }
        else if (MessageType.SIP_NOTIFY == messageType) {
            retValue = incomingNotifyResolver;
        }
        else if (MessageType.SIP_ACK == messageType || MessageType.SIP_PRACK == messageType) {
            retValue = lateMessageResolver;
        }
        else if (MessageType.SIP_CANCEL == messageType) {
            retValue = incomingCancelResolver;
        }
        else if (MessageType.SIP_UPDATE == messageType) {
            retValue = incomingUpdateResolver;
        }
        else if (MessageType.SIP_MESSAGE == messageType) {
            retValue = incomingPageMessageResolver;
        }
        else if (MessageType.SIP_OPTIONS == messageType) {
            retValue = incomingOptionsResolver;
        }
        else {
            assert false : "Unsupported message type message: " + msg.shortDescription();
        }
        return retValue;
    }

    
    public boolean isPossiblyServerTransactionInitiatorMessage(final BaseSipMessage msg) {
        return msg instanceof Request && TRANSACTION_INITIATE_MESSAGES.contains(MessageType.parse(msg.getMethod()));
    }
}
