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
 * A listener type for receiving notifications of conference and chat session establishment.
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation.
 */
public interface ConferenceManagerListener {
    
    /**
     * Notifies the application of an incoming chat session invitation. 
     * The application must accept or reject the chatInvitation.
     * 
     * @param chatInvitation - a handler for accepting or rejecting the chat invitation
     */
    void chatInvitationReceived(ChatInvitation chatInvitation);
    
    /**
     * Notifies the application that the chat was successfully started.
     * 
     * @param chat - the Chat
     */
    void chatStarted(Chat chat);
    
    /**
     * Notifies the application that the chat could not be successfully 
     * started. This could either be because the invitation has been 
     * canceled, because of a connection error, or because the 
     * invitation has expired.
     * 
     * @param sessionId - the identifier of the concerned chat session
     * @param reason - the ReasonInfo to indicate why the request failed
     */
    void chatStartFailed(String sessionId, ReasonInfo reason);
    
    /**
     * Notifies the application of an incoming conference session invitation. 
     * The application must accept or reject the conferenceInvitation.
     * 
     * @param conferenceInvitation - a handler for accepting or rejecting the conference invitation
     */
    void conferenceInvitationReceived(ConferenceInvitation conferenceInvitation);
    
    /**
     * Notifies the application that the conference was successfully started.
     * 
     * @param conference - the Conference
     */
    void conferenceStarted(Conference conference);
    
    /**
     * Notifies the application that the conference could not be successfully 
     * started. This could either be because the invitation has been canceled, 
     * because of a connection error, or because the invitation has expired.
     * 
     * @param sessionId - the identifier of the concerned conference session
     * @param reason - the ReasonInfo  to indicate why the request failed
     */
    void conferenceStartFailed(String sessionId, ReasonInfo reason);

}
