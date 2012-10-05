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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.ManagableScheduledFuture;
import javax.microedition.ims.common.ScheduledService;

/**
 * 
 * @author Andrei Khomushko
 *
 */
public class AlarmScheduledService extends BroadcastReceiver implements ScheduledService, FutureTaskCancelListener {
    
    private static final String LOG_TAG = "AlarmScheduledService";
    
    public static final String ACTION_IMS_ALARM_SCHEDULE_SERVICE = "ACTION_IMS_ALARM_SCHEDULE_SERVICE";
    private static final String EXTRA_TASK_ID = "TASK_ID";
    private static final int ALARM_MESSAGE = 1;

    private final Map<Long, ScheduledFutureTask<?>> futuresMap = Collections.synchronizedMap(new HashMap<Long, ScheduledFutureTask<?>>());
    private final Context context;
    private final AlarmManager alarmManager;
    private final WakeLockGuard wakeLock;
    
    private final AlarmServiceHandler serviceHandler;
    
    private final class AlarmServiceHandler extends Handler {
        public AlarmServiceHandler(Looper looper) {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg) {
            Logger.log(LOG_TAG, "handleMessage#msg = " + msg);
            
            if(msg.what == ALARM_MESSAGE) {
                doHandleAlarmMessage((Long)msg.obj);
            }
        }

        private void doHandleAlarmMessage(Long taskNumber) {
            ScheduledFutureTask<?> futureTask = futuresMap.get(taskNumber);
            if(futureTask != null) {
                Logger.log(LOG_TAG, "doHandleAlarmMessage#futureTask[" + futureTask + "]");
                
                futuresMap.remove(taskNumber);
                futureTask.removeTaskListener(AlarmScheduledService.this);
                
                futureTask.run();
                
            } else {
                Log.e(LOG_TAG, "doHandleAlarmMessage#can't find task with number = " + taskNumber);
            }
        }
    }
    
    public AlarmScheduledService(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        
        PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        WakeLock powerWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOG_TAG);
        this.wakeLock = new WakeLockGuard(powerWakeLock);
        
        {
            HandlerThread thread = new HandlerThread(LOG_TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();
            serviceHandler = new AlarmServiceHandler(thread.getLooper());
            
        }
    }
    
    @Override
    public ManagableScheduledFuture<?> schedule(Runnable command, Object transactionId, long delay, TimeUnit unit) {
        Logger.log(LOG_TAG, "schedule#delay = " + delay + ", unit = " + unit);
        if (command == null) {
            throw new IllegalArgumentException("The command argument is null");
        }
            
        if (unit == null) {
            throw new IllegalArgumentException("The unit argument is null");
        }
        
        return doSchedule(command, transactionId, triggerTime(delay, unit));
    }
    
    private ManagableScheduledFuture<?> doSchedule(Runnable command, Object transactionId, long triggerTime) {
        
        final ScheduledFutureTask<Void> future = new ScheduledFutureTask<Void>(command, transactionId, triggerTime, wakeLock);
        future.addTaskListener(this);
        
        sheduleTask(future.getTaskId(), future.getTriggerTime());
        
        futuresMap.put(future.getTaskId(), future);

        Logger.log(LOG_TAG, "doSchedule#task = " + future + " ,(delay =  " + (future.getTriggerTime() - System.currentTimeMillis())/1000 + " secs )");        
        
        return future;
    }

    private void sheduleTask(long taskId, long triggerTime) {
        Intent intent = new Intent(ACTION_IMS_ALARM_SCHEDULE_SERVICE);
        intent.putExtra(EXTRA_TASK_ID, taskId);

        PendingIntent senderIntent = PendingIntent.getBroadcast(context, (int)taskId, intent , 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, senderIntent);
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_IMS_ALARM_SCHEDULE_SERVICE.equals(intent.getAction())) {
            
            long taskId = intent.getLongExtra(EXTRA_TASK_ID, -1l);
            Logger.log(LOG_TAG, "onReceive#task number[" + taskId + "]");
            
            Message message = serviceHandler.obtainMessage(ALARM_MESSAGE);
            message.obj = taskId;
            
            wakeLock.acquire();
            
            serviceHandler.sendMessage(message);
            Logger.log(LOG_TAG, "onReceive#finished");
        }
    }

    @Override
    public void shutdown() {
        Logger.log(LOG_TAG, "shutdown#");
        
        for(Entry<Long, ScheduledFutureTask<?>> taskEntry: futuresMap.entrySet()) {
            unsheduleTask(taskEntry.getKey());
            taskEntry.getValue().removeTaskListener(this);
        }
        
        futuresMap.clear();
        
        wakeLock.shutdown();
        
        serviceHandler.getLooper().quit();
    }
    
    @Override
    public void onCanceled(ScheduledFutureTask<?> futureTask) {
        Logger.log(LOG_TAG, "onCanceled#" + futureTask);

        futuresMap.remove(futureTask.getTaskId());
        futureTask.removeTaskListener(this);
        
        unsheduleTask(futureTask.getTaskId());
    }

    
    private void unsheduleTask(long taskId) {
        Logger.log(LOG_TAG, "unsheduleTask#taskId = " + taskId);
        
        Intent intent = new Intent(ACTION_IMS_ALARM_SCHEDULE_SERVICE);
        intent.putExtra(EXTRA_TASK_ID, taskId);

        PendingIntent operation = PendingIntent.getBroadcast(context, (int)taskId, intent , 0);
        alarmManager.cancel(operation);
    }
    
    private static long triggerTime(long delay, TimeUnit unit) {
        return System.currentTimeMillis() + unit.toMillis((delay < 0) ? 0 : delay);
    }

    @Override
    public String toString() {
        return "AlarmScheduledService [futuresMap=" + futuresMap + "]";
    }

/*    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("AlarmScheduledService [futuresMap={");

        Map map = Collections.synchronizedMap(futuresMap);

        synchronized (map) {
            Iterator iterator = map.keySet().iterator();
            int i = 0, size = map.size();
            while (iterator.hasNext()) {
                sb.append(i).append("=")
                        .append(iterator.next()).append(i < size - 1 ? "," : "");

                ++i;
            }
        }
        sb.append("}");

        return sb.toString();
    }
*/

    
}
