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

package javax.microedition.ims.xdm;

import org.w3c.dom.Document;

import javax.microedition.ims.ReasonInfo;

/**
 * This listener type is used to notify the application about document
 * subscription status and updates on documents on the XDM server.
 * 
 * <h4>Notifications of document updates</h4>
 * 
 * Once the subscription has been established, the XDM server will send
 * notifications to the client of document updates. Notifications are in the
 * form of XCAP diff documents. XCAP diff is described in [XCAP-DIFF].
 * 
 * An XCAP diff document can contain information about updates of one or more
 * XDM documents, and for each XDM document, the XCAP diff document can contain
 * information of multiple updates. The different types of updates are:
 * 
 * <ul>
 * <li>Initial information about the XDM document, including the document ETag,
 * sent when the subscription starts.</li>
 * <li>Information about a change in a document, including the old and new ETag
 * and optionally details of the change.</li>
 * <li>Information about a change of the ETag of a document.</li>
 * <li>Information about the deletion of a document.</li>
 * <li>Information about the creation of a new document.</li>
 * </ul>
 * 
 * For each XDM document mentioned in the XCAP diff document, one of the methods
 * documentUpdateReceived or documentDeleted is called once. Note that multiple
 * updates to a single document will always result in exactly one method call.
 * Also note that it is up to the XDM server to decide how often an XCAP diff
 * document is sent to the client, meaning that a single XCAP diff document can
 * include any number of updates.
 * 
 * If the document has been deleted, and a new document has not been created
 * with the same document selector, documentDeleted is called. In all other
 * cases, documentUpdateReceived is called. This includes the following cases:
 * 
 * <ul>
 * <li>When the subscription starts and the initial notification is received.</li>
 * <li>If a new document is created.</li>
 * <li>If a document is changed.</li>
 * <li>If a document is deleted and then a new document with the same document
 * selector is created.</li>
 * </ul>
 * 
 * @see DocumentSubscriber
 * 
 * @author Andrei Khomushko
 * 
 */
public interface DocumentSubscriberListener {
    /**
     * Notifies the application when a document has been deleted on the XDM
     * server.
     * 
     * For details on when this method is called, see the interface description.
     * 
     * Note: If the DocumentSubscriber was created with a document selector (as
     * opposed to a collection URL), the document selector received in this
     * method will be an exact match to the document selector used to create the
     * DocumentSubscriber .
     * 
     * @param subscriber
     *            - the concerned DocumentSubscriber
     * @param documentSelector
     *            - the document selector of the deleted document
     * @param xcapDiff
     *            - the XCAP diff XML document
     */
    void documentDeleted(DocumentSubscriber subscriber,
            String documentSelector, Document xcapDiff);

    /**
     * Notifies the application when a document on the XDM server has been
     * updated.
     * 
     * For details on when this method is called, see the interface description.
     * 
     * Note: If the DocumentSubscriber was created with a document selector (as
     * opposed to a collection URL), the document selector received in this
     * method will be an exact match to the document selector used to create the
     * DocumentSubscriber.
     * 
     * @param subscriber
     *            - the concerned DocumentSubscriber
     * @param documentSelector
     *            - the document selector of the deleted document
     * @param xcapDiff
     *            - the XCAP diff XML document
     */
    void documentUpdateReceived(DocumentSubscriber subscriber,
            String documentSelector, Document xcapDiff);

    /**
     * Notifies the application that the subscription request failed.
     * 
     * The DocumentSubscriber has transited to STATE_INACTIVE.
     * 
     * @param subscriber
     *            - the concerned DocumentSubscriber
     * @param reason
     *            - a ReasonInfo to indicate why the subscription request failed
     */
    void subscriptionFailed(DocumentSubscriber subscriber, ReasonInfo reason);

    /**
     * Notifies the application that the subscription was successfully started.
     * documentUpdateReceived and documentDeleted will be called when the server
     * sends notifications.
     * 
     * The DocumentSubscriber has transited to STATE_ACTIVE.
     * 
     * @param subscriber
     *            - the concerned DocumentSubscriber
     */
    void subscriptionStarted(DocumentSubscriber subscriber);

    /**
     * Notifies the application that the subscription has been terminated.
     * 
     * The DocumentSubscriber has transited to STATE_INACTIVE.
     * 
     * @param subscriber
     *            - the concerned DocumentSubscriber
     */
    void subscriptionTerminated(DocumentSubscriber subscriber);
}
