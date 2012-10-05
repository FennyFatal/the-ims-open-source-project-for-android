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
 * A listener type used to receive messages and files from other participants 
 * in a session. It can also be used to receive system messages and message 
 * notifications in a session. The transfer progress of files can also be observed. 
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation.
 */
public interface IMSessionListener {
    
    /**
     * Notifies the application that a session participant is composing 
     * a message. The sender will be in composing mode until a message 
     * is received from that sender, or until the interval specified 
     * by the timeout argument has expired.
     * <p/>
     * Several notifications may be received from the same sender before
     *  a message is received. The timer should then be refreshed with 
     *  the new timeout value. A timeout of 0 indicates that the sender 
     *  has stopped composing.
     * 
     * @param session - the concerned IMSession
     * @param sender - the user identity of the user that sent the composing indicator
     * @param timeout - the interval in seconds after which the sender will stop composing.
     */
    void composingIndicatorReceived(IMSession session, String sender, int timeout);
    
    /**
     * Notifies the application that a file was received in the session.
     * 
     * @param session - the concerned IMSession
     * @param requestId - the request identifier of the file transfer request
     * @param fileId - the identifier of the corresponding file
     * @param filePath - the full path of the physical file including the file name where the file is stored
     */
    void fileReceived(IMSession session, String requestId, String fileId, String filePath);
    
    /**
     * Notifies the application that a file could not be successfully received.
     * 
     * @param session - the concerned IMSession
     * @param requestId - the request identifier of the file transfer request
     * @param fileId - the identifier of the corresponding file
     * @param reason - the reason info to indicate why the request failed
     */
    void fileReceiveFailed(IMSession session, String requestId, String fileId, ReasonInfo reason);
    
    /**
     * Notifies the application that a file could not be successfully sent.
     * 
     * @param session - the concerned IMSession
     * @param requestId - the request identifier of the file transfer request
     * @param fileId - the identifier of the corresponding file
     * @param reason - the reason info to indicate why the request failed
     */
    void fileSendFailed(IMSession session, String requestId, String fileId, ReasonInfo reason);
    
    /**
     * Notifies the application that a file was successfully sent.
     * 
     * @param session - the concerned IMSession
     * @param requestId - the request identifier of the file transfer request
     * @param fileId - the identifier of the corresponding file
     */
    void fileSent(IMSession session, String requestId, String fileId);
    
    /**
     * Notifies the application that the transfer of files identified by the 
     * requestId argument could not be successfully completed. This could be 
     * because the request has been canceled, because of a connection error, 
     * or because the request has expired.
     * 
     * @param session - the concerned IMSession
     * @param requestId - the request identifier of the file transfer request
     * @param reason - the reason info to indicate why the request failed
     */
    void fileTransferFailed(IMSession session, String requestId, ReasonInfo reason);
    
    /**
     * Notifies the application when there is progress to be reported on 
     * a file transfer. For small transfers this method may never be called.
     * 
     * @param session - the concerned IMSession
     * @param requestId - the request identifier of the file transfer request
     * @param fileId - the identifier of the file being transferred
     * @param bytesTransferred - the total number of bytes that has been transferred
     * @param bytesTotal - the total number of bytes in the content, -1 if the size is not known
     */
    void fileTransferProgress(IMSession session, String requestId, String fileId, long bytesTransferred, long bytesTotal);
    
    /**
     * Notifies the application of a file push request in the session.
     * The application must accept or reject the FilePushRequest.
     * 
     * @param session - the concerned IMSession
     * @param filePushRequest - a handler for accepting or rejecting the file push request
     */
    void incomingFilePushRequest(IMSession session, FilePushRequest filePushRequest);
    
    /**
     * Notifies the application that a message sent to all participants has been received in the session.
     * 
     * @param session - the concerned IMSession
     * @param message - the received message
     */
    void messageReceived(IMSession session, Message message);
    
    /**
     * Notifies the application that the message identified by messageId could not be successfully sent.
     * 
     * @param session - the concerned IMSession
     * @param messageId - the message identifier of the corresponding Message
     * @param reason - the reason info to indicate why the request failed
     */
    void messageSendFailed(IMSession session, String messageId, ReasonInfo reason);
    
    /**
     * Notifies the application that the message identified by messageId was successfully sent.
     * 
     * @param session - the concerned IMSession
     * @param messageId - the message identifier of the corresponding Message
     */
    void messageSent(IMSession session, String messageId);
    
    /**
     * Notifies the application that the session was closed.
     * 
     * @param session - the concerned IMSession
     * @param reason - the reason info to indicate why the session was closed
     */
    void sessionClosed(IMSession session, ReasonInfo reason);
    
    /**
     * Notifies the application that a system message has been received in the session.
     * 
     * @param session - the concerned IMSession
     * @param message - the received message
     */
    void systemMessageReceived(IMSession session, Message message);

}
