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

import javax.microedition.ims.ReasonInfo;

/**
 * This listener type is used to notify the application about watcher
 * subscription status and published presence information of the subscribed
 * entities.
 * 
 * @author Andrei Khomushko
 * 
 */
public interface WatcherListener {

    /**
     * Notifies the application that the subscription was successfully started.
     * Notifications will be received in the presenceInfoReceived event.
     * 
     * The Watcher has transited to STATE_ACTIVE.
     * 
     * @param watcher
     *            - the concerned Watcher
     */
    void subscriptionStarted(Watcher watcher);

    /**
     * Notifies the application that the subscription request, or a request to
     * poll presence information, failed.
     * 
     * The Watcher has transited either to STATE_ACTIVE or STATE_INACTIVE.
     * 
     * @param watcher
     *            - the concerned Watcher
     * @param reasonInfo
     *            - a ReasonInfo to indicate why the request failed
     */
    void subscriptionFailed(Watcher watcher, ReasonInfo reasonInfo);

    /**
     * Notifies the application that the subscription has been terminated. This
     * event combined with an invocation of presenceInfoReceived also occurs
     * after a one-time poll of presence state has been successfully performed.
     * 
     * The Watcher has transited to STATE_INACTIVE.
     * 
     * @param watcher
     *            - the concerned Watcher
     * @param event
     *            - an Event describing the type of event that caused the
     *            termination
     */
    void subscriptionTerminated(Watcher watcher, Event event);

    /**
     * Notifies the application of new or updated presentities for the specified
     * Watcher. After a one-time poll of presence state has been successfully
     * performed, this event will be combined with an invocation of
     * subscriptionTerminated.
     * 
     * Note: Presentities that have not been updated since the last call to
     * presenceInfoReceived are not included.
     * 
     * @param watcher
     *            - the concerned Watcher
     * @param presentities
     *            - an array of Presentity containing presence information of
     *            new or updated presentities
     */
    void presenceInfoReceived(Watcher watcher, Presentity[] presentities);
}
