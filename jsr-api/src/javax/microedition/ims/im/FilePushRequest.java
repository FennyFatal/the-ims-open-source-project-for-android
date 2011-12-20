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

/**
 * The FilePushRequest is used to handle incoming file push requests, 
 * meaning that the files will be pushed to the recipient. The application has 
 * the possibility to accept or reject an incoming file push request. 
 * The metadata of the files can be inspected by calling the getFileInfos method 
 * before deciding whether to accept or reject the request. The FilePushRequest 
 * is received through the FileTransferManagerListener interface, or through 
 * the IMSessionListener  interface if the files are sent during a conference 
 * or chat. Each file will then be delivered through the fileReceived event in 
 * the FileTransferManagerListener or the IMSessionListener. Both users may receive 
 * transferProgress  events for individual files (or fileTransferProgress events 
 * if the the files are sent during a conference or chat).
 * <p/>
 * Examples
 * <p/>
 * This example shows how Alice accepts an incoming file push request with one file.
 * <p/>
 * <pre>
 *  void incomingFilePushRequest(FilePushRequest filePushRequest) {
 *      // this call to the FileTransferManagerListener indicates that a 
 *      // file push request has been received 
 *      filePushRequest.accept();
 *  }
 *  
 *  void transferProgress(String requestId, String fileId, int bytesTransferred,
 *      int bytesTotal) {
 *      // this call to the FileTransferManagerListener indicates the 
 *      // transfer progress of the incoming file
 *  }
 *  
 *  void fileReceived(String requestId, String fileId, String filePath) {
 *      // this call to the FileTransferManagerListener indicates that 
 *      // the file has been received
 *  }
 * </pre>
 */
public interface FilePushRequest {
   
    /**
     * Accepts the file push request. Accepting this request will cause it 
     * to expire.  The request will also expire if it has been rejected, timed out, 
     * or if the Service  has been closed. The request is always considered expired 
     * if the fileTransferFailed event in the FileTransferManagerListener 
     * or the IMSessionListener has been invoked with the corresponding request identifier.
     * 
     * @throws IllegalStateException - if the request has expired
     */
    void accept();
    
    /**
     * Rejects the file push request. Rejecting this request will cause it to expire. 
     * The request will also expire if it has been accepted, timed out, or if 
     * the Service has been closed. The request is always considered expired if 
     * the fileTransferFailed event in the FileTransferManagerListener or 
     * the IMSessionListener has been invoked with the corresponding request identifier.
     * 
     * @throws IllegalStateException - if the request has expired
     */
    void reject();
    
    /**
     * Returns the request identifier of this FilePushRequest.
     * 
     * @return the request identifier
     */
    String getRequestId();
    
    /**
     * Returns the sender of this FilePushRequest.
     * 
     * @return the user identity of the sender
     */
    String getSender();
    
    /**
     * Returns the recipients of this FilePushRequest.
     * 
     * @return an array of user identities of the recipients
     */
    String[] getRecipients();
    
    /**
     * Returns the subject of this FilePushRequest.
     * 
     * @return the subject, or null if the subject has not been set
     */
    String getSubject();
    
    /**
     * Returns the files included in this FilePushRequest.
     * 
     * @return an array of FileInfo
     */
    FileInfo[] getFileInfos();

}
