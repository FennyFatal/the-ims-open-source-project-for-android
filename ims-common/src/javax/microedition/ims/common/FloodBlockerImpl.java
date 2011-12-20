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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 08-Mar-2010
 * Time: 14:43:39
 */
public class FloodBlockerImpl<T> implements FloodBlocker<T> {

    private final Object mutex = new Object();

    private final BlockingQueue<T> messages;
    private final ScheduledExecutorService executorService;
    private final TimeoutUnit awaitTimeout;

    private final AtomicBoolean isBlocked = new AtomicBoolean(false);

    private class MessageRemover implements Runnable {
        private final T msg;

        public MessageRemover(final T msg) {
            this.msg = msg;
        }

        
        public void run() {
            messages.remove(msg);
        }

        
        public String toString() {
            final StringBuffer sb = new StringBuffer();
            sb.append("MessageRemover");
            sb.append("{msg=").append(msg);
            sb.append('}');
            return sb.toString();
        }
    }

    private class BlockingStatusCleaner implements Runnable {
        
        public void run() {
            isBlocked.set(false);
        }
    }

    public FloodBlockerImpl(final int maxQuantity, final TimeoutUnit awaitTimeout) {
        this.awaitTimeout = awaitTimeout;
        this.messages = new ArrayBlockingQueue<T>(maxQuantity);
        this.executorService = Executors.newSingleThreadScheduledExecutor(new NamedDaemonThreadFactory("FloodBlocker"));
    }


    
    public boolean addIncomingMessage(final T msg) throws BlockedByFloodException {
        boolean retValue = false;

        synchronized (mutex) {
            if (!isBlocked.get()) {
                try {
                    if (!messages.contains(msg)) {
                        retValue = messages.add(msg);
                        executorService.schedule(
                                new MessageRemover(msg),
                                awaitTimeout.getTimeout(),
                                awaitTimeout.getTimeoutUnit()
                        );
                    }
                }
                catch (IllegalStateException e) {
                    isBlocked.set(true);
                    executorService.schedule(
                            new BlockingStatusCleaner(),
                            awaitTimeout.getTimeout(),
                            awaitTimeout.getTimeoutUnit()
                    );
                    throw new BlockedByFloodException();
                }
            }
            else {
                throw new BlockedByFloodException();
            }
        }

        return retValue;
    }

    
    public boolean containsMessage(final T msg) {
        synchronized (mutex) {
            return messages.contains(msg);
        }
    }

    
    public boolean isBlocked() {
        return isBlocked.get();
    }
}
