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
 * The <code>ServiceMethod</code> interface provides methods to manipulate the
 * next client request message and to inspect previously sent request and
 * response messages.
 *
 *
 *
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation
 *
 */

public interface ServiceMethod {
    /**
     * This method returns a handle to the next client request Message within this ServiceMethod to be manipulated by the local endpoint.
     *
     * @return the next client request Message created for this ServiceMethod
     */
    Message getNextRequest();

    /**
     * This method returns a handle to the next client response Message within this ServiceMethod.
     * <p/>
     * It is only possible to manipulate the responses that are due to an application triggering action. T
     * his happens when the application takes an action in response to a callback: Session.accept and Session.reject
     * from a session invitation or update. Otherwise null will be returned.
     *
     * @return the next client response Message created for this ServiceMethod or null
     */
    Message getNextResponse();

    /**
     * This method enables the user to inspect a previously sent or received request message.
     * It is only possible to inspect the last request of each interface method identifier.
     * This method will return null if the interface method identifier has not been sent/received.
     *
     * @param method - the interface method identifier
     * @return the request Message
     * @throws IllegalArgumentException - if the method argument is not a valid identifier
     */
    Message getPreviousRequest(int method);

    /**
     * This method enables the user to inspect previously sent or received response messages.
     * It is only possible to inspect the response(s) for the last request of each interface method identifier.
     * This method will return null if the interface method identifier has not been sent/received.
     *
     * @param method - the interface method identifier
     * @return an array containing all responses associated to the method
     * @throws IllegalArgumentException - if the method argument is not a valid identifier
     */
    Message[] getPreviousResponses(int method);

    /**
     * Returns the remote user identities of this ServiceMethod.
     *
     * @return the remote user identity or null if the remote user identity could not be retrieved
      */
	 String[] 	getRemoteUserId();
     
}
