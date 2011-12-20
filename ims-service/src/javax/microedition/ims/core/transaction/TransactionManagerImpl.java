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

import javax.microedition.ims.common.*;
import javax.microedition.ims.core.IMSEntity;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dispatcher.MessageDispatcher;
import javax.microedition.ims.core.dispatcher.MessageDispatcherRegistry;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 21-Dec-2009
 * Time: 09:53:34
 */
final public class TransactionManagerImpl<V> implements TransactionManager, Shutdownable {

    private class CommonListenerSubscriber implements TransactionBuildUpListener {
        private final MessageDispatcherRegistry messageDispatcherRegistry;

        public CommonListenerSubscriber(final MessageDispatcherRegistry messageDispatcherRegistry) {
            this.messageDispatcherRegistry = messageDispatcherRegistry;
        }

        public void onTransactionCreate(final TransactionBuildUpEvent event) {

            final ListenerSupport<TransactionListener<IMSMessage>> listenerSupport = event.getTransaction();

            listenerSupport.addListener(
                    new UnSubscribeOnLogicCompleteAdapter<IMSMessage>(listenerSupport) {

                        protected void onUnSubscribe(final Transaction transaction) {
                            super.onUnSubscribe(transaction);
                            transactionDialogHolder.remove(transaction);
                        }
                    }
            );

            final MessageDispatcher<? extends IMSMessage> dispatcher =
                    messageDispatcherRegistry.getDispatcher(event.getEntity().getEntityType());

            //subscribe created transaction to receive server messages
            Logger.log("TransactionManagerImpl", "" + event.getEntity().getEntityType());
            if (event.getTransaction().getTransactionType().getEntityType() == IMSEntityType.SIP) {
                Transaction<Boolean, BaseSipMessage> sipTransaction = event.getTransaction();
                MessageDispatcher<BaseSipMessage> sipDispatcher = (MessageDispatcher<BaseSipMessage>) dispatcher;

                sipTransaction.addListener(
                        new SipDispatcherRegistrator(sipTransaction, event.getEntity(), sipDispatcher)
                );
            } else if (event.getTransaction().getTransactionType().getEntityType() == IMSEntityType.MSRP) {
                ListenerSupport<TransactionListener<MsrpMessage>> msrpListenerSupport =
                        (Transaction<Boolean, MsrpMessage>) event.getTransaction();

                MessageDispatcher<MsrpMessage> msrpDispatcher = (MessageDispatcher<MsrpMessage>) dispatcher;

                msrpListenerSupport.addListener(
                        new MsrpDispatcherRegistrator(msrpListenerSupport, event.getEntity(), msrpDispatcher)
                );
            }

            //subscribe created transaction to send generated messages
            listenerSupport.addListener(
                    new TransactionMessageSender<IMSMessage>(
                            listenerSupport,
                            messageDispatcherRegistry.getDispatcherConsumer().getInnerConsumer()
                    )
            );
        }
    }

    private final TransactionHolder transactionDialogHolder;

    private final AtomicBoolean done = new AtomicBoolean(false);
    private final ListenerHolder<TransactionBuildUpListener> listenerHolder =
            new ListenerHolder<TransactionBuildUpListener>(TransactionBuildUpListener.class);


    public TransactionManagerImpl(
            final StackContext transactionContext,
            final MessageDispatcherRegistry messageDispatcherRegistry) {

        assert transactionContext != null;
        assert messageDispatcherRegistry != null;

        this.transactionDialogHolder = new TransactionHolder(transactionContext);

        addListener(new CommonListenerSubscriber(messageDispatcherRegistry));
    }

    //TODO AK: divide listeners by TransactionType

    public void addListener(
            final TransactionBuildUpListener listener) {

        listenerHolder.addListener(listener);
    }

    public void addListener(
            final TransactionBuildUpListener listener,
            final TransactionType.Name name) {

        listenerHolder.addListener(listener, name);
    }

    public void addListener(
            final TransactionBuildUpListener listener,
            final TransactionType.Name name,
            final Object... constraints) {

        List<Object> completeConstraintsList = new ArrayList<Object>(((constraints == null ? 0 : constraints.length) + 1) * 2);
        completeConstraintsList.add(name);
        completeConstraintsList.addAll(Arrays.asList(constraints));

        listenerHolder.addListener(listener, completeConstraintsList.toArray());
    }

