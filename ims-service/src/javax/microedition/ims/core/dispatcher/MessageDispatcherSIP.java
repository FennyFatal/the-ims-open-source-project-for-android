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

package javax.microedition.ims.core.dispatcher;

import javax.microedition.ims.common.*;
import javax.microedition.ims.core.ConsumerRegistryIMS;
import javax.microedition.ims.core.InitiateParty;
import javax.microedition.ims.core.SipHistorySupport;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dispatcher.checker.*;
import javax.microedition.ims.core.sipservice.invite.DialogStateException;
import javax.microedition.ims.core.transaction.TransactionRunnable;
import javax.microedition.ims.core.transaction.TransactionUtils;
import javax.microedition.ims.messages.utils.MessageUtils;
import javax.microedition.ims.messages.utils.StatusCode;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.util.MessageUtilHolder;
import javax.microedition.ims.util.SipMessageUtil;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author ext-plabada
 */
public class MessageDispatcherSIP extends MessageDispatcherBase<BaseSipMessage> {

    private final Collection<? extends MessageChecker> requestCheckers;
    private final Collection<? extends MessageChecker> responseCheckers;

    private final StackContext context;
    private final UnknownMessageResolver<BaseSipMessage> unknownMessageResolver;
    private final HistorySupport<BaseSipMessage> historySupport;

    public MessageDispatcherSIP(final StackContext context,
                                final ProducerListener<IMSMessage> outMsgProducerListener,
                                final UnknownMessageResolver<BaseSipMessage> unknownMessageResolver) {
        super(new ConsumerRegistryIMS<BaseSipMessage>(), outMsgProducerListener);

        this.context = context;
        this.unknownMessageResolver = unknownMessageResolver;
        this.historySupport = new SipHistorySupport();


        RFC3261CompilantChecker rfc3261CompilantChecker = new RFC3261CompilantChecker();

        SupportedRequestsChecker supportedRequestsChecker = new SupportedRequestsChecker(context.getStackRegistry().getCommonRegistry().getMethods());

        MessageType[] allowedMessages = MessageUtilHolder.getSIPMessageUtil().getAllowedMessages(context.getStackRegistry().getCommonRegistry(), context.getConfig());
        AllowedRequestsChecker allowedRequestsChecker = new AllowedRequestsChecker(allowedMessages);

        SupportedFeaturesChecker supportedFeaturesChecker = new SupportedFeaturesChecker(context.getConfig().getSupportedFeatures());

        WellFormedRequestChecker wellFormedRequestChecker = new WellFormedRequestChecker();

        this.requestCheckers = Arrays.asList(
                wellFormedRequestChecker,
                rfc3261CompilantChecker,
                supportedRequestsChecker,
                allowedRequestsChecker,
                supportedFeaturesChecker);

        this.responseCheckers = Arrays.asList(
                rfc3261CompilantChecker);
    }


    protected Consumer<BaseSipMessage> createOuterConsumer() {

        return new Consumer<BaseSipMessage>() {

            public void push(final BaseSipMessage msg) {

                final MessageChecker.CheckResult checkResult = validateMessage(msg);

                if (checkResult.isValid()) {
                    doPush(msg);
                }
                else {
                    SipMessageUtil<BaseSipMessage> sipMessageUtil = MessageUtilHolder
                            .getSIPMessageUtil();

                    final int statusCode = checkResult.getErrorCode();

                    String errorMessage = checkResult.getErrorMessage();
                    final String warningMsg = errorMessage == null ? MessageUtils.getMessageByCode(statusCode) : errorMessage;

                    Logger.log(Logger.Tag.WARNING, warningMsg);
                    sendMessageOutside(sipMessageUtil.buildStatelessResponse(
                            context, msg, statusCode, null));
                }
            }

            private void doPush(final BaseSipMessage msg) {
                final SipMessageUtil<BaseSipMessage> sipMessageUtil = MessageUtilHolder
                        .getSIPMessageUtil();

                TransactionUtils.invokeLater(new TransactionRunnable(
                        "MessageDispatcher.outerConsumer :"
                                + msg.shortDescription()) {
                    public void run() {
                        final boolean duplicateDetected = sipMessageUtil
                                .isDuplicateDetected(context, msg);
                        Logger.log("Detecting duplicate for "
                                + sipMessageUtil.messageShortDescription(msg)
                                + " " + duplicateDetected);

                        if (!duplicateDetected) {
                            onOuterMessageIncome(msg);
                        }
                        else {
                            Logger.log(
                                    Logger.Tag.WARNING,
                                    "Duplicate message detected and skipped. No Further processing. Message "
                                            + sipMessageUtil
                                            .messageShortDescription(msg));
                        }
                    }
                });
            }
        };
    }

