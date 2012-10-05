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

/**
 * A Presentity is an entity that has presence information associated with it. 
 *
 *
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation
 *
 * @see PresenceDocument
 * 
 * @author Andrei Khomushko
 * 
 */
public interface Presentity {
    /**
     * Indicates that the subscription is active and that the subscriber will
     * receive notifications.
     */
    int STATE_ACTIVE = 2;

    /** Indicates that the subscription is awaiting authorization. */
    int STATE_PENDING = 1;

    /**
     * Indicates that the subscription is terminated or that the subscription
     * could not be started.
     */
    int STATE_TERMINATED = 3;

    /**
     * Returns the user identity of this Presentity.
     * 
     * @return the user identity of this Presentity
     */
    String getURI();

    /**
     * Returns the display name of this Presentity. The display name is a
     * human-readable string that describes the Presentity.
     * 
     * @return the display name of this Presentity or null if the display name
     *         is not available
     */
    String getDisplayName();

    /**
     * Returns the subscription state of this Presentity.
     * 
     * @return the subscription state
     */
    int getState();

    /**
     * Returns the PresenceDocument for this Presentity. The PresenceDocument
     * contains the presence information that is associated with the Presentity.
     * 
     * The PresenceDocument returned by this method will be read-only.
     * 
     * @return the PresenceDocument or null if the PresenceDocument is not
     *         available
     */
    PresenceDocument getPresenceDocument();

    /**
     * Returns an Event that describes the type of event that caused this
     * Presentity to transit to STATE_TERMINATED.
     * 
     * @return an Event or null if the Presentity is not in STATE_TERMINATED
     */
    Event getEvent();
}
