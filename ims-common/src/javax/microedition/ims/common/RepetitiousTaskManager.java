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

package javax.microedition.ims.common;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 21-Dec-2009
 * Time: 14:21:42
 */
public final class RepetitiousTaskManager implements Shutdownable {
    //all times in ms
    public static final long T1 = 500; //RTT Estimate  - Timer A, G, E
    public static final long T2 = 4000; //The maximum retransmit interval for non-INVITE requests and INVITE responses
    public static final long T4 = 5000; //Maximum duration a message will remain in the network - Timer I, K

    public static final long REQUEST_TIMEOUT_INTERVAL = 64 * T1; //Timer D - Wait time for UDP response retransmits
    public static final long TRANSACTION_TIMEOUT_INTERVAL = 64 * T1; // transaction  TIMEOUT timer  - Timer B, F, H, J
    public static final long LONG_TRANSACTION_TIMEOUT_INTERVAL = 3 * 64 * T1; // maximum transaction lifetime 32s for calling, 32s for proceeding and 32s for completed states

    public static interface Repeater<T> {
        void onRepeat(T key, Shutdownable task);
    }

    public static interface RepetitiousTimeStrategy {
        /**
         * return delay before execution task next time, should be more than 0.
         *
         * @return
         */
        long getNextTime();
    }

    public static class FixedRepetitiousTimeStrategy implements RepetitiousTimeStrategy {
        private final long repeatTime;

        public FixedRepetitiousTimeStrategy(long repeatTime) {
            this.repeatTime = repeatTime;
        }

        public long getNextTime() {
            return repeatTime;
        }
    }

    public static class ExponentialRepetitiousTimeStrategy implements RepetitiousTimeStrategy {
        private final long t2;
        private long currentDelay;

        /**
         * Recommended values:
         * t1 - 500ms
         * t2 - 4000ms
         *
         * @param t1
         * @param t2
         */
        public ExponentialRepetitiousTimeStrategy(final long t1, final long t2) {
            this.currentDelay = t1;
            this.t2 = t2;
        }

        public long getNextTime() {
            long delay = currentDelay;
            if (delay >= t2) {
                delay = t2;
            }
            else {
                currentDelay *= 2;
            }
            return delay;
        }
    }

    private abstract static class Task<T> implements Runnable {
        private final AtomicBoolean done = new AtomicBoolean(false);
        private final AtomicReference<Repeater<T>> resender = new AtomicReference<Repeater<T>>(null);
        private final AtomicReference<T> key = new AtomicReference<T>(null);
        private final AtomicReference<Shutdownable> taskCallback = new AtomicReference<Shutdownable>(null);

        public Task(final Repeater<T> resender, final T key) {
            this.resender.compareAndSet(null, resender);
            this.key.compareAndSet(null, key);
        }

        public void run() {
            if (!done.get()) {
                try {
                    resender.get().onRepeat(key.get(), taskCallback.get());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    afterRun();
                }
            }
        }

        protected abstract void afterRun();

        void cancel() {
            done.set(true);
            resender.set(null);
            key.set(null);
        }
    }

    public RepetitiousTaskManager(ScheduledService service) {
        this.service = service;
    }

    private final ScheduledService service;

    private final Object mutex = new Object();
    private final Map<Object, ScheduledFuture<?>> futureMap = Collections.synchronizedMap(new HashMap<Object, ScheduledFuture<?>>(20));
    private final Map<Object, Task<?>> taskMap = Collections.synchronizedMap(new HashMap<Object, Task<?>>(20));
    private final AtomicBoolean done = new AtomicBoolean(false);


/*    public <T> void startRepetitiousTask(final T key, final Repeater<T> resender, long repeatTimeInMillis) {
        doScheduleTask(key, resender, 0, new FixedRepetitiousTimeStrategy(repeatTimeInMillis));
    }
*/
    public <T> void startRepetitiousTask(final T key, final Repeater<T> resender, RepetitiousTimeStrategy repetitiousTimeStrategy) {
        doScheduleTask(key, resender, 0, repetitiousTimeStrategy);
    }

    public <T> void startDelayedTask(final T key, final Repeater<T> resender, long timeInMillis) {
        doScheduleTask(key, resender, timeInMillis, null);
    }

    private <T> void doScheduleTask(
            final T key,
            final Repeater<T> resender,
            final long delayTimeInMillis,
            final RepetitiousTimeStrategy repetitiousTimeStrategy) {

        if (done.get()) {
            throw new IllegalStateException("RepetitiousTaskManager already shutdown.");
        }
        
        synchronized (mutex) {
            if (futureMap.get(key) == null) {
                ManagableScheduledFuture<?> resendTask = scheduleTask(key, resender, delayTimeInMillis, repetitiousTimeStrategy);
                futureMap.put(key, resendTask);
                assert futureMap.size() == taskMap.size() : "Illegal state for RepetitiousTaskManager. futureMap.size() = " + futureMap.size() + "  taskMap.size()= " + taskMap.size() + " must be equal.";
            }
        }
    }

