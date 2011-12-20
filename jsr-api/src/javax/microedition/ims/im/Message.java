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

import com.android.ims.core.media.util.UtilsMSRP;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Message class is used when sending and receiving messages. A message 
 * is created with one of the constructors of this class, and content is then 
 * added by using the ContentPart class. The message can then be sent through 
 * the MessageManager interface or the Conference and Chat interfaces. 
 * Messages can be received through the MessageManagerListener interface, 
 * which must be set on the MessageManager. Messages exchanged during a 
 * conference or chat are received through the ConferenceListener or 
 * ChatListener  interfaces.
 * <p/>
 * There are two constructors for the Message class. When using the first 
 * constructor, sender, recipients, and subject can be specified. The second 
 * constructor does not take any parameters. The first constructor must be 
 * used for sending a message with the MessageManager outside of a conference, 
 * or for sending a private message to selected participants in a conference. 
 * In both these cases, the recipients of the message must be specified. The 
 * second constructor is used for sending a message to all participants in a 
 * conference or chat. In this case, the recipients of the message should 
 * not be specified.
 * <p/>
 * Examples
 * <p/>
 * This example shows how to create a Message from a stream.
 * <p/>
 * <pre>
 *  Message message = new Message(null, new String[] {
 *      "sip:bob@example.org"
 *  }, "My Avatar");
 *  ContentPart contentPart = new ContentPart(null, "image/png");
 *  OutputStream os = contentPart.openOutputStream();
 *  // Write content to the stream
 *  os.close();
 *  message.addContentPart(contentPart);
 * </pre>
 */
public class Message {
    
    private String sender;
    private String[] recipients;
    private String subject;
    
    private String messageId;
    private Date timestamp;
    
    private List<ContentPart> contentParts;
    
    /**
     * Constructor for a new Message to be sent with the Conference or Chat 
     * interfaces to all participants in the conference or the chat.
     * <p/>
     * Note: The MessageManager methods sendMessage and sendLargeMessage will 
     * throw an IllegalArgumentException if sending a Message that has been 
     * created using this constructor, since no recipients are specified. 
     */
    public Message() {
        this.messageId = UtilsMSRP.generateUniqueMessageId();
    }
    
    /**
     * Constructor for a new Message to be sent with the MessageManager 
     * outside of a conference, or for sending a private message to 
     * selected participants in a conference.
     * 
     * @param sender - the user identity of the sender, null can be used to 
     * indicate that the default user identity should be used when sending 
     * the message
     * @param recipients - an array of user identities of the recipients
     * @param subject - the subject of the Message, null can be used to 
     * indicate that no subject should be set
     * 
     * @throws IllegalArgumentException - if the sender argument is not null 
     * and not valid user identity
     * @throws IllegalArgumentException - if the recipients argument is null 
     * or an empty array or contains invalid user identities
     */
    public Message(String sender, String[] recipients, String subject) {
        if (sender != null && !UtilsMSRP.isValidUserIdentity(sender)) {
            throw new IllegalArgumentException("The sender argument is not null and not valid user identity");
        }
        if (recipients == null || recipients.length == 0) {
            throw new IllegalArgumentException("The recipients argument is null or an empty array or contains invalid user identities");
        }
        for (String recipient : recipients) {
            if (!UtilsMSRP.isValidUserIdentity(recipient)) {
                throw new IllegalArgumentException("The recipients argument is null or an empty array or contains invalid user identities");
            }
        }
        
        this.sender = sender;
        this.recipients = recipients;
        this.subject = subject;
        
        this.messageId = UtilsMSRP.generateUniqueMessageId();
    }
    
    /**
     * Adds a ContentPart to this Message.
     * 
     * @param contentPart - the ContentPart to add
     * @throws IllegalArgumentException - if the contentPart argument is null
     */
    public void addContentPart(ContentPart contentPart) {
        if (contentPart == null) {
            throw new IllegalArgumentException("The contentPart argument is null");
        }
        if (contentParts == null) {
            contentParts = new ArrayList<ContentPart>();
        }
        contentParts.add(contentPart);
    }
    
    /**
     * Returns all ContentPart objects in this Message.
     * 
     * @return an array of ContentPart or an empty array
     * if no ContentPart are available
     */
    public ContentPart[] getContentParts() {
        if (contentParts != null) {
            return (ContentPart[])contentParts.toArray(new ContentPart[0]);
        }
        return new ContentPart[0];
    }
    
    /**
     * Returns the message identifier of this Message.
     * 
     * @return the message identifier
     */
    public String getMessageId() {
        return messageId;
    }
    
    /**
     * Returns the recipients of this Message.
     * 
     * @return an array of user identities of the recipients, or null if the recipients have not been set
     */
    public String[] getRecipients() {
        return recipients;
    }
    
    /**
     * Returns the sender of this Message.
     * 
     * @return the user identity of the sender, or null if the default user identity was used when creating the Message
     */
    public String getSender() {
        return sender;
    }
    
    /**
     * Returns the subject of this Message.
     * 
     * @return the subject, or null if the subject has not been set
     */
    public String getSubject() {
        return subject;
    }
    
    /**
     * Returns the time stamp when this Message was sent. The time stamp is 
     * only available for messages that have been received, and only if it 
     * was included in the received message.
     * 
     * @return the time stamp, or null if the time stamp is not available
     */
    public Date getTimestamp() {
        return timestamp;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
}
