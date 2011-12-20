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

import javax.microedition.ims.ImsException;
import javax.microedition.ims.core.media.Media;

/**
 * The <code>Session</code> is a representation of a media exchange between two
 * IMS endpoints. A <code>Session</code> can be created either locally through
 * calling <code>CoreService.createSession</code>, or by a remote user, in which
 * case the session will be passed as a parameter to
 * <code>CoreServiceListener.sessionInvitationReceived</code>. </p>
 * <p>
 * <p/>
 * A <code>Session</code> is able to carry media of the type <code>Media</code>.
 * <code>Media</code> objects represent a media connection and not the
 * media/content itself. Note that to be able to start a <code>Session</code> it
 * MUST include at least one <code>Media</code> component, or an
 * <code>ImsException</code> will be thrown.
 * </p>
 * <p>
 * <p/>
 * <p/>
 * The IMS media connections are negotiated through an offer/answer model. The
 * first offer/answer negotiation may take place during session establishment.
 * However, new offers may be sent by any of the session's parties at any time.
 * This is shown below in the session renegotiation procedure.
 * <p/>
 * </p>
 * <h2>Session Life Cycle</h2> A <code>Session</code> has eight states:
 * <code>STATE_INITIATED</code>, <code>STATE_NEGOTIATING</code>,
 * <code>STATE_ESTABLISHING</code>, <code>STATE_ESTABLISHED</code>,
 * <code>STATE_RENEGOTIATING</code>, <code>STATE_REESTABLISHING</code>,
 * <code>STATE_TERMINATING</code> and <code>STATE_TERMINATED</code>. The purpose
 * of the life cycle states is to ensure that the handling of the session is
 * kept synchronized between the local endpoint and the remote endpoint. The
 * session life cycle also determines which actions are valid and which
 * information is accessible in certain states.
 * <p>
 * <p/>
 * <p/>
 * Below follows a description of two different scenarios: <i>Session
 * Establishment</i> and <i>Session Renegotiation</i>. The scenarios are
 * explained from two perspectives: the <i>Mobile Originated</i>-perspective
 * which represents the initiator of an action. The counterpart is the <i>Mobile
 * Terminated</i>-perspective representing the remote side of the session which
 * will receive events of the initiators actions. In general terms an action at
 * one side will generate a call to a listener method on the remote side of the
 * session.
 * <p/>
 * </p>
 * <h2>Session Establishment</h2> This section describes how a session
 * invitation is sent to a remote endpoint and the actions needed to respond to
 * an invitation.
 * <p>
 * <p/>
 * </p>
 * <h3>Mobile Originated Session Establishment</h3>
 * <p/>
 * <p/>
 * <img src="doc-files/session-mo.png"><br>
 * <br>
 * <i><b>Figure 1:</b> The mobile originated session establishment states</i><br>
 * <br>
 * <p/>
 * <h4>STATE_INITIATED</h4>
 * <p/>
 * When a <code>Session</code> is created through a call to
 * <code>CoreService.createSession</code>, it will enter
 * <code>STATE_INITIATED</code>. After the <code>Session</code> is created,
 * media can be added by calling <code>Session.createMedia</code>
 * <p/>
 * <p>
 * A <code>Session</code> transits to:
 * </p>
 * <ul>
 * <li> <code>STATE_NEGOTIATING</code> when <code>start</code> is called on the
 * <code>Session</code>.</li>
 * <p/>
 * <li> <code>STATE_TERMINATING</code> when <code>terminate</code> is called on
 * the <code>Session</code>.</li>
 * </ul>
 * <p/>
 * <h4>STATE_NEGOTIATING</h4>
 * <p/>
 * Once this state is entered the communication with the remote endpoint starts
 * and a session invitation with the first media offer is sent.
 * <p>
 * <p/>
 * <p/>
 * Within <code>STATE_NEGOTIATING</code> the listener method
 * <code>sessionAlerting</code> will be called when the remote endpoint has
 * received the session invitation.
 * </p>
 * <p>
 * <p/>
 * A <code>Session</code> transits to:
 * </p>
 * <ul>
 * <li> <code>STATE_ESTABLISHED</code> if the remote endpoint accepts the session
 * invitation. The <code>sessionStarted</code> callback will then be invoked.</li>
 * <p/>
 * <li> <code>STATE_TERMINATING</code> if the local endpoint invokes <code>
 * terminate</code>
 * .</li>
 * <li> <code>STATE_TERMINATED</code> if the remote endpoint rejects the session
 * invitation. The <code>sessionStartFailed</code> callback will then be
 * invoked.</li>
 * <p/>
 * </ul>
 * <p/>
 * <h3>Mobile Terminated Session Establishment</h3>
 * <p/>
 * <img src="doc-files/session-mt.png"><br>
 * <br>
 * <i><b>Figure 2:</b> The mobile terminated session establishment states</i><br>
 * <br>
 * <p/>
 * <h4>STATE_INITIATED</h4>
 * <p/>
 * <p/>
 * <code>STATE_INITIATED</code> is irrelevant to a receiving terminal because as
 * soon as it is notified of an invitation the session is already in
 * <code>STATE_NEGOTIATING</code>.
 * <p/>
 * <h4>STATE_NEGOTIATING</h4>
 * <p/>
 * When a session invitation reaches the terminating application it will be
 * notified by a call to <code>sessionInvitationReceived</code> and the
 * <code>Session</code> will reside in <code>STATE_NEGOTIATING</code>.
 * <p>
 * <p/>
 * <p/>
 * A <code>Session</code> transits to:
 * </p>
 * <ul>
 * <li> <code>STATE_ESTABLISHING</code> if <code>accept</code> is called on the
 * <code>Session</code>.</li>
 * <p/>
 * <li> <code>STATE_TERMINATED</code> if <code>reject</code> is called on the
 * <code>Session</code> or when the callback <code>sessionStartFailed</code> is
 * invoked.</li>
 * <li> <code>STATE_TERMINATING</code> if <code>terminate</code> is called on the
 * session.</li>
 * <p/>
 * <p/>
 * </ul>
 * <p/>
 * <h4>STATE_ESTABLISHING</h4>
 * In this state <code>accept</code> has been called and the
 * <code>Session</code> is about to be established at both endpoints.
 * <p/>
 * A <code>Session</code> transits to:
 * <ul>
 * <p/>
 * <li> <code>STATE_ESTABLISHED</code> when the callback
 * <code>sessionStarted</code> is invoked.</li>
 * <li> <code>STATE_TERMINATED</code> when the callback
 * <code>sessionStartFailed</code> is invoked.</li>
 * <p/>
 * <li> <code>STATE_TERMINATING</code> if <code>terminate</code> is called on the
 * session.</li>
 * </ul>
 * <p/>
 * <h2>Session Renegotiation</h2>
 * This section describes how a session renegotiation is handled by the local
 * and remote endpoints. A renegotiation of the session may include
 * modifications and removal of current media as well as adding new media. The
 * renegotiation can be initiated by either endpoint. In <i>Figure 1</i> and
 * <i>Figure 2</i> the session update is initiated by the same endpoint that
 * initiated the session.
 * <p>
 * <p/>
 * <p/>
 * </p>
 * <h3>Mobile Originated Session Renegotiation</h3>
 * <p/>
 * <h4>STATE_ESTABLISHED</h4>
 * In this state the endpoint can modify the session as well as adding new
 * media. An server update from the remote endpoint will discard all changes
 * that the local endpoint has made to the session.
 * <ul>
 * <li> <code>STATE_RENEGOTIATING</code> when the callback
 * <code>sessionUpdateRecieved</code> is invoked.</li>
 * <p/>
 * <li> <code>STATE_TERMINATED</code> when the callback
 * <code>sessionTerminated</code> is invoked.</li>
 * <li> <code>STATE_TERMINATING</code> if <code>terminate</code> is called on the
 * session.</li>
 * <p/>
 * </ul>
 * <p/>
 * <h4>STATE_RENEGOTIATING</h4>
 * <p/>
 * A <code>Session</code> assumes <code>STATE_RENEGOTIATING</code> through a
 * call to <code>update</code>. Incoming session updates from the remote
 * endpoint will be discarded in this state.
 * <p>
 * <p/>
 * A <code>Session</code> transits back to <code>STATE_ESTABLISHED</code> when
 * the listener method:
 * </p>
 * <ul>
 * <p/>
 * <li> <code>sessionUpdated</code> is called, meaning that the remote endpoint
 * accepted the new session offer.</li>
 * <li> <code>sessionUpdateFailed</code> is called, but here meaning that the
 * remote endpoint rejected the update and the session keeps its previous
 * settings.</li>
 * </ul>
 * <p/>
 * <h3>Mobile Terminated Session Renegotiation</h3>
 * <p/>
 * <p/>
 * <h4>STATE_RENEGOTIATING</h4>
 * <p/>
 * If the remote endpoint has offered changes to the session, the state will
 * transit to <code>STATE_RENEGOTIATING</code> and the session will be notified
 * with a call to the <code>sessionUpdateReceived</code> in the
 * <code>SessionListener</code> interface.
 * <p>
 * <p/>
 * A <code>Session</code> transits to:
 * </p>
 * <ul>
 * <p/>
 * <li> <code>STATE_REESTABLISHING</code> if the <code>accept</code> method of
 * the <code>Session</code> is called, thus the media offer is accepted.</li>
 * <li> <code>STATE_ESTABLISHED</code> if the <code>reject</code> method is
 * called but in this case the session will keep its old settings.</li>
 * <p/>
 * <li> <code>STATE_TERMINATING</code> if <code>terminate</code> is called on the
 * session.</li>
 * <li> <code>STATE_TERMINATED</code> when the callback
 * <code>sessionTerminated</code> is invoked.</li>
 * <p/>
 * </ul>
 * <p/>
 * <h4>STATE_REESTABLISHING</h4>
 * In this state <code>accept</code> has been called and the
 * <code>Session</code> is about to be updated at both endpoints.
 * <p/>
 * A <code>Session</code> transits to:
 * <ul>
 * <p/>
 * <li> <code>STATE_ESTABLISHED</code> when the callback
 * <code>sessionUpdated</code> is invoked.</li>
 * <li> <code>STATE_TERMINATING</code> if <code>terminate</code> is called on the
 * session.</li>
 * <p/>
 * <li> <code>STATE_TERMINATED</code> when the callback
 * <code>sessionTerminated</code> is invoked.</li>
 * </ul>
 * <p/>
 * <h2>Session Termination</h2>
 * The <code>terminate</code> method can be called from both endpoints on the
 * <code>Session</code> and thereby cancel the ongoing session establishment or
 * terminate a <code>Session</code> in progress.
 * <p>
 * <p/>
 * <p/>
 * </p>
 * <h4>STATE_TERMINATING</h4> A <code>Session</code> enters
 * <code>STATE_TERMINATING</code> when the <code>terminate</code> method has
 * been called on a <code>Session</code>.
 * <p>
 * <p/>
 * A <code>Session</code> transits to:
 * </p>
 * <ul>
 * <li> <code>STATE_TERMINATED</code> when the remote endpoint has acknowledged
 * the terminate request. The <code>sessionTerminated</code> callback will then
 * be invoked.</li>
 * </ul>
 * <p/>
 * <p>
 * An server terminate request from the remote endpoint is notified with the
 * <code>sessionTerminated</code> callback.
 * </p>
 */

