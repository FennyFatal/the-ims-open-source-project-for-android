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
 * A presence source is an entity that provides presence information to a
 * presence server. Each presence source is responsible for publishing its own
 * presence information, and the IMS engine will not merge presence information
 * from multiple presence sources
 * 
 * When the presence information is published, a watcher can subscribe to the
 * presence information until the presence source has called unpublish or if the
 * presence source transits to STATE_INACTIVE.
 * 
 * The PresenceSource life cycle consist of four states: STATE_INACTIVE,
 * STATE_PENDING_PUBLISH, STATE_PENDING_UNPUBLISH, and STATE_ACTIVE. A new
 * PresenceSource starts in STATE_INACTIVE and when publish is called the state
 * transits to STATE_PENDING_PUBLISH and remains there until a response arrives.
 * 
 * In STATE_ACTIVE, unpublish can be called to terminate the publication and the
 * state will then transit to STATE_PENDING_UNPUBLISH and remain there until a
 * response arrives. If the PresenceService is closed all presence sources will
 * transit to STATE_INACTIVE according to the figure below.
 * 
 * @author Andrei Khomushko
 * 
 */
public interface PresenceSource {
    int STATE_ACTIVE = 4;
    int STATE_INACTIVE = 1;
    int STATE_PENDING_PUBLISH = 2;
    int STATE_PENDING_UNPUBLISH = 3;

    /**
     * Returns the current PresenceDocument. If the PresenceDocument has not
     * been previously modified, the returned PresenceDocument will be empty.
     * 
     * @return the current PresenceDocument
     */
    PresenceDocument getPresenceDocument();

    /**
     * Sends a publication request. The publication will be refreshed until
     * unpublish is called.
     * 
     * The PresenceSource will transit to STATE_PENDING_PUBLISH when the request
     * has been sent.
     * 
     * @throws ServiceClosedException
     *             - if the Service is closed
     * @throws IOException
     *             - if an I/O error occurs
     * @throws IllegalStateException
     *             - if the PresenceSource is in STATE_PENDING_PUBLISH or
     *             STATE_PENDING_UNPUBLISH
     */
    void publish() throws ServiceClosedException, IOException;

    /**
     * Terminates the publication.
     * 
     * The PresenceSource will transit to STATE_PENDING_UNPUBLISH when the
     * request has been sent.
     * 
     * @throws IOException
     *             - if an I/O error occurs
     * @throws IllegalStateException
     *             - if the PresenceSource is not in STATE_ACTIVE
     */
    void unpublish() throws IOException;

    /**
     * Sets a listener for this PresenceSource, replacing any previous
     * PresenceSourceListener. A null value removes any existing listener.
     * 
     * @param listener - the listener to set, or null
     */
    void setListener(PresenceSourceListener listener);

    /**
     * Returns the state of this PresenceSource.
     * 
     * @return the current state 
     **/
    int getState();
}
