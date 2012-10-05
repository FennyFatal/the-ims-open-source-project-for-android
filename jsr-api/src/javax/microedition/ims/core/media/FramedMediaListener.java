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
 * FramedMediaListener is interface to get callbacks about
 * events in the FramedMedia (MSRP).
 *
 *
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation
 *
 */
public interface FramedMediaListener {
    /**
     * Notifies the application when an I/O error has occurred.
     *
     * @param media
     */
    void connectionError(FramedMedia media);

    /**
     * Notifies the application when the content is completely received.
     *
     * @param media
     * @param messageId
     * @param size
     * @param fileName
     */
    void contentReceived(FramedMedia media, String messageId, int size, String fileName);

    /**
     * Notifies the application that the content could not be received or that the content has been canceled by the sending endpoint.
     *
     * @param media
     */
    void contentReceiveFailed(FramedMedia media, String messageId);

    /**
     * Notifies the application when the content corresponding to the messageId has not been successfully delivered to the remote endpoint.
     *
     * @param media
     * @param messageId
     * @param statusCode
     * @param reasonPhrase
     */
    void deliveryFailure(FramedMedia media, String messageId, int statusCode, String reasonPhrase);

    /**
     * Notifies the application when the content corresponding to the messageId has been successfully delivered to the remote endpoint.
     *
     * @param media
     * @param messageId
     */
    void deliverySuccess(FramedMedia media, String messageId);

    /**
     * Notifies the application when there is progress to be reported.
     *
     * @param media
     * @param messageId
     * @param bytesTransferred
     * @param bytesTotal
     */
    void transferProgress(FramedMedia media, String messageId, int bytesTransferred, int bytesTotal);

}