public interface Session extends ServiceMethod {
    /**
     * This state specifies that the Session is established.
     */
    static final int STATE_ESTABLISHED = 4;

    /**
     * This state specifies that the Session is accepted by the mobile
     * terminated endpoint.
     */
    static final int STATE_ESTABLISHING = 3;

    /**
     * This state specifies that the Session is created but not started.
     */
    static final int STATE_INITIATED = 1;

    /**
     * This state specifies that the Session establishment has been started.
     */
    static final int STATE_NEGOTIATING = 2;

    /**
     * This state specifies that the Session update is accepted by the mobile
     * terminated endpoint.
     */
    static final int STATE_REESTABLISHING = 6;

    /**
     * This state specifies that the Session is negotiating updates to the
     * session.
     */
    static final int STATE_RENEGOTIATING = 5;

    /**
     * This state specifies that the Session has been terminated.
     */
    static final int STATE_TERMINATED = 8;

    /**
     * This state specifies that an established Session is being terminated by
     * the local endpoint.
     */
    static final int STATE_TERMINATING = 7;

    /**
     * Identifier for the SIP status code "433 - Anonymity Disallowed".
     */
    static final int STATUSCODE_433_ANONYMITY_DISALLOWED = 433;

    /**
     * Identifier for the SIP status code "480 - Temporarily Unavailable".
     */
    static final int STATUSCODE_480_TEMPORARILY_UNAVAILABLE = 480;

