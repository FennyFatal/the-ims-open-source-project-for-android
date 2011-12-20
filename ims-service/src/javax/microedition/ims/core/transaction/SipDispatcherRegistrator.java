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

import javax.microedition.ims.common.ListenerSupport;
import javax.microedition.ims.core.IMSEntity;
import javax.microedition.ims.core.MessageFilterSIP;
import javax.microedition.ims.core.dispatcher.Filter;
import javax.microedition.ims.core.dispatcher.MessageDispatcher;
import javax.microedition.ims.core.sipservice.TransactionStateChangeEvent;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;


/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 17.01.2010
 * Time: 17:44:19
 */
class SipDispatcherRegistrator extends UnSubscribeOnCompleteAdapter<BaseSipMessage> {
    private final IMSEntity entity;
    private final MessageDispatcher<BaseSipMessage> messageDispatcher;

    public SipDispatcherRegistrator(
            final ListenerSupport<TransactionListener<BaseSipMessage>> listenerSupport,
            final IMSEntity entity,
            final MessageDispatcher<BaseSipMessage> messageDispatcher) {

        super(listenerSupport);

        this.entity = entity;
        this.messageDispatcher = messageDispatcher;
    }

    /*
    public void onTransactionInited(TransactionEvent<BaseSipMessage> event) {
        final TransactionType type = event.getTransaction().getTransactionType();

        Filter<BaseSipMessage> messageFilter;

        final SipMessageUtil<BaseSipMessage> sipMessageUtil = MessageUtilHolder.getSIPMessageUtil();
        final BaseSipMessage sipMsg = event.getInitialMessage();
        String branch = sipMessageUtil.getMessageBranch(sipMsg);

        messageFilter = new MessageFilterSIP(
                entity,
                TransactionType.getApplicableMessages(type),
                branch
        );

        messageDispatcher.registerConsumer(
                entity,
                event.getTransaction(),
                messageFilter
        );
    }
    */


    public void onTransactionInit(TransactionEvent<BaseSipMessage> event) {
        final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();
        final TransactionType type = transaction.getTransactionType();

        final Filter<BaseSipMessage> messageFilter = new MessageFilterSIP(
                entity,
                TransactionType.getApplicableMessages(type)
        );

        transaction.addListener(new TransactionListenerAdapter<BaseSipMessage>() {

            public void onOutgoingMessage(final TransactionEvent<BaseSipMessage> transactionEvent) {
                final BaseSipMessage lastOutMessage = transactionEvent.getLastOutMessage();
                messageFilter.update(lastOutMessage);
            }


            public void onTransactionComplete(
                    TransactionEvent<BaseSipMessage> baseSipMessageTransactionEvent,
                    final TransactionResult.Reason reason) {
                transaction.removeListener(this);
            }
        });

        messageDispatcher.registerConsumer(
                entity,
                event.getTransaction(),
                messageFilter
        );


    }

    public void onStateChanged(final TransactionStateChangeEvent<BaseSipMessage> event) {
        super.onStateChanged(event);
    }

    public void onTransactionComplete(
            final TransactionEvent<BaseSipMessage> event,
            final TransactionResult.Reason reason) {

        //do listener un-subscribe here
        super.onTransactionComplete(event, reason);
        doCleanUp(event.getTransaction());
    }

    private void doCleanUp(final Transaction<Boolean, BaseSipMessage> consumer) {
        messageDispatcher.unregisterConsumer(entity, consumer);
    }


    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("DispatcherRegistrator");
        sb.append("{entity=").append(entity);
        sb.append(", messageDispatcher=").append(messageDispatcher);
        sb.append('}');
        return sb.toString();
    }
}