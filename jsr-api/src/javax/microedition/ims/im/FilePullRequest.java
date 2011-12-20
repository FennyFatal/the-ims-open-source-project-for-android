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
 * The FilePullRequest is used to handle incoming file pull requests, 
 * meaning that files will be pulled from the request recipient to 
 * the IM user that requested the files. The application has the possibility to accept 
 * or reject an incoming file pull request. The metadata of the files 
 * can be inspected by calling the getFileSelectors method before 
 * deciding whether to accept or reject the request. The fileSent event 
 * will be invoked for each file for the IM user that received the file 
 * pull request.  Both users may receive transferProgress events for individual files.
 * <p/>
 * Before a FilePullRequest can be accepted, all file selectors in 
 * the request must be mapped to a physical file by calling the setFilePath method for each selector.
 * <p/>
 * Examples
 * <p/>
 * This example shows how Alice accepts an incoming file pull request with one file.
 * <p/>
 * <pre>
 * 
 *  void incomingFilePullRequest(FilePullRequest filePullRequest) {
 *      // this call to the FileTransferManagerListener indicates that a 
 *      // file pull request has been received 
 *      FileSelector selector = filePullRequest.getFileSelectors()[0];
 *      ...
 *      //investigate the file selector
 *      ...
 *      filePullRequest.setFilePath(selector, "file:///CFCard/img/bob.png");
 *      filePullRequest.accept();
 *  }
 *  
 *  void transferProgress(String requestId, String fileId, int bytesTransferred,
 *      int bytesTotal) {
 *      // this call to the FileTransferManagerListener indicates the 
 *      // transfer progress of the outgoing file
 *  }
 *  
 *  void fileSent(String requestId, String fileId) {
 *      // this call to the FileTransferManagerListener indicates that 
 *      // the file has been sent
 *  }
 * </pre>
 */
public interface FilePullRequest {
    
    /**
     * Accepts the file pull request. Accepting this request will cause it to expire. 
     * The request will also expire if it has been rejected, timed out, or if 
     * the Service has been closed. The request is always considered expired if 
     * the fileTransferFailed event in the FileTransferManagerListener has been 
     * invoked with the corresponding request identifier.
     * <p/>
     * Note: Before this method can be called, each FileSelector in this FilePullRequest 
     * must be mapped to a physical file using the setFilePath method.
     *
     * @throws IllegalStateException - if the request has expired
     * @throws ImsException - if a file path has not been set for all file selectors in the request
     */
    void accept() throws ImsException;

    /**
     * Rejects the file transfer request. Rejecting this request will cause it to expire. 
     * The request will also expire if it has been accepted, timed out, 
     * or if the Service has been closed. The request is always considered expired 
     * if the fileTransferFailed event in the FileTransferManagerListener has been invoked 
     * with the corresponding request identifier.
     * 
     * @throws IllegalStateException - if the request has expired
     */
    void reject();
    
    /**
     * Returns the request identifier of this FilePullRequest.
     * 
     * @return the request identifier
     */
    String getRequestId();
    
    /**
     * Returns the sender of this FilePullRequest.
     * 
     * @return the user identity of the sender
     */
    String getSender();
    
    /**
     * Returns the recipient of this FilePullRequest.
     * 
     * @return the user identity of the recipient
     */
    String getRecipient();
    
    /**
     * Returns the subject of this FilePullRequest.
     * 
     * @return the subject, or null if the subject has not been set
     */
    String getSubject();
    
    /**
     * Returns the files included in this FilePullRequest.
     * 
     * @return an array of FileSelector
     */
    FileSelector[] getFileSelectors();
    
    /**
     * Sets the path of a FileSelector to the physical file indicated by the filePath  parameter.
     * <p/>
     * Note: This method must called for each FileSelector in this FilePullRequest before the request can be accepted. 
     * 
     * @param fileSelector - the file selector
     * @param filePath - the full path of the physical file including the file name
     * 
     * @throws IllegalArgumentException - if the fileSelector  argument is null
     * @throws IllegalArgumentException - if the file indicated by the fileSelector argument was not included in this FilePullRequest
     * @throws IOException - if the physical file indicated by the filePath argument could not be opened
     * @throws SecurityException - if accessing a file is not permitted
     */
    void setFilePath(FileSelector fileSelector, String filePath)
            throws IOException, SecurityException;

}
