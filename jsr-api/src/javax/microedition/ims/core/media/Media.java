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
 * The <code>Media</code> interface represents a generic media object of a
 * session. Methods are provided common for all media types.
 * </p><p>
 * </p><h2>Media objects</h2>
 * When a calling terminal creates a session, a number of media objects can be
 * added. When the session is started, the IMS core creates a media
 * offer based on the properties of the included media, and sends it to the
 * remote endpoint.
 * If the session is accepted, the remote endpoint has a sufficient amount of
 * information to allow it to create the corresponding media objects. In this
 * way, both endpoints get the same view of the media transfer.
 * A common and useful scenario for IMS applications is to stream video and
 * audio to render in real-time. To support efficient implementations here, an
 * application can pass the stream to a platform-supplied standard
 * <code>Player</code> that supports an appropriate common codec to do the
 * rendering.<p>
 * <p/>
 * The <code>Media</code> interface is used to represent the generic concept
 * of a media in an IMS session. Methods are available that address the signalling
 * plane of the media, while other methods supports the media plane.
 * <p/>
 * </p><h2>Media objects from a media plane perspective</h2>
 * A media object has an associated <em>media flow</em>. The media flow
 * refers to the actual realtime streaming of media content between
 * the local and remote endpoints over the IMS network.
 * An important function of the IMSAPI is to enable an IMS application to read
 * and write data on the flow, for example to exchange chat messages, audio and
 * video or gaming data with the remote. This is the essential part of
 * end-to-end communication of the IMS.<p>
 * While <code>Media</code> is a generic media flow-independent interface,
 * the four <em>media types</em> interfaces extend <code>Media</code> according
 * to the transport protocol.
 * <code>StreamMedia</code> uses RTP/AVP for the basic profile, and RTP/AVPF
 * also in the MMTel profile, <code>FramedMedia</code>
 * <p/>
 * uses TCP/MSRP, <code>BasicUnreliableMedia</code> uses UDP, and
 * <code>BasicReliableMedia</code> is based on TCP. A
 * media object part of a session implements one of the four media type
 * interfaces. Because of this mapping, each media type has its specific uses.
 * For further information on each media type, see their interfaces.
 * </p><p>
 * </p><h3>Media flow and mode properties</h3>
 * The IMS core manages three generic boolean properties for a media flow,
 * called media <em>mode properties</em>. By observing the current state of the
 * mode properties, the application will know what actions are possible on the
 * flow, for example read or write operations. The mode properties are:
 * <ul>
 * <p/>
 * <li>exists: True if there is an end-to-end media flow, false otherwise.</li>
 * <li>canRead: True if the fmedia low accepts read of data from it, false
 * otherwise</li>
 * <li>canWrite: True if the flow accepts write of datfa to it, false otherwise</li>
 * </ul>
 * Each mode mode property depends on a number of <em>mode factors</em>, that
 * describe the media object in general. The node properties change their values
 * dynamically based on the mode factors.
 * <p>
 * For 'exists', there is one mode factor for any type of media:
 * </p><ul>
 * <p/>
 * <li>'Exists' becomes true when the data flow is set up according to the
 * procedures of the media type transport protocol, and false when the media
 * flow is torn down.</li>
 * </ul>
 * That mode factor implies 'exists' is false if media is in
 * <code>STATE_INACTIVE</code>, <code>STATE_DELETED</code> and
 * <code>STATE_PROPOSAL</code>.
 * <p>
 * For 'canRead' the following factors apply for any type of media:
 * </p><ul>
 * <p/>
 * <li>The 'exists' mode is true.</li>
 * <li>The media direction is <code>DIRECTION_RECEIVE</code> or
 * <code>DIRECTION_SEND_RECEIVE</code></li>
 * <li>The flow contains, or is capable to contain, data available for read.</li>
 * </ul>
 * For 'canWrite' the following factors apply for any type of media:
 * <ul>
 * <p/>
 * <li>The 'exists' mode is true.</li>
 * <li>The media direction is <code>DIRECTION_SEND</code> or
 * <code>DIRECTION_SEND_RECEIVE</code></li>
 * <li>The media is in <code>STATE_ACTIVE</code></li>
 * </ul>
 * The media must be in <code>STATE_ACTIVE</code> due to that the application
 * should be able to write data before the session is established.
 * <p>
 * <p/>
 * The IMS core feeds the <code>StreamMedia</code> with input to the flow,
 * and this additional factor applies there for 'canWrite':
 * </p><ul>
 * <li>There is data available from the stream media source</li>
 * </ul>
 * 'canRead' and 'canWrite' are independent of each other.
 * <p>
 * Besides the above, device-specific constraints may apply. For example, if
 * there is
 * a higher priority process that claims networking resources, then 'canRead'
 * and 'canWrite may be set to false until the resources are made available
 * again to the application.
 * </p><p>
 * <p/>
 * The boolean methods <code>Media.exists</code>,
 * <code>Media.canRead</code>, and <code>Media.canWrite</code> are
 * available to all media objects to get the current property values. An
 * application that reads server data SHOULD make sure that
 * <code>Media.canRead</code> returns true before attempting to read, and that
 * <code>Media.canWrite</code> returns true when writing data.</p><p>
 * Due to that properties are changed dynamically, the
 * <code>MediaListener</code> callback interface is provided. This interface
 * is used when the IMS core notifies the application of mode changes on
 * the media.
 * </p><p>
 * <p/>
 * </p><h4>A note on mode properties and media direction</h4>
 * The direction of the media component is part of the session contract, i.e.
 * it is an agreement between the local and remote devices on how the media
 * is supposed to be used. By observing the media mode properties to see if it
 * is allowed to operate on the media flow, the application does not have to
 * consider the media direction in its code.
 * <p/>
 * <h2>Media objects from a signaling perspective</h2>
 * There are some aspects of the media objects seen from a signaling
 * perspective. These are described below.
 * <p>
 * </p><h3>Initializing the media</h3>
 * There are different rules for how a media MUST be initialized between
 * media creation and session start or update. See further sections for
 * the respective media types.
 * <p>
 * </p><h3>Content Types</h3>
 * <p/>
 * Content types identifies the content type of a <code>Media</code>. They can
 * be registered MIME types or some user defined type that follow the MIME
 * syntax. See [RFC2045] and [RFC2046].
 * <p>
 * Example content types:
 * </p><ul>
 * <li>"text/plain"</li>
 * <li>"image/png"</li>
 * <li>"video/mpeg"</li>
 * <p/>
 * <li>"application/myChess"</li>
 * </ul>
 * <p>
 * </p><h3>Media Life Cycle</h3>
 * The life cycle of each <code>Media</code> is independent of other
 * <code>Medias</code> life cycles and has four main states:
 * <code>STATE_INACTIVE</code>, <code>STATE_PENDING</code>,
 * <code>STATE_ACTIVE</code> and <code>STATE_DELETED</code>. There is also a
 * fifth state, <code>STATE_PROPOSAL</code>, that is not part of the ordinary
 * life cycle but instead only meant to track changes on a modified
 * <code>Media</code>.
 * <p>
 * <p/>
 * The media life cycle is connected to the life cycle of the
 * <code>Session</code>. A transition in a <code>Media</code> is the effect
 * of a session transition in the form of a session callback, or a method call
 * in the session, as can be seen in <i>Figure 1</i>.
 * </p><p>
 * <p/>
 * <img src="doc-files/media-1.png"><br>
 * <br>
 * <i><b>Figure 1:</b> The media states visible on the originating endpoint,
 * except STATE_PROPOSAL</i><br>
 * <p/>
 * <br>
 * <p/>
 * </p><h3>Update state</h3>
 * The update state is used to track changes that have been done to an active
 * <code>Media</code> on either the originating or terminating endpoint. If a
 * <code>Media</code> has been accepted in a session the update state will be
 * <code>UPDATE_UNCHANGED</code>.
 * <p>
 * If a <code>Media</code> is proposed to be removed or modified the
 * <code>getUpdateState</code> reflect the changes but the modifications does
 * not take place before the <code>Session</code> has been negotiated and
 * accepted. The modifications can however be traced by inspecting the proposed
 * <code>Media</code>, that can be obtained with the <code>getProposal</code>
 * <p/>
 * method, until the <code>Session</code> is updated.
 * </p><p>
 * After a session update the application SHOULD call <code>getMedia</code> on
 * the <code>Session</code> and take actions if needed.
 * <p/>
 * </p><h3>Examples of the Media states and update states</h3>
 * This example shows which state and update state the <code>Media</code> can
 * reside in at the terminating endpoint when receiving a session invitation or
 * session update. The state and update state will be the same for the
 * originating endpoint as well.
 * <p>
 * </p><pre> public void sessionInvitationReceived(CoreService service, Session session) {
 *   // all medias are pending in a session invite
 *  media.getState() == STATE_PENDING;
 *  // getUpdateState is not applicable in this state
 * }
 * ...
 * public void sessionUpdateReceived(Session session) {
 *  // this will show that the media is proposed to be added to the session
 *   media.getState() == STATE_PENDING;
 *   // getUpdateState is not applicable in this state
 *   ...
 *   // this will show that the media is proposed to be removed from the session
 *   media.getState() == STATE_ACTIVE;
 *   media.getUpdateState() == UPDATE_REMOVED;
 *   ...
 *   // this will show that the media is proposed to be modified in a session
 *   media.getState() == STATE_ACTIVE;
 *   media.getUpdateState() == UPDATE_MODIFIED;
 *   media.getProposal(); // returns a fictious media to track changes
 *   ...
 *   // this will show a media that is unchanged in the session update
 *   media.getState() == STATE_ACTIVE;
 *   media.getUpdateState() == UPDATE_UNCHANGED;
 * }
 * </pre>
 * <p/>
 * <p>
 * This example shows how an application can inspect changes done to a media in
 * a session update.
 * </p><pre> public void sessionUpdateReceived(Session session) {
 *   // if the first media in the array is a media in STATE_ACTIVE and
 *   // update state is UPDATE_MODIFIED
 * <p/>
 *   Media currentMedia = session.getMedia()[0];
 *   Media changedMedia = currentMedia.getProposal();
 * <p/>
 *   // then the application can track changes, for example
 *   if (currentMedia.getDirection() != changedMedia.getDirection()) {
 *     ...
 *   }
 *   // and now the application can decide if the update should be accepted or
 *   // rejected
 * <p/>
 *  session.accept();
 * }
 * </pre>
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
