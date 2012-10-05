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
import javax.microedition.ims.Service;
import javax.microedition.ims.ServiceClosedException;

/**
 * The <code>CoreService</code> gives the application the possibility to call
 * remote peers over the IMS network. 
 *
 *
 *
 * </p><p>For detailed implementation guidelines and complete API docs,
 * please refer to JSR-281 and JSR-235 documentation.
 *
 * @see CoreServiceListener
 */


public interface CoreService extends Service {

    /**
     * Creates a Capabilities with from as sender, addressed to to.
     * @param from - the sender SIP or TEL URI with an optional display name. If null, from is assumed to be the local user identity of the Service
     * @param to - the recipient SIP or TEL URI with an optional display name.
     * @return a new Capabilities
     * @throw IllegalArgumentException - if the to argument is null
     * @throw IllegalArgumentException - if the syntax of the from or to argument is invalid
     * @throw ServiceClosedException - if the Service is closed
     * @throw ImsException - if the Capabilities could not be created
     */
     Capabilities createCapabilities(String from, String to) 
         throws ServiceClosedException, ImsException;

    /**
     * Creates a PageMessage with from as sender, addressed to to.
     *
     * @param from - the sender SIP or TEL URI with an optional display name. If null, from is assumed to be the local user identity of the Service
     * @param to   - the recipient SIP or TEL URI with an optional display name to send a PageMessage to
     * @return a new PageMessage
     * @throws IllegalArgumentException - if the to argument is null
     *                                  IllegalArgumentException - if the syntax of the from or to argument is invalid
     *                                  ServiceClosedException - if the Service is closed
     *                                  ImsException - if the PageMessage could not be created
     */
    PageMessage createPageMessage(String from, String to)
            throws ServiceClosedException, ImsException;

    /**
     * Creates a Publication for an event package with from as sender and to as the user identity to publish event state on.
     * <p/>
     * @param from - the sender SIP or TEL URI with an optional display name. 
     * If null, from is assumed to be the local user identity of the Service
     * @param to - the recipient SIP or TEL URI with an optional display name to publish event state information on. 
     * If null, to is assumed to be the local user of the Service
     * @param event - the event package to publish event state information on
     * 
     * @return a new Publication
     * 
     * @throws IllegalArgumentException - if the syntax of the from or to argument is invalid
     * @throws IllegalArgumentException - if the event argument is not a defined event package
     * @throws ServiceClosedException - if the Service is closed
     * @throws ImsException - if the Publication could not be created
     */
    Publication createPublication(String from, String to, String event) throws ServiceClosedException, ImsException;

    /**
     * Creates a <code>Reference</code> with <code>from</code> as sender,
     * addressed to <code>to</code> and <code>referTo</code> as the URI to
     * refer to.
     *
     * @param from        the sender SIP or TEL URI with an optional display name. If
     *                    <code>null</code>, it is assumed to be the local user identity
     *                    of the <code>Service</code>
     * @param to          the recipient SIP or TEL URI with an optional display name
     * @param referTo     any URI, not just a user id
     * @param referMethod the reference method to be used by the reference
     *                    request, e.g. "INVITE", "BYE" or <code>null</code>
     * @return a new <code>Reference</code>
     * @throws IllegalArgumentException if the <code>to</code> or
     *                                  <code>referTo</code> argument is <code>null</code>
     * @throws IllegalArgumentException if the syntax of the <code>from</code>,
     *                                  <code>to</code> is invalid, or if <code>referTo</code>
     *                                  argument is <code>null</code>
     * @throws IllegalArgumentException if the syntax of the
     *                                  <code>referMethod</code> argument is invalid
     * @throws ServiceClosedException   if the <code>Service</code> is closed
     * @throws ImsException             if the <code>Reference</code> could not be created
     */
    Reference createReference(String from, String to, String referTo, String referMethod)
            throws ServiceClosedException, ImsException;

    /**
     * Creates a <code>Session</code> with <code>from</code> as sender, with
     * <code>to</code> as recipient.
     *
     * @param from - the sender SIP or TEL URI with an optional display name. If
     *             the argument is <code>null</code> then it is by default set to
     *             the preferred public user identity from this core service. If it
     *             has a domain name of <code>anonymous.invalid</code> then the
     *             originator will be kept anonymous according to [RFC3323] and
     *             [RFC3325]. <br>
     *             The format of SIP URI is described in [RFC3261], TEL URI in
     *             [RFC3966], and display names in general in [RFC5322].
     * @param to   - the recipient SIP or TEL URI with an optional display name.
     * @return a new <code>Session</code>
     * @throws IllegalArgumentException if the <code>to</code> argument is
     *                                  <code>null</code>
     * @throws IllegalArgumentException if the syntax of the <code>from</code>
     *                                  or <code>to</code> argument is invalid
     * @throws ServiceClosedException   if the <code>Service</code> is closed
     * @throws ImsException             if the <code>Session</code> could not be created
     */
    Session createSession(String from, String to) throws ServiceClosedException, ImsException;

    /**
     * Creates a Subscription for an event package with from as sender and 
     * to as the user identity to subscribe event state on.
     * 
     * @param from - the sender SIP or TEL URI with an optional display name. 
     * If null, from is assumed to be the local user identity of the Service
     * @param to - the recipient SIP or TEL URI with an optional display name 
     * to subscribe state information on. If null, to is assumed is assumed 
     * to be the local user of the Service
     * @param event - the event package to subscribe state information on
     * @return a new Subscription
     * @throws IllegalArgumentException - if the syntax of the from  or to argument is invalid
     * @throws IllegalArgumentException - if the event argument is not a defined event package
     * @throws ServiceClosedException - if the Service is closed
     * @throws ImsException - if the Subscription could not be created
     */
    Subscription createSubscription(String from, String to, String event) throws ServiceClosedException, ImsException;

    /**
     * Returns the display name and public user identity for the CoreService.
     *
     * @return
     */
    String getLocalUserId();

    /**
     * Sets a listener for this CoreService, replacing any previous CoreServiceListener.
     * A <code>null</code> removes any existing listener.
      *
      * @param listener
      */
	 void setListener(CoreServiceListener listener);
}
