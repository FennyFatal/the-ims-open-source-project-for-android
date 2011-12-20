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

package javax.microedition.ims.core.sipservice.subscribe;

import javax.microedition.ims.common.Shutdownable;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.sipservice.subscribe.listener.SubscriptionStateAdapter;
import javax.microedition.ims.core.sipservice.subscribe.listener.SubscriptionTerminatedEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 26.5.2010
 * Time: 11.29.55
 */
public class SubscriptionStorageImpl implements SubscriptionStorage, Shutdownable {
    public interface SubscriptionProvider {
        Subscription onDemand(
                final ClientIdentity localParty,
                final String remoteParty,
                final SubscriptionInfo info);
    }

    private final Map<SubscriptionKey, Subscription> subscriptionMap =
            Collections.synchronizedMap(new HashMap<SubscriptionKey, Subscription>());

    private final SubscriptionProvider subscriptionProvider;
    private final AtomicBoolean done = new AtomicBoolean(false);


    public SubscriptionStorageImpl(final SubscriptionProvider dialogProvider) {
        this.subscriptionProvider = dialogProvider;
    }


    public Subscription lookUp(
            final ClientIdentity localParty,
            final String remoteParty,
            final SubscriptionInfo info) {

        if (done.get()) {
            throw new IllegalStateException("lookup is forbidden after Subscription storage is shutdown");
        }

        final SubscriptionKey subscriptionKey = new SubscriptionKeyDefaultImpl(remoteParty, info.getEvent());

        Subscription subscription;

        synchronized (subscriptionMap) {
            subscription = subscriptionMap.get(subscriptionKey);
            if (subscription == null) {
                subscriptionMap.put(
                        subscriptionKey,
                        subscription = subscriptionProvider.onDemand(localParty, remoteParty, info)
                );

                final Subscription subscriptionToBeObserved = subscription;
                subscriptionToBeObserved.addSubscriptionStateListener(new SubscriptionStateAdapter() {

                    public void onSubscriptionTerminated(SubscriptionTerminatedEvent event) {
                        subscriptionToBeObserved.removeSubscriptionStateListener(this);
                        doUnbind(subscriptionToBeObserved);
                    }
                });
            }
        }


        assert info.getEvent() == subscription.getDescription().getEvent();
        assert remoteParty.equals(subscription.getDialog().getRemoteParty());


        return subscription;
    }


    public Subscription find(final SubscriptionKey subscriptionKey) {
        return subscriptionMap.get(subscriptionKey);
    }

    public void unBind(final Subscription subscription) {
        doUnbind(subscription);
    }

    private void doUnbind(Subscription subscription) {
        subscriptionMap.remove(subscription.getDialog().getCallId());
    }


    public void shutdown() {
        if (done.compareAndSet(false, true)) {
            subscriptionMap.clear();
        }
    }


    public String toString() {
        return "SubscriptionStorageImpl{" +
                "subscriptionMap=" + subscriptionMap +
                '}';
    }
}
