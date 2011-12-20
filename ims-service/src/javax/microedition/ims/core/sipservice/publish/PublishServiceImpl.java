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

package javax.microedition.ims.core.sipservice.publish;

import javax.microedition.ims.common.ListenerHolder;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.TimeoutUnit;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.Dialog.ParamKey;
import javax.microedition.ims.core.sipservice.AbstractService;
import javax.microedition.ims.core.sipservice.RefreshHelper.Refresher;
import javax.microedition.ims.core.transaction.*;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import java.util.concurrent.atomic.AtomicBoolean;


public class PublishServiceImpl extends AbstractService implements PublishService {
    private final static String TAG = "PublishServiceImpl";

    private final AtomicBoolean done = new AtomicBoolean(false);
    private final PublishService transactionSafeView;
    private final long publishExpirationSeconds;
    private PublishRefresher refresher;

    private final ListenerHolder<PublishStateListener> publishStateListenerHolder = new ListenerHolder<PublishStateListener>(PublishStateListener.class);

    public PublishServiceImpl(StackContext stackContext,
                              TransactionManager transactionManager,
                              final long publishExpirationSeconds) {

        super(stackContext, transactionManager);
        this.publishExpirationSeconds = publishExpirationSeconds;

        transactionSafeView = TransactionUtils.wrap(this, PublishService.class);
        subscribeToTransactionManager();
    }

    public PublishService getTransactionSafeView() {
        return transactionSafeView;
    }

    class PublishRefresher implements Refresher {

        private final Dialog dialog;
        private final PublishInfo publishInfo;


        public PublishRefresher(Dialog dialog, PublishInfo initial) {
            super();
            this.dialog = dialog;
            this.publishInfo = new PublishInfo(initial.getEventType(), null, PublishType.REFRESH, initial.getETag(), null);
        }


        
        public void refresh(long timeOutInMillis) {
            TransactionUtils.invokeLaterSmart(new TransactionRunnable("Publish refresh") {
                public void run() {
                    sendPublishMessage(dialog, publishInfo);
                }
            });

        }


        public void updateETag(String etag) {
            publishInfo.setETag(etag);
        }
    }

    private TransactionListener<BaseSipMessage> obtainNewTransactionListener(final Dialog dialog) {
        return new PublishTransactionListener(getStackContext().getRepetitiousTaskManager(), dialog, publishStateListenerHolder.getNotifier(), publishExpirationSeconds, refresher);
    }

    private final TransactionBuildUpListener<BaseSipMessage> clientMessageListener =
            new TransactionBuildUpListener<BaseSipMessage>() {
                public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
                    Dialog dialog = (Dialog) event.getEntity();
                    event.getTransaction().addListener(obtainNewTransactionListener(dialog));
                }

            };


    
    public void addPublishStateListener(Dialog dialog,
                                        PublishStateListener listener) {
        publishStateListenerHolder.addListener(listener, dialog);
    }

    
    public void removePublishStateListener(PublishStateListener listener) {
        publishStateListenerHolder.removeListener(listener);
    }

    /*
     * (non-Javadoc)
     *  EPAs MUST NOT send a new PUBLISH request (not a re-transmission) for
       the same Request-URI, until they have received a final response from
       the ESC for the previous one or the previous PUBLISH request has
       timed out.
     */
    public void sendPublishMessage(Dialog dialog, PublishInfo publishInfo) {
        Logger.log(TAG, "sendPublishMessage#started : " + publishInfo);

        assert !done.get();

        if (!done.get()) {
            refresher = new PublishRefresher(dialog, publishInfo);
            doSendPublishMessage(dialog, TRANSACTION_TIMEOUT, publishInfo);
        }
        Logger.log(TAG, "sendPublishMessage#finished");
    }

    public void sendUnpublishMessage(Dialog dialog, PublishInfo publishInfo) {
        Logger.log(TAG, "sendUnpublishMessage#started");
        assert !done.get();

        if (!done.get()) {
            doSendPublishMessage(dialog, TRANSACTION_TIMEOUT, publishInfo);
        }
        Logger.log(TAG, "sendUnpublishMessage#finished");
    }

    private void doSendPublishMessage(final Dialog dialog, final TimeoutUnit timeoutUnit, PublishInfo publishInfo) {
        Logger.log(TAG, "doSendPublishMessage#started");

        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get();
        assert publishInfo != null : "Empty publish info";
        Logger.log(TAG, "doSendPublishMessage: " + publishInfo);

        dialog.putCustomParameter(ParamKey.PUBLISH_INFO, publishInfo);
        dialog.putCustomParameter(ParamKey.PUBLISH_EXPIRES, publishExpirationSeconds);
        final Transaction transaction =
                getTransactionManager().newTransaction(
                        dialog,
                        null,
                        TransactionType.SIP_PUBLISH_CLIENT
                );

        Logger.log(TAG, "doSendPublishMessage#transaction - " + transaction);

        runAsynchronously((Transaction<Boolean, BaseSipMessage>) transaction, timeoutUnit);
        Logger.log(TAG, "doSendPublishMessage#finished");
    }


    
    public void shutdown() {
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Shutdowning PublishService");
        if (done.compareAndSet(false, true)) {
            unSubscribeFromTransactionManager();
        }
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "PublishService shutdown successfully");
    }

    private void subscribeToTransactionManager() {
        getTransactionManager().addListener(clientMessageListener, TransactionType.Name.SIP_PUBLISH_CLIENT);
    }

    private void unSubscribeFromTransactionManager() {
        getTransactionManager().removeListener(clientMessageListener);
    }


}
