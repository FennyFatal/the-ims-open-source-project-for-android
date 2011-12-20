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
 * response messages. The headers and body parts that are set will be
 * transmitted in the next message that is triggered by an interface method, see
 * code example below:
 * </p><p>
 * <p/>
 * </p><pre> Session mySession;
 * Message myMessage;
 * <p/>
 * mySession = coreService.createSession(null, "sip:bob@home.net");
 * myMessage = mySession.getNextRequest();
 * myMessage.addHeader("P-ImageIncluded", "yes");
 * mySession.start();
 * </pre>
 * <p/>
 * <p/>
 * In this code example, <code>mySession.start</code> is the triggering
 * interface method that sends a session invitation with a header
 * <code>"P-ImageIncluded"</code> that is set to <code>"yes"</code>.
 * <p>
 * <p/>
 * The latest transaction is saved for each interface method that triggers
 * messages to be sent, see <code>Message</code> interface for available message
 * identifiers.
 * </p><p>
 * <p/>
 * <p/>
 * </p><h2>Service Methods</h2>
 * Instances of service methods can be created by using factory methods in the
 * <code>CoreService</code> interface. To learn more about using the different
 * service methods please refer to the related interfaces, see list below:
 * <p/>
 * <ul>
 * <li><code>Session</code></li>
 * </ul>
 * <p/>
 * In general the methods in the subinterfaces should supply all the
 * functionality an application needs for each respective method. Should this
 * not be enough this superinterface gives some additional and powerful control
 * such as manipulating headers and body.
 * <p/>
 * <h2>Service Method Transactions</h2>
 * <p/>
 * The simplest form of communication in the IMS consists of two messages, an
 * initial request sent to a remote endpoint and then an answer message or
 * response. This interaction is commonly known as a transaction. Figure 1,
 * shows an example transaction.
 * <p/>
 * <br>
 * <p/>
 * <br>
 * <img src="doc-files/transaction-1.png"><br>
 * <br>
 * <i><b>Figure 1:</b> A simple transaction.</i> <br>
 * <br>
 * <p/>
 * The communication can grow in complexity and include several transactions. An
 * example could be the session invitation procedure with resource reservations,
 * see figure 2 below.
 * <p/>
 * <br>
 * <p/>
 * <br>
 * <img src="doc-files/transaction-2.png"><br>
 * <br>
 * <i><b>Figure 2:</b> The session invite procedure.</i> <br>
 * <br>
 * <p/>
 * A transaction is defined by one request and its related responses. There can
 * be more than one response for each request and there can be transactions with
 * no responses at all. All messages in the figure above are given a number to
 * clarify which transaction the different messages belong to.
 * <p>
 * <p/>
 * <p/>
 * </p><h2>Example of how to access headers and body parts</h2>
 * In this example, an image is attached as a separate body part and it is
 * indicated by setting a <code>"P-ImageIncluded"</code> header in the initial
 * INVITE method on the originating side.<br>
 * On the terminating side, the application retrieves the image and confirms
 * this by setting the header in the 200 Ok response message.
 * <p/>
 * <h3>Originating endpoint</h3>
 * <p/>
 * <pre> Session mySession = myService.createSession(...);
 * ...
 * Message myMessage = mySession.getNextRequest();
 * myMessage.addHeader("P-ImageIncluded", "yes");
 * MessageBodyPart messagePart = myMessage.createBodyPart();
 * messagePart.setHeader("Content-Type", "image/jpeg");
 * OutputStream os = messagePart.openContentOutputStream();
 * os.write(imageData);
 * os.flush();
 * os.close();
 * mySession.start();
 * </pre>
 * <p/>
 * <p/>
 * <h3>Terminating endpoint</h3>
 * <p/>
 * <pre> public void sessionInvitationReceived(CoreService service, Session session) {
 * <p/>
 * Message request = session.getPreviousRequest(Message.SESSION_START);
 * Message resp;
 * if ("yes".equals(mess.getHeaders("P-ImageIncluded")[0])) {
 * MessageBodyPart[] parts = mess.getBodyParts();
 * <p/>
 * for (int i = 0; i &lt; parts.length; i++) {
 * if ("image/jpeg".equals(parts[i].getHeader("Content-Type"))) {
 * InputStream is = parts[i].openContentInputStream();
 * byte[] data = new byte[1024];
 * // read content
 * while ((is.read(data)) != -1) {
 * ...
 * }
 * }
 * }
 * }
 * // Add header to accept response to session invitation
 * resp = session.getNextResponse();
 * resp.addHeader("P-ImageIncluded", "yes");
 * session.accept();
 * }
 * </pre>
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
