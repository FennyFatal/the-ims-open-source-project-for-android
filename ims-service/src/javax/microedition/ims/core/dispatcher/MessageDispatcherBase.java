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

package javax.microedition.ims.core.dispatcher;

import javax.microedition.ims.common.*;
import javax.microedition.ims.core.IMSEntity;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 21.4.2010
 * Time: 14.28.07
 */
public abstract class MessageDispatcherBase<T extends IMSMessage> implements
        MessageDispatcher<T>, DispatcherConsumer<T>, Shutdownable {

    private final Object mutex = new Object();

    private final Map<Consumer<T>, Filter<T>> consumerFilterMap =
            Collections.synchronizedMap(new HashMap<Consumer<T>, Filter<T>>(10));

    private final ConsumerRegistry<T> registry;


    private final Consumer<T> outerConsumer;
    private final Consumer<T> innerConsumer;

    protected final AtomicBoolean done = new AtomicBoolean(false);
    protected final ProducerListener<IMSMessage> outMsgProducerListener;

    public MessageDispatcherBase(final ConsumerRegistry<T> registry, final ProducerListener<IMSMessage> outMsgProducerListener) {
        this.registry = registry;

        this.outerConsumer = createOuterConsumer();
        this.innerConsumer = createInnerConsumer();
        this.outMsgProducerListener = outMsgProducerListener;
    }

    protected abstract Consumer<T> createInnerConsumer();

    protected abstract Consumer<T> createOuterConsumer();


    
    public Consumer<T> getOuterConsumer() {
        return outerConsumer;
    }

    
    public Consumer<T> getInnerConsumer() {
        return innerConsumer;
    }

    
    public void registerConsumer(final IMSEntity entity, final Consumer<T> consumer, final Filter<T> filter) {
        if (!done.get()) {
            synchronized (mutex) {
                consumerFilterMap.put(consumer, filter);
                registry.registerConsumer(entity, consumer);
            }
        }
    }

    
    public void unregisterConsumer(final IMSEntity entity, final Consumer<T> consumer) {
        synchronized (mutex) {
            consumerFilterMap.remove(consumer);
            registry.unregisterConsumer(entity, consumer);
        }
    }

    protected List<Consumer<T>> getFilteredConsumers(T msg) {
        Map<Consumer<T>, Filter<T>> filterMap;
        synchronized (mutex) {
            filterMap = new HashMap<Consumer<T>, Filter<T>>(consumerFilterMap);
        }

        return registry.filterConsumersForMessage(msg, filterMap);
    }

    protected void feedConsumers(final List<Consumer<T>> consumers, final T msg) {
        for (Consumer<T> consumer : consumers) {
            consumer.push(msg);
        }
    }

    
    public DispatcherConsumer<T> getConsumer() {
        return this;
    }

    public void shutdown() {
        if (done.compareAndSet(false, true)) {
            Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Shutdowning MessageDispatcher");

            ((Shutdownable) registry).shutdown();
            consumerFilterMap.clear();

            Logger.log(getClass(), Logger.Tag.SHUTDOWN, "MessageDispatcher shutdown successfully");
        }
    }

    protected void sendMessageOutside(final IMSMessage msg) {
        outMsgProducerListener.onPop(msg);
    }
}
