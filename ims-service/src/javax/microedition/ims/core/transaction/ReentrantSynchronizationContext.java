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

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.RepetitiousTaskManager;
import javax.microedition.ims.common.Shutdownable;
import javax.microedition.ims.common.TimeoutUnit;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 15-Dec-2009
 * Time: 08:39:44
 */

/*
It's a reentrant synchronization point for many threads.
First coming thread initiate so calling transaction and all subsecuent threads just wait in append() method till some
outside thread calls release(). After relase() new iteration begins. First new comming thread initiate transaction and
wait and subsequent threads waite too till new release(). And all over again.
* */
class ReentrantSynchronizationContext<T> implements Shutdownable {

    private static final String TAG = "ReentrantSynchronizationContext";

    private static final int FINALIZATION_TIMEOUT = 50000;

    static interface ContextCallback<T> {
        void initiateTransaction();

        @SmartCall
        @PriorityCall(priority = Priority.HIGH, override = PriorityCall.Override.NO)
        void finalizeTransaction(TransactionResult<T> result);
    }

    //The mutex for common synchronization
    private final Object mutex = new Object();

    //Every new context iteration will create new transactionId. this is to distinguish between threads
    // coming in new iteration and left from the old one
    private final AtomicReference<AtomicReference<TransactionResult<T>>> transactionId =
            new AtomicReference<AtomicReference<TransactionResult<T>>>(null);
    //Every server threads will hang in  it's own latch. So though it's list of latches but every latch connected with it's own thread
    private final List<CountDownLatch> awaitingThreads;
    //this map contains a finilization tasks for each context iteartion. Key is a transaction id described above.
    // When it's time to finish current iteration a new finalization task created. it's intended for running outer
    //  finalization routine and for releasing latches and making common clean up
    private final Map<AtomicReference<? extends TransactionResult<T>>, FutureTask> finalizationMap;

    //the flag shows that there exists some awating threads and they not yet released from theirs latches
    private final AtomicBoolean transactionInProgress;
    //that is the default value for transaction result. If there no outer release with some other value,
    // this value is used as transaction result
    private final T initialValue;
    //this flag shows whether there was call to shutdown
    private final AtomicBoolean done = new AtomicBoolean(false);
    //this callback for doing outer initialization and finalization duties
    private final ContextCallback<T> callback;
    //this executor runs finalization routine

    private final RepetitiousTaskManager repetitiousTaskManager;
    /**
     * @param callback     outer routines for initialization and finalization
     * @param initialValue the default return value
     */
    ReentrantSynchronizationContext(ContextCallback<T> callback, T initialValue, RepetitiousTaskManager repetitiousTaskManager) {

        if (initialValue == null || callback == null) {
            throw new IllegalArgumentException("Neither of arguments allow NULL value.");
        }

        this.callback = callback;
        this.initialValue = initialValue;

        this.repetitiousTaskManager = repetitiousTaskManager;

        transactionInProgress = new AtomicBoolean(false);

        //barrierList = Collections.synchronizedList(new ArrayList<CountDownLatch>(20));
        awaitingThreads = Collections.synchronizedList(new ArrayList<CountDownLatch>(20));
        finalizationMap = Collections.synchronizedMap(new HashMap<AtomicReference<? extends TransactionResult<T>>, FutureTask>(5));
    }

    TransactionResult<T> append(final TimeoutUnit timeoutUnit) {
        return doAppend(timeoutUnit, true);
    }

    void appendNoBlock(final TimeoutUnit timeoutUnit) {
        doAppend(timeoutUnit, false);
    }


    //calling threads append until some outer routine releases them by calling release() or till TIMEOUT EXPIRES
    //if TIMEOUT EXPIRES and there were not outer release() call default value returned.
    //no matter how awaiting threads released (TIMEOUT or outer call) finalization routine invoked in dedicated helper thread