    public void removeListener(TransactionBuildUpListener listener) {
        listenerHolder.removeListener(listener);
    }

    public <T, V extends T> T newTransaction(
            final IMSEntity entity,
            final TransactionDescription description,
            final TransactionType<T, V> transactionType) {

        assert !done.get();
        T retValue = null;

        if (!done.get()) {
            try {
                retValue = createTransaction(entity, description, transactionType);
                assert transactionType.getPublicInterface() == null || retValue instanceof Proxy;
            } catch (TransactionInstantiationException e) {
                //TODO: refactor this to something more useful
                Logger.log(Logger.Tag.WARNING, "Error during transaction construction. ");
                Logger.log(Logger.Tag.WARNING, "transactionType " + transactionType);
                Logger.log(Logger.Tag.WARNING, "description " + description);
                Logger.log(Logger.Tag.WARNING, "entity" + entity);

                Throwable preCause = e.getInstantiationCause().getCause();

                if (preCause != null) {
                    preCause.printStackTrace();
                }
                e.printStackTrace();

                throw new RuntimeException("Transaction instantiation exception. " + e.getInstantiationCause());
            }
        }

        return retValue;
    }

    public <T, V extends T> T lookUpTransaction(
            final IMSEntity entity,
            final TransactionDescription description,
            final TransactionType<T, V> transactionType
    ) {
        assert !done.get();
        T retValue = null;

        if (!done.get()) {
            try {
                retValue = obtainTransaction(entity, description, transactionType);
                assert transactionType.getPublicInterface() == null || retValue instanceof Proxy;
            } catch (TransactionInstantiationException e) {
                //TODO: refactor this to something more useful
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        return retValue;
    }

    public <T, V extends T> T findTransaction(
            final IMSEntity entity,
            final TransactionType<T, V> transactionType
    ) {
        assert !done.get();
        T retValue = null;

        if (!done.get()) {
            retValue = transactionDialogHolder.find(entity, transactionType);
            assert retValue == null || transactionType.getPublicInterface() == null || retValue instanceof Proxy;
        }

        return retValue;
    }

    private <T, V extends T> T obtainTransaction(
            final IMSEntity dialog,
            final TransactionDescription description,
            final TransactionType<T, V> transactionType) throws TransactionInstantiationException {

        final T retValue;

        retValue = transactionDialogHolder.obtain(
                dialog,
                description,
                transactionType,
                new TransactionBuildUpListener() {
                    public void onTransactionCreate(final TransactionBuildUpEvent event) {
                        Logger.log("New transaction created for " + event.getEntity() + " : " + event.getTransaction());

                        TransactionUtils.invokeAndWait(
                                new TransactionRunnable("TransactionManagerImpl.obtainTransaction.fireOnTransactionCreate[" + dialog + ", " + event.getTransaction() + "]") {
                                    public void run() {
                                        listenerHolder.getNotifier().onTransactionCreate(event);
                                    }
                                }
                        );
                    }
                }
        );

        return retValue;
    }

    private <T, V extends T> T createTransaction(
            final IMSEntity entity,
            final TransactionDescription description,
            final TransactionType<T, V> transactionType) throws TransactionInstantiationException {

        final T retValue;

        retValue = transactionDialogHolder.createNew(
                entity,
                description,
                transactionType,
                new TransactionBuildUpListener() {
                    public void onTransactionCreate(final TransactionBuildUpEvent event) {
                        Logger.log("New transaction created for " + event.getEntity() + " : " + event.getTransaction());

                        TransactionUtils.invokeAndWait(
                                new TransactionRunnable("TransactionManagerImpl.obtainTransaction.fireOnTransactionCreate[" + entity + ", " + event.getTransaction() + "]") {
                                    public void run() {
                                        listenerHolder.getNotifier().onTransactionCreate(event);
                                    }
                                }
                        );
                    }
                }
        );
        return retValue;
    }

    public void cleanUpEntity(final IMSEntity entity) {
        if (!done.get()) {
            transactionDialogHolder.stopAllFor(entity);
        }
    }

    public void terminateAllTransactions() {
        if (!done.get()) {
            transactionDialogHolder.stopAll();
        }
    }

    public void shutdown() {
        if (done.compareAndSet(false, true)) {
            transactionDialogHolder.shutdown();
            listenerHolder.shutdown();
        }
    }


    static void log(String msg, String prefix) {
        Logger.log(prefix, msg);
    }

}
