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

package javax.microedition.ims.core.media;

/**
 * FramedMedia offers interface to handle FramedMedia (MSRP), to
 * access received content and send out content in preset
 * MSPR-session.
 *
 *
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation
 *
 */
public interface FramedMedia extends Media {
    /**
     * Cancels the ongoing transfer.
     *
     * @param messageId
     */
    void cancel(String messageId);

    /**
     * Returns the accepted content type(s) of this media.
     *
     * @return
     */
    String[] getAcceptedContentTypes();

    /**
     * Returns the content type of the content that is identified by the messageId parameter.
     *
     * @param messageId
     * @return
     */
    String getContentType(String messageId);

    /**
     * Returns the header value from the content that is identified by the messageId parameter.
     *
     * @param messageId
     * @param key
     * @return
     */
    String getHeader(String messageId, String key);

    /**
     * Receives content from the remote endpoint.
     *
     * @param messageId
     * @return
     */
    byte[] receiveBytes(String messageId);

    /**
     * Receives a file from the remote endpoint.
     *
     * @param messageId
     * @param locator
     */
    void receiveFile(String messageId, String locator);

    /**
     * Sends the content to the remote endpoint over a reliable connection.
     *
     * @param content
     * @param contentType
     * @param headers
     * @return
     */
    String sendBytes(byte[] content, String contentType, String[][] headers);

    /**
     * Sends a file to the remote endpoint over a reliable connection.
     *
     * @param locator
     * @param contentType
     * @param headers
     * @return
     */
    String sendFile(String locator, String contentType, String[][] headers);

    /**
     * Sets the accepted content type(s) of this media.
     *
     * @param acceptedContentTypes
     */
    void setAcceptedContentTypes(String[] acceptedContentTypes);

    /**
     * Sets a listener for this FramedMedia, replacing any previous FramedMediaListener.
     *
     * @param listener
     */
    void setListener(FramedMediaListener listener);

}
