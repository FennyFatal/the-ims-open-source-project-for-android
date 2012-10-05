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
import java.io.IOException;

/**
 * IMSession is a superinterface for communication with other IM users.
 *
 *
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation.
 *
 */
public interface IMSession {
    
    /**
     * Cancels an entire file transfer or the transfer of a specific file.
     * <p/>
     * 
     * @param identifier - the request identifier of the file transfer or the file identifier of the specific file
     * 
     * @throws IllegalArgumentException - if there is no file transfer associated with the identifier
     * @throws IllegalStateException - if the IMSession is closed
     */
    void cancelFileTransfer(String identifier);
    
    /**
     * Closes a session. If the session has already been closed this method will not do anything.
     */
    void close();
    
    /**
     * Returns the accepted content types that can be used in the session.
     * <p/>
     * Note: "*" indicates that any content type may be used in the session. 
     * 
     * @return an array of the accepted content types
     */
    String[] getAcceptedContentTypes();
    
    /**
     * Returns the maximum message size in bytes accepted in the session.
     * 
     * @return the maximum message size, or -1 if not set
     */
    int getMaxSize();
    
    /**
     * Returns the unique identifier for this IMSession.
     * 
     * @return the session identifier
     */
    String getSessionId();
    
    /**
     * Sends a message to all participants to indicate that the user is 
     * composing a message. 
     * 
     * @param timeout - the interval in seconds after which the recipients will assume that the user has stopped composing
     * 
     * @throws IllegalArgumentException - if the timeout argument is less than 0
     * @throws ImsException - if the composing indicator message could not be sent to the network 
     * @throws IllegalStateException - if the IMSession is closed
     */
    void sendComposingIndicator(int timeout) throws ImsException;
    
    /**
     * Sends files to all participants in the session. 
     * 
     * @param files - an array of FileInfo of the files to send
     * @param deliveryReport - true if a delivery report should be requested for each file, false otherwise
     * @return the request identifier of the file transfer request
     * 
     * @throws IllegalArgumentException - if the files  argument is null or an empty array or contains null values
     * @throws IOException - if an I/O error occurs when handling the file or if the file could not be opened
     * @throws ImsException - if the files could not be sent to the network 
     * @throws IllegalStateException - if the IMSession  is closed
     * @throws SecurityException - if sending a file is not permitted
     */
    String sendFiles(FileInfo[] files, boolean deliveryReport) throws IOException, ImsException, SecurityException;
    
    /**
     * Sends a message to all participants in the session. Any recipients 
     * specified in the Message will be ignored.
     * 
     * @param message - the Message to send
     * @param deliveryReport - true if a delivery report should be requested, false otherwise
     * 
     * @throws IllegalArgumentException - if the message argument is null
     * @throws IllegalArgumentException - if there are no content parts specified in the message argument
     * @throws IllegalArgumentException - if the contentType of a content part is not one of the accepted content types
     * @throws ImsException - if the message could not be sent to the network
     * @throws IllegalStateException - if the IMSession is closed 
     */
    void sendMessage(Message message, boolean deliveryReport) throws ImsException;

}