    /**
     * Identifier for the SIP status code "486 - Busy Here".
     */
    static final int STATUSCODE_486_BUSY_HERE = 486;

    /**
     * Identifier for the SIP status code "488 - Not Acceptable Here".
     */
    static final int STATUSCODE_488_NOT_ACCEPTABLE_HERE = 488;

    /**
     * Identifier for the SIP status code "600 - Busy Everywhere".
     */
    static final int STATUSCODE_600_BUSY_EVERYWHERE = 600;

    /**
     * Identifier for the SIP status code "603 - Decline".
     */
    static final int STATUSCODE_603_DECLINE = 603;

    /**
     * This method can be used to accept a session invitation or a session
     * update depending on the context.
     * <p/>
     * It can be used to accept a session invitation in STATE_NEGOTIATING if the
     * remote endpoint has initiated the session. The Session will transit to
     * STATE_ESTABLISHING.
     * <p/>
     * It can be used to accept a session update in STATE_RENEGOTIATING if the
     * remote endpoint has initiated the session update. The Session will
     * transit to STATE_REESTABLISHING.
     * 
     * @throws IllegalStateException
     *             - if the Session is not in STATE_NEGOTIATING or
     *             STATE_RENEGOTIATING ImsException - if the Session can't be
     *             accepted
     */
    void accept() throws ImsException;

