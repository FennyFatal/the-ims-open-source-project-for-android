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

package javax.microedition.ims;

// TODO: this should extend the "io.Connection"

/**
 * <code>Service</code> is the base interface for IMS Services, and follows
 * the <i>Generic Connection Framework</i> (GCF).
 * </p><h3>Creating services using the Generic Connection Framework</h3>
 * <p/>
 * The application creates services using the <code>Connector.open()</code>
 * where the string argument has the general form
 * (described in [RFC2396]):
 * <ul>
 * <li><b><code>&lt;scheme&gt;:&lt;target&gt;[&lt;params&gt;]</code></b></li>
 * </ul>
 * where:
 * <ul>
 * <p/>
 * <li><b><code>scheme</code></b> is a supported IMS scheme. In this
 * specification the <code>imscore</code> is defined in the <code>CoreService</code>
 * interface
 * </li>
 * <li><code><b>target</b></code> is an AppId. An AppId should follow the form
 * of fully qualified Java class names or any naming syntax that provides a
 * natural way to differentiate between vendors.</li>
 * <p/>
 * <li><code><b>params</b></code> are optional semi-colon separated parameters
 * particular to the IMS scheme used. If the parameter value is set
 * incorrectly an <code>IllegalArgumentException</code> is thrown.</li>
 * </ul>
 * The returned <code>Service</code> object depends on the used scheme.
 * <p>
 * The call to <code>Connector.open()</code> is synchronous, and for some
 * services the call might take several seconds to complete. Also observe
 * that the following actions to open a service are equivalent:
 * <code>Connector.open("imscore://myAppId;ServiceID=")</code> and
 * <code>Connector.open("imscore://myAppId")</code>.
 * <p/>
 * </p><h4>Exceptions when opening a service</h4>
 * <p/>
 * The following exceptions can be thrown when calling
 * <code>Connector.Open()</code>.
 * <p></p><ul>
 * <li><code>IllegalArgumentException</code> - If a parameter is invalid.</li>
 * <li><code>ConnectionNotFoundException</code> - If the target of the name
 * cannot be found, or if the requested protocol type is not supported.</li>
 * <li><code>IOException</code> - If some other kind of I/O error occurs.</li>
 * <li><code>SecurityException</code> - May be thrown if access to the protocol
 * handler is prohibited.</li>
 * <p/>
 * </ul>
 * <p/>
 * The following exceptions can be throws due to IMS specific errors.
 * <p></p><ul>
 * <li><code>IOException</code> - The IMS profile is missing or corrupt.</li>
 * <li><code>IOException</code> - The Internet profile is missing or corrupt.
 * </li>
 * <li><code>IOException</code> - The subscriber failed to authenticate within
 * the IMS network.</li>
 * <p/>
 * <li><code>IOException</code> - The Register request failed to reach the IMS
 * network.</li>
 * <li><code>IOException</code> - The IMS network responded with a service or
 * server specific error code.</li>
 * <li><code>ConnectionNotFoundException</code> - The AppId is not an installed
 * IMS Application.</li>
 * </ul>
 * <p/>
 * <h4>Closing a service</h4>
 * The application SHOULD invoke <code>close()</code> on the
 * <code>Service</code> when it is finished using the <code>Service</code>.
 *
 * @see CoreService
 */

public interface Service extends Connection {
    /**
     * Returns the application id string that this Service was created with.
     *
     * @return application ID.
     */
    String getAppId();

    /**
     * Returns the scheme used for this Service.
     *
     * @return the scheme.
     */
	String getScheme();
}