    private TransactionResult<T> doAppend(final TimeoutUnit timeoutUnit, final boolean block) {
        Logger.log("ReentrantSynchronizationContext", "doAppend#timeoutUnit = " + timeoutUnit + ", block = " + block + ", done.get() = " + done.get());
        TransactionResult<T> retValue = null;

        //possible to proceed only if there were no shutdown call on this instance
        if (!done.get()) {

            //latch for calling thread
            CountDownLatch localBarrier;

            //if the calling thread is a first on in this iteration the flag will get 'true' value
            boolean needInitiateTransaction;

            //each iteration has it's own instance of threadLocalTransactionId. Also it holds the current iteration transaction value.
            //null means default value should be used
            final AtomicReference<TransactionResult<T>> threadLocalTransactionId;

            //nobody can go through if the same monitor is taken in other place. Probably in finalization routine
            Logger.log("ReentrantSynchronizationContext", "doAppend#before sync");
            synchronized (mutex) {
                Logger.log("ReentrantSynchronizationContext", "doAppend#after sync");
                //each calling thread would have it's own latch.
                //It's supposed that some other awaking activity would countdown all the latches from list.
                localBarrier = new CountDownLatch(1);

                //list of all latches and associated threads. Used to release all threads at once for this transaction iteration
                awaitingThreads.add(localBarrier);
                //barierList.add(localBarrier);

                //see description above
                needInitiateTransaction = false;
                if (transactionInProgress.compareAndSet(false, true)) {
                    //if we first time in this iteration in this peace of code we mark that new transaction shoud be started
                    needInitiateTransaction = true;
                    //also we prepare new transaction id. null means we don't have outer call to release()
                    transactionId.set(new AtomicReference<TransactionResult<T>>(null));
                }
                //each thread must hold transaction id it it's own heap
                threadLocalTransactionId = transactionId.get();
            }

            //call outer initialization routine
            if (needInitiateTransaction) {
                Logger.log("ReentrantSynchronizationContext", "initiateTransaction#start");
                callback.initiateTransaction();
                Logger.log("ReentrantSynchronizationContext", "initiateTransaction#end");
            }

            if (block) {
                TransactionResult.Reason reason = TransactionResult.Reason.OUTER_INTERRUPT;
                try {
                    //make thread to wait until transaction finished or TIMEOUT EXPIRES
                    if (timeoutUnit != null) {
                        Logger.log("ReentrantSynchronizationContext", "start await, timeoutUnit = " + timeoutUnit);
                        localBarrier.await(
                                timeoutUnit.getTimeout(),
                                timeoutUnit.getTimeoutUnit() == null ?
                                        TimeUnit.MILLISECONDS :
                                        timeoutUnit.getTimeoutUnit()
                        );
                    }
                    else {
                        localBarrier.await();
                    }

                    reason = localBarrier.getCount() > 0 ?
                            TransactionResult.Reason.TIMEOUT :
                            TransactionResult.Reason.OUTER_INTERRUPT;
                    
                    Logger.log("ReentrantSynchronizationContext", "end await, reason = " + reason);
                }
                catch (InterruptedException e) {
                    //set thread flag
                    Thread.interrupted();
                    reason = TransactionResult.Reason.OUTER_INTERRUPT;
                }
                finally {
                    //try to run finalization routine
                    final TransactionResult<T> transactionResult = threadLocalTransactionId.get();
                    Logger.log("ReentrantSynchronizationContext", "start tr. finalization");
                    runFinalization(
                            threadLocalTransactionId,
                            true,
                            reason,
                            transactionResult == null ? null : transactionResult.getValue(),
                            callback
                    );
                    Logger.log("ReentrantSynchronizationContext", "end tr. finalization");
                    retValue = threadLocalTransactionId.get();
                    Logger.log("ReentrantSynchronizationContext", "start tr. finalization, retValue = " + retValue);
                }
            }
            else {
                if (needInitiateTransaction) {
                    boolean needTimeout = !(timeoutUnit == null ||
                            timeoutUnit.getTimeout() == null ||
                            timeoutUnit.getTimeoutUnit() == null);

                    if (needTimeout) {
                        long timeoutInMillis = timeoutUnit.getTimeoutUnit().toMillis(timeoutUnit.getTimeout());

                        repetitiousTaskManager.startDelayedTask(
                                threadLocalTransactionId,
                                new RepetitiousTaskManager.Repeater<AtomicReference<TransactionResult<T>>>() {
                                    @Override
                                    public void onRepeat(AtomicReference<TransactionResult<T>> key, Shutdownable shutdownable) {
                                        Logger.log(TAG, String.format("onRepeat"));
                                        final TransactionResult<T> transactionResult = key.get();
                                        runFinalization(
                                                key,
                                                true,
                                                TransactionResult.Reason.TIMEOUT,
                                                transactionResult == null ? null : transactionResult.getValue(),
                                                callback
                                        );
                                        shutdownable.shutdown();
                                    }
                                },
                                timeoutInMillis
                        );
                    }
                }
            }
        }
        else {
            throw new IllegalStateException("Context already shutdown.");
        }

        return retValue;
    }

    private void runFinalization(
            final AtomicReference<TransactionResult<T>> transactionId,
            final boolean innerCall,
            final TransactionResult.Reason reason,
            final T transactionResult,
            final ContextCallback<T> callback) {

        if (transactionId != null) {
            doRunFinalization(transactionId, innerCall, reason, transactionResult, callback);
        }
    }

