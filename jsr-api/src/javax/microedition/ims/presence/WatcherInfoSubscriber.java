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

package javax.microedition.ims.presence;

import javax.microedition.ims.ServiceClosedException;
import java.io.IOException;

/**
 * A WatcherInfoSubscriber is an entity that subscribes to a dynamic set of
 * watchers and the state of their subscriptions. This is used for reactive
 * authorization and to keep track of URIs that are subscribing to the presence
 * information of the local presence source.
 * 
 * The watcher information subscriber will receive notifications when the
 * subscription is started and whenever the watcher information is changed. This
 * API only enables subscription to the user's own watcher information.
 * 
 * This is used for reactive authorization and to keep track of URIs that are
 * subscribing to the presence information of the local presence source. More
 * information about watcher information can be found in [RFC3857] and
 * [RFC3858].
 * 
 * The WatcherInfoSubscriber life cycle consist of four states: STATE_INACTIVE,
 * STATE_PENDING_SUBSCRIBE, STATE_PENDING_UNSUBSCRIBE, and STATE_ACTIVE. A new
 * WatcherInfoSubscriber starts in STATE_INACTIVE and when subscribe is called
 * the state transits to STATE_PENDING_SUBSCRIBE and remains there until a
 * response arrives.
 * 
 * @author Andrei Khomushko
 * 
 */
public interface WatcherInfoSubscriber {

    int STATE_ACTIVE = 4;
    int STATE_INACTIVE = 1;
    int STATE_PENDING_SUBSCRIBE = 2;
    int STATE_PENDING_UNSUBSCRIBE = 3;

    /**
     * Sends a subscription request. Invocation of this method will result in an
     * unfiltered subscribe request, meaning that all available watcher
     * information will be sent.
     * 
     * The WatcherInfoSubscriber will transit to STATE_PENDING_SUBSCRIBE when
     * the request has been sent.
     * 
     * @throws IllegalStateException
     *             - if the WatcherInfoSubscriber is in STATE_PENDING_SUBSCRIBE
     *             or STATE_PENDING_UNSUBSCRIBE
     * @throws ServiceClosedException
     *             - if the Service is closed
     * @throws IOException
     *             - if an I/O error occurs
     */
    void subscribe() throws ServiceClosedException, IOException;

    /**
     * Terminates the subscription.
     * 
     * The WatcherInfoSubscriber will transit to STATE_PENDING_UNSUBSCRIBE when
     * this method is called.
     * 
     * @throws IllegalStateException
     *             - if the WatcherInfoSubscriber is not in STATE_ACTIVE
     * @throws IOException
     *             - if an I/O error occurs
     */
    void unsubscribe() throws IOException;

    /**
     * Sets a listener for this WatcherInfoSubscriber, replacing any previous
     * WatcherInfoSubscriberListener. A null value removes any existing
     * listener.
     */
    void setListener(WatcherInfoSubscriberListener listener);

    /** Returns the current state of this WatcherInfoSubscriber. */
    int getState();
}
