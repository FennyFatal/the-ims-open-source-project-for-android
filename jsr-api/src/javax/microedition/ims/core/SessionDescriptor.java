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

/**
 * The Session Description Protocol (SDP) [RFC4566] is used to convey
 * information about media streams in a session such that the endpoints
 * can connect and participate. SDP standardizes
 * a textual format for such information in the application/sdp media type.
 * A content of that type is generally referred to as "an SDP",
 * or "the SDP" when the reference to the particular content is unambiguous.
 * </p><p>
 * The endpoints in a session, exchange SDPs according to the SDP Offer/Answer
 * model [RFC3264] in the SIP signalling. The IMS Engine manages this
 * process at all times. For a session at a particular state, and at an
 * endpoint, there is a pair of SDPs: an
 * "server SDP", part of a SIP message received from the remote, and an
 * "client SDP", that is to be put in a SIP message to be sent
 * to the remote. Note that not all SIP messages carry SDP bodies.</p><p>
 * The <code>SessionDescriptor</code> is an interface to the session-level
 * part of this SDP pair (for the media-level part, see
 * <code>MediaDescriptor</code>). The
 * getters read from
 * the last server SDP. The setters apply to all client SDPs in the current
 * session, beginning with the one to be sent next. Note that using the
 * interface setters does not in itself trigger the IMS core to send the
 * SDP in a SIP message. If there is a point in the
 * session lifetime where the modifications should cease, the application
 * is responsible to use the setters again, and make sure the SDP is sent.
 * </p><p>
 * <p/>
 * On the originating endpoint, it is most useful for the application to
 * use the setters when the session is in the initiated state. The changes will
 * then be in effect from the start of the session. It is not possible
 * to use the getters since there is no server SDP at the first
 * state. In an established
 * session, the originating endpoint for a session update may read and change
 * the SDP before applying the update.
 * </p><p>
 * On the terminating endpoint, the application can read the server SDP
 * but not set an client SDP in the
 * <code>CoreService.SessionInvitationReceived</code>
 * callback. The reason is that all SDP carrying messages have already been
 * exchanged at the time of callback. If the terminating side wants to add
 * attributes it has to do so when the session is established, and trigger a
 * session update.
 * </p><p>
 * There are several places in the call flow at session setup and session
 * update where the application have no opportunity to influence the
 * negotiation, for example right after receiving the INVITE at the terminating
 * endpoint. In this case, the IMS core copies all application-defined
 * SDP attributes from the offer into the answer, and from the answer into
 * subsequent offers until the session is established or updated.
 * </p><p>
 * To use the interface in a meaningful way, a thorough understanding of
 * the SDP protocol and its use in IMS and SIP is assumed.
 * </p><p>
 * For a session with a streaming media object of audio and video type,
 * there could be an SDP that looks something like:
 * <p/>
 * </p><pre><b>
 * <p/>
 * v=0
 * o=alice 2890844526 2890844526 IN IP4 host.atlanta.example.com
 * s=
 * c=IN IP4 host.atlanta.example.com
 * t=0 0</b>
 * m=audio 49170 RTP/AVP 0 8 97
 * a=rtpmap:0 PCMU/8000
 * a=rtpmap:8 PCMA/8000
 * a=rtpmap:97 iLBC/8000
 * m=video 51372 RTP/AVP 31 32
 * a=rtpmap:31 H261/90000
 * a=rtpmap:32 MPV/90000
 * </pre>
 * <p/>
 * The session-level part includes the bold lines. The application can get, and
 * set some SDP lines that are not reserved for the IMS core. Attributes can
 * be get, set and removed freely except reserved. The following attributes are
 * classified as reserved and can not be modified
 * using the <code>SessionDescriptor</code> interface: charset, charset:iso8895-1, group,
 * maxprate, ice-lite, ice-mismatch, ice-options, ice-pwd, ice-ufrag, inactive,
 * sendonly, recvonly, sendrecv, csup, creq, acap and tcap
 */

public interface SessionDescriptor {
    /**
     * Adds an attribute (a=) to the Session.
     * <p/>
     * Adding attributes that the IMS engine has reserved, such as "sendonly",
     * "recvonly", "sendrecv", leads to IllegalArgumentException.
     * <p/>
     * Example:
     * SessionDescriptor desc = session.getSessionDescriptor();
     * desc.addAttribute("synced");
     * <p/>
     * The resulting attribute line will be:
     * a=synced
     *
     * @param attribute - the attribute to add
     * @throws IllegalArgumentException - if the attribute argument is null or if the syntax of the attribute argument is invalid
     *                                  IllegalArgumentException - if the attribute already exists in the Session
     *                                  IllegalArgumentException - if the attribute is reserved for the IMS engine
     *                                  IllegalStateException - if the Session is not in STATE_INITIATED or STATE_ESTABLISHED state
     */
    void addAttribute(String attribute);

    /**
     * Returns all attributes (the a= lines) for the session.
     * If there are no attributes, an empty string array will be returned.
     *
     * @return a string array containing the attributes
     */
    String[] getAttributes();

    /**
     * Returns the version (v=) of the SDP.
     *
     * @return
     */
    String getProtocolVersion();

    /**
     * Returns a unique identifier (o=) for the session.
     *
     * @return
     */
    String getSessionId();

    /**
     * Returns textual information (i=) about the session.
     *
     * @return
     */
    String getSessionInfo();

    /**
     * Returns the textual session name (s=).
     *
     * @return
     */
    String getSessionName();

    /**
     * Removes an attribute (a=) from the session.
     *
     * @param attribute - the attribute to remove
     * @throws IllegalArgumentException - if the attribute argument is null
     *                                  IllegalArgumentException - if the attribute does not exist in the Session
     *                                  IllegalArgumentException - if the attribute is reserved for the IMS engine
     *                                  IllegalStateException - if the Session is not in STATE_INITIATED or STATE_ESTABLISHED state
     */
    void removeAttribute(String attribute);

    /**
     * Sets the textual information (i=) about the session.
     *
     * @param info - session information
     * @throw IllegalStateException - if the Session is not in STATE_INITIATED
     * @throw IllegalArgumentException - if the info argument is null
     */
    void setSessionInfo(String info);

    /**
     * Sets the name of the session (s=).
     *
     * @param name -  - the session name
     * @throws IllegalStateException - if the Session is not in STATE_INITIATED
     *                               IllegalArgumentException - if the name argument is null
     */
	void setSessionName(String name);
    
}
