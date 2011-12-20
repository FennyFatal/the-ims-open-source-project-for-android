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

import javax.microedition.ims.Service;

/**
 * The IMService is the entry point for handling instant message functionality 
 * according to the OMA SIMPLE IM 1.0, see [OMA_IM_SPEC]. This includes 
 * functionality for sending instant messages, handling file transfers, 
 * setting up a conference with one or more participants, and handling 
 * deferred messages and history messages.
 * <p/>
 * Creating an IMService
 * <p/>
 * Connector.open(String name)
 * <p/>
 * An IMService is created with Connector.open(), according to the Generic 
 * Connection Framework (GCF), using a name string in the following format {scheme}:{target}[{params}] where:
 * <p/>
 * - {scheme} is the protocol for IM "imsim"
 * <p/>
 * - {target} is always a double slash "//" followed by the application id
 * <p/>
 * - {params} is an optional parameter that can be used to set the local 
 * user identity on the format ;userId=<sip user identity>. This can be 
 * used to override the default user identity provisioned by the IMS network 
 * and the sip user identity must be on the format described in UserIdentity.
 * <p/>
 * Connector.open(String name, int mode)
 * <p/>
 * The optional second parameter mode in Connector.open() specifies 
 * the access mode. This is not applicable when creating an IMService.
 * <p/>
 * Connector.open(String name, int mode, boolean timeouts)
 * <p/>
 * The optional third parameter is a boolean flag that indicates if 
 * the calling code can handle timeout exceptions. If this parameter 
 * is set the implementation may throw an InterruptedIOException when it detects a timeout condition.
 * <p/>
 * Exceptions when opening an IMService
 * <p/>
 * IMService does not define any new exceptions, see Service for exceptions that can be thrown.
 * <p/>
 * Examples
 * <p/>
 * The following example shows how an application can get an instance of IMService using GCF.
 * <pre>
 *  IMService service = (IMService) Connector
 *      .open("imsim://com.myCompany.apps.myApp");
 * </pre>
 * <p/>
 * Closing an IMService
 * <p/>
 * The application SHOULD invoke close on IMService when it is finished 
 * using it. The IMS engine may also close the IMService due to external 
 * events. This will trigger a call to serviceClosed in the IMServiceListener interface. 
 */
public interface IMService extends Service {
    
    /**
     * Returns a handle to the conference manager. This manager can be used 
     * to send invitations to conference and chat sessions and to join 
     * a predefined conference. It is also used to receive invitations to 
     * conference and chat sessions by setting the ConferenceManagerListener.
     * <p/>
     * Note: If the listener on the ConferenceManager is not set, the 
     * application will not be able to receive invitations.
     * 
     * @return a handle to the conference manager
     */
    ConferenceManager getConferenceManager();
    
    /**
     * Returns a handle to the deferred message manager. This manager can be 
     * used to retrieve deferred messages from the IM server. The device 
     * may be configured to have deferred messages pushed automatically 
     * to the device when the IM user becomes available for messaging. 
     * Otherwise, the deferred messages must be retrieved manually from the IM server.
     * <p/>
     * Note: If the listener on the DeferredMessageManager is not set, 
     * the application will not be able to receive incoming deferred messages. 
     * 
     * @return a handle to the deferred message manager
     */
    DeferredMessageManager getDeferredMessageManager();
    
    /**
     * Returns a handle to the file transfer manager. This manager handles 
     * both file requests that the IM user wants to send and file requests 
     * that are sent to the IM user.
     * <p/>
     * Note: If the listener on the FileTransferManager is not set, the 
     * application will not be able to receive incoming file requests.
     * 
     * @return a handle to the file transfer manager
     */
    FileTransferManager getFileTransferManager();
    
    /**
     * Returns a handle to the history manager. This manager can be used 
     * to retrieve stored messages and conversations from the IM server. 
     * 
     * @return a handle to the history manager
     */
    HistoryManager getHistoryManager();
    
    /**
     * Returns the local user identity used in this IMService.
     * 
     * @return the local user identity
     */
    String getLocalUserId();
    
    /**
     * Returns a handle to the message manager.  This manager handles both 
     * messages that the IM user wants to send and messages that are sent to the IM user.
     * <p/>
     * Note: If the listener on the MessageManager is not set, the 
     * application will not be able to receive incoming messages.
     * 
     * @return a handle to the message manager
     */
    MessageManager getMessageManager();
    
    /**
     * Sets a listener for this IMService, replacing any previous 
     * IMServiceListener. A null value removes any existing listener.
     * 
     * @param listener - the listener to set, or null
     */
    void setListener(IMServiceListener listener);

}