    /**
     * Creates a Capabilities with the remote endpoint.
     * 
     * @return
     * 
     * @throws ImsException
     *             - if the Capabilities could not be created
     * @throws IllegalStateException
     *             - if the Session is not in STATE_ESTABLISHED
     */
    Capabilities createCapabilities() throws ImsException;

    /**
     * Creates a Media object with a media type name and adds it to the Session.
     * If a Media is added to an established Session, the application has the
     * responsibility to call update on the Session.
     * 
     * @param type
     *            - a Media type
     * @param direction
     *            - the direction of the Media flow
     * @return a new Media implementing the type name interface
     * @throws IllegalArgumentException
     *             - if the direction or type argument is invalid
     *             IllegalStateException - if the Session is not in
     *             STATE_ESTABLISHED, STATE_INITIATED
     */
    Media createMedia(Media.MediaType type, int direction);

    /**
     * This method is used for referring the remote endpoint to a third party
     * user or service.
     * 
     * @param referTo
     *            - name to refer to
     * @param referMethod
     *            - the reference method
     * @return
     * @throws IllegalArgumentException
     *             if the <code>referTo</code> argument is <code>null</code>
     * @throws IllegalArgumentException
     *             if the <code>referMethod</code> argument is not recognized by
     *             the device
     * @throws ImsException
     *             if the <code>Reference</code> could not be created
     * @throws IllegalStateException
     *             if the <code>Session</code> is not in
     *             <code>STATE_ESTABLISHED</code>
     */
    Reference createReference(String referTo, String referMethod)
            throws ImsException;

    /**
     * Returns the Media that are part of this Session .
     * 
     * @return
     */
    Media[] getMedia();

    /**
     * Returns the session descriptor associated with this Session.
     * 
     * @return
     */
    SessionDescriptor getSessionDescriptor();

