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

import java.util.ArrayList;
import java.util.List;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 5.4.2010
 * Time: 11.28.32
 */
public class TemporaryStorageImpl<T> implements TemporaryStorage<T>, Shutdownable {
    private final Callback<T> callback;
    private final TimeoutUnit timeoutUnit;
    private final List<T> list = new ArrayList<T>(10);

    private final RepetitiousTaskManager repetitiousTaskManager;

    private final RepetitiousTaskManager.Repeater<T> timeoutHandler = new RepetitiousTaskManager.Repeater<T>() {
        
        public void onRepeat(T key, Shutdownable shutdownable) {

            boolean wasRemoved;

            synchronized (list) {
                wasRemoved = list.remove(key);
                repetitiousTaskManager.cancelTask(key);
            }

            if (wasRemoved) {
                try {
                    callback.onDelete(key);
                }
                catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    shutdownable.shutdown();
                }
            }
        }
    };

    public TemporaryStorageImpl(Callback<T> callback, TimeoutUnit timeoutUnit, RepetitiousTaskManager repetitiousTaskManager) {
        this.callback = callback;
        this.timeoutUnit = timeoutUnit;

        this.repetitiousTaskManager = repetitiousTaskManager;
    }

    
    public void add(T element) {

        synchronized (list) {
            list.add(element);
            repetitiousTaskManager.startDelayedTask(
                    element,
                    timeoutHandler,
                    timeoutUnit.getTimeoutUnit().toMillis(timeoutUnit.getTimeout())
            );
        }

    }

    
    public boolean contains(T element) {
        return list.contains(element);
    }

    
    public boolean containsAndRemove(T element) {
        return list.remove(element);
    }

    
    public void shutdown() {
        synchronized (list) {
            List<T> listCopy = new ArrayList<T>(list);

            for (T t : listCopy) {
                list.remove(t);
                repetitiousTaskManager.cancelTask(t);
            }
        }
    }

    
    public String toString() {
        synchronized (list) {
            return "TemporaryStorageImpl{" +
                    "timeoutUnit=" + timeoutUnit +
                    ", list=" + list +
                    '}';
        }
    }
}
