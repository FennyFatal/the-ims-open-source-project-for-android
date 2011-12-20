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

import javax.microedition.ims.ServiceClosedException;

/**
 * The Capabilities is used to query a remote endpoint whether it has matching
 * capabilities matching the local ones.
 * 
 * @author Andrei Khomushko
 * 
 */
public interface Capabilities {
    
    /** The capability response is received. */
    int STATE_ACTIVE = 3;

    /** The capability request has not been sent. */
    int STATE_INACTIVE = 1;

    /**
     * The capability request is sent and the platform is waiting for a
     * response.
     */
    int STATE_PENDING = 2;

    /**
     * Sends a capability request to a remote endpoint. The Capabilities will
     * transit to STATE_PENDING after calling this method.
     * 
     * @param sdpInRequest
     *            - if true, the IMS engine will add a Session Description
     *            Protocol (SDP) to the capability request
     * @throw ServiceClosedException - if the Service is closed
     * @throw IllegalStateException - if the Capabilities is not in
     *        STATE_INACTIVE
     */
    void queryCapabilities(boolean sdpInRequest) throws ServiceClosedException;

    /**
     * Returns an array of strings representing valid user identities for the
     * remote endpoint.
     * 
     * @return an array of user identities, an empty array will be returned if
     *         no user identities could be retrieved
     * @throws IllegalStateException
     *             - if the Capabilities is not in STATE_ACTIVE
     */
    String[] getRemoteUserIdentities();

    /**
     * Returns the current state of this Capabilities.
     * 
     * @return the current state of this Capabilities
     */
    int getState();

    /**
     * This method returns true if the remote endpoint is believed to be
     * sufficiently capable of handling requests from a certain core service on
     * the local endpoint. The core service is specified with an imscore
     * connector string.
     * 
     * Note: Even if this method returns true, it is not guaranteed that
     * requests are indeed supported at the remote.
     * 
     * @param connection
     *            - the imscore connector string to check.
     * @return true if the remote endpoint has the capabilities, otherwise false
     * @throws IllegalStateException
     *             - if the Capabilities is not in STATE_ACTIVE
     * @throws IllegalArgumentException - if the connection argument is
     *             null, unknown or invalid syntax
     */
    boolean hasCapabilities(String connection);

    /**
     * Sets a listener for this Capabilities, replacing any previous
     * CapabilitiesListener. A null reference is allowed and has the effect of
     * removing any existing listener.
     * 
     * @param listener
     *            - the listener to set, or null
     */
    void setListener(CapabilitiesListener listener);
}
