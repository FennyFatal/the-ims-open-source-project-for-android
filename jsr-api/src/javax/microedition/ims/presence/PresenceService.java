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

import javax.microedition.ims.ImsException;
import javax.microedition.ims.Service;
import javax.microedition.ims.ServiceClosedException;

/**
 * 
 * The PresenceService is the entry point for handling presence functionality
 * according to the OMA Presence 1.1 release. This includes functionality to
 * publish presence information, subscribe to presence and watcher information
 * and to manage the presence document.
 * 
 * Creating a PresenceService
 * 
 * Connector.open(String name)
 * 
 * A PresenceService is created with Connector.open(), according to the Generic
 * Connection Framework (GCF), using a name string of the format
 * 
 * {scheme}:{target}[{params}]
 * 
 * where:
 * 
 * <ul>
 * <li>{scheme} is the protocol for presence "imspresence"</li>
 * <li>{target} is always a double slash "//" followed by the application id</li>
 * <li>{params} is an optional parameter that can be used to set the local user
 * identity on the format ;userId=<sip user identity>. This can be used to
 * override the default user identity provisioned by the IMS network and the sip
 * user identity must be on the format described in UserIdentity.</li>
 * </ul>
 * 
 * Closing a PresenceService
 * 
 * The application SHOULD invoke close on PresenceService when it is finished
 * using it. The IMS engine may also close the PresenceService due to external
 * events. This will trigger a call to serviceClosed on the
 * PresenceServiceListener interface.
 * 
 * @author Andrei Khomushko
 * 
 */
public interface PresenceService extends Service {
    /**
     * Creates a PresenceSource that can publish presence information.
     * 
     * @return a new PresenceSource
     * @throws ServiceClosedException
     *             - if the Service is closed
     * @throws ImsException
     *             - if the PresenceSource could not be created
     */
    PresenceSource createPresenceSource() throws ServiceClosedException,
            ImsException;

    /**
     * Creates a Watcher to subscribe to presence information about a single
     * presentity.
     * 
     * @param targetURI
     *            - the user identity of the presentity
     * @return a new Watcher
     * @throws IllegalArgumentException
     *             - if the targetURI argument is not a valid user identity
     * @throws ServiceClosedException
     *             - if the Service is closed
     * @throws ImsException
     *             - if the Watcher could not be created
     */
    Watcher createWatcher(String targetURI) throws ServiceClosedException,
            ImsException;

    /**
     * Creates a Watcher to subscribe to presence information about a presence
     * list, containing a number of presentities.
     * 
     * For more information about presence lists, see the XDM enabler.
     * 
     * @param targetURI
     *            - the service URI of the presence list
     * @return a new Watcher
     * @throws IllegalArgumentException
     *             - if the targetURI argument is not a valid SIP URI
     * @throws ServiceClosedException
     *             - if the Service is closed
     * @throws ImsException
     *             - if the Watcher could not be created
     */
    Watcher createListWatcher(String targetURI) throws ServiceClosedException,
            ImsException;

    /**
     * Creates a WatcherInfoSubscriber to subscribe to the users own watcher
     * information.
     * 
     * @return a new WatcherInfoSubscriber
     * 
     * @throws IllegalArgumentException
     *             - if the targetURI argument is not a valid SIP URI
     * @throws ServiceClosedException
     *             - if the Service is closed
     * @throws ImsException
     *             - if the WatcherInfoSubscriber could not be created
     */
    WatcherInfoSubscriber createWatcherInfoSubscriber()
            throws ServiceClosedException, ImsException;

    /**
     * Sets a listener for this PresenceService, replacing any previous
     * PresenceServiceListener. A null value removes any existing listener.
     * 
     * @param listener
     *            - the listener to set, or null
     */
    void setListener(PresenceServiceListener listener);

    /**
     * Returns the local user identity used in this PresenceService.
     * 
     * @return the local user identity
     */
    String getLocalUserId();
}
