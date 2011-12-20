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
import java.util.Map.Entry;

/**
 * @author Andrei Khomushko
 * @param <E>
 */
public class TimeObserver<E> implements Shutdownable {
    private static final Logger.Tag TAG = Logger.Tag.WARNING;
    private static final long CHECK_INTERVAL = 100l;

    public final static boolean needProfiling = isAssertionEnabled();

    private static boolean isAssertionEnabled() {
        boolean isAssertionEnabled = false;

        //this is special service code to check code performance
        //noinspection AssertWithSideEffects
        assert isAssertionEnabled = true;

        return isAssertionEnabled;
    }

    private final Map<E, Long> entities = Collections.synchronizedMap(new HashMap<E, Long>());

    private final TimerTask task;

    public TimeObserver(final long timeThreshold, final TimeObserverCallback<E> observerCallback) {

        task = new TimerTask() {
            @Override
            public void run() {
                long currentTimeMillis = System.currentTimeMillis();

                final Collection<Entry<E, Long>> iterateCopy = new ArrayList<Entry<E, Long>>(entities.entrySet());

                for (Entry<E, Long> entry : iterateCopy) {
                    if (currentTimeMillis - entry.getValue() > timeThreshold) {
                        observerCallback.timeExeeceded(entry.getKey());
                    }
                }
            }
        };

        if (needProfiling) {
            new Timer().scheduleAtFixedRate(task, 0, CHECK_INTERVAL);

//            RepetitiousTaskManager.getInstance().startRepetitiousTask(this, new RepetitiousTaskManager.Repeater<TimeObserver<E>>() {
//                public void onRepeat(TimeObserver<E> key) {
//                    long currentTimeMillis = System.currentTimeMillis();
//
//                    final Collection<Entry<E, Long>> iterateCopy = new ArrayList<Entry<E, Long>>(entities.entrySet());
//
//                    for (Entry<E, Long> entry : iterateCopy) {
//                        if (currentTimeMillis - entry.getValue() > timeThreshold) {
//                            observerCallback.timeExeeceded(entry.getKey());
//                        }
//                    }
//                }
//            }, CHECK_INTERVAL);
        }
    }

    interface TimeObserverCallback<E> {
        void timeExeeceded(E entity);
    }

    public void addEntity(E entity) {
        entities.put(entity, System.currentTimeMillis());
    }

    public void removeEntity(E entity) {

        if (needProfiling) {
            Long startTime = entities.get(entity);
            if(startTime != null) {
                Long executionTime = System.currentTimeMillis() - startTime;

                if (executionTime > CHECK_INTERVAL) {
                    Logger.log(TAG, String.format("removeEntity#entity = %s, time = %s", entity, executionTime));
                }
            }
        }


        entities.remove(entity);
    }

    public void shutdown() {
        entities.clear();
        if (needProfiling) {
            task.cancel();

//            RepetitiousTaskManager.getInstance().cancelTask(this);
        }
    }
}