    /**
     * Returns the current state of this Session.
     * 
     * @return
     */
    int getState();

    /**
     * This method checks if there are changes in this Session that have not
     * been negotiated.
     * 
     * @return true if there is a pending update, false otherwise
     */
    boolean hasPendingUpdate();

    /**
     * This method can be used to reject a session invitation or a session
     * update depending on the context.
     * 
     * @throws IllegalStateException
     *             - if the Session is not in STATE_NEGOTIATING or
     *             STATE_RENEGOTIATING
     */
    void reject();

    /**
     * This method can be used to reject a session invitation or a session
     * update depending on the context with a specific SIP status code.
     * 
     * @param statusCode
     *            - one from: STATUSCODE_433_ANONYMITY_DISALLOWED,
     *            STATUSCODE_480_TEMPORARILY_UNAVAILABLE,
     *            STATUSCODE_488_NOT_ACCEPABLE_HERE,
     *            STATUSCODE_600_BUSY_EVERYWHERE, STATUSCODE_486_BUSY_HERE, and
     *            STATUSCODE_603_DECLINE.
     * @throws IllegalStateException
     *             - if the Session is not in STATE_NEGOTIATING or
     *             STATE_RENEGOTIATING IllegalArgumentException - if the
     *             statusCode argument is not valid identifier
     */
    void reject(int statusCode);

    /**
     * This method can be used to reject a session invitation with the SIP
     * status code "302 - Moved Temporarily", along with an alternative contact
     * user address.
     * 
     * @param alternativeUserAddress
     */

    void rejectWithDiversion(String alternativeUserAddress);

    /**
     * Removes a Media from the Session.
     * <p/>
     * If a Media is removed from an established Session, the application has
     * the responsibility to call update on the Session.
     * 
     * @param media
     *            - the Media to remove from the Session
     * @throws IllegalArgumentException
     *             - if the Media does not exist in the Session or null
     *             IllegalStateException - if the Session is not in
     *             STATE_ESTABLISHED, STATE_INITIATED
     */
    void removeMedia(Media media);

    /**
     * This method removes all updates that have been made to this Session and
     * to medias that are part of this Session that have not been negotiated.
     */
    void restore();

    /**
     * Sets a listener for this Session, replacing any previous SessionListener.
     * A null reference is allowed and has the effect of removing any existing
     * listener.
     * 
     * @param listener
     *            - the listener to set
     */
    void setListener(SessionListener listener);

    /**
     * Starts a session. When this method is called the remote endpoint is
     * invited to the session. All media MUST be initialized (see rules for the
     * respective media type) for the session to start.
     * <p/>
     * The Session will transit to STATE_NEGOTIATING after calling this method.
     * 
     * @throws IllegalStateException
     *             - if the Session is not in STATE_INITIATED ImsException - if
     *             a Media in the Session is not initialized correctly or if
     *             there are no Media in the Session
     */
    void start() throws ImsException;

    /**
     * Terminate or cancel a session. A session that has been started should
     * always be terminated using this method.
     * <p/>
     * The Session will transit to STATE_TERMINATING.
     * <p/>
     * If the Session is STATE_TERMINATING or STATE_TERMINATED this method will
     * not do anything. If the Session is STATE_INITIATED the Session will
     * transit directly to STATE_TERMINATED, the sessionTerminated callback will
     * not be invoked.
     */
    void terminate();

    /**
     * Synchronizes the session modifications that an application has done with
     * the remote endpoint. Modifications include adding of media, removal of
     * media, and change of existing media (e.g. directionality).
     * <p/>
     * The Session will transit to STATE_RENEGOTIATING after calling this
     * method.
     * 
     * @throws IllegalStateException
     *             - if the hasPendingUpdate method returns false, meaning that
     *             there are no updates to be made to the session
     * @throws IllegalStateException
     *             - if the Session is not in STATE_ESTABLISHED
     * @throws ImsException
     *             - if a Media in the Session is not initiated correctly or if
     *             there are no Media in the Session
     */
    void update() throws ImsException;

}
