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

package javax.microedition.ims.core;

import javax.microedition.ims.common.*;
import javax.microedition.ims.core.dispatcher.ConsumerRegistry;
import javax.microedition.ims.core.dispatcher.Filter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 14-Dec-2009
 * Time: 14:25:46
 */
public class ConsumerRegistryIMS<T extends IMSMessage> implements ConsumerRegistry<T>, Shutdownable {


    private final Object mutex = new Object();
    private final Map<IMSEntity, List<Consumer<T>>> entityConsumersMap =
            Collections.synchronizedMap(new HashMap<IMSEntity, List<Consumer<T>>>(10));

    private final Map<IMSID, IMSEntity> imsIdEntityMap =
            Collections.synchronizedMap(new HashMap<IMSID, IMSEntity>(10));


    private final AtomicBoolean done = new AtomicBoolean(false);

    public ConsumerRegistryIMS() {
    }

    
    public void registerConsumer(final IMSEntity entity, final Consumer<T> consumer) {
        Logger.log(Logger.Tag.MESSAGE_DISPATCHER, "registerEntityConsumer: " + entity + " entityId:" + entity.getIMSEntityId().toString() + ":" + consumer);

        if (!done.get()) {
            synchronized (mutex) {
                List<Consumer<T>> consumerList = entityConsumersMap.get(entity);
                if (consumerList == null) {
                    entityConsumersMap.put(entity, consumerList = new ArrayList<Consumer<T>>(5));
                    imsIdEntityMap.put(entity.getIMSEntityId(), entity);

                }
                consumerList.add(consumer);
            }
        }
        Logger.log(Logger.Tag.MESSAGE_DISPATCHER, "entityConsumersMap" + entityConsumersMap.keySet());
        Logger.log(Logger.Tag.MESSAGE_DISPATCHER, "" + this);
    }

    
    public void unregisterConsumer(final IMSEntity entity, final Consumer<T> consumer) {
        Logger.log(Logger.Tag.MESSAGE_DISPATCHER, "unregisterConsumer: " + entity + ":" + consumer);
        synchronized (mutex) {
            List<Consumer<T>> consumerList = entityConsumersMap.get(entity);
            if (consumerList != null) {
                consumerList.remove(consumer);
                if (consumerList.size() == 0) {
                    entityConsumersMap.remove(entity);
                    imsIdEntityMap.remove(entity.getIMSEntityId());
                }
            }
            else {
                assert false : "trying to unregister unknow entity(" + entity + "). consumer=" + consumer;
            }
        }
        Logger.log(Logger.Tag.MESSAGE_DISPATCHER, "entityConsumersMap" + entityConsumersMap.keySet());
    }

    
    public List<Consumer<T>> filterConsumersForMessage(final T msg, final Map<Consumer<T>, Filter<T>> filterMap) {

        Logger.log(Logger.Tag.MESSAGE_DISPATCHER, "filterConsumersForMessage instance: " + this);
        Logger.log(Logger.Tag.MESSAGE_DISPATCHER, "filterConsumersForMessage for: " + msg.shortDescription());
        Logger.log(Logger.Tag.MESSAGE_DISPATCHER, "filterConsumersForMessage entities: " + imsIdEntityMap);

        List<Consumer<T>> retValue = null;

        if (!done.get()) {
            IMSEntity entity;
            IMSID entityId = msg.getIMSEntityId();
            Logger.log(Logger.Tag.MESSAGE_DISPATCHER, "filterConsumersForMessage: " + " entityId:" + entityId);

            synchronized (mutex) {
                entity = imsIdEntityMap.get(entityId);
                retValue = entityConsumersMap.get(entity);
            }

            Logger.log(Logger.Tag.MESSAGE_DISPATCHER, "filterConsumersForMessage foundEntity: " + entity);
            Logger.log(Logger.Tag.MESSAGE_DISPATCHER, "filterConsumersForMessage allConsumers: " + retValue);

        }

        retValue = (retValue == null) ?
                Collections.<Consumer<T>>emptyList() :
                new ArrayList<Consumer<T>>(filterConsumerList(retValue, filterMap, msg));

        Logger.log(Logger.Tag.MESSAGE_DISPATCHER, "filterConsumersForMessage filteredConsumers: " + retValue);

        return retValue;
    }

    private List<Consumer<T>> filterConsumerList(
            final List<Consumer<T>> consumerList,
            final Map<Consumer<T>, Filter<T>> filterMap,
            final T msg
    ) {

        List<Consumer<T>> retValue = null;

        if (consumerList != null && consumerList.size() > 0) {

            retValue = new ArrayList<Consumer<T>>(consumerList.size());

            Filter<T> filter;
            for (Consumer<T> consumer : consumerList) {
                filter = filterMap.get(consumer);

                if (filter == null || filter.isApplicable(msg)) {
                    retValue.add(consumer);
                }
            }
        }

        return retValue == null ? Collections.<Consumer<T>>emptyList() : retValue;
    }


    
    public void shutdown() {
        if (done.compareAndSet(false, true)) {

            //atomically clear lists
            synchronized (mutex) {
                entityConsumersMap.clear();
                imsIdEntityMap.clear();
            }
        }

    }

}
