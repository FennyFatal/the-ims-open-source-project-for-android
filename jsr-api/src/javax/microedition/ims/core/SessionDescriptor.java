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
 * The Session Description Protocol (SDP) for representing
 * information about media streams. SessionDescriptor contains these
 * media attributes.
 *
 *
 *
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation
 *
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
