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
 * remote peers over the IMS network. There is a set of basic call types in the
 * CoreService that can be created via factory methods, see
 * <code>ServiceMethod</code>
 * </p><p>
 * The application can react to server IMS calls by implementing the listener
 * interface <code>CoreServiceListener</code>.
 * </p><p>
 * </p><h3>Integration of CoreService to GCF</h3>
 * <p/>
 * <h4>Connector.open(String name)</h4>
 * A <code>CoreService</code> is created with <code>Connector.open</code>,
 * see <code>Service</code>, using an imscore connector string as <code>name</code>:
 * <ul>
 * <li><b><code>imscore:&lt;target&gt;[&lt;params&gt;]</code></b></li>
 * <p/>
 * </ul>
 * where <code>target</code> is the application id, and
 * <code>&lt;params&gt;</code> are the optional semi-colon separated
 * parameters:
 * <ul>
 * <li><b><code>userId=&lt;public user identity&gt;</code></b> is the local
 * user identity that the user prefers to be in effect for this rpc service.
 * The preference is used to override the default public user identity of the
 * device. The userId parameter accepts a display name together with the URI
 * using standard syntax.</li>
 * <p/>
 * <li><b><code>serviceId=&lt;service alias&gt;</code></b> is set if this
 * <code>CoreService</code> shall support the service alias specified in the
 * Registry. If this parameter is unspecified, the service alias defaults to the
 * empty string.</li>
 * </ul>
 * <p>
 * The <code>javax.microedition.io.Connector</code> class is defined in the
 * CLDC specifications (JSR30, JSR139) and is used here to create services. The
 * <code>Open</code> method allows some variation, and it is shown below how
 * this works for the <code>imscore:</code> connector string.
 * <p/>
 * </p><h4>Connector.open(String name, String mode)</h4>
 * <p/>
 * The optional second parameter 'mode' in <code>Connector.open</code>
 * specifies the access mode that can be <code>READ</code>,
 * <code>WRITE</code>, <code>READ_WRITE</code>. The mode is not relevant.
 * <p>
 * </p><h4>Connector.open(String name, String mode, boolean timeouts)</h4>
 * The optional third parameter is a boolean flag that indicates if the calling
 * code can handle timeout exceptions when creating the service.
 * <p>
 * <p/>
 * </p><h4>Exceptions when opening a CoreService</h4>
 * The following exceptions can be throws due to <code>CoreService</code>
 * specific errors, see <code>Service</code> for more exceptions.
 * <p>
 * </p><ul>
 * <li><code>CoreServiceException</code> - The <code>CoreService</code>
 * <p/>
 * could not be created, see <code>javax.microedition.ims.rpc</code> for more
 * information. The reason for not creating the <code>CoreService</code> could
 * be retrieved from the <code>ReasonInfo</code> interface. </li>
 * <p/>
 * <pre> ...
 * } catch (CoreServiceException cse) {
 * ReasonInfo info;
 * String reasonPhrase;
 * int reasonType;
 * int statusCode;
 * <p/>
 * info = cse.getReasonInfo();
 * reasonPhrase = info.getReasonPhrase();
 * reasonType = info.getReasonType();
 * statusCode = info.getStatusCode();
 * ...
 * }
 * </pre>
 * <p/>
 * </ul>
 * <p/>
 * <h3>CoreService creation rules</h3>
 * For a given application identity and no service identity, it is possible to
 * create at most one instance of a <code>CoreService</code>. For a given
 * application identity and a service identity, it is possible to have at most
 * one <code>CoreService</code>.
 * <p>
 * <p/>
 * <p/>
 * </p><h4>Examples</h4>
 * This code shows how to create a <code>CoreService</code> with the
 * application identity <code>com.myCompany.games.chess</code>. In the first
 * example the user identity will be the default user identity provisioned by
 * the IMS network. In the second example the application overrides the user
 * identity to use in this <code>CoreService</code>. The third example shows
 * a call used to create a <code>CoreService</code> with a service identity
 * specified.
 * <p/>
 * <pre> service = (CoreService) Connector.open("imscore://com.myCompany.games.chess");
 * <p/>
 * service = (CoreService) Connector.open("imscore://com.myCompany.games.chess;
 * userId=Alice &lt;sip:alice@home.net&gt;");
 * service = (CoreService) Connector.open("imscore://com.myCompany.games.chess;
 * userId=Alice &lt;sip:alice@home.net&gt;
 * <p/>
 * serviceId=game");
 * </pre>
 * <p/>
 * <h4>Closing a CoreService</h4>
 * The application SHOULD invoke <code>close</code> on the
 * <code>CoreService</code> when it is finished using the
 * <code>CoreService</code>. The IMS core may also close the
 * <code>CoreService</code> due to external events. This will trigger a call
 * to <code>serviceClosed</code> on the <code>CoreServiceListener</code>
 * <p/>
 * interface.
 * <p/>
 * <h3>Creating a service method, example</h3>
 * Alice invites Bob using their public user identities as addresses: <br>
 * <p/>
 * <pre> coreService.createSession("sip:alice@home.net", "sip:bob@home.net");
 * </pre>
 * <p/>
 * <p>
 * The sender requests anonymity in this request by using "Anonymous" as display
 * name in the <code>from</code> argument: <br>
 * <p/>
 * <p/>
 * </p><pre> coreService.createSession("Anonymous &lt;sip:anonymous@anonymous.invalid&gt;",
 * "sip:bob@home.net");
 * </pre>
 * <p/>
 * <p>
 * Alice uses her display name when inviting Bob:
 * <p/>
 * </p><pre> coreService.createSession("Alice &lt;sip:alice@home.net&gt;", "sip:bob@home.net");
 * </pre>
 * <p/>
 * For more information on the contents of the <code>from</code> and
 * <code>to</code> arguments in service methods, please see section on User
 * and Service Addressing in the overview.
 * <p>
 * <p/>
 * </p><p>
 * <p/>
 * </p>
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
     * The event package must be defined as a JAD file property or set with the setRegistry method in the Configuration class. 
     * 
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