    private <T> ManagableScheduledFuture<?> scheduleTask(final T key, final Repeater<T> resender, final long timeInMillis, final RepetitiousTimeStrategy repetitiousTimeStrategy) {
        return repetitiousTimeStrategy == null ?
                scheduleDelayedTask(key, resender, timeInMillis) :
                scheduleRepititiousTask(key, resender, repetitiousTimeStrategy);
    }

    private <T> ManagableScheduledFuture<?> scheduleDelayedTask(final T key, final Repeater<T> resender, final long timeInMillis) {


        final Task<T> task = new Task<T>(resender, key) {
            @Override
            protected void afterRun() {
                ScheduledFuture<?> future;
                Task<?> taskToRemove;
                synchronized (mutex) {
                    future = futureMap.remove(key);
                    taskToRemove = taskMap.remove(key);

                    assert future != null : "task map in wrong state. Must contain task for key '" + key + "'. Now value for key: " + future;
                    assert taskToRemove != null : "task map in wrong state. Must contain task for key '" + key + "'. Now value for key: " + taskToRemove;
                    assert futureMap.size() == taskMap.size() : "Illegal state for RepetitiousTaskManager. futureMap.size() = " + futureMap.size() + "  taskMap.size()= " + taskMap.size() + " must be equal.";

                }
                future.cancel(false);
                taskToRemove.cancel();

            }
        };

        synchronized (mutex) {

            taskMap.put(key, task);

            ManagableScheduledFuture<?> future = service.schedule(
                    task,
                    key,
                    timeInMillis,
                    TimeUnit.MILLISECONDS
            );
            
            task.taskCallback.set(future);
            
            return future;
        }
    }

    private <T> ManagableScheduledFuture<?> scheduleRepititiousTask(final T key, final Repeater<T> resender, final RepetitiousTimeStrategy repetitiousTimeStrategy) {
        Task<T> resendTask = new Task<T>(resender, key) {
            @Override
            protected void afterRun() {
                ScheduledFuture<?> future;

                synchronized (mutex) {
                    future = futureMap.remove(key);
                    taskMap.remove(key);
                }
                future.cancel(false);
                scheduleNextTask();
            }

            private void scheduleNextTask() {
                long nextDelay = repetitiousTimeStrategy.getNextTime();
                if (nextDelay > 0) {
                    synchronized (mutex) {
                        ScheduledFuture<?> futureTask = service.schedule(this, key, nextDelay, TimeUnit.MILLISECONDS);
                        futureMap.put(key, futureTask);
                        taskMap.put(key, this);

                        assert futureMap.size() == taskMap.size() : "Illegal state for RepetitiousTaskManager. futureMap.size() = " + futureMap.size() + "  taskMap.size()= " + taskMap.size() + " must be equal.";
                    }
                }
                else {
                    this.cancel();
                }
            }
        };

        synchronized (mutex) {
            taskMap.put(key, resendTask);
            
            ManagableScheduledFuture<?> future = service.schedule(resendTask, key, repetitiousTimeStrategy.getNextTime(), TimeUnit.MILLISECONDS);
            
            resendTask.taskCallback.set(future);
            
            return future;
        }
    }

    public void shutdown() {
        if (done.compareAndSet(false, true)) {
            cancelAllTasks();
            service.shutdown();
        }
    }

    private void cancelAllTasks() {
        List<ScheduledFuture<?>> futures;
        List<Task<?>> tasks;

        synchronized (mutex) {
            futures = new ArrayList<ScheduledFuture<?>>(futureMap.values());
            tasks = new ArrayList<Task<?>>(taskMap.values());

            futureMap.clear();
            taskMap.clear();
        }

        for (ScheduledFuture<?> scheduledFuture : futures) {
            scheduledFuture.cancel(true);
        }
        for (Task<?> task : tasks) {
            task.cancel();
        }

        assert futureMap.size() == taskMap.size() : "Illegal state for RepetitiousTaskManager. futureMap.size() = " + futureMap.size() + "  taskMap.size()= " + taskMap.size() + " must be equal.";
    }

    public void cancelTask(final Object key) {
        doCancelTask(key);

        assert futureMap.size() == taskMap.size() : "Illegal state for RepetitiousTaskManager. futureMap.size() = " + futureMap.size() + "  taskMap.size()= " + taskMap.size() + " must be equal.";
    }

    private void doCancelTask(final Object key) {
        ScheduledFuture<?> scheduledFuture;
        final Task<?> task;

        synchronized (mutex) {
            scheduledFuture = futureMap.remove(key);
            task = taskMap.remove(key);
        }

        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }

        if (task != null) {
            task.cancel();
        }
    }

    public int getCountScheduledTasks() {
        return futureMap.size();
    }

    public void reset() {
        cancelAllTasks();
    }
}
