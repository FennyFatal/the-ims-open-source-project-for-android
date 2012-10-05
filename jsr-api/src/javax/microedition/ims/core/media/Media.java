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

package javax.microedition.ims.core.media;

/**
 * The <code>Media</code> interface represents a media objects for 
 * sessions.
 *
 *
 *
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation
 *
 */
public interface Media {
    /**
     * The Media has an inactive direction, meaning it cannot send or receive content.
     */
    static final int DIRECTION_INACTIVE = 0;

    /**
     * The Media can receive content, but not send.
     */
    static final int DIRECTION_RECEIVE = 1;

    /**
     * The Media can send content, but not receive.
     */
    static final int DIRECTION_SEND = 2;

    /**
     * The Media can send and receive content, also known as a bi-directional media.
     */
    static final int DIRECTION_SEND_RECEIVE = 3;

    /**
     * The Media exists in a session and the media offer has been accepted by both parties.
     */
    static final int STATE_ACTIVE = 3;

    /**
     * The Media has been part of an existing session but is now removed, or the Media has
     * been rejected or deleted by the IMS core.
     */
    static final int STATE_DELETED = 4;

    /**
     * The Media is created and added to a session.
     */
    static final int STATE_INACTIVE = 1;

    /**
     * The Media exists in a session and a media offer has been sent to the remote endpoint.
     */
    static final int STATE_PENDING = 2;

    /**
     * The Media is a fictitious media and is only meant to be inspected to track
     * changes during a session update.
     */
    static final int STATE_PROPOSAL = 5;

    /**
     * This sub-state specifies that this Media is proposed to be modified and must be
     * negotiated before the modifications can be deployed.
     */
    static final int UPDATE_MODIFIED = 2;

    /**
     * This sub-state specifies that this Media is proposed to be removed from the
     * session and must be negotiated before removal can be made.
     */
    static final int UPDATE_REMOVED = 3;

    /**
     * This sub-state specifies that this Media is unchanged since session establishment
     * or the last session update
     */
    static final int UPDATE_UNCHANGED = 1;

    /**
     * Allowed media types
     */
    enum MediaType {
        StreamMedia,
    }

    /**
     * Returns true if it is possible to read from the data flow.
     *
     * @return
     */
    boolean canRead();

    /**
     * Returns true if it is possible to write to the data flow.
     *
     * @return true if a data flow allows read, false otherwise
     */
    boolean canWrite();

    /**
     * Returns true if there is a data flow for this media.
     *
     * @return true if a data flow allows write, false otherwise
     */
    boolean exists();

    /**
     * Returns the current direction of this Media.
     *
     * @return the current direction of this Media
     * @throws IndexOutOfBoundsException If no media descriptor has been set
     *                                   (the direction is an attribute on the media descriptor)
     */
    int getDirection();

    /**
     * Returns the media descriptor(s) associated with this Media.
     *
     * @return the media descriptor(s)
     */
    MediaDescriptor[] getMediaDescriptors();

    /**
     * Returns a fictitious media that is only meant to track changes that are about to be made to the media.
     * <p/>
     * After the Session has been accepted or rejected this proposed media should be considered discarded.
     *
     * @return a media proposal
     * @throws IllegalStateException - if the Media is not in STATE_ACTIVE
     *                               IllegalStateException - if the update state is not in UPDATE_MODIFIED
     */
    Media getProposal();

    /**
     * Returns the current state of this Media.
     *
     * @return the current state
     */
    int getState();

    /**
     * Returns the current update state of this Media.
     *
     * @return the current update state
     * @throws IllegalStateException - if the Media is not in STATE_ACTIVE
     */
    int getUpdateState();

    /**
     * Sets the direction of this Media.
     * <p/>
     * If a Media is changed in an established Session, the application has the responsibility to call update on the Session.
     * <p/>
     * Note: If the Media is in STATE_ACTIVE the direction will be set on the proposal media until the Session has been updated.
     * The proposal media can be retrieved with the getProposal method on the Media interface.
     *
     * @param direction - the direction of the Media
     * @throws IllegalStateException - if the Media is not in STATE_INACTIVE or STATE_ACTIVE
     *                               IllegalArgumentException - if the direction argument is invalid
     */
    void setDirection(int direction);

    /**
     * Sets a listener for this Media, replacing any previous MediaListener.
     * The method MediaListener.modeChanged is called directly after setting a MediaListener with this method.
     * <p/>
     * A null reference is allowed and has the effect of removing any existing listener.
     *
     * @param listener - the listener to set, or null
     */
	void setMediaListener(MediaListener listener);
}
