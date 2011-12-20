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
 * The ConferenceManager interface can be used to set up a conference with 
 * one or more IM users, join a predefined conference, or start a chat 
 * with another IM user.
 * <p/>
 * A conference is created as an ad hoc session by sending an invitation 
 * to one or more individual users.
 * <p/>
 * A chat always consists of only two participants (One-to-One session) 
 * with no conference server involved, and is created by sending an invitation 
 * to the other participant. However, if additional participants are invited 
 * during the session, the chat is replaced by a conference. If it is 
 * anticipated that new participants will be added, it is recommended 
 * to send a conference invitation instead of a chat invitation.
 * <p/>
 * A conference with a predefined IM group can be joined through the URI 
 * of the predefined group. Depending on the settings of the predefined 
 * group (see SharedGroupDocument in the XDM enabler for more information), 
 * invitations may then be automatically sent to the other members of the 
 * group if the conference is not already active.
 * <p/>
 * The ConferenceManagerListener must be set in order to receive conference 
 * and chat invitations from other IM users.
 * <p/>
 * Examples
 * <p/>
 * This example shows how Alice sends a conference invitation to two IM users called Bob and Charlotte.
 * <p/>
 * <pre>
 *  IMService service = (IMService) Connector
 *      .open("imsim://com.myCompany.apps.myApp");
 *  service.setListener(imServiceListener);
 *  ConferenceManager conferenceManager = service.getConferenceManager();
 *  conferenceManager.setListener(conferenceManagerListener);
 *  
 *  String sessionId = conferenceManager.sendConferenceInvitation(null, 
 *                                           new String[]{"sip:bob@example.org", 
 *                                           "sip:charlotte@example.org"}, 
 *                                           null);
 *  
 *  ...
 *  
 *  void conferenceStarted(Conference conference) {
 *      // indicates that the conference has been started
 *      conference.setListener(conferenceListener);
 *  }
 * </pre>
 * <p/>
 * This example shows how Bob receives an invitation to a conference and decides to accept it.
 * <p/>
 * <pre>
 *  void conferenceInvitationReceived(ConferenceInvitation conferenceInvitation) {
 *      // indicates that a conference invitation has been received
 *  
 *      // here the application may provide the user with the possibility
 *      // to accept or reject the conference
 *      conferenceInvitation.accept();
 *  }
 *  
 *  void conferenceStarted(Conference conference) {
 *      // indicates that the conference has been started
 *      conference.setListener(conferenceListener);
 *  }
 * </pre>
 */
public interface ConferenceManager {

    /**
     * Cancels an invitation sent by the local user for a conference or chat session that has not been started.
     * 
     * @param sessionId - the identifier of the conference or chat session
     * 
     * @throws ServiceClosedException - if the Service is closed 
     * @throws IllegalArgumentException - if there is no pending invitation with the given identifier sent by the local user
     */
    void cancelInvitation(String sessionId) throws ServiceClosedException;
    
    /**
     * Returns the maximum number of allowed participants in a conference according to the server policy.
     * 
     * @return the maximum number of allowed participants, -1 if no server policy information is available
     */
    int getMaxAllowedParticipants();
    
    /**
     * Sends a request to join a predefined conference.
     * 
     * @param sender - the user identity of the sender, null  can be used to indicate that the default user identity should be used
     * @param conferenceURI - the predefined conference URI
     * @return the identifier of the conference session
     * 
     * @throws ServiceClosedException  - if the Service is closed 
     * @throws IllegalArgumentException - if the sender argument is not null or a valid user identity
     * @throws IllegalArgumentException - if the conferenceURI argument is null  or an empty string
     * @throws ImsException - if the request to join a predefined conference could not be sent to the network
     */
    String joinPredefinedConference(String sender, String conferenceURI) throws ServiceClosedException, ImsException;
    
    /**
     * Sends a chat invitation to another IM user.
     * 
     * @param sender - the user identity of the sender, null can be used to indicate that the default user identity should be used
     * @param recipient - the user identity of the invitation recipient
     * @param subject - the subject of the chat, null can be used to indicate that no subject should be set
     * @return the identifier of the chat session
     * 
     * @throws ServiceClosedException - if the Service is closed 
     * @throws IllegalArgumentException - if the sender argument is not null and not valid user identity
     * @throws IllegalArgumentException - if the recipient argument is not a valid user identity
     * @throws ImsException - if the chat invitation could not be sent to the network
     */
    String sendChatInvitation(String sender, String recipient, String subject) throws ServiceClosedException, ImsException;
    
    /**
     * Sends a conference invitation to one or more IM users.
     * 
     * @param sender - the user identity of the sender, null can be used to indicate that the default user identity should be used
     * @param recipients - an array of user identities of the invitation recipients
     * @param subject - the subject of the conference, null  can be used to indicate that no subject should be set
     * @return the identifier of the conference session
     * 
     * @throws ServiceClosedException  - if the Service is closed 
     * @throws IllegalArgumentException - if the sender argument is not null 
     * or a valid user identity
     * @throws IllegalArgumentException - if the recipients argument is null 
     * or an empty array or contains invalid user identities
     * @throws IllegalArgumentException - if the total number of participants 
     * including the sender exceeds the maximum allowed participants in a conference
     * @throws ImsException - if the conference invitation could not be sent to the network
     */
    String sendConferenceInvitation(String sender, String[] recipients, String subject) throws ServiceClosedException, ImsException;
    
    /**
     * Sets a listener for this ConferenceManager, replacing any existing 
     * ConferenceManagerListener. A null value removes any existing listener.
     * 
     * @param listener - the listener to set, or null
     */
    void setListener(ConferenceManagerListener listener);
    
}
