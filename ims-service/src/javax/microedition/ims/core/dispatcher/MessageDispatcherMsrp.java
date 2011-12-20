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


import javax.microedition.ims.common.Consumer;
import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.ProducerListener;
import javax.microedition.ims.core.ConsumerRegistryIMS;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.sipservice.invite.DialogStateException;
import javax.microedition.ims.core.transaction.TransactionRunnable;
import javax.microedition.ims.core.transaction.TransactionUtils;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MessageDispatcherMsrp extends MessageDispatcherBase<MsrpMessage> {

    private static final String TAG = "MessageDispatcherMsrp";

    //private final StackContext context;
    private final UnknownMessageResolver<MsrpMessage> unknownMessageResolver;
    //private final HistorySupport<MsrpMessage> historySupport;
    private Set<String> shortMessageDescriptionHistory = Collections.synchronizedSet(new HashSet<String>());

    public MessageDispatcherMsrp(
            final StackContext context,
            final ProducerListener<IMSMessage> outMsgProducerListener,
            final UnknownMessageResolver<MsrpMessage> unknownMessageResolver) {

        super(new ConsumerRegistryIMS<MsrpMessage>(), outMsgProducerListener);

        //this.context = context;
        this.unknownMessageResolver = unknownMessageResolver;
        //this.historySupport = new SipHistorySupport();
    }

    
    protected Consumer<MsrpMessage> createOuterConsumer() {

        return new Consumer<MsrpMessage>() {
            
            public void push(final MsrpMessage msg) {
                //Object[] checkResult = new Object[2];
                boolean canProcessMessage = messageCanBeProcessed();

                if (canProcessMessage) {
                    doPush(msg);
                }
                else {
                    //TODO write this code
                    /* SipMessageUtil<BaseSipMessage> sipMessageUtil = MessageUtilHolder.getSIPMessageUtil();

                 int statusCode = (Integer) checkResult[0];
                 String warningMsg = (String) checkResult[1];

                 Logger.log(Logger.Tag.WARNING, warningMsg);
                 sendMessageOutside(sipMessageUtil.buildStatelessResponse(context, msg, statusCode, null));*/
                }
            }

            private void doPush(final MsrpMessage msg) {

                Logger.log(TAG, "==========================doPush#started   Message:" + msg.shortDescription());

                //final boolean duplicateDetected = false;
                final boolean duplicateDetected = shortMessageDescriptionHistory.contains(msg.shortDescription());
                if (!duplicateDetected) {
                    shortMessageDescriptionHistory.add(msg.shortDescription());
                }

                TransactionUtils.invokeLater(
                        new TransactionRunnable("MsrpMessageDispatcher.outerConsumer :" + msg.shortDescription()) {
                            
                            public void run() {

                                if (!duplicateDetected) {
                                    onOuterMessageIncome(msg);
                                }
                                else {
                                    Logger.log(Logger.Tag.WARNING, "Duplicate message detected and skipped. No Further processing. Message " + msg.shortDescription());
                                }
                            }
                        }
                );
            }
        };
    }

    private boolean messageCanBeProcessed() {
        //SipMessageUtil<MsrpMessage> sipMessageUtil = MessageUtilHolder.getSIPMessageUtil();

        //boolean retValue = true;
        return true;

        /* //check against non RFC3261 messages
       if (retValue && !sipMessageUtil.isMessageRFC3261Compilant(msg)) {

           retValue = false;

           final String warningMsg = "Incoming message not RFC3261 compilant. " +
                   "It has no magic cookie 'z9hG4bK' in the topmost Via HEADER. " +
                   "Incoming message " + sipMessageUtil.messageShortDescription(msg);

           checkResult[0] = StatusCode.BAD_REQUEST;
           checkResult[1] = warningMsg;

           assert sipMessageUtil.isMessageRFC3261Compilant(msg) : warningMsg;
       }

       //Check against supported requests
       21.5.2 501 Not Implemented
          The server does not support the functionality required to fulfill the
          request.  This is the appropriate response when a UAS does not
          recognize the request method and is not capable of supporting it for
          any USER.  (Proxies forward all requests regardless of method.)

       if (retValue && sipMessageUtil.isRequest(msg) && !sipMessageUtil.isSupportedRequest(msg)) {
           retValue = false;

           final String warningMsg = "Unsupported request detected. Incoming message " +
                   sipMessageUtil.messageShortDescription(msg);

           checkResult[0] = StatusCode.NOT_IMPLEMENTED;
           checkResult[1] = warningMsg;
       }

       if (retValue && sipMessageUtil.isRequest(msg) && !sipMessageUtil.isWellFormedRequest(msg)) {
           retValue = false;

           final String warningMsg = "Bad request detected " +
                   sipMessageUtil.messageShortDescription(msg);

           checkResult[0] = StatusCode.BAD_REQUEST;
           checkResult[1] = warningMsg;
       }


       return retValue;*/
    }

    private void onOuterMessageIncome(final MsrpMessage msg) {
        if (!done.get()) {

            List<Consumer<MsrpMessage>> filteredConsumers = getFilteredConsumers(msg);

            assert filteredConsumers.size() <= 1 : filteredConsumers.size() + " consumers for incoming message " +
                    msg.shortDescription();

            if (messageCanBeAddressed(filteredConsumers)) {
                feedConsumers(filteredConsumers, msg);
            }
            else {
                try {
                    unknownMessageResolver.resolveMessage(msg);
                }
                catch (DialogStateException e) {
                    handleDialogStateException();
                }
            }
        }
    }


    private void handleDialogStateException() {
        //TODO implement
        /* BaseSipMessage message =  e.getTriggeringMessage();
        SipMessageUtil<BaseSipMessage> messageUtil = MessageUtilHolder.getSIPMessageUtil();

        Dialog dlg = e.getMsrpDialog();
        BaseSipMessage response = null;

        switch (e.getError()) {
            case REINVITE_FOR_EARLY_DIALOG: {
                dlg.putCustomParameter(Dialog.ParamKey.USE_RETRY_AFTER, Boolean.TRUE);
                response = messageUtil.buildResponse(
                        dlg,
                        message,
                        dlg.getInitiateParty() == Dialog.InitiateParty.LOCAL ?
                                StatusCode.REQUEST_PENDING :
                                StatusCode.SERVER_INTERNAL_ERROR,
                        null
                );
                dlg.putCustomParameter(Dialog.ParamKey.USE_RETRY_AFTER, Boolean.FALSE);
            }
            break;
            case REINVITE_DURING_PREVIOUS_REINVITE: {
                dlg.putCustomParameter(Dialog.ParamKey.USE_RETRY_AFTER, Boolean.TRUE);
                response = messageUtil.buildResponse(
                        dlg,
                        message,
                        dlg.getReInviteInitiateParty() == Dialog.InitiateParty.LOCAL ?
                                StatusCode.REQUEST_PENDING :
                                StatusCode.SERVER_INTERNAL_ERROR,
                        null
                );
                dlg.putCustomParameter(Dialog.ParamKey.USE_RETRY_AFTER, Boolean.FALSE);
            }
            break;
            case REQUEST_FOR_UNKNOWN_DIALOG: {
                response = messageUtil.buildStatelessResponse(
                        context,
                        message,
                        StatusCode.CALL_OR_TRANSACTION_DOESNOT_EXISTS,
                        null
                );
            }
            break;
        }

        if (response != null) {
            if (dlg != null) {
                onInnerMessageIncome(response);
            } else {
                sendMessageOutside(response);
            }
        } else {
            e.printStackTrace();
            assert false : e.toString();
        }*/
    }


    
    protected Consumer<MsrpMessage> createInnerConsumer() {
        return new Consumer<MsrpMessage>() {
            
            public void push(final MsrpMessage msg) {
                onInnerMessageIncome(msg);
            }
        };
    }

    private void onInnerMessageIncome(final MsrpMessage msg) {
        if (!done.get()) {
            sendMessageOutside(msg);
        }
    }


    private boolean messageCanBeAddressed(final List<?> filteredConsumers) {
        return filteredConsumers != null && filteredConsumers.size() != 0;
    }

    protected void feedConsumers(final List<Consumer<MsrpMessage>> consumers, final MsrpMessage msg) {
        super.feedConsumers(consumers, msg);
    }


    private void sendMessageOutside(final MsrpMessage msg) {
        outMsgProducerListener.onPop(msg);
    }


    public void shutdown() {
        if (done.compareAndSet(false, true)) {
            super.shutdown();
        }
    }
}
