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

import javax.microedition.ims.ImsException;
import javax.microedition.ims.ServiceClosedException;

/**
 * The MessageManager can be used to send messages to or receive instant 
 * messages from an IM user or a list of IM users.
 *
 *
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation.
 * 
 */
public interface MessageManager {
    
    /**
     * Cancels the transfer of a large message that has not been completely delivered yet.
     * 
     * @param messageId - the message identifier of the large message
     * 
     * @throws IllegalArgumentException - if there is no large message transfer associated with the messageId
     */
    void cancel(String messageId);
    
    /**
     * Sends a message to the recipients specified in the Message as a large 
     * message. The recipients will have the possibility to accept or reject 
     * the message request. 
     * 
     * @param message - the Message to send
     * @param deliveryReport - true if a delivery report should be requested, false otherwise
     * 
     * @throws ServiceClosedException  - if the Service is closed 
     * @throws IllegalArgumentException - if the message  argument is null
     * @throws IllegalArgumentException - if there are no recipients specified in the message argument
     * @throws IllegalArgumentException - if there are no content parts specified in the message argument
     * @throws ImsException - if the message could not be sent to the network
     */
    void sendLargeMessage(Message message, boolean deliveryReport) throws ServiceClosedException, ImsException;
    
    /**
     * Sends a message to the recipients specified in the Message.
     * 
     * @param message - the Message to send
     * @param deliveryReport - true if a delivery report should be requested,  false otherwise
     * 
     * @throws ServiceClosedException - if the Service is closed
     * @throws IllegalArgumentException - if the message  argument is null
     * @throws IllegalArgumentException - if there are no recipients specified in the message argument
     * @throws IllegalArgumentException - if there are no content parts specified in the message argument
     * @throws ImsException - if the message could not be sent to the network
     */
    void sendMessage(Message message, boolean deliveryReport) throws ServiceClosedException, ImsException;
    
    /**
     * Sets a listener for this MessageManager, replacing any existing 
     * MessageManagerListener. A null value removes any existing listener.
     * 
     * @param listener - the listener to set, or null
     */
    void setListener(MessageManagerListener listener);

}