    private void doRunFinalization(final AtomicReference<TransactionResult<T>> transactionId,
                                   final boolean innerCall,
                                   final TransactionResult.Reason reason,
                                   final T transactionResult,
                                   final ContextCallback<T> callback) {

        repetitiousTaskManager.cancelTask(transactionId);

        synchronized (mutex) {

            //this code may be called from three points:
            //1. On latch TIMEOUT. In that case first coming thread sets the default return value, runs finalization routine and awaits it finishes.
            //2. On shutdown call. In that case calling thread set the default return value, runs finalization routine and returns.
            //3. On release call. In that case calling thread set the return value equals to the value passed by calling thread, runs finalization routine and returns.

            final boolean trnsIdEmpty = transactionId.compareAndSet(
                    null,
                    transactionResult == null ?
                            new TransactionResultImpl<T>(initialValue, reason) :
                            new TransactionResultImpl<T>(transactionResult, reason)
            );

            if (trnsIdEmpty) {

                //flag whether we go through 1. scenario

                //create new finalization routine
                final FutureTask<Object> futureTask = new FutureTask<Object>(
                        new Runnable() {
                            public void run() {
                                //run outer finalization routine
                                //Thread.currentThread().setName("Transaction finalizator");
                                try {
                                    callback.finalizeTransaction(transactionId.get());
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                                finally {
                                    //let this iteration awaiting threads to be free and continue execution
                                    cleanUpFinalization(transactionId);
                                }
                            }
                        },
                        null
                );

                //hold and execute new finalization routine
                finalizationMap.put(transactionId, futureTask);
                //TransactionUtils.getExecutorService().execute(futureTask);
                TransactionUtils.invokeLaterSmart(new TransactionRunnable("SynchronizationContext.doRunFinalization()") {
                    public void run() {
                        if (!futureTask.isCancelled() && !futureTask.isDone()) {
                            futureTask.run();
                            futureTask.cancel(true);
                        }
                    }
                });

                //set free waiting thread and initiate new iteration
                doReleaseThreads();
            }
        }

        //here one more synchronization point for awaiting threads from this iteration
        //this time threads waiting till the end of finalization routine.
        //All outer threads (shutdown(), release()) do not block here
        if (innerCall) {
            boolean needCleanupFinalization = false;

            //for all separate transaction we have separate transactionId.
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (transactionId) {
                long startWait = System.currentTimeMillis();

                //waiting here until finalization task is done. Finalization task clears the map by itself
                //threads do not wait more then FINALIZATION_TIMEOUT*2
                while (finalizationMap.containsKey(transactionId)) {
                    try {
                        assert !TransactionUtils.isTransactionExecutionThread() : "Task queue blocking task detected";
                        transactionId.wait(FINALIZATION_TIMEOUT);
                    }
                    catch (InterruptedException e) {
                        Thread.interrupted();
                    }

                    if ((System.currentTimeMillis() - startWait) >= FINALIZATION_TIMEOUT) {
                        needCleanupFinalization = true;
                        break;
                    }
                }
            }

            //if something happens to finalization routine (helper theread abruptly dead or something like that) we make map clean up.
            //and try to notify all awaitng threads
            if (needCleanupFinalization) {
                Logger.log("Warning!!! transaction finalization took  too long time. More than " + FINALIZATION_TIMEOUT + " millis");
                cleanUpFinalization(transactionId);
            }
        }
    }

    //clean maps and notify threads in seconds synchronization point

    private void cleanUpFinalization(final AtomicReference<TransactionResult<T>> transactionId) {
        FutureTask futureTask = finalizationMap.remove(transactionId);

        if (futureTask != null) {
            //for all separate transaction we have separate transactionId.
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (transactionId) {
                transactionId.notifyAll();
            }
            futureTask.cancel(true);
        }
    }

    //routine for outer context release and initiation of new iteration

    void release(T result) {
        if (!done.get()) {
            if (transactionId.get() == null) {
                throw new IllegalStateException("Trying to release transaction which nobody ever tried to append.");
            }
        }
        runFinalization(transactionId.get(), false, TransactionResult.Reason.RELEASE, result, callback);
    }

    //do release all awaiting threads and initiate new iteration

    private void doReleaseThreads() {
        List<CountDownLatch> transactionResultsCopy;

        synchronized (mutex) {
            transactionInProgress.set(false);
            transactionResultsCopy = new ArrayList<CountDownLatch>(awaitingThreads);
            awaitingThreads.clear();
        }

        doReleaseLatches(transactionResultsCopy);
    }

    public void shutdown() {

        //marks that context is shutdown and non of public methods call will have effect since
        if (done.compareAndSet(false, true)) {
            runFinalization(
                    transactionId.get(),
                    false,
                    TransactionResult.Reason.OUTER_INTERRUPT,
                    initialValue,
                    callback
            );
        }
    }

    //releases all threads from it's latches

    private void doReleaseLatches(Collection<CountDownLatch> barrierListCollection) {
        for (CountDownLatch latch : barrierListCollection) {
            while (latch.getCount() > 0) {
                latch.countDown();
            }
        }
    }


    public String toString() {
        return "ReentrantSynchronizationContext{" +
                "transactionInProgress=" + transactionInProgress +
                ", done=" + done +
                '}';
    }
}
