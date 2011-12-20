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

import javax.microedition.ims.common.CompositeKey;
import javax.microedition.ims.common.Shutdownable;
import javax.microedition.ims.core.IMSEntity;
import javax.microedition.ims.core.StackContext;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 14-Jan-2010
 * Time: 11:28:40
 */
class TransactionHolder implements Shutdownable {

    private final Map<CompositeKey<IMSEntity, TransactionType>, Transaction> entityToTransactionMap =
            Collections.synchronizedMap(new HashMap<CompositeKey<IMSEntity, TransactionType>, Transaction>(10));

    private final Map<Transaction, Transaction> transactionToWrapMap =
            Collections.synchronizedMap(new HashMap<Transaction, Transaction>(10));

    private final Map<Transaction, IMSEntity> transactionToEntityMap =
            Collections.synchronizedMap(new HashMap<Transaction, IMSEntity>(10));

    private final Object mutex = new Object();
    private final StackContext transactionContext;
    private final AtomicBoolean done = new AtomicBoolean(false);

    TransactionHolder(final StackContext transactionContext) {
        this.transactionContext = transactionContext;
    }

    void remove(Transaction trnsToBeRemoved) {
        Transaction removedTrns, removedTrnsWrp;

        synchronized (mutex) {
            IMSEntity entity = transactionToEntityMap.remove(trnsToBeRemoved);

            final CompositeKey<IMSEntity, TransactionType> key = createKey(entity, trnsToBeRemoved.getTransactionType());

            removedTrns = entityToTransactionMap.remove(key);
            removedTrnsWrp = transactionToWrapMap.remove(trnsToBeRemoved);
        }

        assert (removedTrns == null) || ((removedTrnsWrp != null) && (removedTrnsWrp.getTransactionId() == removedTrns.getTransactionId()));
        assert done.get() || removedTrns == null || trnsToBeRemoved == removedTrns;
    }

    /**
     * Never return null
     */
    <T, V extends T> T obtain(
            final IMSEntity entity,
            final TransactionDescription description,
            final TransactionType<T, V> trns,
            final TransactionBuildUpListener builder) throws TransactionInstantiationException {

        final CompositeKey<IMSEntity, TransactionType> key = createKey(entity, trns);

        T retValue;
        boolean newTransactionCreated = false;

        synchronized (mutex) {
            if (entityToTransactionMap.get(key) == null) {

                V newTransaction;

                assert !done.get() : "instantiating transaction after TransactionManager shutdown";
                entityToTransactionMap.put(
                        key,
                        (Transaction) (newTransaction = trns.instantiate(entity, transactionContext, description))
                );

                T wrappedTransaction = trns.wrap(newTransaction);

                transactionToWrapMap.put((Transaction) newTransaction, (Transaction) wrappedTransaction);
                transactionToEntityMap.put((Transaction) newTransaction, entity);

                newTransactionCreated = true;
            }
            T keyToWrappedTransaction = (T) entityToTransactionMap.get(key);
            assert keyToWrappedTransaction != null;
            retValue = (T) transactionToWrapMap.get(keyToWrappedTransaction);
        }

        if (newTransactionCreated) {
            TransactionBuildUpEvent event = new TransactionBuildUpEventWithId(
                    entity,
                    (Transaction) retValue,
                    description
            );

            builder.onTransactionCreate(event);
        }

        return retValue;
    }

    <T, V extends T> T createNew(
            final IMSEntity entity,
            final TransactionDescription description,
            final TransactionType<T, V> trns,
            final TransactionBuildUpListener builder) throws TransactionInstantiationException {

        T retValue;

        synchronized (mutex) {

            assert !done.get() : "instantiating transaction after TransactionManager shutdown";

            V newTransaction = trns.instantiate(entity, transactionContext, description);
            T wrappedTransaction = trns.wrap(newTransaction);

            transactionToWrapMap.put((Transaction) newTransaction, (Transaction) wrappedTransaction);
            transactionToEntityMap.put((Transaction) newTransaction, entity);

            retValue = wrappedTransaction;
        }

        TransactionBuildUpEvent event = new TransactionBuildUpEventWithId(
                entity,
                (Transaction) retValue,
                description
        );

        builder.onTransactionCreate(event);

        return retValue;
    }

    /**
     * May return null
     */
    <T, V extends T> T find(final IMSEntity entity, final TransactionType<T, V> trns) {

        final CompositeKey<IMSEntity, TransactionType> key = createKey(entity, trns);

        T retValue = null;

        synchronized (mutex) {
            T keyToWrappedTransaction = (T) entityToTransactionMap.get(key);
            if (keyToWrappedTransaction != null) {
                retValue = (T) transactionToWrapMap.get(keyToWrappedTransaction);
            }
        }

        return retValue;
    }

    IMSEntity get(Transaction transaction) {
        synchronized (mutex) {
            return transactionToEntityMap.get(transaction);
        }
    }

    Collection<Transaction> stopAllFor(final IMSEntity entityToFilter) {

        //make safe copy here
        final Map<Transaction, IMSEntity> trnsEntityMapCopy;
        synchronized (mutex) {
            trnsEntityMapCopy = new HashMap<Transaction, IMSEntity>(transactionToEntityMap);
        }

        //collection for filtered transaction to be shutdown
        Collection<Transaction> transactionsToShutdown = new HashSet<Transaction>(trnsEntityMapCopy.size() * 2);

        //iterate over collection and filter by entity
        for (Transaction transaction : trnsEntityMapCopy.keySet()) {
            IMSEntity candidateEntity = trnsEntityMapCopy.get(transaction);
            if(entityToFilter == candidateEntity){
                transactionsToShutdown.add(transaction);
            }
        }

        TransactionManagerImpl.log("Shutdown transactions for  " + entityToFilter, "TRANSACTION MANAGER");
        TransactionManagerImpl.log("Transactions to be shutdown " + transactionsToShutdown, "TRANSACTION MANAGER");

        return doStopTransactions(transactionsToShutdown);
    }


    Collection<Transaction> stopAll() {
        Collection<Transaction> transactionsToShutdown;
        synchronized (mutex) {
            transactionsToShutdown = new ArrayList<Transaction>(transactionToEntityMap.keySet());
        }

        return doStopTransactions(transactionsToShutdown);
    }

    private Collection<Transaction> doStopTransactions(Collection<Transaction> retValue) {
        for (Transaction transaction : retValue) {

            TransactionManagerImpl.log("Clearing transaction: " + transaction, "TRANSACTION MANAGER");
            try {
                ((Shutdownable) transaction).shutdown();
            }
            catch (Exception e) {
                TransactionManagerImpl.log("Unable to shutdown transaction: " + e.toString(), "TRANSACTION MANAGER");
                e.printStackTrace();
            }
        }

        return retValue;
    }

    void clearAll() {
        synchronized (mutex) {
            transactionToEntityMap.clear();
            entityToTransactionMap.clear();
            transactionToWrapMap.clear();
        }
    }

    public void shutdown() {
        if (done.compareAndSet(false, true)) {
            stopAll();
            clearAll();
        }
    }

    private CompositeKey<IMSEntity, TransactionType> createKey(final IMSEntity entity, final TransactionType trns) {
        return new CompositeKey<IMSEntity, TransactionType>(entity, trns);
    }
}
