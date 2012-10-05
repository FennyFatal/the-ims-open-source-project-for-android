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
 * IMS endpoints.
 *
 *
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation
 *
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
