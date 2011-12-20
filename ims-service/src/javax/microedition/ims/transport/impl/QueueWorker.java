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

package javax.microedition.ims.transport.impl;


import javax.microedition.ims.common.*;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class QueueWorker<T> implements Consumer<T>, Shutdownable {

    private static final int SLEEP_BETWEEN_MESSAGES = 100;

    private final BlockingQueue<T> queue = new LinkedBlockingQueue<T>();
    private final ProducerListener producer;
    private final T dummyMessage;
    private final CyclicBarrier barrier = new CyclicBarrier(2);
    private final ExecutorService executorService;
    //private final Future<Object> future;
    private final AtomicBoolean done;

    public QueueWorker(final ProducerListener<T> messageProducer, final T dummyMessage) throws QueueException {
        super();

        producer = messageProducer;
        this.dummyMessage = dummyMessage;
        done = new AtomicBoolean(false);

        executorService = ThreadEnvironmentHolder.getThreadEnvironment().forQueueWorker();
        //future = executorService.submit(
        executorService.execute(
                new Runnable() {
                    public void run() {
                        //rendezvou with creating thread
                        try {
                            //TODO: add while cycle with checking waiting parties number
                            barrier.await();
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        catch (BrokenBarrierException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }

                        while (!done.get()) {
                            try {
                                T msg = queue.take();
                                if (!done.get()) {
                                    producer.onPop(msg);
                                    //give other threads chance To have CPU time
                                    TimeUnit.MILLISECONDS.sleep(SLEEP_BETWEEN_MESSAGES);
                                }
                            }
                            catch (InterruptedException e) {
                                //If we done with message queue just set interruption flag
                                if (done.get()) {
                                    Thread.interrupted();
                                }
                            }
                            catch (Exception e) {
                                //if something wrong happens in onPop, we just log it and continue To work
                                e.printStackTrace();
                            }
                        }
                        log("QueueWorker is finalizing execution of it's thread", "shutdown");
                        //just return a dummy object. But it can be something meanfull
                        //return new Object();
                    }
                }
        );


        try {
            //rendezvou with thread being created
            barrier.await();
        }
        catch (InterruptedException e) {
            throw new QueueException(e);
        }
        catch (BrokenBarrierException e) {
            throw new QueueException(e);
        }
    }

    public void push(T msg) {
        if (!done.get()) {

            boolean sucess = false;

            while (!sucess) {
                try {
                    queue.put(msg);
                    sucess = true;
                }
                catch (InterruptedException e) {
                    //clear interruption flag
                    Thread.interrupted();
                    //Give a chance To other thread
                    Thread.yield();
                }
            }
        }
        else {
            assert false : "Code MUST never be there";
        }
    }

    public void shutdown() {
        if (done.compareAndSet(false, true)) {

            queue.offer(dummyMessage);

            Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Shutdowning QueueWorker");
            Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Canceling QueueWorker task");
            //future.cancel(true);

            Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Cancaling QueueWorker thread");
            executorService.shutdown();

            Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Eptying QueueWorker message queue");
            emptyQueue(queue);

            Logger.log(getClass(), Logger.Tag.SHUTDOWN, "QueueWorker shutdown successfully");
        }
        else {
            assert false : "Code MUST never be there";
        }
    }

    private void emptyQueue(Queue queue) {
        //noinspection StatementWithEmptyBody
        while (queue.poll() != null) {
            ;
        }
    }

    private static void log(String msg, String prefix) {
        Logger.log(prefix, msg);
    }

}
