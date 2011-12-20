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

/**
 * The Chat interface can be used to to send messages and files to another 
 * IM user. Through the ChatListener interface, messages and files from 
 * the other participant can be received.
 * <p/>
 * A chat is a special case of a conference with only two participants 
 * (One-to-One session) and no conference server involved. However, the 
 * chat can be extended to a conference by inviting additional participants. 
 * If it is anticipated that new participants will be added, it is recommended 
 * to send a conference invitation instead of a chat invitation.
 * <p/>
 * Chat invitations are sent using the ConferenceManager interface. Incoming 
 * chat invitations are received through the ConferenceManagerListener interface.
 * <p/>
 * Examples
 * <p/>
 * This example shows how Alice sends a message to the other participant 
 * in the chat. The chat has already been set up.
 * <p>
 * <pre>
 *  Message message = new Message();
 *  ContentPart contentPart = new ContentPart("Hi, I'm Alice!".getBytes(),
 *                               "text/plain");
 *  message.addContentPart(contentPart);                             
 *  chat.sendMessage(message, false);
 *  
 *  void messageSent(IMSession session, String messageId) {
 *    // this call to the ChatListener indicates that the message was successfully sent
 *  }
 * </pre>
 * <p/>
 * This example shows how Alice extends the chat to a conference by inviting 
 * Charlotte to the chat. The chat has already been set up.
 * <p/>
 * <pre>
 *  chat.extendToConference(new String[]{"sip:charlotte@example.org"});
 *  
 *  void chatExtended(Chat chat, Conference conference) {
 *    // this call to the ChatListener indicates that the chat was extended
 *  }
 * </pre>
 */
public interface Chat extends IMSession {

    /**
     * Extends this Chat to a Conference. At least one new participant 
     * must be invited to the conference.  The original participants 
     * of the chat will be invited automatically.
     * 
     * @param additionalParticipants - an array of user identities that will be invited to the Conference
     * @throws IllegalArgumentException - if the additionalParticipants 
     * argument is null or an empty array or contains invalid user identities 
     * @throws IllegalStateException - if the Chat is closed
     * @throws IllegalArgumentException - if the total number of participants 
     * including the original participants exceeds the maximum allowed participants in a conference
     * @throws ImsException - if the request could not be sent to the network
     */
    void extendToConference(String[] additionalParticipants) throws ImsException;
    
    /**
     * Sets a listener for this Chat, replacing any previous ChatListener. 
     * A null value removes any existing listener.
     * 
     * @param listener - the listener to set, or null
     */
    void setListener(ChatListener listener);
    
}
