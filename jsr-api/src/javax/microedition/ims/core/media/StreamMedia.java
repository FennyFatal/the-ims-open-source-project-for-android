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

import javax.microedition.ims.media.Player;
import java.io.IOException;

/**
 * The <code>StreamMedia</code> represents a standardized
 * application-independent streaming media that can be rendered in real-time.
 *
 *
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation
 *
 */

public interface StreamMedia extends Media {
    /**
     * Enables the platform to choose a higher quality codec during the negotiation of the media.
     */
    static final int QUALITY_HIGH = 3;
    /**
     * Enables the platform to choose a lower quality codec during the negotiation of the media.
     */
    static final int QUALITY_LOW = 1;

    /**
     * Enables the platform to choose a medium quality codec during the negotiation of the media.
     */
    static final int QUALITY_MEDIUM = 2;

    /**
     * Indicates that the streaming will consist only of audio.
     */
    static final int STREAM_TYPE_AUDIO = 1;

    /**
     * Indicates that the streaming will consist of audio and video.
     */
    //static final int STREAM_TYPE_AUDIO_VIDEO = 3;

    /**
     * Indicates that the streaming will consist only of video.
     */
    static final int STREAM_TYPE_VIDEO = 2;

    /**
     * Returns a Player initiated to render the receiving part of this Media.
     * The returned Player is in the REALIZED state.
     *
     * @return
     * @throws IOException - if the exists property is false for the media
     */
    Player getReceivingPlayer() throws IOException;


    /**
     * Returns a Player initiated to source the sending part of this Media.
     * The returned Player is in the REALIZED state.
     *
     * @return
     * @throws IOException - if the exists property is false for the media
     */
    Player getSendingPlayer() throws IOException;

    /**
     * Returns the stream type for the Media.
     *
     * @return
     */
    int getStreamType();

    /**
     * Sets a preferred quality of the media stream.
     *
     * @param quality
     */
    void setPreferredQuality(int quality);

    /**
     * Sets the source to capture media from.
     * If this method is not called, then the audio is sourced
     * from a default microphone of the device,
     * and video is sourced from a default camera.
     * <p/>
     * Supported sources:
     * <p/>
     * capture://<device>
     * file://<path>
     *
     * @param source
     * @throws IllegalStateException - if the Media is not in STATE_INACTIVE
     *                               IllegalArgumentException - if the syntax of the source argument is invalid or not supported
     */
    void setSource(String source);

    /**
     * Sets the stream type for the Media.
     *
     * @param type
     * @throws IllegalStateException - if the Media is not in STATE_INACTIVE
     *                               IllegalArgumentException - if the type argument is not a valid identifier or not supported
     */
    void setStreamType(int type);

}
