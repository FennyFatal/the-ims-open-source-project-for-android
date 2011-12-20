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

package javax.microedition.ims.core.sipservice.timer;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.NamedDaemonThreadFactory;
import javax.microedition.ims.common.Shutdownable;
import javax.microedition.ims.core.transaction.TransactionUtils;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class TimeoutTimer implements Shutdownable {

    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(
            new NamedDaemonThreadFactory("TimeoutTimer")
    );
    private final Map<TimeoutListener, ScheduledFuture<?>> futureMap = Collections.synchronizedMap(new HashMap<TimeoutListener, ScheduledFuture<?>>(20));
    private final AtomicBoolean done = new AtomicBoolean(false);


    public static interface TimeoutListener {
        void onTimeout();
    }


    private TimeoutTimer() {
    }

    private static TimeoutTimer INSTANCE;

    public static TimeoutTimer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TimeoutTimer();
        }
        return INSTANCE;
    }

    public void startTimerTransactionSafe(final TimeoutListener timeoutListener, long timeInMillis) {
        startTimer(
                timeoutListener,
                timeInMillis
        );
    }

    public void startTimer(final TimeoutListener timeoutListener, long timeInMillis) {
        Logger.log("Starting TIMEOUT timer for(msec): " + timeInMillis + " " + timeoutListener);


        if (!done.get()) {
            synchronized (futureMap) {
                ScheduledFuture<?> timeoutTask = service.schedule(
                        new Runnable() {
                            private final TimeoutListener wrappedListener = TransactionUtils.wrap(timeoutListener, TimeoutListener.class);

                            public void run() {
                                try {
                                    wrappedListener.onTimeout();
                                }
                                catch (Throwable e) {
                                    e.printStackTrace();
                                }
                                stopTimeoutTimer(timeoutListener);
                            }
                        },
                        timeInMillis, TimeUnit.MILLISECONDS
                );

                futureMap.put(timeoutListener, timeoutTask);
            }
        }
        else {
            throw new IllegalStateException("RepetitiousTaskManager already shutdown.");
        }

    }

    public void shutdown() {
        if (done.compareAndSet(false, true)) {
            cancellAllTasks();

            service.shutdown();
        }
    }

    private void cancellAllTasks() {
        List<ScheduledFuture<?>> futures;

        synchronized (futureMap) {
            futures = new ArrayList<ScheduledFuture<?>>(futureMap.values());
            futureMap.clear();
        }

        for (ScheduledFuture<?> scheduledFuture : futures) {
            scheduledFuture.cancel(true);
        }
    }

    public void stopTimeoutTimer(final TimeoutListener lstnr) {
        doCancellTimeoutTimer(lstnr);
    }

    private void doCancellTimeoutTimer(final TimeoutListener lstnr) {
        ScheduledFuture<?> scheduledFuture;

        synchronized (futureMap) {
            scheduledFuture = futureMap.remove(lstnr);
        }

        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    public void reset() {
        cancellAllTasks();
    }
}
