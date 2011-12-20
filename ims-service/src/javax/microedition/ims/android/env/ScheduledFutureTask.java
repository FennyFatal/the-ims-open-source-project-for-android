/*
 * This software code is &copy; 2010 T-Mobile USA, Inc. All Rights Reserved.
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

package javax.microedition.ims.android.env;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.microedition.ims.common.ManagableScheduledFuture;

/**
 * 
 * @author Andrei Khomushko
 *
 */
class ScheduledFutureTask<V> extends FutureTask<V> implements ManagableScheduledFuture<V>{
    private static final String LOG_TAG = "ScheduledFutureTask";
    
    private static final AtomicLong sequencer = new AtomicLong(0);
    
    private final long sequenceNumber;
    private final long time;
    private final WakeLockGuard wakeLock;

    private final Object transactionId;
    
    private final List<FutureTaskCancelListener> taskListeners = new ArrayList<FutureTaskCancelListener>();
    
    public ScheduledFutureTask(Runnable command, Object transactionId, long ns, WakeLockGuard wakeLock) {
        super(command, null);
        this.time = ns;
        this.sequenceNumber = sequencer.getAndIncrement();
        this.wakeLock = wakeLock;

        this.transactionId = transactionId;
    }
    
    long getTaskId() {
        return sequenceNumber;
    }
    
    long getTriggerTime() {
        return time;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long d = unit.convert(time - now(), TimeUnit.NANOSECONDS);
        return d;
    }

    private final long now() {
        return System.nanoTime();
    }
    
    public void addTaskListener(FutureTaskCancelListener taskListener) {
        taskListeners.add(taskListener);
    }
    
    public void removeTaskListener(FutureTaskCancelListener taskListener) {
        taskListeners.remove(taskListener);
    }
    
    @Override
    public int compareTo(Delayed other) {
        if (other == this)
            return 0;
        
        if (other instanceof ScheduledFutureTask) {
            ScheduledFutureTask<?> x = (ScheduledFutureTask<?>)other;
            long diff = time - x.time;
            if (diff < 0)
                return -1;
            else if (diff > 0)
                return 1;
            
        }
        
        long d = (getDelay(TimeUnit.NANOSECONDS) -
                  other.getDelay(TimeUnit.NANOSECONDS));
        
        return (d == 0)? 0 : ((d < 0)? -1 : 1);
    }
    
    @Override
    public void run() {
        Log.d(LOG_TAG, "run#started, " + toString());
        
        super.run();
        Log.d(LOG_TAG, "run#ended, " + toString());
    };
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        Log.d(LOG_TAG, "cancel#" + toString());
        boolean isCancelled = super.cancel(mayInterruptIfRunning);
        
        notifyTaskCanceled();
        
        return isCancelled;
    }
    
    private void notifyTaskCanceled() {
        for(FutureTaskCancelListener taskListener: taskListeners) {
            taskListener.onCanceled(this);
        }
    }

    @Override
    public void shutdown() {
        Log.d(LOG_TAG, "shutdown#" + toString());
        wakeLock.release();
    }
    
    @Override
    public String toString() {
        return "ScheduledFutureTask [sequenceNumber=" + sequenceNumber + ", time=" + time + ", transactionId = " + transactionId + "]";
    }
}