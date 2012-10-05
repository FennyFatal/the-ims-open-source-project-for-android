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
 * A listener type for receiving notifications of when a file is sent or received. 
 * The transfer progress of files can also be observed. 
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation.
 */
public interface FileTransferManagerListener {
    
    /**
     * Notifies the application that a file was successfully sent.
     * <p>
     * Note: This does not indicate that the file was delivered to the recipients, only that the file was accepted by the IMS network.
     * 
     * @param requestId - the request identifier of the file transfer request
     * @param fileId - the identifier of the corresponding file
     */
    void fileSent(String requestId, String fileId);
    
    /**
     * Notifies the application that a file could not be successfully sent.
     * 
     * @param requestId - the request identifier of the file transfer request
     * @param fileId - the identifier of the corresponding file
     * @param reason - the reason info to indicate why the request failed
     */
    void fileSendFailed(String requestId, String fileId, ReasonInfo reason);
    
    /**
     * Notifies the application that a file was received.
     * <p/>
     * NOTE: The implementation may add a prefix/suffix to the filename to assure it's uniqueness. 
     * 
     * @param requestId - the request identifier of the file transfer request
     * @param fileId - the identifier of the corresponding file
     * @param filePath - the full path of the physical file including the file name where the file is stored
     */
    void fileReceived(String requestId, String fileId, String filePath);
    
    /**
     * Notifies the application that a file could not be successfully received.
     * 
     * @param requestId - the request identifier of the file transfer request
     * @param fileId - the identifier of the corresponding file
     * @param reason - the reason info to indicate why the request failed
     */
    void fileReceiveFailed(String requestId, String fileId, ReasonInfo reason);
    
    /**
     * Notifies the application of a file push request. The application must accept or reject the FilePushRequest.
     * 
     * @param filePushRequest - a handler for accepting or rejecting the file push request
     */
    void incomingFilePushRequest(FilePushRequest filePushRequest);
    
    /**
     * Notifies the application of a file pull request. The application must accept or reject the FilePullRequest.
     * 
     * @param filePullRequest - a handler for accepting or rejecting the file pull request
     */
    void incomingFilePullRequest(FilePullRequest filePullRequest);
    
    /**
     * Notifies the application when there is progress to be reported on a file transfer. 
     * For small transfers this method may never be called.
     * 
     * @param requestId - the request identifier of the file transfer request
     * @param fileId - the identifier of the file being transferred
     * @param bytesTransferred - the total number of bytes that has been transferred
     * @param bytesTotal - the total number of bytes in the content, -1 if the size is not known
     */
    void transferProgress(String requestId, String fileId, long bytesTransferred, long bytesTotal);
    
    /**
     * Notifies the application that the transfer of files identified by 
     * the requestId argument could not be successfully completed. This could be because 
     * the request has been canceled, because of a connection error, or because the request has expired.
     * 
     * @param requestId - the request identifier of the file transfer request
     * @param reason - the reason info to indicate why the request failed
     */
    void fileTransferFailed(String requestId, ReasonInfo reason);

}