    private MessageChecker.CheckResult validateMessage(final BaseSipMessage msg) {


        boolean isRequest = MessageUtilHolder.getSIPMessageUtil().isRequest(msg);
        MessageChecker.CheckResult checkResult = null;

        Collection<? extends MessageChecker> messageCheckers = (isRequest ? requestCheckers : responseCheckers);

        for (MessageChecker messageChecker : messageCheckers) {
            checkResult = messageChecker.check(msg);

            if (!checkResult.isValid()) {
                break;
            }
        }

        return checkResult;
    }

    // 17.1.3 Matching Responses to Client Transactions
    //
    // When the transport layer in the client receives a response, it has to
    // determine which client transaction will handle the response, so that
    // the processing of Sections 17.1.1 and 17.1.2 can take place. The
    // branch parameter in the top Via HEADER field is used for this
    // purpose. A response matches a client transaction under two
    // conditions:
    //
    // 1. If the response has the same value of the branch parameter in
    // the top Via HEADER field as the branch parameter in the top
    // Via HEADER field of the request that created the transaction.
    //
    // 2. If the method parameter in the CSeq HEADER field matches the
    // method of the request that created the transaction. The
    // method is needed since a SIP_CANCEL request constitutes a
    // different transaction, but shares the same value of the branch
    // parameter.
    //
    // If a request is sent via multicast, it is possible that it will
    // generate multiple responses from different servers. These responses
    // will all have the same branch parameter in the topmost Via, but vary
    // in the To tag. The first response received, based on the rules
    // above, will be used, and others will be viewed as retransmissions.
    // That is not an error; multicast SIP provides only a rudimentary
    // "single-hop-discovery-like" service that is limited to processing a
    // single response. See Section 18.1.1 for details.

    // 17.2.3 Matching Requests to Server Transactions
    //
    // When a request is received from the network by the server, it has to
    // be matched to an existing transaction. This is accomplished in the
    // following manner.
    //
    // The branch parameter in the topmost Via HEADER field of the request
    // is examined. If it is present and begins with the magic cookie
    // "z9hG4bK", the request was generated by a client transaction
    // compliant to this specification. Therefore, the branch parameter
    // will be unique across all transactions sent by that client. The
    // request matches a transaction if:
    //
    // 1. the branch parameter in the request is equal to the one in the
    // top Via HEADER field of the request that created the
    // transaction, and
    //
    // 2. the sent-by value in the top Via of the request is equal to the
    // one in the request that created the transaction, and
    //
    // 3. the method of the request matches the one that created the
    // transaction, except for SIP_ACK, where the method of the request
    // that created the transaction is SIP_INVITE.
    //
    // This matching rule applies to both SIP_INVITE and non-SIP_INVITE
    // transactions
    // alike.
    //
    // The sent-by value is used as part of the matching process because
    // there could be accidental or malicious duplication of branch
    // parameters from different clients.

    // TODO all said above MUST be checked here. This is an entry point for
    // server requests and responses.

    private void onOuterMessageIncome(final BaseSipMessage msg) {
        if (!done.get()) {

            List<Consumer<BaseSipMessage>> filteredConsumers = getFilteredConsumers(msg);

            // TODO during ReInvite there are two consumers, Invite and ReInvite
            // transactions, Invite transaction should ignore this message
            // assert filteredConsumers.size() <= 1 : filteredConsumers.size() +
            // " consumers for incoming message " +
            // msg.shortDescription();

            final Dialog dialog = context.getDialogStorage().findDialogForMessage(msg);
            final boolean messageHasAddressee = messageCanBeAddressed(filteredConsumers);
            assert !messageHasAddressee || (messageHasAddressee && dialog != null) : "No DIALOG for response " + msg.buildContent();

            if (dialog != null && messageHasAddressee) {
                historySupport.addHistoryMessage(dialog, msg, true);
                feedConsumers(filteredConsumers, msg);
            }
            else {
                try {
                    unknownMessageResolver.resolveMessage(msg);
                }
                catch (DialogStateException e) {
                    handleDialogStateException(e);
                }
            }
        }
    }

