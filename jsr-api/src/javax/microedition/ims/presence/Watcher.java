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
 * A Watcher is an entity that subscribes to presence information about a single
 * presentity or a presence list containing a number of presentities. 
 * 
 *
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation
 *
 * @author Andrei Khomushko
 * 
 */
public interface Watcher {
    /**
     * Identifier for the state active. Presence information can now be sent to
     * the application.
     */
    int STATE_ACTIVE = 4;

    /**
     * Identifier for the state inactive. This is also the initial state of the
     * Watcher.
     */
    int STATE_INACTIVE = 1;

    /**
     * Identifier for the state pending when a subscribe request has been sent.
     */
    int STATE_PENDING_SUBSCRIBE = 2;

    /**
     * Identifier for the state pending when a request to terminate the
     * subscription has been sent.
     */
    int STATE_PENDING_UNSUBSCRIBE = 3;

    /**
     * Sends a subscription request. Invocation of this method will result in an
     * unfiltered subscribe request, meaning that all available presence
     * information will be sent.
     * 
     * The Watcher will transit to STATE_PENDING_SUBSCRIBE when the request has
     * been sent.
     * 
     * Note: It is highly recommended to use filters to limit the data received
     * in the presenceInfoReceived event of the WatcherListener interface.
     * 
     * @throws IllegalArgumentExceptionIllegalStateException
     *             - if the Watcher is in STATE_PENDING_SUBSCRIBE or
     *             STATE_PENDING_UNSUBSCRIBE
     * @throws ServiceClosedException
     *             - if the Service is closed
     * @throws IOException
     *             - if an I/O error occurs
     */
    void subscribe() throws ServiceClosedException, IOException;

    /**
     * Sends a subscription request. Invocation of this method will result in a
     * filtered subscribe request, meaning that a selected subset of the
     * presence information will be sent.
     * 
     * The Watcher will transit to STATE_PENDING_SUBSCRIBE when the request has
     * been sent.
     * 
     * @param filters
     *            - a WatcherFilterSet
     * 
     * @throws IllegalArgumentException
     *             - if the filters argument is null
     * @throws IllegalArgumentException
     *             - if the WatcherFilterSet does not contain any WatcherFilter
     * @throws IllegalStateException
     *             - if the Watcher is in STATE_PENDING_SUBSCRIBE or
     *             STATE_PENDING_UNSUBSCRIBE
     * @throws ServiceClosedException
     *             - if the Service is closed
     * @throws IOException
     *             - if an I/O error occurs
     */
/*    void subscribe(WatcherFilterSet filters) throws ServiceClosedException,
            IOException;
*/
    /**
     * Terminates the subscription.
     * 
     * The Watcher will transit to STATE_PENDING_UNSUBSCRIBE when the request
     * has been sent.
     * 
     * @throws IllegalStateException
     *             - if the Watcher is not in STATE_ACTIVE
     * @throws IOException
     *             - if an I/O error occurs
     */
    void unsubscribe() throws IOException;

    /**
     * Polls the presence information once. Invocation of this method will
     * result in an unfiltered poll request, meaning that all available presence
     * information will be sent.
     * 
     * The Watcher will transit to STATE_PENDING_SUBSCRIBE when the request has
     * been sent.
     * 
     * Note: It is highly recommended to use filters to limit the data received
     * in the presenceInfoReceived event of the WatcherListener interface.
     * 
     * @throws IllegalStateException
     *             - if the Watcher is not in STATE_INACTIVE
     * @throws ServiceClosedException
     *             - if the Service is closed
     * @throws IOException
     *             - if an I/O error occurs
     */
    void poll() throws ServiceClosedException, IOException;

    /**
     * Polls the presence information once. Invocation of this method will
     * result in a filtered poll request, meaning that a selected subset of the
     * presence information will be sent.
     * 
     * The Watcher will transit to STATE_PENDING_SUBSCRIBE when the request has
     * been sent.
     * 
     * @param filters
     *            - a WatcherFilterSet
     * 
     * @throws IllegalArgumentException
     *             - if the filters argument is null
     * @throws IllegalArgumentException
     *             - if the WatcherFilterSet does not contain any WatcherFilter
     * @throws IllegalArgumentException
     *             - if the Watcher is not in STATE_INACTIVE
     * @throws ServiceClosedException
     *             - if the Service is closed
     * @throws IOException
     *             - if an I/O error occurs
     */
/*    void poll(WatcherFilterSet filters) throws ServiceClosedException,
            IOException;
*/
    /**
     * Sets a listener for this Watcher , replacing any existing
     * WatcherListener. A null value removes any previously set listener.
     * 
     * @param listener
     *            - the listener to set, or null
     */
    void setListener(WatcherListener listener);

    /**
     * Returns the URI that this Watcher was created with.
     * 
     * @return the URI
     * @see PresenceService.createWatcher(String),
     *      PresenceService.createListWatcher(String)
     */
    String getTargetURI();

    /**
     * Returns the current state of this Watcher.
     * 
     * @return the subscription state
     */
    int getState();

    /**
     * Returns all presentities that this Watcher has received notifications of.
     * 
     * @return an array of presentities or an empty array if no presentities are
     *         available
     * 
     * @throws IllegalStateException
     *             - if the Watcher is not in STATE_ACTIVE
     */
    Presentity[] getPresentities();
}
