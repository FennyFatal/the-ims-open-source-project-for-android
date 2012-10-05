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

package javax.microedition.ims.im;

import javax.microedition.ims.ReasonInfo;

/**
 * A listener type for receiving notifications of when messages are sent 
 * or received. The transfer progress of large messages can also be observed. 
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation.
 */
public interface MessageManagerListener {
    
    /**
     * Notifies the application of a large message request. The application 
     * must accept or reject the largeMessageRequest.
     *     
     * @param largeMessageRequest - a handler for accepting or rejecting the large message request
     */
    void incomingLargeMessage(LargeMessageRequest largeMessageRequest);
    
    /**
     * Notifies the application that a message was received.
     * 
     * @param message - the received Message  
     */
    void messageReceived(Message message);
    
    /**
     * Notifies the application that a large message could not be received. 
     * This could either be because the transfer has been canceled, because 
     * of a connection error, or because the request has expired.
     * 
     * @param messageId - the message identifier of the corresponding large message
     * @param reason- the reason info to indicate why the message could not be received
     */
    void messageReceiveFailed(String messageId, ReasonInfo reason);
    
    /**
     * Notifies the application that the message identified by messageId 
     * could not be successfully sent.
     * 
     * @param messageId - the message identifier of the corresponding Message
     * @param reason - the reason info to indicate why the request failed
     */
    void messageSendFailed(String messageId, ReasonInfo reason);
    
    /**
     * Notifies the application that the message identified by messageId was successfully sent.
     * <p/>
     * Note: This does not indicate that the message was delivered to the 
     * recipients, only that the message was accepted by the IMS network. 
     * 
     * @param messageId - the message identifier of the corresponding Message
     */
    void messageSent(String messageId);
    
    /**
     * Notifies the application when there is progress to be reported on 
     * a transfer of a large message. For small transfers this method may never be called.
     * 
     * @param messageId - the message identifier of the large message
     * @param bytesTransferred - the total number of bytes that has been transferred
     * @param bytesTotal - the total number of bytes in the content, -1 if the size is not known
     */
    void transferProgress(String messageId, int bytesTransferred, int bytesTotal);

}