    protected Consumer<BaseSipMessage> createInnerConsumer() {
        return new Consumer<BaseSipMessage>() {

            public void push(final BaseSipMessage msg) {
                onInnerMessageIncome(msg);
            }
        };
    }

    private void onInnerMessageIncome(final BaseSipMessage msg) {
        if (!done.get()) {
            Dialog dialog = context.getDialogStorage()
                    .findDialogForMessage(msg);
            historySupport.addHistoryMessage(dialog, msg, false);
            sendMessageOutside(msg);
        }
    }

    private boolean messageCanBeAddressed(final List<?> filteredConsumers) {
        return filteredConsumers != null && filteredConsumers.size() != 0;
    }

    protected void feedConsumers(
            final List<Consumer<BaseSipMessage>> consumers,
            final BaseSipMessage msg) {

        final String shortDescription = msg.shortDescription();
        final boolean isServerTransaction = unknownMessageResolver
                .isPossiblyServerTransactionInitiatorMessage(msg);
        Logger.log(Logger.Tag.MESSAGE_DISPATCHER,
                "PossiblyServerTransactionInitiatorMessage: "
                        + isServerTransaction + " " + shortDescription);

        assert !isServerTransaction : "Forbidden message come "
                + shortDescription + "  but still alive " + consumers;

        super.feedConsumers(consumers, msg);
    }

    private void handleDialogStateException(final DialogStateException e) {
        BaseSipMessage message = e.getTriggeringMessage();
        SipMessageUtil<BaseSipMessage> messageUtil = MessageUtilHolder
                .getSIPMessageUtil();

        Dialog dlg = e.getDialog();
        BaseSipMessage response = null;

        switch (e.getError()) {
            case REINVITE_FOR_EARLY_DIALOG: {
                dlg.putCustomParameter(Dialog.ParamKey.USE_RETRY_AFTER,
                        Boolean.TRUE);
                response = messageUtil
                        .buildResponse(
                                dlg,
                                message,
                                dlg.getInitiateParty() == InitiateParty.LOCAL ? StatusCode.REQUEST_PENDING
                                        : StatusCode.SERVER_INTERNAL_ERROR, null);
                dlg.putCustomParameter(Dialog.ParamKey.USE_RETRY_AFTER,
                        Boolean.FALSE);
            }
            break;
            case REINVITE_DURING_PREVIOUS_REINVITE: {
                dlg.putCustomParameter(Dialog.ParamKey.USE_RETRY_AFTER,
                        Boolean.TRUE);
                response = messageUtil
                        .buildResponse(
                                dlg,
                                message,
                                dlg.getReInviteInitiateParty() == InitiateParty.LOCAL ? StatusCode.REQUEST_PENDING
                                        : StatusCode.SERVER_INTERNAL_ERROR, null);
                dlg.putCustomParameter(Dialog.ParamKey.USE_RETRY_AFTER,
                        Boolean.FALSE);
            }
            break;
            case REQUEST_FOR_UNKNOWN_DIALOG: {
                response = messageUtil.buildStatelessResponse(context, message,
                        StatusCode.CALL_OR_TRANSACTION_DOESNOT_EXISTS, null);
            }
            break;
            case REQUEST_CANNOT_BE_HANDLED: {
                response = messageUtil.buildStatelessResponse(context, message,
                        StatusCode.UNSUPPORTED_MEDIA_TIME, null);
            }
            break;
            case ADDRESSEE_NOT_FOUND: {
                response = messageUtil.buildStatelessResponse(context, message,
                        StatusCode.NOT_FOUND, null);
            }
            break;
            default: {
                response = messageUtil.buildStatelessResponse(context, message,
                        StatusCode.FORBIDDEN, null);
            }
            break;
        }

        if (response != null) {
            if (dlg != null) {
                onInnerMessageIncome(response);
            }
            else {
                sendMessageOutside(response);
            }
        }
        else {
            e.printStackTrace();
            assert false : e.toString();
        }
    }

    public void shutdown() {
        if (done.compareAndSet(false, true)) {
            super.shutdown();
        }
    }
}
