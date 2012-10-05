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
import java.io.IOException;

/**
 * The FileTransferManager can be used to send files to one or several IM 
 * users, or request files from an IM user.
 *
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation.
 *
 */
public interface FileTransferManager {

    /**
     * Sends a number of files to the specified recipients in a push request. 
     * Each file is described using a FileInfo. The recipients will have the 
     * possibility to accept or reject the file push request. A delivery 
     * report can be requested if the sender wants to be notified when each 
     * file is delivered to the recipients' devices.
     * 
     * @param sender - the user identity of the sender, null can be used to 
     * indicate that the default user identity should be used when sending the file request
     * @param recipients - an array of user identities of the recipients
     * @param subject - the subject of the file transfer, null can be used to 
     * indicate that no subject should be set
     * @param files - the files to send
     * @param deliveryReport - true if a delivery report should be requested for each file, false otherwise
     * 
     * @return the request identifier of the file transfer request
     * 
     * @throws ServiceClosedException - if the Service is closed
     * @throws IllegalArgumentException - if the sender argument is neither null nor a valid user identity
     * @throws IllegalArgumentException - if the recipients argument is null or an empty array or contains invalid user identities 
     * @throws IllegalArgumentException - if the files argument is null or an empty array or contains null values
     * @throws IOException - if an I/O error occurs when handling the file or if the file could not be opened
     * @throws ImsException - if the files could not be sent to the network
     * @throws SecurityException - if sending a file is not permitted
     */
    String sendFiles(String sender, String[] recipients, String subject,
            FileInfo[] files, boolean deliveryReport)
        throws ServiceClosedException, IOException, ImsException, SecurityException;
    
    /**
     * Requests a number of files from the specified request recipient 
     * in a pull request. Each file is described using a FileSelector. 
     * The request recipient will have the possibility to accept or reject the request to send the files.
     * 
     * @param requestSender - the user identity of the request sender, 
     * null can be used to indicate that the default user identity should be used when sending the file request
     * @param requestRecipient - the user identity of the request recipient
     * @param subject - the subject of the file transfer,  null can be used to indicate that no subject should be set
     * @param files - the files to request
     * 
     * @return the request identifier of the file transfer request
     * 
     * @throws ServiceClosedException - if the Service is closed
     * @throws IllegalArgumentException - if the requestSender argument is neither null nor a valid user identity
     * @throws IllegalArgumentException - if the requestRecipient argument is not a valid user identity
     * @throws IllegalArgumentException - if the files  argument is null or an empty array or contains null values
     * @throws ImsException - if the file request could not be sent to the network
     */
    String requestFiles(String requestSender, String requestRecipient, String subject,
            FileSelector[] files)
        throws ServiceClosedException, ImsException;
    
    /**
     * Cancels an entire file transfer or the transfer of a specific file.
     * <p/>
     * Note: Individual files can only be canceled if the specific file is currently being transferred, 
     * while an entire file transfer can be canceled as soon as the file transfer request has been sent or received. 
     * 
     * @param identifier - the request identifier of the file transfer or the file identifier of the specific file
     * 
     * @throws IllegalArgumentException - if there is no file transfer associated with the identifier
     */
    void cancel(String identifier);

    /**
     * Sets a listener for this FileTransferManager, replacing any existing FileTransferManagerListener. 
     * A null value removes any existing listener.
     * 
     * @param listener - the listener to set, or null
     */
    void setListener(FileTransferManagerListener listener);

}
